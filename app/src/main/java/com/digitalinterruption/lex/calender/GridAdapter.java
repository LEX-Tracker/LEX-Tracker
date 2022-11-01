package com.digitalinterruption.lex.calender;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.digitalinterruption.lex.R;

import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GridAdapter extends ArrayAdapter {
    private static final String TAG = GridAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private List<Date> monthlyDates;
    private Calendar currentDate;
    private List<EventObject> allEvents;
    CalendarCustomView calendarCustomView;
    public Calendar dateCal;

    public GridAdapter(Context context, List<Date> monthlyDates, Calendar currentDate, List<EventObject> allEvents) {
        super(context, R.layout.single_cell_layout);
        this.monthlyDates = monthlyDates;
        this.currentDate = currentDate;
        this.allEvents = allEvents;
        this.calendarCustomView = calendarCustomView;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Date mDate = monthlyDates.get(position);
        dateCal = Calendar.getInstance();
        dateCal.setTime(mDate);
        int dayValue = dateCal.get(Calendar.DAY_OF_MONTH);
        int displayMonth = dateCal.get(Calendar.MONTH) + 1;
        int displayYear = dateCal.get(Calendar.YEAR);
        int currentMonth = currentDate.get(Calendar.MONTH) + 1;
        int currentYear = currentDate.get(Calendar.YEAR);


        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.single_cell_layout, parent, false);
        }
        if (displayMonth == currentMonth && displayYear == currentYear) {
            view.setBackgroundColor(Color.parseColor("#F5F5F5"));
        } else {
            view.setBackgroundColor(Color.parseColor("#66F5F5F5"));
//            view.setAlpha(0.4f);
        }

        //Add day to calendar
        TextView cellNumber = (TextView) view.findViewById(R.id.calendar_date_id);
        cellNumber.setText(String.valueOf(dayValue));
        //Add events to the calendar
        TextView eventIndicator = (TextView) view.findViewById(R.id.event_id);
        Calendar eventCalendar = Calendar.getInstance();
        for (int i = 0; i < allEvents.size(); i++) {
            Date getDate = Date.from(allEvents.get(i).getDate().toInstant(ZoneOffset.UTC));


            eventCalendar.setTime(getDate);
            if (dayValue == eventCalendar.get(Calendar.DAY_OF_MONTH) && displayMonth == eventCalendar.get(Calendar.MONTH) + 1) {
                eventIndicator.setText(allEvents.get(i).getMessage());
                view.setBackgroundColor(
                    (allEvents.get(i).getColor() != 0) ? allEvents.get(i).getColor() : Color.parseColor("#FF94B8")
                );
            }

        }
        return view;
    }


    @Override
    public int getCount() {
        return monthlyDates.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return monthlyDates.get(position);
    }

    public EventObject getEvent(int position) {
        return allEvents.get(position);
    }

    @Override
    public int getPosition(Object item) {
        return monthlyDates.indexOf(item);
    }
}