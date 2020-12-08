package com.paqua.loancalculator.util;

import android.content.Context;
import android.widget.DatePicker;

import com.paqua.loancalculator.R;

import java.util.Calendar;

public class LoanCommonUtils {
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

}
