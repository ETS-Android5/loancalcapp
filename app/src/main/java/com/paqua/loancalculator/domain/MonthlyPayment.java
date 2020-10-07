package com.paqua.loancalculator.domain;

//import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents detailed information about monthly payment
 * @author Artyom Panfutov
 */
public final class MonthlyPayment implements Serializable {
    private static final long serialVersionUID = -8046672296117265073L;

    /**
     * Month
     */
    private final Integer monthNumber;

    /**
     * Amount of remaining debt (Balance)
     */
    private final BigDecimal loanBalanceAmount;

    /**
     * Amount of debt in payment (Principal)
     */
    private final BigDecimal debtPaymentAmount;

    /**
     * Amount of interest in payment
     */
    private final BigDecimal interestPaymentAmount;

    /**
     * Payment amount
     */
    private final BigDecimal paymentAmount;

   // @ConstructorProperties({"monthNumber", "loanBalanceAmount", "debtPaymentAmount", "interestPaymentAmount", "paymentAmount"})
    public MonthlyPayment(Integer monthNumber, BigDecimal loanBalanceAmount, BigDecimal debtPaymentAmount, BigDecimal interestPaymentAmount, BigDecimal paymentAmount) {
        this.monthNumber = monthNumber;
        this.loanBalanceAmount = loanBalanceAmount;
        this.debtPaymentAmount = debtPaymentAmount;
        this.interestPaymentAmount = interestPaymentAmount;
        this.paymentAmount = paymentAmount;
    }

    /**
     * @return Month number
     */
    public Integer getMonthNumber() {
        return monthNumber;
    }

    /**
     * @return Amount of remaining debt (Balance)
     */
    public BigDecimal getLoanBalanceAmount() {
        return loanBalanceAmount;
    }

    /**
     * @return Amount of debt in payment (Principal)
     */
    public BigDecimal getDebtPaymentAmount() {
        return debtPaymentAmount;
    }

    /**
     * @return Amount of interest in payment
     */
    public BigDecimal getInterestPaymentAmount() {
        return interestPaymentAmount;
    }

    /**
     * @return Amount of payment
     */
    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public static MonthlyPaymentBuilder builder() {
        return new MonthlyPaymentBuilder();
    }
    /**
     * Builder for MonthlyPayment
     */
    public static final class MonthlyPaymentBuilder {
        private Integer monthNumber;
        private BigDecimal loanBalanceAmount;
        private BigDecimal debtPaymentAmount;
        private BigDecimal interestPaymentAmount;
        private BigDecimal paymentAmount;

        public MonthlyPaymentBuilder() {
        }

        public MonthlyPaymentBuilder(Integer monthNumber, BigDecimal loanBalanceAmount, BigDecimal debtPaymentAmount, BigDecimal interestPaymentAmount, BigDecimal paymentAmount) {
            this.monthNumber = monthNumber;
            this.loanBalanceAmount = loanBalanceAmount;
            this.debtPaymentAmount = debtPaymentAmount;
            this.interestPaymentAmount = interestPaymentAmount;
            this.paymentAmount = paymentAmount;
        }

        public MonthlyPaymentBuilder monthNumber(Integer monthNumber) {
            this.monthNumber = monthNumber;
            return this;
        }
        public MonthlyPaymentBuilder loanBalanceAmount(BigDecimal loanBalanceAmount) {
            this.loanBalanceAmount = loanBalanceAmount;
            return this;
        }
        public MonthlyPaymentBuilder debtPaymentAmount(BigDecimal debtPaymentAmount) {
            this.debtPaymentAmount = debtPaymentAmount;
            return this;
        }
        public MonthlyPaymentBuilder interestPaymentAmount(BigDecimal interestPaymentAmount) {
            this.interestPaymentAmount = interestPaymentAmount;
            return this;
        }
        public MonthlyPaymentBuilder paymentAmount(BigDecimal paymentAmount) {
            this.paymentAmount = paymentAmount;
            return this;
        }

        public MonthlyPayment build() {
            return new MonthlyPayment(monthNumber, loanBalanceAmount, debtPaymentAmount, interestPaymentAmount, paymentAmount);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonthlyPayment that = (MonthlyPayment) o;

        if (monthNumber != null ? !monthNumber.equals(that.monthNumber) : that.monthNumber != null)
            return false;
        if (loanBalanceAmount != null ? !loanBalanceAmount.equals(that.loanBalanceAmount) : that.loanBalanceAmount != null)
            return false;
        if (debtPaymentAmount != null ? !debtPaymentAmount.equals(that.debtPaymentAmount) : that.debtPaymentAmount != null)
            return false;
        if (interestPaymentAmount != null ? !interestPaymentAmount.equals(that.interestPaymentAmount) : that.interestPaymentAmount != null)
            return false;
        return paymentAmount != null ? paymentAmount.equals(that.paymentAmount) : that.paymentAmount == null;
    }

    @Override
    public int hashCode() {
        int result = monthNumber != null ? monthNumber.hashCode() : 0;
        result = 31 * result + (loanBalanceAmount != null ? loanBalanceAmount.hashCode() : 0);
        result = 31 * result + (debtPaymentAmount != null ? debtPaymentAmount.hashCode() : 0);
        result = 31 * result + (interestPaymentAmount != null ? interestPaymentAmount.hashCode() : 0);
        result = 31 * result + (paymentAmount != null ? paymentAmount.hashCode() : 0);
        return result;
    }
}
