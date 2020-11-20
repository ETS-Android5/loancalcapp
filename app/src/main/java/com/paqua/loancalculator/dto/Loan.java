package com.paqua.loancalculator.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * This class represent input data for loan amortization calculation
 *
 * @author Artyom Panfutov
 */
public final class Loan implements Serializable {
    private static final long serialVersionUID = 8435495249049946452L;

    /**
     * Loan amount
     */
    private final BigDecimal amount;

    /**
     * Name
     */
    private final String name;

    /**
     * Interest rate
     */
    private final BigDecimal rate;

    /**
     * Loan term in months
     */
    private final Integer term;

    /**
     * Early payments
     *
     * Key: number of payment in payment schedule
     * Value: early payment data(amount, strategy)
     */
    private final Map<Integer, EarlyPayment> earlyPayments;

    public Loan(String name, BigDecimal amount, BigDecimal rate, Integer term, Map<Integer,EarlyPayment> earlyPayments) {
        this.name = name;
        this.amount = amount;
        this.rate = rate;
        this.term = term;
        this.earlyPayments = earlyPayments;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @return Loan amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @return Interest rate
     */
    public BigDecimal getRate() {
        return rate;
    }

    /**
     * @return Loan term in months
     */
    public Integer getTerm() {
        return term;
    }

    /**
     * Early payments
     *
     * Key: number of payment in payment schedule
     * Value: early payment data(amount, strategy)
     *
     * @return Early payments
     */
    public Map<Integer, EarlyPayment> getEarlyPayments() {
        return earlyPayments;
    }

    public static LoanBuilder builder() {
        return new LoanBuilder();
    }

    /**
     * Builder class for Loan
     */
    public static final class LoanBuilder  {

        private BigDecimal amount;
        private BigDecimal rate;
        private Integer term;
        private Map<Integer, EarlyPayment> earlyPayments;
        private String name;

        public LoanBuilder() {
        }

        public LoanBuilder(String name, BigDecimal amount, BigDecimal rate, Integer term, Map<Integer, EarlyPayment> earlyPayments) {
            this.name = name;
            this.amount = amount;
            this.rate = rate;
            this.term = term;
            this.earlyPayments = earlyPayments;
        }

        public LoanBuilder name(String name) {
            this.name = name;
            return this;
        }
        public LoanBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public LoanBuilder rate(BigDecimal rate) {
            this.rate = rate;
            return this;
        }

        public LoanBuilder term(Integer term) {
            this.term = term;
            return this;
        }

        public LoanBuilder earlyPayments(Map<Integer, EarlyPayment> earlyPayments) {
            this.earlyPayments = earlyPayments;
            return this;
        }

        public Loan build() {
            return new Loan(name, amount, rate, term, earlyPayments);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Loan loan = (Loan) o;

        if (amount != null ? !amount.equals(loan.amount) : loan.amount != null) return false;
        if (name != null ? !name.equals(loan.name) : loan.name != null) return false;
        if (rate != null ? !rate.equals(loan.rate) : loan.rate != null) return false;
        if (term != null ? !term.equals(loan.term) : loan.term != null) return false;
        return earlyPayments != null ? earlyPayments.equals(loan.earlyPayments) : loan.earlyPayments == null;
    }

    @Override
    public int hashCode() {
        int result = amount != null ? amount.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (rate != null ? rate.hashCode() : 0);
        result = 31 * result + (term != null ? term.hashCode() : 0);
        result = 31 * result + (earlyPayments != null ? earlyPayments.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "amount=" + amount +
                ", name='" + name + '\'' +
                ", rate=" + rate +
                ", term=" + term +
                ", earlyPayments=" + earlyPayments +
                '}';
    }
}
