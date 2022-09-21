package com.digitalinterruption.lex.ui

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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment(), CalendarView.OnDateChangeListener {
    private var startMonth: Int = 0
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding
    private lateinit var callback: OnBackPressedCallback
    var exit: Boolean = false
    lateinit var prefs: SharedPrefs
    lateinit var countDown: CountDownTimer

    var selectedDates: List<Date>? = null
    var start: Date? = null
    var end: Date? = null
    var initialDate: Date? = null
    var lastDate: Date? = null
    var startDay: Int = 0
    var oneTime: Boolean = true
    var oneTimeEvent: Boolean = true

    val args: HomeFragmentArgs by navArgs()
    val myViewModel: MyViewModel by viewModels()
    val mEvents: MutableList<EventObjects> = ArrayList<EventObjects>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        prefs = SharedPrefs(requireContext())


        myViewModel.readAllData.observe(viewLifecycleOwner) {
            if (it.stream().anyMatch { it.date != "" }) {
                listData.addAll(it)
            }
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

        //new layout

        //Custom Events

        val yellow = ContextCompat.getColor(requireContext(), R.color.darkYellow)
        val green = ContextCompat.getColor(requireContext(), R.color.green)
        val todayColor = ContextCompat.getColor(requireContext(), R.color.blue)

        val pms = ContextCompat.getColor(requireContext(), R.color.pms)
        val ov = ContextCompat.getColor(requireContext(), R.color.ov)

        CoroutineScope(IO).launch {
            delay(1000)
            if (args.fromWhichPin == "firstPin" && !LockFragment.isSecondaryPin) {
                val eventObjectsToday = EventObjects(80, "Today", Date())
                eventObjectsToday.color = todayColor
                mEvents.add(eventObjectsToday)
                listData.forEach {
                    if (it.date != "") {
                        Log.i("TLogs", "onViewCreated: ${it.date}")
                        val date = it.date?.let { it1 -> dateFormat(it1) }
                        if (oneTime) {
                            startDay = it.date?.substringBefore(" ")?.toInt()!!
                            oneTime = false
                        }

                        if (it.date?.substringBefore(" ")?.toInt()!! < startDay) {
                            startDay = it.date.substringBefore(" ").toInt()
                        }


                        if (it.intensity != "") {
                            initialDate = date
                            var eventObjects: EventObjects? = null
                            if (date != lastDate) {
                                eventObjects = EventObjects(it.id, "     ", date)
                                if (it.symptom != "Bleeding") {
                                    eventObjects.color = green
                                } else {
                                    Log.i("TLogs", "yello: ")
                                    eventObjects.color = yellow
                                }
                            }

                            if (it.symptom == "Bleeding") {
                                if (prefs.getOvulationEnabled()) {
                                    val eventObjects2 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 13)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventObjects2)

                                    val eventOv2 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 14)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventOv2)

                                    val eventOv3 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 15)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventOv3)

                                    val eventOv4 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 16)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventOv4)

                                    val eventOv5 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 17)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventOv5)

                                    val eventOv6 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 18)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventOv6)


                                }

                                if (prefs.getPmsEnabled()) {
                                    val eventPms1 = EventObjects(
                                        it.id + 2,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.minus(
                                            86400000 * 5
                                        )?.let { it1 -> Date(it1) })
                                    eventPms1.color = pms
                                    val eventPms2 = EventObjects(
                                        it.id + 3,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.minus(
                                            86400000 * 4
                                        )?.let { it1 -> Date(it1) })
                                    eventPms2.color = pms
                                    val eventPms3 = EventObjects(
                                        it.id + 4,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.minus(
                                            86400000 * 3
                                        )?.let { it1 -> Date(it1) })
                                    eventPms3.color = pms
                                    val eventPms4 = EventObjects(
                                        it.id + 5,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.minus(
                                            86400000 * 2
                                        )?.let { it1 -> Date(it1) })
                                    eventPms4.color = pms
                                    val eventPms5 = EventObjects(
                                        it.id + 6,
                                        "",
                                        dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.minus(
                                            86400000 * 1
                                        )?.let { it1 -> Date(it1) })
                                    eventPms5.color = pms

                                    mEvents.add(eventPms1)
                                    mEvents.add(eventPms2)
                                    mEvents.add(eventPms3)
                                    mEvents.add(eventPms4)
                                    mEvents.add(eventPms5)
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
                            Log.i("TLogs", "onViewCreated: ${l} ${MainActivity.month}")
                            if (adapterView.adapter.getView(l.toInt(), null, null).alpha == 0.4f) {
                                Log.d("hello", "hello")
                            } else {
                                val today = Calendar.getInstance()
                                today.time = Date()
                                val tapedDay = Calendar.getInstance()
                                tapedDay.time = adapterView.adapter.getItem(l.toInt()) as Date
                                val sameDay =
                                    tapedDay[Calendar.YEAR] == tapedDay[Calendar.YEAR] && today[Calendar.DAY_OF_YEAR] == tapedDay[Calendar.DAY_OF_YEAR]
                                /*  if (today.after(tapedDay) && !sameDay) {
                                      Toast.makeText(
                                          requireContext(),
                                          "You can't select previous date.",
                                          Toast.LENGTH_LONG
                                      ).show()
                                      adapterView.adapter.getView(l.toInt(), null, null)

                                  } else {*/
                                Log.i("TLogs", "onDateSelected: ${calendarCustomView.mAdapter.dateCal.get(Calendar.DAY_OF_MONTH)} ${MainActivity.month} ${MainActivity.year}")
                                val action =
                                    HomeFragmentDirections.actionHomeFragmentToSymptomsFragment("${calendarCustomView.mAdapter.dateCal.get(Calendar.DAY_OF_MONTH)} ${MainActivity.month} ${MainActivity.year}")
                                findNavController().navigate(action)
                                // }
                            }
                        }
                    //calendarCustomView.setRangesOfDate(makeDateRanges());
                }
            } else {
                listData.forEach {
                    val eventObjectsToday = EventObjects(80, "Today", Date())
                    eventObjectsToday.color = todayColor
                    mEvents.add(eventObjectsToday)
                    if (it.date != "") {
                        Log.i("TLogs", "onViewCreated: ${it.date}")
                        if (oneTime) {
                            startDay = it.date?.substringBefore(" ")?.toInt()!!
                            val month = it.date.substringAfter(" ")
                            startMonth = month.substringBefore(" ").toInt()
                            Log.i("TLogs", "onViewCreated: ${startMonth}")
                            oneTime = false
                        }
                        val date = startDay

                        if (it.date?.substringBefore(" ")?.toInt()!! < startDay) {
                            if (it.symptom == "Bleeding" && startDay == 0) {
                                startDay = it.date.substringBefore(" ").toInt()
                            }
                        }
                        if ((it.date.substringAfter(" ").substringBefore(" ")).toInt() < startMonth) {
                            startMonth = (it.date.substringAfter(" ").substringBefore(" ")).toInt()
                        }

                        while (startMonth <= 12) {
                            if (it.intensity != "") {
                                val eventObjects = EventObjects(it.id, "     ", dateFormat("$startDay $startMonth ${MainActivity.year}"))

                                if (it.symptom == "Bleeding") {
                                    eventObjects.color = yellow
                                } else {
                                    eventObjects.color = green
                                }
                                mEvents.add(eventObjects)

                                if (prefs.getOvulationEnabled()) {
                                    val eventObjects2 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.plus(86400000 * 13)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventObjects2)

                                    val eventOv2 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.plus(86400000 * 14)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventOv2)

                                    val eventOv3 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.plus(86400000 * 15)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventOv3)

                                    val eventOv4 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.plus(86400000 * 16)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventOv4)

                                    val eventOv5 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.plus(86400000 * 17)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventOv5)

                                    val eventOv6 = EventObjects(
                                        it.id + 1,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.plus(86400000 * 18)?.let { it1 -> Date(it1) })
                                    eventObjects2.color = ov
                                    mEvents.add(eventOv6)


                                }

                                if (prefs.getPmsEnabled()) {
                                    val eventPms1 = EventObjects(
                                        it.id + 2,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.minus(86400000 * 5)?.let { it1 -> Date(it1) })
                                    eventPms1.color = pms
                                    val eventPms2 = EventObjects(
                                        it.id + 3,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.minus(86400000 * 4)?.let { it1 -> Date(it1) })
                                    eventPms2.color = pms
                                    val eventPms3 = EventObjects(
                                        it.id + 4,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.minus(
                                            86400000 * 3
                                        )?.let { it1 -> Date(it1) })
                                    eventPms3.color = pms
                                    val eventPms4 = EventObjects(
                                        it.id + 5,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.minus(
                                            86400000 * 2
                                        )?.let { it1 -> Date(it1) })
                                    eventPms4.color = pms
                                    val eventPms5 = EventObjects(
                                        it.id + 6,
                                        "",
                                        dateFormat("$startDay ${startMonth.plus(1)} ${MainActivity.year}")?.time?.minus(
                                            86400000 * 1
                                        )?.let { it1 -> Date(it1) })
                                    eventPms5.color = pms

                                    mEvents.add(eventPms1)
                                    mEvents.add(eventPms2)
                                    mEvents.add(eventPms3)
                                    mEvents.add(eventPms4)
                                    mEvents.add(eventPms5)

                                }

                            }
                            startMonth++
                            startDay + 2
                        }
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
                            Log.i("TLogs", "onViewCreated: ${l} ${MainActivity.month}")
                            if (adapterView.adapter.getView(l.toInt(), null, null).alpha == 0.4f) {
                                Log.d("hello", "hello")
                            } else {
                                val today = Calendar.getInstance()
                                today.time = Date()
                                val tapedDay = Calendar.getInstance()
                                tapedDay.time = adapterView.adapter.getItem(l.toInt()) as Date
                                val sameDay =
                                    tapedDay[Calendar.YEAR] == tapedDay[Calendar.YEAR] && today[Calendar.DAY_OF_YEAR] == tapedDay[Calendar.DAY_OF_YEAR]
                                if (today.after(tapedDay) && !sameDay) {
                                    Toast.makeText(
                                        requireContext(),
                                        "You can't select previous date.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    adapterView.adapter.getView(l.toInt(), null, null)

                                } else {
                                    Log.i("TLogs", "onDateSelected: ${calendarCustomView.mAdapter.dateCal.get(Calendar.DAY_OF_MONTH)} ${MainActivity.month} ${MainActivity.year}")
                                    val action =
                                        HomeFragmentDirections.actionHomeFragmentToSymptomsFragment("${calendarCustomView.mAdapter.dateCal.get(Calendar.DAY_OF_MONTH)} ${MainActivity.month} ${MainActivity.year}")
                                    findNavController().navigate(action)
                                }
                            }
                        }
                    //calendarCustomView.setRangesOfDate(makeDateRanges());
                }
            }

        }

    }

    fun addEvents(context: Context): MutableList<EventObjects> {
        mEvents.clear()
        var prefs: SharedPrefs = SharedPrefs(context)
        val yellow = ContextCompat.getColor(context, R.color.darkYellow)
        val green = ContextCompat.getColor(context, R.color.green)
        val todayColor = ContextCompat.getColor(context, R.color.blue)

        val pms = ContextCompat.getColor(context, R.color.pms)
        val ov = ContextCompat.getColor(context, R.color.ov)
        val eventObjectsToday = EventObjects(80, "Today", Date())
        eventObjectsToday.color = todayColor
        mEvents.add(eventObjectsToday)
        listData.forEach {
            if (it.date != "") {
                Log.i("TLogs", "onViewCreated: ${it.date}")
                val date = it.date?.let { it1 -> dateFormat(it1) }
                if (oneTime) {
                    startDay = it.date?.substringBefore(" ")?.toInt()!!
                    oneTime = false
                }

                if (it.date?.substringBefore(" ")?.toInt()!! < startDay) {
                    startDay = it.date.substringBefore(" ").toInt()
                }


                if (it.intensity != "") {
                    initialDate = date
                    var eventObjects: EventObjects? = null
                    if (date != lastDate) {
                        eventObjects = EventObjects(it.id, "     ", date)
                        if (it.symptom != "Bleeding") {
                            eventObjects.color = green
                        } else {
                            Log.i("TLogs", "yello: ")
                            eventObjects.color = yellow
                        }
                    }

                    if (it.symptom == "Bleeding") {

                        if (prefs.getOvulationEnabled()) {
                            val eventObjects2 = EventObjects(
                                it.id + 1,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 13)?.let { it1 -> Date(it1) })
                            eventObjects2.color = ov
                            mEvents.add(eventObjects2)

                            val eventOv2 = EventObjects(
                                it.id + 1,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 14)?.let { it1 -> Date(it1) })
                            eventObjects2.color = ov
                            mEvents.add(eventOv2)

                            val eventOv3 = EventObjects(
                                it.id + 1,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 15)?.let { it1 -> Date(it1) })
                            eventObjects2.color = ov
                            mEvents.add(eventOv3)

                            val eventOv4 = EventObjects(
                                it.id + 1,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 16)?.let { it1 -> Date(it1) })
                            eventObjects2.color = ov
                            mEvents.add(eventOv4)

                            val eventOv5 = EventObjects(
                                it.id + 1,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 17)?.let { it1 -> Date(it1) })
                            eventObjects2.color = ov
                            mEvents.add(eventOv5)

                            val eventOv6 = EventObjects(
                                it.id + 1,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.plus(86400000 * 18)?.let { it1 -> Date(it1) })
                            eventObjects2.color = ov
                            mEvents.add(eventOv6)


                        }

                        if (prefs.getPmsEnabled()) {
                            val eventPms1 = EventObjects(
                                it.id + 2,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.minus(
                                    86400000 * 5
                                )?.let { it1 -> Date(it1) })
                            eventPms1.color = pms
                            val eventPms2 = EventObjects(
                                it.id + 3,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.minus(
                                    86400000 * 4
                                )?.let { it1 -> Date(it1) })
                            eventPms2.color = pms
                            val eventPms3 = EventObjects(
                                it.id + 4,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.minus(
                                    86400000 * 3
                                )?.let { it1 -> Date(it1) })
                            eventPms3.color = pms
                            val eventPms4 = EventObjects(
                                it.id + 5,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.minus(
                                    86400000 * 2
                                )?.let { it1 -> Date(it1) })
                            eventPms4.color = pms
                            val eventPms5 = EventObjects(
                                it.id + 6,
                                "",
                                dateFormat("$startDay ${date?.month?.plus(1)} ${date?.year?.plus(1900)}")?.time?.minus(
                                    86400000 * 1
                                )?.let { it1 -> Date(it1) })
                            eventPms5.color = pms

                            mEvents.add(eventPms1)
                            mEvents.add(eventPms2)
                            mEvents.add(eventPms3)
                            mEvents.add(eventPms4)
                            mEvents.add(eventPms5)
                        }
                    }

                    if (eventObjects != null) {
                        mEvents.add(eventObjects)
                    }
                }
                lastDate = date
            }
        }
        Log.i("TLogs", "addEvents: ${mEvents.size}")
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

        // findNavController().navigate(action)
    }


    private fun makeDateRanges(): List<EventObjects> {
        if (lastDate?.after(initialDate) == true) {
            start = initialDate
            end = lastDate
        } else {
            start = lastDate
            end = initialDate
        }
        val eventObjectses: MutableList<EventObjects> = ArrayList()
        val gcal = GregorianCalendar()
        gcal.time = start
        while (!gcal.time.after(end)) {
            val d = gcal.time
            val eventObject = EventObjects("", d)
            //eventObject.color = resources.getColor(R.color.green)

            eventObjectses.add(eventObject)
            gcal.add(Calendar.DATE, 1)
        }
        return eventObjectses
    }

    private fun dateFormat(date: String): Date? {

        val day = date.substringBefore(" ")
        val beforeMonth = date.substringBeforeLast(" ")
        val month = beforeMonth.substringAfter(" ")
        val year = date.substringAfterLast(" ")
        myYear = year.toInt()

        Log.i("TLogs", "dateFormat:  $year, $month, $day")

//        String dtStart = "2022-08-07T09:27:37Z";

//        String dtStart = "2022-08-07T09:27:37Z";
        val dtStart = year + "-" + month + "-" + day + "T00:00:00Z"
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        return try {
            val newDate = format.parse(dtStart)
            Log.i("MyTag", "onCreate: $newDate")
            newDate
        } catch (e: ParseException) {
            Log.i("MyTag", "onCreate: ")
            e.printStackTrace()
            null
        }
    }

    companion object {
        var myYear = 0
        var listData = arrayListOf<SymptomModel>()
    }

}