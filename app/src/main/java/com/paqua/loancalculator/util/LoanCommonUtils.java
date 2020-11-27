package com.paqua.loancalculator.util;

import android.content.Context;

import com.paqua.loancalculator.R;

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
