package com.paqua.loancalculator.util;

import android.content.Context;
import android.widget.DatePicker;

import com.paqua.loancalculator.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Garbage class
 * // TODO REFACTOR
 */
public class LoanCommonUtils {
    private static final SimpleDateFormat DISPLAYING_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Default loan name for saving into local storage
     *
     * @return loan name
     */
    public static String getDefaultLoanName(Context context) {
        return context.getResources().getString(R.string.my_loan_text);
    }

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

}
