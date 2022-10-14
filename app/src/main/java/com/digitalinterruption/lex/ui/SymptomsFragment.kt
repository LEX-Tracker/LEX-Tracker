package com.digitalinterruption.lex.ui

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
    var date: String? = null
    var dbDate: String? = null
    val args: SymptomsFragmentArgs by navArgs()
    lateinit var prefs: SharedPrefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSymptomsBinding.inflate(inflater, container, false)
        prefs = SharedPrefs(requireContext())
        if (!LockFragment.isSecondaryPin) {
            date = args.date
        } else {
            date = "-1 -1 -1"
        }

        Log.i("TLogs", "onCreateView: $date")

        val defaultSymptomsArray = resources.getStringArray(R.array.symptoms) //Gets list of default symptoms

        for(i in 1..defaultSymptomsArray.size){
            listSymptoms.add(SymptomModel(i, "", defaultSymptomsArray[i-1], "")) //Adds them as a symptom
        }

        for(i in 0 until defaultSymptomsArray.size - 1)
            for(j in 0 until defaultSymptomsArray.size - 1)
                if(listSymptoms[i].symptom == listSymptoms[j].symptom)
                    if(listSymptoms[i].intensity != "") listSymptoms.removeAt(j)
                    else listSymptoms.removeAt(i)

        myViewModel.readAllData.observe(viewLifecycleOwner) { data ->
            listDb.clear()
            data.forEach {
                if (it.date == date) {
                    dbDate = it.date
                    listDb.add(it)
                }

                if (it.intensity == "") {
                    listSymptomsData.add(it)
                }
            }
            listSymptomsData.forEach { it1 ->
                if (listDb.stream().anyMatch { it.symptom != it1.symptom }) {
                    listDb.add(it1)
                }

            }
            binding?.recyclerView?.adapter?.notifyDataSetChanged()
        }



        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentBackPress()

        if (prefs.isFirst()) {
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
                if (date != "-1 -1 -1") {
                    if (date == dbDate) {
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
//        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
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

    override fun myItemClicked(currentView: TextView, position: Int, list: ArrayList<SymptomModel>) {

        if (date != "-1 -1 -1") {

            if (currentView.tag != myTag) {
                when (currentView.tag) {

                    "low$position" -> {
                        if (listDb.size > 0){
                            if (listDb[position].intensity != "") {
                                listDb[position] = SymptomModel(listDb[position].id, date!!, list[position].symptom, "low")
                            }
                            else {
                                try {
                                    if (listSymptomsSelected.size > 0) {

                                        if (listSymptomsSelected.stream().anyMatch { it.symptom == list[position].symptom }) {

                                            listSymptomsSelected[listSymptomsSelected.indexOf(list[position])] = SymptomModel(0, date!!, list[position].symptom, "low")
                                        } else {
                                            listSymptomsSelected.add(SymptomModel(listDb[position].id, date!!, list[position].symptom, "low"))
                                        }


                                    } else {

                                        listSymptomsSelected.add(SymptomModel(listDb[position].id, date!!, list[position].symptom, "low"))
                                    }
                                } catch (e: Exception) {
                                }

                            }
                        }
                         else {
                            try {
                                if (listSymptomsSelected.size > 0) {

                                    if (listSymptomsSelected.stream().anyMatch { it.symptom == list[position].symptom }) {

                                        listSymptomsSelected[listSymptomsSelected.indexOf(list[position])] = SymptomModel(0, date!!, list[position].symptom, "low")
                                    } else {
                                        listSymptomsSelected.add(SymptomModel(0, date!!, list[position].symptom, "low"))
                                    }


                                } else {

                                    listSymptomsSelected.add(SymptomModel(0, date!!, list[position].symptom, "low"))
                                }
                            } catch (e: Exception) {
                            }

                        }
                    }
                    "med$position" -> {
                        if (listDb.size>0){
                            if (listDb[position].intensity != "") {
                                listDb[position] = SymptomModel(listDb[position].id, date!!, list[position].symptom, "med")

                            } else {

                                try {
                                    if (listSymptomsSelected.size > 0) {

                                        if (listSymptomsSelected.stream().anyMatch { it.symptom == list[position].symptom }) {

                                            listSymptomsSelected[listSymptomsSelected.indexOf(list[position])] = SymptomModel(0, date!!, list[position].symptom, "med")
                                        } else {

                                            listSymptomsSelected.add(SymptomModel(listDb[position].id, date!!, list[position].symptom, "med"))
                                        }


                                    } else {

                                        listSymptomsSelected.add(SymptomModel(listDb[position].id, date!!, list[position].symptom, "med"))
                                    }
                                } catch (e: Exception) {

                                }
                            }
                        }
                        else {

                            try {
                                if (listSymptomsSelected.size > 0) {

                                    if (listSymptomsSelected.stream().anyMatch { it.symptom == list[position].symptom }) {

                                        listSymptomsSelected[listSymptomsSelected.indexOf(list[position])] = SymptomModel(0, date!!, list[position].symptom, "med")
                                    } else {

                                        listSymptomsSelected.add(SymptomModel(0, date!!, list[position].symptom, "med"))
                                    }


                                } else {

                                    listSymptomsSelected.add(SymptomModel(0, date!!, list[position].symptom, "med"))
                                }
                            } catch (e: Exception) {

                            }
                        }


                    }
                    else -> {
                        if(listDb.size > 0){
                            if (listDb[position].intensity != "") {
                                listDb[position] = SymptomModel(listDb[position].id, date!!, list[position].symptom, "high")

                            } else {
                                try {
                                    if (listSymptomsSelected.size > 0) {

                                        if (listSymptomsSelected.stream().anyMatch { it.symptom == list[position].symptom }) {

                                            listSymptomsSelected[listSymptomsSelected.indexOf(list[position])] = SymptomModel(listDb[position].id, date!!, list[position].symptom, "high")
                                        } else {
                                            listSymptomsSelected.add(SymptomModel(listDb[position].id, date!!, list[position].symptom, "high"))
                                        }


                                    } else {

                                        listSymptomsSelected.add(SymptomModel(listDb[position].id, date!!, list[position].symptom, "high"))
                                    }
                                } catch (e: Exception) {

                                }
                            }
                        }
                        else {
                            try {
                                if (listSymptomsSelected.size > 0) {

                                    if (listSymptomsSelected.stream().anyMatch { it.symptom == list[position].symptom }) {

                                        listSymptomsSelected[listSymptomsSelected.indexOf(list[position])] = SymptomModel(0, date!!, list[position].symptom, "high")
                                    } else {
                                        listSymptomsSelected.add(SymptomModel(0, date!!, list[position].symptom, "high"))
                                    }


                                } else {

                                    listSymptomsSelected.add(SymptomModel(0, date!!, list[position].symptom, "high"))
                                }
                            } catch (e: Exception) {

                            }
                        }

                    }
                }
            }
        }
        myTag = currentView.tag

    }

    override fun onPause() {
        if (date != "0 0 0" && date != "-1 -1 -1") {

            if (listSymptomsSelected.size == 0) {
                listDb.forEach { it1 ->
                    it1.intensity?.let { it1.date?.let { it2 -> it1.symptom?.let { it3 -> myViewModel.updateData(it, it2, it3) } } }
                }

            } else {
                myViewModel.addData(listSymptomsSelected)
            }
        }

        super.onPause()
    }

    companion object {
        val listSymptomsSelected = arrayListOf<SymptomModel>()
    }


}