package com.digitalinterruption.lex.ui.main

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.digitalinterruption.lex.MainActivity
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.calender.CalendarCustomView
import com.digitalinterruption.lex.calender.EventObjects
import com.digitalinterruption.lex.databinding.FragmentHomeBinding
import com.digitalinterruption.lex.models.MyViewModel
import com.digitalinterruption.lex.models.SymptomModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.sql.Timestamp
import java.util.*
import java.time.format.DateTimeFormatter


class HomeFragment : Fragment(), CalendarView.OnDateChangeListener {
    private var startMonth: Int = 0
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding
    private lateinit var callback: OnBackPressedCallback
    var exit: Boolean = false
    lateinit var prefs: SharedPrefs
    lateinit var countDown: CountDownTimer

    var selectedDates: List<Date>? = null
    var start: LocalDateTime? = null
    var end: LocalDateTime? = null
    var initialDate: LocalDateTime? = null
    var lastDate: LocalDateTime? = null
    var startDay: Int = 0
    var oneTime: Boolean = true
    var oneTimeEvent: Boolean = true

    val args: HomeFragmentArgs by navArgs()
    val myViewModel: MyViewModel by viewModels()
    val mEvents: MutableList<EventObjects> = ArrayList<EventObjects>()
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    //Colours

    val todayColor = R.color.blue
    val yellow = R.color.darkYellow
    val green = R.color.green
    val pms = R.color.pms
    val ov = R.color.ov

