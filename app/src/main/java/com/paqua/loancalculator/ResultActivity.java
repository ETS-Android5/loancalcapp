package com.paqua.loancalculator;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.paqua.loancalculator.dto.EarlyPayment;
import com.paqua.loancalculator.dto.EarlyPaymentRepeatingStrategy;
import com.paqua.loancalculator.dto.EarlyPaymentStrategy;
import com.paqua.loancalculator.dto.Loan;
import com.paqua.loancalculator.dto.LoanAmortization;
import com.paqua.loancalculator.dto.LoanAmortizationRq;
import com.paqua.loancalculator.dto.MonthlyPayment;
import com.paqua.loancalculator.util.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

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

    private Loan loan = null;
    private LoanAmortization amortization;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_set);

        setVisibilityForAll(INVISIBLE);

        try {
            Intent intent = getIntent();
            loan = (Loan) intent.getExtras().get(Constant.LOAN_OBJECT);

            calculateLoanAmortization();

        } catch (Exception e) {
            showSomethingWentWrongDialog();
            e.printStackTrace(); // TODO
        }
    }

    /**
     * Makes REST API request and builds amortization table
     * @param loan Loan
     * @throws JSONException
     * @throws IOException
     */
    private void calculateLoanAmortization() throws JSONException, IOException {
        RequestQueue queue = Volley.newRequestQueue(ResultActivity.this);

        LoanAmortizationRq lastLoanRequestParam = new LoanAmortizationRq(loan);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

        JSONObject requestParams = new JSONObject(objectMapper.writeValueAsString(lastLoanRequestParam));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, GET_LOAN_AMROTIZATION_URL, requestParams, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        calculateLoanAmortizationCallback(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showSomethingWentWrongDialog();
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
     * Show common dialog in case of errors
     */
    private void showSomethingWentWrongDialog() {
        getSomethingWentWrongDialog().show();
    }

    /**
     *
     * @return Common alert dialog in case of errors
     */
    private AlertDialog getSomethingWentWrongDialog() {
        return new AlertDialog.Builder(ResultActivity.this)
                .setTitle("Something went wrong :(")
                .setMessage("Please, try again later")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO
                    }
                })
                .create();
    }

    /**
     * Callback on amortization data request
     * Sets values on views
     *
     * @param response
     */
    private void calculateLoanAmortizationCallback(JSONObject response) {
        Gson gson = new Gson();
        amortization = gson.fromJson(response.toString(), LoanAmortization.class);
        System.out.println(amortization);

        fillHeaderValues();

        rebuildAmortizationTable();

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
    private void rebuildAmortizationTable() {
        TableLayout tl = (TableLayout)findViewById(R.id.amortizationTable);
        tl.removeAllViews();

        buildAmortizationTableHeader(tl);

        buildAmortizationTableContent(tl);
    }

    /**
     * Builds table content
     * @param tl
     */
    // TODO Too complex method - refactor
    private void buildAmortizationTableContent(TableLayout tl) {
        Integer paymentNumber = 0;

        int textColor = getResources().getColor(R.color.coolDarkColor);

        Typeface standardTypeface = getStandardTypeface();
        Typeface boldTypeface = getBoldTypeface();

        Drawable cellBackground = getResources().getDrawable(R.drawable.amortization_cell_background);
        Drawable cellBackgroundWithoutStroke = getResources().getDrawable(R.drawable.amortization_cell_background_without_stroke);
        Drawable earlyPaymentBackground = getResources().getDrawable(R.drawable.calc_button);

        for(MonthlyPayment payment : amortization.getMonthlyPayments()) {
            TableRow row = new TableRow(ResultActivity.this);

            row.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            row.setMinimumHeight(57);

            int minHeight = 150; // TODO temp
            TextView currentPaymentNumber = new TextView(ResultActivity.this);
            currentPaymentNumber.setText((++paymentNumber).toString());
            currentPaymentNumber.setTypeface(standardTypeface);
            currentPaymentNumber.setTextColor(textColor);
            currentPaymentNumber.setBackground(cellBackground);
            currentPaymentNumber.setPadding(30, 10, 10, 10);
            currentPaymentNumber.setMinHeight(minHeight);


            TextView loanAmount = new TextView(ResultActivity.this);
            loanAmount.setText(DECIMAL_FORMAT.format(payment.getLoanBalanceAmount()));
            loanAmount.setTypeface(standardTypeface);
            loanAmount.setTextColor(textColor);
            loanAmount.setBackground(cellBackground);
            loanAmount.setPadding(10, 10 ,10 ,10);
            loanAmount.setMinHeight(minHeight);

            TextView interestAmount = new TextView(ResultActivity.this);
            interestAmount.setText(DECIMAL_FORMAT.format(payment.getInterestPaymentAmount()));
            interestAmount.setTypeface(standardTypeface);
            interestAmount.setTextColor(textColor);
            interestAmount.setBackground(cellBackground);
            interestAmount.setPadding(10, 10 ,10 ,10);
            interestAmount.setMinHeight(minHeight);

            TextView principalAmount = new TextView(ResultActivity.this);
            principalAmount.setText(DECIMAL_FORMAT.format(payment.getDebtPaymentAmount()));
            principalAmount.setTypeface(standardTypeface);
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
            paymentAmount.setTypeface(standardTypeface);
            paymentAmount.setTextColor(textColor);
            paymentAmount.setBackground(cellBackground);
            paymentAmount.setMinHeight(minHeight / 2);
            paymentAmount.setPadding(10, 10, 30, 10);
            innerRowForAmount.addView(paymentAmount);

            String earlyPaymentValue = getEarlyPaymentValue(payment);

            TextView earlyPayment = new TextView(this);
            earlyPayment.setText(earlyPaymentValue);
            earlyPayment.setTypeface(boldTypeface);
            earlyPayment.setTextColor(textColor);
            earlyPayment.setBackground(earlyPaymentBackground);
            earlyPayment.setMinHeight((minHeight / 2) - 2); // TODO What the fuck is this thing?
            earlyPayment.setPaintFlags(earlyPayment.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            earlyPayment.setPadding(10, 10 ,10 ,10);
            earlyPayment.setTextSize(11);

            earlyPayment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    earlyPaymentAddOnClickCallback(v);
                }
            });

            TableRow innerRowForEarlyPayment = new TableRow(this);
            innerRowForEarlyPayment.addView(earlyPayment);
            innerRowForEarlyPayment.setTag(paymentNumber - 1);

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
     * Determines values for early payment cell
     *
     * @param payment
     * @return Value for early payment cell
     */
    private String getEarlyPaymentValue(MonthlyPayment payment) {
        String earlyPaymentValue;
        if (payment.getAdditionalPaymentAmount() != null && payment.getAdditionalPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
            earlyPaymentValue = "+ " + DECIMAL_FORMAT.format(payment.getAdditionalPaymentAmount());
        } else {
            earlyPaymentValue = "+ досрочно";
        }
        return earlyPaymentValue;
    }

    /**
     * Handler on early payment button click
     *
     * Shows early payment dialog
     */
    private void earlyPaymentAddOnClickCallback(View v) {
        final TextView textView = (TextView) v;
        final Integer paymentNumber = (Integer) ((TableRow) v.getParent()).getTag();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                earlyPaymentAddOnOKCallback((AlertDialog) dialog, paymentNumber);
            }
        });
        builder.setNegativeButton("Отказаться", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // TODO
            }
        });
        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.early_payment, null));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Handler on early payment add confirm
     * Makes an API call and rebuilds the result table
     *
     * @param paymentNumber Number of the payment in the schedule
     */
    private void earlyPaymentAddOnOKCallback(AlertDialog dialog, Integer paymentNumber) {
        BigDecimal amount = new BigDecimal(((EditText) dialog.findViewById(R.id.earlyPaymentAmount)).getText().toString());
        EarlyPaymentRepeatingStrategy repeatingStrategy = EarlyPaymentRepeatingStrategy.SINGLE;// TODO
        EarlyPaymentStrategy earlyPaymentStrategy = ((RadioButton) dialog.findViewById(R.id.termDecrease)).isChecked() ? EarlyPaymentStrategy.DECREASE_TERM : EarlyPaymentStrategy.DECREASE_MONTHLY_PAYMENT;

        Map<Integer, EarlyPayment> earlyPayments = loan.getEarlyPayments() != null ? loan.getEarlyPayments() : new HashMap<Integer, EarlyPayment>();
        earlyPayments.put(paymentNumber, new EarlyPayment(amount, earlyPaymentStrategy, repeatingStrategy));

        // Construct new loan because it is immutable TODO
        loan = Loan.builder()
                .amount(loan.getAmount())
                .rate(loan.getRate())
                .term(loan.getTerm())
                .earlyPayments(earlyPayments)
                .build();

        try {
            calculateLoanAmortization();
        } catch (Exception e) {
            showSomethingWentWrongDialog();
            System.out.println(e.toString());
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

        Typeface standardTypeface = getStandardTypeface();

        header.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        header.setBackground(background);
        header.setPadding(20, 50, 10, 50);

        TextView paymentNumberColumn = new TextView(ResultActivity.this);
        paymentNumberColumn.setText("№   ");
        paymentNumberColumn.setTextSize(headerFontSize);
        paymentNumberColumn.setTextColor(textColor);
        paymentNumberColumn.setTypeface(standardTypeface);
        paymentNumberColumn.setPadding(30, 0, 0 , 0);

        TextView loanBalanceAmountColumn = new TextView(ResultActivity.this);
        loanBalanceAmountColumn.setText("Остаток основного долга");
        loanBalanceAmountColumn.setTextSize(headerFontSize);
        loanBalanceAmountColumn.setTextColor(textColor);
        loanBalanceAmountColumn.setSingleLine(false);
        loanBalanceAmountColumn.setTypeface(standardTypeface);
        loanBalanceAmountColumn.setPadding(20, 0 , 0, 0);

        TextView interestAmountColumn = new TextView(ResultActivity.this);
        interestAmountColumn.setText("Проценты банку");
        interestAmountColumn.setTextSize(headerFontSize);
        interestAmountColumn.setTextColor(textColor);
        interestAmountColumn.setTypeface(standardTypeface);
        interestAmountColumn.setSingleLine(false);
        interestAmountColumn.setPadding(20, 0 ,0 ,0);

        TextView principalAmountColumn = new TextView(ResultActivity.this);
        principalAmountColumn.setText("Основной долг");
        principalAmountColumn.setTextSize(headerFontSize);
        principalAmountColumn.setTextColor(textColor);
        principalAmountColumn.setTypeface(standardTypeface);
        principalAmountColumn.setSingleLine(false);
        principalAmountColumn.setPadding(20, 0,0,0);

        TextView paymentAmountColumn = new TextView(ResultActivity.this);
        paymentAmountColumn.setText("Платеж");
        paymentAmountColumn.setTextSize(headerFontSize);
        paymentAmountColumn.setTextColor(textColor);
        paymentAmountColumn.setTypeface(standardTypeface);
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
     * Base type face
     * @return
     */
    private Typeface getStandardTypeface() {
        Typeface typeface;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.base_font);
        } else {
            typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.base_font);
        }
        return typeface;
    }

    /**
     * Bold type face
     * @return
     */
    private Typeface getBoldTypeface() {
        Typeface typeface;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.bold_font);
        } else {
            typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.bold_font);
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