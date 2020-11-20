package com.paqua.loancalculator.util;

import com.paqua.loancalculator.dto.Loan;

public class LoanCommon {
    /**
     * Default loan name
     *
     * Consists of amount, rate and term
     *
     * @return loan name
     */
    public static String getDefaultLoanName(Loan loan) {
        return String.format("Amount: %s / Rate: %s / Term: %s", loan.getAmount(), loan.getRate(), loan.getTerm());
    }

}
