package com.tf.routerrecorder.Utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;

import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

public class DateTimePicker implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private final Context context;
    private Button _btn;
    public DateTimePicker (Context context){
        this.context = context;
    }

    private long datetime;
    private int mYear;
    private int mDay;
    private int mMonth;

    public void getDatetime(Button btn){
        _btn = btn;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, this, year, month, day);
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        mYear = year;
        mDay = day;
        mMonth = month;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, this, hour, minute, DateFormat.is24HourFormat(context));
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        Calendar calendar = new Calendar.Builder()
                .setDate(mYear, mMonth, mDay)
                .setTimeOfDay(hourOfDay, minute, 0)
                .build();
        _btn.setText(calendar.getTime().toString());
        datetime = calendar.getTimeInMillis() / 1000;
    }

    public long getEpochTime(){
        return datetime;
    }
}