    private fun getDuressData(_seedDate: LocalDateTime?): Collection<SymptomModel> {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val seedDate = _seedDate?.minusDays(4)
        val duressData: MutableList<SymptomModel> = arrayListOf()

        duressData.add(0, SymptomModel(1, seedDate?.format(formatter),"Bleeding", "high"))

        //ToDo: make this more robust
        var i = 1
        while (i < 20){
            duressData.add(i,
                SymptomModel(i, LocalDateTime.now().plusDays(i.toLong()).format(formatter), "", "low")
            )
            i +=1
        }


        return duressData
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        prefs = SharedPrefs(requireContext())

        if (!prefs.getIsDuressPin()){
            myViewModel.readAllData.observe(viewLifecycleOwner) {
                if (it.stream().anyMatch { it.date != "" }) {
                    listData.addAll(it)
                }
            }
        }else{
            // getDuressData generates fake data - probably a good idea to store it somewhere
            // in case app needs opening multiple times it should look consistent
            listData.addAll(getDuressData(LocalDateTime.now()))//duress data
        }
        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentBackPress()

        binding?.ivHome?.setOnClickListener {}
        binding?.ivSymptoms?.setOnClickListener {
            moveToNext("Symptoms")
        }
        binding?.ivSettings?.setOnClickListener {
            moveToNext("Settings")
        }

        binding?.calenderV?.setOnDateChangeListener(this)

        var calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE))
        val endOfMonth: Long = calendar.timeInMillis
        calendar = Calendar.getInstance()
        calendar.set(Calendar.DATE, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val startOfMonth: Long = calendar.timeInMillis
        binding?.calenderV?.maxDate = endOfMonth
        binding?.calenderV?.minDate = startOfMonth

        CoroutineScope(IO).launch {
        delay(1000)
        //ToDo: fix calendar - it currently clears when you move from the current month
            // the events are passed in (if duress pin false data is generated and passed in instead)
        val eventObjectsToday = EventObjects(80, "Today", LocalDateTime.now())
        eventObjectsToday.color = todayColor
        mEvents.add(eventObjectsToday)
        listData.forEach {
            if (it.date != "") {

                val date = LocalDateTime.parse(it.date, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

                if (oneTime) {
                    startDay = date.dayOfMonth
                    oneTime = false
                }

                if (startDay > date.dayOfMonth){
                    startDay = date.dayOfMonth
                }

                if (it.intensity != "") {
                    initialDate = date
                    var eventObjects: EventObjects? = null
                    if (date != lastDate) {
                        eventObjects = EventObjects(it.id, "     ", date)
                        if (it.symptom != "Bleeding") {
                            eventObjects.color = green
                        } else {
                            eventObjects.color = yellow
                        }
                    }

                    //ToDo: investigate why colours aren't being assigned correctly
                    if (it.symptom == "Bleeding") {
                        if (prefs.getOvulationEnabled()) {
                            populateOvEvents(mEvents, it)
                        }
                        if (prefs.getPmsEnabled()) {
                            populatePMSEvents(mEvents, it)
                        }
                    }

                    if (eventObjects != null) {
                        mEvents.add(eventObjects)
                    }
                }
                lastDate = date
            }
        }

        withContext(Main) {
            binding?.progressBar?.isVisible = false
            binding?.layoutCalender?.removeAllViews()
            binding?.layoutCalender?.orientation = LinearLayout.VERTICAL

            val calendarCustomView = CalendarCustomView(requireContext(), mEvents)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            calendarCustomView.layoutParams = layoutParams
            binding?.layoutCalender?.addView(calendarCustomView)



            calendarCustomView.calendarGridView.onItemClickListener =
                OnItemClickListener { adapterView, view, i, l ->

                    if (adapterView.adapter.getView(l.toInt(), null, null).alpha == 0.4f) { //WHAT?
                        Log.d("hello", "hello")
                    } else {
                        val today = Calendar.getInstance()
                        today.time = Timestamp.valueOf(
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                .toString())
                        val tappedDay = Calendar.getInstance()
                        tappedDay.time = Timestamp.valueOf(
                            LocalDateTime.ofInstant(
                                (adapterView.adapter.getItem(l.toInt()) as Date).toInstant(),
                                ZoneOffset.systemDefault()
                            ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                .toString()
                        )
                            //adapterView.adapter.getItem(l.toInt()) as Date

                        val action =
                            HomeFragmentDirections.actionHomeFragmentToSymptomsFragment(
                                LocalDateTime.ofInstant(
                                    tappedDay.time.toInstant(),
                                    ZoneOffset.systemDefault()
                                ).format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                ).toString()
                            )
                        findNavController().navigate(action)

                    }
                }
            }

        }

    }

    fun addEvents(context: Context): MutableList<EventObjects> {
        mEvents.clear()
        var prefs: SharedPrefs = SharedPrefs(context)
        val eventObjectsToday = EventObjects(80, "Today", LocalDateTime.now())
        eventObjectsToday.color = todayColor
        mEvents.add(eventObjectsToday)
        listData.forEach {
            if (it.date != "") {

                val date = LocalDateTime.now()
                if (oneTime) {
                    startDay = date.dayOfMonth
                    oneTime = false
                }

                if (startDay > date.dayOfMonth){
                    startDay = date.dayOfMonth
                }


                if (it.intensity != "") {
                    initialDate = date
                    var eventObjects: EventObjects? = null
                    if (date != lastDate) {
                        eventObjects = EventObjects(it.id, "     ", date)
                        if (it.symptom != "Bleeding") {
                            eventObjects.color = green
                        } else {
                            eventObjects.color = yellow
                        }
                    }

                    if (it.symptom == "Bleeding") {

                        if (prefs.getOvulationEnabled()) {
                            populateOvEvents(mEvents, it)
                        }

                        if (prefs.getPmsEnabled()) {
                            populatePMSEvents(mEvents, it)
                        }
                    }

                    if (eventObjects != null) {
                        mEvents.add(eventObjects)
                    }
                }
                lastDate = date
            }
        }
        return mEvents
    }

    private fun moveToNext(destination: String) {
        if (findNavController().currentDestination?.id == R.id.homeFragment) {
            when (destination) {
                "Symptoms" -> {
                    findNavController().navigate(R.id.action_homeFragment_to_symptomsFragment)
                }
                "Settings" -> {
                    findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
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

    override fun onSelectedDayChange(p0: CalendarView, p1: Int, month: Int, date: Int) {


        val action = HomeFragmentDirections.actionHomeFragmentToSymptomsFragment("$date ${month + 1}")

    }



    fun populateOvEvents(mEvents: MutableList<EventObjects>, symptom: SymptomModel){
        var days = 13
        while (days < 19) {

            val date = LocalDateTime.parse(symptom.date, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                .plusDays(days.toLong())
                .plusMonths(1)
            val event = EventObjects(
                symptom.id + 1,
                "ov",
                date
            )
            event.color = R.color.ov
            mEvents.add(event)
            days++
        }

    }

    fun populatePMSEvents(mEvents: MutableList<EventObjects>, symptom: SymptomModel){
        var days = 6
        while (days > 0) {
            val date = LocalDateTime.parse(symptom.date, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                .plusDays(days.toLong())
                .plusMonths(1)
            val event = EventObjects(
                symptom.id + 1,
                "pms",
                date
            )
            event.color = R.color.pms
            mEvents.add(event)
            days--
        }

    }

    companion object {
        var myYear = 0
        var listData = arrayListOf<SymptomModel>()
    }

}