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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.calender.CalendarCustomView
import com.digitalinterruption.lex.calender.EventObject
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
import java.time.LocalDate
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
    var initialDate: LocalDateTime? = LocalDate.parse("1970-01-01").atStartOfDay()
    var lastDate: LocalDateTime? = initialDate
    var startDay: Int = 0
    var oneTime: Boolean = true
    var oneTimeEvent: Boolean = true

    val args: HomeFragmentArgs by navArgs()
    val myViewModel: MyViewModel by viewModels()
    val mEvents: MutableList<EventObject> = ArrayList<EventObject>()
    val defaultDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    //Colours
    // initialise here but get correct values on view create
    var todayColor = R.color.blue
    var yellow = R.color.darkYellow
    var green = R.color.green
    var ov = R.color.ov
    var pms = R.color.pms

    private fun getDuressData(_seedDate: LocalDateTime?): Collection<SymptomModel> {

        val seedDate = _seedDate?.minusDays(4)
        val duressData: MutableList<SymptomModel> = arrayListOf()

        if (seedDate != null) {
            duressData.add(0, SymptomModel(1, seedDate.format(defaultDateTimeFormat),"Bleeding", "high"))
        }

        //ToDo: make this more robust
        var i = 1
        while (i < 20){
            duressData.add(i,
                SymptomModel(i, LocalDateTime.now().plusDays(i.toLong()).format(defaultDateTimeFormat), "", "low")
            )
            i +=1
        }


        return duressData
    }

    fun populateEvents(listData: ArrayList<SymptomModel>, prefs: SharedPrefs){
        if (!prefs.getIsDuressPin()){
            myViewModel.readAllData.observe(viewLifecycleOwner) {
                if (it.stream().anyMatch { it.date != "" }) {
                    Companion.listData.addAll(it)
                }
            }
        }else{
            listData.addAll(getDuressData(prefs.getDuressSeedDate()))
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        prefs = SharedPrefs(requireContext())

        populateEvents(listData, prefs)
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

        val appContext = activity?.applicationContext!!

        CoroutineScope(IO).launch {
        delay(1000)
        //ToDo: fix calendar - it currently clears when you move from the current month
        // the events are passed in (if duress pin false data is generated and passed in instead)

        addEvents(appContext)

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
                    } else {
                        val today = Calendar.getInstance()
                        today.time = Timestamp.valueOf(
                            LocalDateTime.now().format(defaultDateTimeFormat)
                                .toString())

                        val tappedDay = Calendar.getInstance()
                        tappedDay.time = Timestamp.valueOf(
                            LocalDateTime.ofInstant(
                                (adapterView.adapter.getItem(l.toInt()) as Date).toInstant(),
                                ZoneOffset.systemDefault()
                            ).format(defaultDateTimeFormat)
                                .toString()
                        )

                        val action =
                            HomeFragmentDirections.actionHomeFragmentToSymptomsFragment(
                                LocalDateTime.ofInstant(
                                    tappedDay.time.toInstant(),
                                    ZoneOffset.systemDefault()
                                ).format(
                                    defaultDateTimeFormat
                                ).toString()
                            )
                        findNavController().navigate(action)

                    }
                }
            }

        }

    }

    fun addEvents(context: Context): MutableList<EventObject> {
        mEvents.clear()
        var prefs: SharedPrefs = SharedPrefs(context)
        //colours
        todayColor = ContextCompat.getColor(context,R.color.blue)
        yellow = ContextCompat.getColor(context, R.color.darkYellow)
        green = ContextCompat.getColor(context, R.color.green)
        ov = ContextCompat.getColor(context, R.color.ov)
        pms = ContextCompat.getColor(context, R.color.pms)

        val eventObjectToday = EventObject(
            80,
            "Today",
            LocalDate.now(),
            todayColor
        )

        mEvents.add(eventObjectToday)

        listData.forEach {
            if (it.date != "") {

                val date = LocalDate.parse(it.date) //this should be in the correct format anyway as we wrote it out in the correct format

                if (oneTime) {
                    startDay = date.dayOfMonth
                    oneTime = false
                }
                if (startDay > date.dayOfMonth){
                    startDay = date.dayOfMonth
                }

                if (it.intensity != "") {
                    initialDate = date.atStartOfDay()
                    var eventObject: EventObject? = null

                    if (!date.isEqual(lastDate?.toLocalDate())) {
                            eventObject = EventObject(it.id, "     ", date, yellow)
                            if (it.symptom != "Bleeding") {
                                eventObject.color = green
                            } else {
                                eventObject.color = yellow
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

                    if (eventObject != null) {
                        mEvents.add(eventObject)
                    }
                }
                lastDate = date.atStartOfDay()
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

    fun populateOvEvents(mEvents: MutableList<EventObject>, symptom: SymptomModel){

        var days = 13
        while (days < 19) {

            val date = LocalDate.parse(symptom.date).atStartOfDay()
                .plusDays( -days.toLong())
                .plusMonths(1)
            if (symptom.symptom == "Bleeding"){ //ToDo: workout how non-bleeding events ended up in here
                mEvents.add(
                    EventObject(
                        symptom.id + 1,
                        "ov" ,
                        date.toLocalDate(),
                        ov
                    )
                )
            }
            days++

        }

    }

    fun populatePMSEvents(mEvents: MutableList<EventObject>, symptom: SymptomModel){
        var days = 5
        while (days > 0) {
            val date = LocalDate.parse(symptom.date).atStartOfDay()
                .plusDays(-days.toLong())
                .plusMonths(1)
            if (symptom.symptom == "Bleeding"){
                mEvents.add(
                    EventObject(
                        symptom.id + 1,
                        "",
                        date.toLocalDate(),
                        pms
                    )
                )
            }
            days--
        }

    }

    companion object {
        var listData = arrayListOf<SymptomModel>()
    }

}