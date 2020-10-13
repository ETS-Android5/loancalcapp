package com.paqua.loancalculator;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.paqua.loancalculator.dto.Loan;
import com.paqua.loancalculator.dto.LoanAmortization;
import com.paqua.loancalculator.dto.LoanAmortizationRq;
import com.paqua.loancalculator.dto.MonthlyPayment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ResultActivity extends AppCompatActivity {
    private static final String GET_LOAN_AMROTIZATION_URL ="https://loan-amortization-server.herokuapp.com/loanAmortization";

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");
    private static final DecimalFormatSymbols SYMBOLS = DECIMAL_FORMAT.getDecimalFormatSymbols();

    static {
        SYMBOLS.setGroupingSeparator(' ');
        DECIMAL_FORMAT.setDecimalFormatSymbols(SYMBOLS);
        DECIMAL_FORMAT.setGroupingUsed(true);
        DECIMAL_FORMAT.setGroupingSize(3);
    }

    private LoanAmortization amortization;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_set);

        setVisibilityForAll(INVISIBLE);

        try {
            getLoanAmortizationData();
        } catch (JSONException | IOException e) {
            e.printStackTrace(); // TODO
        }
    }

    private void getLoanAmortizationData() throws JSONException, IOException {
        RequestQueue queue = Volley.newRequestQueue(ResultActivity.this);

        Intent intent = getIntent();
        Loan loan = Loan.builder()
                .amount(new BigDecimal(intent.getStringExtra("loanAmount")))
                .rate(new BigDecimal(intent.getStringExtra("interestRate")))
                .term(Integer.parseInt(intent.getStringExtra("monthTerm")))
                .build();

        LoanAmortizationRq loanAmortizationRq = new LoanAmortizationRq(loan);

        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject requestParams = new JSONObject(objectMapper.writeValueAsString(loanAmortizationRq));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, GET_LOAN_AMROTIZATION_URL, requestParams, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        getAmortizationDataCallback(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Alert
                        System.out.println("Something went wrong :( " + error);
                    }
                }) {
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(jsonObjectRequest);
    }

    /**
     * Callback on amortization data request
     * Sets values on views
     *
     * @param response
     */
    private void getAmortizationDataCallback(JSONObject response) {
        Gson gson = new Gson();
        amortization = gson.fromJson(response.toString(), LoanAmortization.class);
        System.out.println(amortization);

        fillHeaderValues();

        buildAmortizationTable();

        setVisibilityForAll(VISIBLE);
        setOverpaymentAmountVisibility();
    }

    /**
     * Sets visibility according to value
     */
    private void setOverpaymentAmountVisibility() {
        TextView overPaymentAmountWithEarly = (TextView) findViewById(R.id.overPaymentWithEarly);
        if (overPaymentAmountWithEarly.getText() == null || overPaymentAmountWithEarly.getText().length() == 0) {
            overPaymentAmountWithEarly.setVisibility(INVISIBLE);
        }
    }


    /**
     * Fills headers with values from request
     */
    private void fillHeaderValues() {
        TextView monthlyPaymentAmount = (TextView) findViewById(R.id.monthlyPaymentValue);

        monthlyPaymentAmount.setText(
                DECIMAL_FORMAT.format(amortization
                            .getMonthlyPaymentAmount()
                            .setScale(2, RoundingMode.HALF_UP))
        );


        TextView overPaymentAmount = (TextView) findViewById(R.id.overPaymentAmount);

        overPaymentAmount.setText(
                DECIMAL_FORMAT.format(amortization
                        .getOverPaymentAmount()
                        .setScale(2, RoundingMode.HALF_UP))
        );
    }

    /**
     * Builds amortization table content
     */
    private void buildAmortizationTable() {
        TableLayout tl = (TableLayout)findViewById(R.id.amortizationTable);

        buildAmortizationTableHeader(tl);

        buildAmortizationTableContent(tl);
    }

    /**
     * Builds table content
     * @param tl
     */
    private void buildAmortizationTableContent(TableLayout tl) {
        Integer paymentNumber = 0;
        int textColor = getResources().getColor(R.color.coolDarkColor);
        Typeface typeface = getTypeface();
        Drawable cellBackground = getResources().getDrawable(R.drawable.amortization_cell_background);
        Drawable cellBackgroundWithoutStroke = getResources().getDrawable(R.drawable.amortization_cell_background_without_stroke);

        for(MonthlyPayment payment : amortization.getMonthlyPayments()) {
            TableRow row = new TableRow(ResultActivity.this);

            row.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            row.setMinimumHeight(57);

            int minHeight = 150; // TODO temp
            TextView currentPaymentNumber = new TextView(ResultActivity.this);
            currentPaymentNumber.setText((++paymentNumber).toString());
            currentPaymentNumber.setTypeface(typeface);
            currentPaymentNumber.setTextColor(textColor);
            currentPaymentNumber.setBackground(cellBackground);
            currentPaymentNumber.setPadding(30, 10, 10, 10);
            currentPaymentNumber.setMinHeight(minHeight);


            TextView loanAmount = new TextView(ResultActivity.this);
            loanAmount.setText(DECIMAL_FORMAT.format(payment.getLoanBalanceAmount()));
            loanAmount.setTypeface(typeface);
            loanAmount.setTextColor(textColor);
            loanAmount.setBackground(cellBackground);
            loanAmount.setPadding(10, 10 ,10 ,10);
            loanAmount.setMinHeight(minHeight);

            TextView interestAmount = new TextView(ResultActivity.this);
            interestAmount.setText(DECIMAL_FORMAT.format(payment.getInterestPaymentAmount()));
            interestAmount.setTypeface(typeface);
            interestAmount.setTextColor(textColor);
            interestAmount.setBackground(cellBackground);
            interestAmount.setPadding(10, 10 ,10 ,10);
            interestAmount.setMinHeight(minHeight);

            TextView principalAmount = new TextView(ResultActivity.this);
            principalAmount.setText(DECIMAL_FORMAT.format(payment.getDebtPaymentAmount()));
            principalAmount.setTypeface(typeface);
            principalAmount.setTextColor(textColor);
            principalAmount.setBackground(cellBackground);
            principalAmount.setPadding(10, 10 ,10 ,10);
            principalAmount.setMinHeight(minHeight);

            TableLayout innerTable = new TableLayout(this);
//            innerTable.setPadding(10, 10, 20, 10);
            innerTable.setBackground(cellBackground);

            TableRow innerRowForAmount = new TableRow(this);
            innerTable.addView(innerRowForAmount);

            TextView paymentAmount = new TextView(ResultActivity.this);
            paymentAmount.setText(DECIMAL_FORMAT.format(payment.getPaymentAmount()));
            paymentAmount.setTypeface(typeface);
            paymentAmount.setTextColor(textColor);
            paymentAmount.setBackground(cellBackground);
            paymentAmount.setMinHeight(minHeight / 2);
            innerRowForAmount.addView(paymentAmount);

            TextView earlyPayment = new TextView(this);
            earlyPayment.setText("+ досрочно");
            earlyPayment.setTypeface(typeface);
            earlyPayment.setTextColor(textColor);
            earlyPayment.setBackground(cellBackground);
            earlyPayment.setMinHeight((minHeight / 2) - 2);
            earlyPayment.setTextSize(11);

            TableRow innerRowForEarlyPayment = new TableRow(this);
            innerRowForEarlyPayment.addView(earlyPayment);

            innerTable.addView(innerRowForEarlyPayment);

            row.addView(currentPaymentNumber);
            row.addView(loanAmount);
            row.addView(interestAmount);
            row.addView(principalAmount);
            row.addView(innerTable);

            tl.addView(row);
        }
    }

    /**
     * Builds table header
     * @param tl
     */
    private void buildAmortizationTableHeader(TableLayout tl) {
        // Header
        TableRow header = new TableRow(ResultActivity.this);
        Drawable background = getResources().getDrawable(R.drawable.amortization_header_background);
        int headerFontSize = 16;
        int textColor = getResources().getColor(R.color.coolDarkColor);

        Typeface typeface = getTypeface();

        header.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        header.setBackground(background);
        header.setPadding(20, 50, 10, 50);

        TextView paymentNumberColumn = new TextView(ResultActivity.this);
        paymentNumberColumn.setText("№   ");
        paymentNumberColumn.setTextSize(headerFontSize);
        paymentNumberColumn.setTextColor(textColor);
        paymentNumberColumn.setTypeface(typeface);
        paymentNumberColumn.setPadding(30, 0, 0 , 0);

        TextView loanBalanceAmountColumn = new TextView(ResultActivity.this);
        loanBalanceAmountColumn.setText("Остаток основного долга");
        loanBalanceAmountColumn.setTextSize(headerFontSize);
        loanBalanceAmountColumn.setTextColor(textColor);
        loanBalanceAmountColumn.setSingleLine(false);
        loanBalanceAmountColumn.setTypeface(typeface);
        loanBalanceAmountColumn.setPadding(20, 0 , 0, 0);

        TextView interestAmountColumn = new TextView(ResultActivity.this);
        interestAmountColumn.setText("Проценты банку");
        interestAmountColumn.setTextSize(headerFontSize);
        interestAmountColumn.setTextColor(textColor);
        interestAmountColumn.setTypeface(typeface);
        interestAmountColumn.setSingleLine(false);
        interestAmountColumn.setPadding(20, 0 ,0 ,0);

        TextView principalAmountColumn = new TextView(ResultActivity.this);
        principalAmountColumn.setText("Основной долг");
        principalAmountColumn.setTextSize(headerFontSize);
        principalAmountColumn.setTextColor(textColor);
        principalAmountColumn.setTypeface(typeface);
        principalAmountColumn.setSingleLine(false);
        principalAmountColumn.setPadding(20, 0,0,0);

        TextView paymentAmountColumn = new TextView(ResultActivity.this);
        paymentAmountColumn.setText("Платеж");
        paymentAmountColumn.setTextSize(headerFontSize);
        paymentAmountColumn.setTextColor(textColor);
        paymentAmountColumn.setTypeface(typeface);
        paymentAmountColumn.setSingleLine(false);
        paymentAmountColumn.setPadding(20, 0 , 0 ,0);

        header.addView(paymentNumberColumn);
        header.addView(loanBalanceAmountColumn);
        header.addView(interestAmountColumn);
        header.addView(principalAmountColumn);
        header.addView(paymentAmountColumn);

        tl.addView(header);
    }

    /**
     * Base type face TODO: move to commons
     * @return
     */
    private Typeface getTypeface() {
        Typeface typeface;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.base_font);
        } else {
            typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.base_font);
        }
        return typeface;
    }

    /**
     * Sets visibilty for all child for base layout
     * @param value
     */
    private void setVisibilityForAll(int value) {
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.base_layout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setVisibility(value);
        }
    }
}