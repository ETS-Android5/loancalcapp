package com.paqua.loancalculator.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Represents loan amortization calculation result
 *
 * @author Artyom Panfutov
 */
public final class LoanAmortization implements Serializable {
    private static final long serialVersionUID = 6600584767577828784L;

    /**
     * Amount of monthly payment
     */
    private final BigDecimal monthlyPaymentAmount;

    /**
     * Overpayment of interest
     */
    private final BigDecimal overPaymentAmount;

    /**
     * Overpayment without early payments
     */
    private BigDecimal overPaymentAmountWithoutEarlyPayments;

    /**
     * Calculated list of monthly payments
     */
    private final List<MonthlyPayment> monthlyPayments;

    /**
     * Early payments
     *
     * Key: number of payment in payment schedule
     * Value: early payment data(amount, strategy)
     *
     * @return Early payments
     */
    private final Map<Integer, EarlyPayment> earlyPayments;

    public LoanAmortization(BigDecimal monthlyPaymentAmount, BigDecimal overPaymentAmount, List<MonthlyPayment> monthlyPayments, Map<Integer, EarlyPayment> earlyPayments, BigDecimal overPaymentAmountWithoutEarlyPayments) {
        this.monthlyPaymentAmount = monthlyPaymentAmount;
        this.overPaymentAmount = overPaymentAmount;
        this.monthlyPayments = monthlyPayments;
        this.earlyPayments = earlyPayments;
        this.overPaymentAmountWithoutEarlyPayments = overPaymentAmountWithoutEarlyPayments;
    }

    public void setOverPaymentAmountWithoutEarlyPayments(BigDecimal overPaymentAmountWithoutEarlyPayments) {
        this.overPaymentAmountWithoutEarlyPayments = overPaymentAmountWithoutEarlyPayments;
    }

    /**
     * @return Monthly payment amount
     */
    public BigDecimal getMonthlyPaymentAmount() {
        return monthlyPaymentAmount;
    }

    /**
     * @return Total overpayment (interests)
     */
    public BigDecimal getOverPaymentAmount() {
        return overPaymentAmount;
    }

    /**
     * @return Payments schedule
     */
    public List<MonthlyPayment> getMonthlyPayments() {
        return monthlyPayments;
    }

    /**
     * Key: number of payment in payment schedule
     * Value: early payment data(amount, strategy)
     *
     * @return Early payments (additional payments to monthly payments)
     */
    public Map<Integer, EarlyPayment> getEarlyPayments() {
        return earlyPayments;
    }

    public BigDecimal getOverPaymentAmountWithoutEarlyPayments() {
        return overPaymentAmountWithoutEarlyPayments;
    }

    public static LoanAmortizationBuilder builder() {
        return new LoanAmortizationBuilder();
    }

    /**
     * Builder for LoanAmortization class
     */
    public static final class LoanAmortizationBuilder {
        private BigDecimal monthlyPaymentAmount;
        private BigDecimal overPaymentAmount;
        private List<MonthlyPayment> monthlyPayments;
        private Map<Integer, EarlyPayment> earlyPayments;
        private BigDecimal overPaymentWithoutEarlyPayments;

        public LoanAmortizationBuilder() {
        }

        public LoanAmortizationBuilder(BigDecimal monthlyPaymentAmount, BigDecimal overPaymentAmount, List<MonthlyPayment> monthlyPayments, Map<Integer, EarlyPayment> earlyPayments, BigDecimal overPaymentWithoutEarlyPayments) {
            this.monthlyPaymentAmount = monthlyPaymentAmount;
            this.overPaymentAmount = overPaymentAmount;
            this.monthlyPayments = monthlyPayments;
            this.earlyPayments = earlyPayments;
            this.overPaymentWithoutEarlyPayments = overPaymentWithoutEarlyPayments;
        }

        public LoanAmortizationBuilder monthlyPaymentAmount(BigDecimal monthlyPaymentAmount) {
            this.monthlyPaymentAmount = monthlyPaymentAmount;
            return this;
        }

        public LoanAmortizationBuilder overPaymentAmount(BigDecimal overPaymentAmount) {
            this.overPaymentAmount = overPaymentAmount;
            return this;
        }

        public LoanAmortizationBuilder monthlyPayments(List<MonthlyPayment> monthlyPayments) {
            this.monthlyPayments = monthlyPayments;
            return this;
        }

        public LoanAmortizationBuilder earlyPayments(Map<Integer, EarlyPayment> earlyPayments) {
            this.earlyPayments = earlyPayments;
            return this;
        }

        public LoanAmortizationBuilder overPaymentWithoutEarlyPayments(BigDecimal overPaymentWithoutEarlyPayments) {
            this.overPaymentWithoutEarlyPayments = overPaymentWithoutEarlyPayments;
            return this;
        }

        public LoanAmortization build() {
            return new LoanAmortization(monthlyPaymentAmount, overPaymentAmount, monthlyPayments, earlyPayments, overPaymentWithoutEarlyPayments);
        }

        @Override
        public String toString() {
            return "LoanAmortizationBuilder{" +
                    "monthlyPaymentAmount=" + monthlyPaymentAmount +
                    ", overPaymentAmount=" + overPaymentAmount +
                    ", monthlyPayments=" + monthlyPayments +
                    ", earlyPayments=" + earlyPayments +
                    ", overPaymentWithoutEarlyPayments=" + overPaymentWithoutEarlyPayments +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoanAmortization that = (LoanAmortization) o;

        if (monthlyPaymentAmount != null ? !monthlyPaymentAmount.equals(that.monthlyPaymentAmount) : that.monthlyPaymentAmount != null)
            return false;
        if (overPaymentAmount != null ? !overPaymentAmount.equals(that.overPaymentAmount) : that.overPaymentAmount != null)
            return false;
        if (overPaymentAmountWithoutEarlyPayments != null ? !overPaymentAmountWithoutEarlyPayments.equals(that.overPaymentAmountWithoutEarlyPayments) : that.overPaymentAmountWithoutEarlyPayments != null)
            return false;
        if (monthlyPayments != null ? !monthlyPayments.equals(that.monthlyPayments) : that.monthlyPayments != null)
            return false;
        return earlyPayments != null ? earlyPayments.equals(that.earlyPayments) : that.earlyPayments == null;
    }

    @Override
    public int hashCode() {
        int result = monthlyPaymentAmount != null ? monthlyPaymentAmount.hashCode() : 0;
        result = 31 * result + (overPaymentAmount != null ? overPaymentAmount.hashCode() : 0);
        result = 31 * result + (overPaymentAmountWithoutEarlyPayments != null ? overPaymentAmountWithoutEarlyPayments.hashCode() : 0);
        result = 31 * result + (monthlyPayments != null ? monthlyPayments.hashCode() : 0);
        result = 31 * result + (earlyPayments != null ? earlyPayments.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LoanAmortization{" +
                "monthlyPaymentAmount=" + monthlyPaymentAmount +
                ", overPaymentAmount=" + overPaymentAmount +
                ", overPaymentAmountWithoutEarlyPayments=" + overPaymentAmountWithoutEarlyPayments +
                ", monthlyPayments=" + monthlyPayments +
                ", earlyPayments=" + earlyPayments +
                '}';
    }

}
