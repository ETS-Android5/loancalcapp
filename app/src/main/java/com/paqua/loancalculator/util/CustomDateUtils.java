package com.paqua.loancalculator.util;

import android.widget.DatePicker;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Consists of static methods which somehow relates to dates or calendars for this particular app
 */
public class CustomDateUtils {
    private static final SimpleDateFormat DISPLAYING_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * @param datePicker
     * @return a java.util.Date
     */
    public static Calendar getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar;
    }

    /**
     * @return formatter that can be used to display date to users
     */
    public static SimpleDateFormat getDateFormatterForDisplayingToUser() {
        return DISPLAYING_DATE_FORMAT;
    }
    /**
     * @return formatter that can be used in api calls
     */
    public static SimpleDateFormat getDateFormatterForApi() {
        return API_DATE_FORMAT;
    }

    /**
     * Converts string date from api format to displaying format
     *
     * @param date date in API format {@link this#API_DATE_FORMAT}
     * @return formatted in {@link this#DISPLAYING_DATE_FORMAT} or EMPTY string
     */
    public static String convertToDisplayingFormat(@NonNull String date) {
        Date parsed = null;
        try {
            parsed = API_DATE_FORMAT.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parsed != null ? DISPLAYING_DATE_FORMAT.format(parsed) : "";
    }

}
