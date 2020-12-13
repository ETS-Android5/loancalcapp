package com.paqua.loancalculator.util;

import android.content.Context;
import android.widget.DatePicker;

import com.paqua.loancalculator.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Garbage class
 */
@Deprecated
public class LoanCommonUtils {


    /**
     * Default loan name for saving into local storage
     *
     * @return loan name
     */
    public static String getDefaultLoanName(Context context) {
        return context.getResources().getString(R.string.my_loan_text);
    }


}
