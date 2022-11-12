package com.digitalinterruption.lex.calender;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digitalinterruption.lex.MainActivity;
import com.digitalinterruption.lex.R;
import com.digitalinterruption.lex.adapters.MyMonthChanged;
import com.digitalinterruption.lex.ui.main.HomeFragment;
import com.digitalinterruption.lex.ui.Utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarCustomView extends LinearLayout implements CalenderUtils {

    private ImageView previousButton, nextButton;
    private TextView currentDate;
    public ExpandableHeightGridView calendarGridView;

    private static final int MAX_CALENDAR_COLUMN = 42;
    private int month, year;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public Calendar cal = Calendar.getInstance(Locale.ENGLISH);
    private Context context;
    public GridAdapter mAdapter;
    List<EventObjects> calendarEvents = new ArrayList<>();

    public CalendarCustomView(Context context, List<EventObjects> calendarEvents) {
        super(context);
        this.calendarEvents = calendarEvents;
        this.context = context;
        initializeUILayout();
        setUpCalendarAdapter((ArrayList<EventObjects>) calendarEvents);
        setPreviousButtonClickEvent();
        setNextButtonClickEvent();
        setGridCellClickEvents();
        setCurrentDateClickEvent();

    }

    public CalendarCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initializeUILayout();
        setPreviousButtonClickEvent();
        setNextButtonClickEvent();
        setGridCellClickEvents();
        setCurrentDateClickEvent();
    }

    public CalendarCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initializeUILayout() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calender_layout, this);
        previousButton = (ImageView) view.findViewById(R.id.previous_month);
        nextButton = (ImageView) view.findViewById(R.id.next_month);
        currentDate = (TextView) view.findViewById(R.id.display_current_date);
        calendarGridView = (ExpandableHeightGridView) view.findViewById(R.id.calendar_grid);
        calendarGridView.setExpanded(true);
    }

    private void setPreviousButtonClickEvent() {
        previousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarCustomView.this.previousMonths();

            }
        });
    }

    private void setNextButtonClickEvent() {
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarCustomView.this.nextMonth();

            }
        });
    }

    private void setCurrentDateClickEvent() {
        currentDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar currentDate = Calendar.getInstance();
                final Calendar date;
                date = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, final int dayOfMonth) {
                        cal.set(year, monthOfYear, dayOfMonth);
                        setUpCalendarAdapter((ArrayList<EventObjects>) calendarEvents);
                    }
                }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE));
                datePickerDialog.show();
            }
        });
    }

    public String setGridCellClickEvents() {
        final String[] text = new String[1];

        return text[0];
    }


    public void setUpCalendarAdapter(ArrayList<EventObjects> calendarEvents) {
        List<Date> dayValueInCells = new ArrayList<Date>();
        Calendar mCal = (Calendar) cal.clone();
        mCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfTheMonth = mCal.get(Calendar.DAY_OF_WEEK) - 1;
        mCal.add(Calendar.DAY_OF_MONTH, -firstDayOfTheMonth);
        while (dayValueInCells.size() < MAX_CALENDAR_COLUMN) {
            dayValueInCells.add(mCal.getTime());
            mCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        LocalDateTime sDate = LocalDateTime.ofInstant(cal.getTime().toInstant(), ZoneId.systemDefault());

        MainActivity.Companion.setMonth(cal.getTime().getMonth() + 1);
        MainActivity.Companion.setYear(cal.getTime().getYear() + 1900);


        currentDate.setText(
                sDate.format(
                        DateTimeFormatter.ofPattern("MMM uuuu")
                )
        );
        mAdapter = new GridAdapter(context, dayValueInCells, cal, calendarEvents);
        calendarGridView.setAdapter(mAdapter);
    }

    @Override
    public void nextMonth() {
        //TODO: this needs to be redone as it currently does nothing
        cal.add(Calendar.MONTH, 1);

        MainActivity.Companion.setMonth(cal.getTime().getMonth() + 1);
        MainActivity.Companion.setYear(cal.getTime().getYear() + 1900);

        ArrayList<EventObjects> list = (ArrayList<EventObjects>) Utils.Companion.changeEvents(context);

        this.calendarEvents.clear();
        setUpCalendarAdapter((ArrayList<EventObjects>) calendarEvents);

    }

    @Override
    public void previousMonths() {
        cal.add(Calendar.MONTH, -1);
        MainActivity.Companion.setMonth(cal.getTime().getMonth() + 1);
        MainActivity.Companion.setYear(cal.getTime().getYear() + 1900);

        ArrayList<EventObjects> list = (ArrayList<EventObjects>) Utils.Companion.changeEvents(context);
        /* this will never get hit as getMyYear always returns 0?

        if ((cal.getTime().getYear() + 1900) == HomeFragment.Companion.getMyYear()) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getDate().getYear() != (cal.getTime().getYear())) {
                    list.remove(i);
                }
            }
            Log.i(TAG, "nextMonth3: " + list.size());
            this.calendarEvents = list;

            setUpCalendarAdapter(list);
        } else {
            this.calendarEvents.clear();
            setUpCalendarAdapter((ArrayList<EventObjects>) calendarEvents);
        }*/
         this.calendarEvents.clear();
         setUpCalendarAdapter((ArrayList<EventObjects>) calendarEvents);
    }

    public void moveMonth(boolean moveForward){
        if (moveForward){
            cal.add(Calendar.MONTH, -1);
        }else{
            cal.add(Calendar.MONTH, 1);
        }

        MainActivity.Companion.setMonth(cal.getTime().getMonth()+1);
        MainActivity.Companion.setYear(cal.getTime().getYear()+1900);

        ArrayList<EventObjects> list = (ArrayList<EventObjects>) Utils.Companion.changeEvents(context);
        this.calendarEvents.clear();
        setUpCalendarAdapter((ArrayList<EventObjects>) calendarEvents);
    }

    public void setSelectedDates(EventObjects calendarEvents) {
        this.calendarEvents.add(calendarEvents);
        mAdapter.notifyDataSetChanged();
    }

    public void setRangesOfDate(List<EventObjects> calendarEvents) {
        this.calendarEvents = calendarEvents;
        setUpCalendarAdapter((ArrayList<EventObjects>) calendarEvents);
        mAdapter.notifyDataSetChanged();
    }

    public void removeSelectedDate(EventObjects calendarEvents) {
        for (int i = 0; i < this.calendarEvents.size(); i++) {
            if (this.calendarEvents.get(i).getDate().toString().equals(calendarEvents.getDate().toString())) {
                this.calendarEvents.remove(i);

            }
        }
        mAdapter.notifyDataSetChanged();
    }
}

