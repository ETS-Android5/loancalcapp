package com.paqua.loancalculator.dto;

import java.io.Serializable;

public class LoanAmortizationRq implements Serializable {
    private static final long serialVersionUID = -5704069440983904696L;

    private final Loan loan;

    public LoanAmortizationRq(Loan loan) {
        this.loan = loan;
    }

    public Loan getLoan() {
        return loan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoanAmortizationRq that = (LoanAmortizationRq) o;

        return loan != null ? loan.equals(that.loan) : that.loan == null;
    }

    @Override
    public int hashCode() {
        return loan != null ? loan.hashCode() : 0;
    }
}
