package com.paqua.loancalculator;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.paqua.loancalculator.domain.LoanAmortization;
import com.paqua.loancalculator.domain.MonthlyPayment;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {
    private static final String GET_LOAN_AMROTIZATION_URL ="https://loan-amortization-server.herokuapp.com/loanAmortization";

    private LoanAmortization amortization;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_set);

        getLoanAmortizationData();
    }

    private void getLoanAmortizationData() {
        RequestQueue queue = Volley.newRequestQueue(ResultActivity.this);
        final Map<String, String> params = new HashMap<>();
        Intent intent = getIntent();
        params.put("loanAmount", intent.getStringExtra("loanAmount"));
        params.put("interestRate", intent.getStringExtra("interestRate"));
        params.put("monthTerm", intent.getStringExtra("monthTerm"));

        // TODO
        String uri = GET_LOAN_AMROTIZATION_URL + "?loanAmount=" + params.get("loanAmount") + "&interestRate=" +params.get("interestRate") + "&monthTerm=" + params.get("monthTerm");

        JSONObject loanRequestParams = null;// new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, uri, loanRequestParams, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        amortization = gson.fromJson(response.toString(), LoanAmortization.class);
                        System.out.println(amortization);
                        buildAmortizationTable();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Alert
                        System.out.println("Something went wrong :( " + error);
                    }
                }) {
        };
        queue.add(jsonObjectRequest);
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

        for(MonthlyPayment payment : amortization.getMonthlyPayments()) {
            TableRow row = new TableRow(ResultActivity.this);

            row.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);


//            row.setPadding(25, 25, 25,25);

            TextView currentPaymentNumber = new TextView(ResultActivity.this);
            currentPaymentNumber.setText((++paymentNumber).toString());
            currentPaymentNumber.setTypeface(typeface);
            currentPaymentNumber.setTextColor(textColor);
            currentPaymentNumber.setBackground(cellBackground);
//            currentPaymentNumber.setSingleLine();
            currentPaymentNumber.setPadding(10, 10, 10, 10);

            TextView paymentAmount = new TextView(ResultActivity.this);
            paymentAmount.setText(payment.getPaymentAmount().toString());
            paymentAmount.setTypeface(typeface);
            paymentAmount.setTextColor(textColor);
            paymentAmount.setBackground(cellBackground);
//            paymentAmount.setSingleLine();
            paymentAmount.setPadding(10, 10, 10, 10);

            TextView loanAmount = new TextView(ResultActivity.this);
            loanAmount.setText(payment.getLoanBalanceAmount().toString());
            loanAmount.setTypeface(typeface);
            loanAmount.setTextColor(textColor);
            loanAmount.setBackground(cellBackground);
//            loanAmount.setSingleLine();
            loanAmount.setPadding(10, 10 ,10 ,10);

            TextView interestAmount = new TextView(ResultActivity.this);
            interestAmount.setText(payment.getInterestPaymentAmount().toString());
            interestAmount.setTypeface(typeface);
            interestAmount.setTextColor(textColor);
            interestAmount.setBackground(cellBackground);
//            interestAmount.setSingleLine();
            interestAmount.setPadding(10, 10 ,10 ,10);

            TextView principalAmount = new TextView(ResultActivity.this);
            principalAmount.setText(payment.getDebtPaymentAmount().toString());
            principalAmount.setTypeface(typeface);
            principalAmount.setTextColor(textColor);
            principalAmount.setBackground(cellBackground);
//            principalAmount.setSingleLine();
            principalAmount.setPadding(10, 10 ,10 ,10);

            row.addView(currentPaymentNumber);
            row.addView(loanAmount);
            row.addView(interestAmount);
            row.addView(principalAmount);
            row.addView(paymentAmount);

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
        header.setPadding(10, 50, 10, 50);

        TextView paymentNumberColumn = new TextView(ResultActivity.this);
        paymentNumberColumn.setText("№   ");
        paymentNumberColumn.setTextSize(headerFontSize);
        paymentNumberColumn.setTextColor(textColor);
        paymentNumberColumn.setTypeface(typeface);
        paymentNumberColumn.setPadding(20, 0, 0 , 0);

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
}