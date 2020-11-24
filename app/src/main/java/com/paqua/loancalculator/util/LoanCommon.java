package com.paqua.loancalculator.util;

import android.content.Context;

import com.paqua.loancalculator.R;
import com.paqua.loancalculator.dto.Loan;

public class LoanCommon {
    /**
     * Default loan name
     *
     * Consists of amount, rate and term
     *
     * @return loan name
     */
    public static String getDefaultLoanName(Context context, Loan loan) {
        return context.getResources().getString(R.string.my_loan_text);
    }

}
