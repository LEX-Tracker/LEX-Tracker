package com.digitalinterruption.lex.ui.main

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.adapters.MyItemSelected
import com.digitalinterruption.lex.adapters.SymptomsAdapter
import com.digitalinterruption.lex.databinding.FragmentSymptomsBinding
import com.digitalinterruption.lex.models.MyViewModel
import com.digitalinterruption.lex.models.SymptomModel
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SymptomsFragment : Fragment(), MyItemSelected {
    private var _binding: FragmentSymptomsBinding? = null
    private val binding get() = _binding
    private lateinit var callback: OnBackPressedCallback
    var exit: Boolean = false
    lateinit var countDown: CountDownTimer
    private val listSymptoms = arrayListOf<SymptomModel>()
    private val listSymptomsData = arrayListOf<SymptomModel>()
    private val listDb = arrayListOf<SymptomModel>()

    private val myViewModel: MyViewModel by viewModels()
    private val defaultDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val defaultShortDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val defaultDate: LocalDateTime = LocalDateTime.parse("1970-01-01 00:00:00", defaultDateFormat)

    var date: LocalDateTime = defaultDate
    var dbDate: LocalDate = defaultDate.toLocalDate()
    val args: SymptomsFragmentArgs by navArgs()
    lateinit var prefs: SharedPrefs

    fun populateSymptoms(ldt: LocalDateTime){
        myViewModel.readAllData.observe(viewLifecycleOwner){data ->
            data.forEach{

                if (
                    it.date != "" &&
                    ldt.toLocalDate().equals(
                        LocalDate.parse(it.date, defaultShortDateFormat)
                    )
                ){
                    listSymptomsSelected.add(it)
                }
            }
        }
    }

    fun populateSymptomsFiltered(ldt: LocalDateTime, filter: String){
        myViewModel.readAllData.observe(viewLifecycleOwner){data ->
            data.forEach{
                if (
                    it.date != "" &&
                    ldt.toLocalDate().equals(
                        LocalDateTime.parse(it.date, defaultDateFormat).toLocalDate()
                    ) && it.symptom.contentEquals(filter)
                ){
                    listSymptomsSelected.add(it)
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSymptomsBinding.inflate(inflater, container, false)
        prefs = SharedPrefs(requireContext())

        date = LocalDateTime.parse(args.date, defaultDateFormat)
        val defaultSymptomsArray = resources.getStringArray(R.array.symptoms) //Gets list of default symptoms
        for(i in 1..defaultSymptomsArray.size){
            listSymptoms.add(SymptomModel(i, "", defaultSymptomsArray[i-1], "")) //Adds them as a symptom
        }


        myViewModel.readAllData.observe(viewLifecycleOwner) { data ->
            listDb.clear()
            data.forEach {

                var pDate: LocalDateTime = LocalDateTime.parse(defaultDate.toString())
                if (!it.date.isNullOrEmpty()){

                    pDate = LocalDate.parse(it.date, defaultShortDateFormat).atStartOfDay()
                }

                if (pDate.isEqual(date)) {
                    dbDate = pDate.toLocalDate()
                    listDb.add(it)
                }

                if (it.intensity == "") {
                    // check it doesn't exist first
                    if (!listSymptomsData.contains(it)){
                        listSymptomsData.add(it)
                    }
                }

            if (!listDb.contains(it)){
                listDb.add(it)
            }
        }

            binding?.recyclerView?.adapter?.notifyDataSetChanged()
        }

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentBackPress()

        if (prefs.getIsPinSet()) {
            myViewModel.addData(listSymptoms)
        }
        binding?.ivHome?.setOnClickListener {
            moveToNext("Home")
        }
        binding?.ivSymptoms?.setOnClickListener {
        }
        binding?.ivSettings?.setOnClickListener {
            moveToNext("Settings")
        }

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                binding?.progressBar?.isVisible = false
                if (
                    !date.isEqual(
                        LocalDateTime.parse("1970-01-01 00:00:00", defaultDateFormat)
                    )
                ) {
                    if (date.toLocalDate().isEqual(dbDate)) { // we only really need the date here
                        binding?.recyclerView?.adapter = SymptomsAdapter(listDb, requireContext(), this@SymptomsFragment)
                    } else {
                        binding?.recyclerView?.adapter = SymptomsAdapter(listSymptomsData, requireContext(), this@SymptomsFragment)
                    }
                } else {
                    binding?.recyclerView?.adapter = SymptomsAdapter(listSymptomsData, requireContext(), this@SymptomsFragment)
                }

            }
        }

        binding?.recyclerView?.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        populateSymptoms(date)

        binding?.addNewSymptom?.setOnClickListener {
            val dialogSymptoms = Dialog(requireContext())
            dialogSymptoms.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogSymptoms.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogSymptoms.setContentView(R.layout.dialog_new_symptom)
            dialogSymptoms.create()
            dialogSymptoms.setCancelable(true)
            val window: Window? = dialogSymptoms.window
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            dialogSymptoms.show()

            val btnOk: TextView = dialogSymptoms.findViewById(R.id.btnOk)
            val btnCancel: TextView = dialogSymptoms.findViewById(R.id.btnCancel)
            val editText: EditText = dialogSymptoms.findViewById(R.id.et_symptom_name)

            btnOk.setOnClickListener {
                if (editText.text.toString().trim() != "") {
                    myViewModel.addData(arrayListOf(SymptomModel(0, "", editText.text.toString(), "")))
                    dialogSymptoms.dismiss()
                    binding?.recyclerView?.adapter?.notifyDataSetChanged()

                } else {
                    Toast.makeText(requireContext(), "Please enter symptom", Toast.LENGTH_SHORT).show()
                }
            }

            btnCancel.setOnClickListener {
                dialogSymptoms.dismiss()
            }
        }

    }

    private fun moveToNext(destination: String) {
        if (findNavController().currentDestination?.id == R.id.symptomsFragment) {
            when (destination) {
                "Home" -> {
                    findNavController().navigate(R.id.action_symptomsFragment_to_homeFragment)
                }
                "Settings" -> {
                    findNavController().navigate(R.id.action_symptomsFragment_to_settingsFragment)
                }
            }
        }
    }

    private fun fragmentBackPress() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (exit) {
                    requireActivity().finishAndRemoveTask()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.press_again_to_exit),
                        Toast.LENGTH_SHORT
                    ).show()
                    exit = true
                    countDown = object : CountDownTimer(3000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                            exit = false
                        }
                    }
                    countDown.start()

                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    override fun onDestroyView() {
        listSymptomsSelected.clear()

        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::callback.isInitialized) {
            callback.isEnabled = false
            callback.remove()
        }
    }

    var myTag: Any = ""


    fun getSelectedSymptomsForDate(date: LocalDate):  List<SymptomModel>{
        val filtered = listDb.filter{ // listDb is all items
                item -> item.date != "" &&
                item.date == date.toString()
        }
        for (symptom in filtered){
            Log.d("filtered", symptom.symptom.toString())
        }

        return filtered
    }

    fun addSelectedSymptom(
        sym_id: Int,
        position: Int,
        date: LocalDate,
        list: ArrayList<SymptomModel>,
        intensity: String){
        val debug_tag = "addSelectedSymptom"
        try{
            listDb.add(
              SymptomModel(
                        sym_id,
                        date.toString(),
                        list[position].symptom, //todo: change way this works - its a bit murky
                        intensity
                    )
                )

            myViewModel.addData(listDb)



        }catch (e: java.lang.Exception){
            Log.e(debug_tag,e.toString())
        }
    }


    fun populateSelectedSymptoms(date: LocalDate){
        val blankSymptoms = listDb.filter { item -> item.date == "" }
        val symptomsToday = getSelectedSymptomsForDate(date)

        for (symptom in symptomsToday){
            Log.d("symptom", symptom.symptom.toString())
        }
    }

    fun setSymptomSeverity(position: Int, date: LocalDate, intensity: String){
        val selectedSymptom = listDb[position]
        val sym_id = selectedSymptom.id + 43 //this will need to change to be one more than the size of the total record count in the listDb
        val symptomsToday = getSelectedSymptomsForDate(date)
        val found = symptomsToday.filter {
                item -> item.symptom == selectedSymptom.symptom
        }

        if (found.isNotEmpty()){
            listDb[listDb.indexOf(found[0])] = SymptomModel(
                found[0].id,
                date.toString(),
                selectedSymptom.symptom,
                intensity
            )
        }else{
            addSelectedSymptom(sym_id,position,date,listDb,intensity)
        }
        listSymptomsSelected.clear()
        myViewModel.addData(listDb)

    }
    override fun myItemClicked(currentView: TextView, position: Int, list: ArrayList<SymptomModel>) {
        if (!date.isEqual(defaultDate)){
            if (currentView.tag != myTag) {
                when (currentView.tag) {
                    "low$position" -> {
                        setSymptomSeverity(
                            position,
                            date.toLocalDate(),
                            "low"
                        )
                    }
                    "med$position" -> {
                        setSymptomSeverity(
                            position,
                            date.toLocalDate(),
                            "med"
                        )
                    }
                    else -> {
                        setSymptomSeverity(
                            position,
                            date.toLocalDate(),
                            "high"
                        )
                    }
                }
            }
        }
        myTag = currentView.tag

    }

    override fun onPause() {
        if (!date.isEqual(defaultDate)) {
                listDb.forEach { it1 ->
                    it1.intensity?.let { it1.date?.let { it2 -> it1.symptom?.let { it3 -> myViewModel.updateData(it, it2, it3) } } }
                }
        }
        super.onPause()
    }
    companion object {
        val listSymptomsSelected = arrayListOf<SymptomModel>()
    }


}