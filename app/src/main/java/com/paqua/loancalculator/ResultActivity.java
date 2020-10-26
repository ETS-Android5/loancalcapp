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
import android.widget.Button;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.paqua.loancalculator.util.Constant.GET_LOAN_AMROTIZATION_URL;
import static com.paqua.loancalculator.util.ValidationUtils.hasEmptyText;
import static com.paqua.loancalculator.util.ValidationUtils.setupRestoringBackgroundOnTextChange;

public class ResultActivity extends AppCompatActivity {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");
    private static final DecimalFormatSymbols SYMBOLS = DECIMAL_FORMAT.getDecimalFormatSymbols();
    static {
        SYMBOLS.setGroupingSeparator(' ');
        DECIMAL_FORMAT.setDecimalFormatSymbols(SYMBOLS);
        DECIMAL_FORMAT.setGroupingUsed(true);
        DECIMAL_FORMAT.setGroupingSize(3);
    }

    private Loan loan;
    private LoanAmortization amortization;
    private BigDecimal overPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_set);

        setVisibilityForAll(INVISIBLE);

        initLoanFromMainActivity();

        tryCalculateLoanAmortization();

        initResetAllEarlyPaymentsView();
    }

    /**
     * Initializes paint flags and on-click callback for the reset view
     */
    private void initResetAllEarlyPaymentsView() {
        TextView resetEarlyPayments = (TextView) findViewById(R.id.resetAllEarlyPayments);
        resetEarlyPayments.setPaintFlags(resetEarlyPayments.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        resetEarlyPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllEarlyPayments();
                setOverpaymentAmountVisibility();
            }
        });
    }

    /**
     * Initializes loan from previous screen
     */
    private void initLoanFromMainActivity() {
        Intent intent = getIntent();
        loan = (Loan) intent.getExtras().get(Constant.LOAN_OBJECT);
    }

    /**
     * Wrapper for {@link this#calculateLoanAmortization()}
     */
    private void tryCalculateLoanAmortization() {
        try {
            calculateLoanAmortization();
        } catch (Exception e) {
            showSomethingWentWrongDialog();
            e.printStackTrace(); // TODO
        }
    }

    /**
     * Makes REST API request and builds the amortization table
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
                        // TODO audit or something
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
        TextView reset = (TextView) findViewById(R.id.resetAllEarlyPayments);
        TextView hintText = (TextView) findViewById(R.id.hintToEarlyPayment);

        if (overPaymentAmountWithEarly.getText() == null || overPaymentAmountWithEarly.getText().length() == 0) {
            overPaymentAmountWithEarly.setVisibility(INVISIBLE);
            reset.setVisibility(INVISIBLE);
            hintText.setVisibility(VISIBLE);
        } else {
            overPaymentAmountWithEarly.setVisibility(VISIBLE);
            reset.setVisibility(VISIBLE);
            hintText.setVisibility(INVISIBLE);
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
        if (amortization.getEarlyPayments() != null && amortization.getEarlyPayments().entrySet().size() > 0) {
            TextView overPaymentAmountWithEarly = (TextView) findViewById(R.id.overPaymentWithEarly);
            overPaymentAmountWithEarly.setText(DECIMAL_FORMAT.format(amortization.getOverPaymentAmount()));

            findViewById(R.id.hintToEarlyPayment).setVisibility(INVISIBLE);
        }

        // Save the overpayment amount calculated in the first request - will use it to overpayment header that does not include early payments
        if (overPayment == null) {
            overPayment = amortization.getOverPaymentAmount();
            setOverpaymentAmountVisibility();
        }

        if (overPayment != null) {
            overPaymentAmount.setText(DECIMAL_FORMAT.format(overPayment));
        }
    }

    /**
     * Builds amortization table content
     */
    private void rebuildAmortizationTable() {
        TableLayout tableLayout = (TableLayout)findViewById(R.id.amortizationTable);
        tableLayout.removeAllViews();

        buildAmortizationTableHeader(tableLayout);

        buildAmortizationTableContent(tableLayout);
    }

    /**
     * Builds table content
     * @param tableLayout
     */
    // TODO Too complex method - refactor
    private void buildAmortizationTableContent(TableLayout tableLayout) {
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

            tableLayout.addView(row);
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
                earlyPaymentAddOnOKCallback((AlertDialog) dialog, paymentNumber); // TODO Probably don't need this - it will be overridden anyway
            }
        });
        builder.setNegativeButton("Отказаться", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // TODO
            }
        });
        builder.setNeutralButton("Сбросить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetEarlyPayment(paymentNumber);
            }
        });
        LayoutInflater inflater = this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.early_payment, null);

        builder.setView(layout);

        if (paymentNumber != null) {
            TextView forEarlyPaymentNumber = (TextView) layout.findViewById(R.id.forEarlyPaymentNumber);

            forEarlyPaymentNumber.setText(String.format("Для платежа №%s", paymentNumber + 1));
        }

        if (paymentNumber != null && loan.getEarlyPayments() != null) {
            EditText earlyPaymentAmountView = (EditText) layout.findViewById(R.id.earlyPaymentAmount);

            if (loan.getEarlyPayments().get(paymentNumber) != null) {
                earlyPaymentAmountView.setText(loan.getEarlyPayments().get(paymentNumber).getAmount().toString());
            }
        }


        final AlertDialog dialog = builder.create();
        dialog.show();

        // Override on click listener to make possible validation input fields
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                earlyPaymentAddOnOKCallback(dialog, paymentNumber);
            }
        });
    }

    /**
     * Deletes one early payment
     * @param paymentNumber
     */
    private void resetEarlyPayment(Integer paymentNumber) {
        Map<Integer, EarlyPayment> earlyPayment = loan.getEarlyPayments();
        earlyPayment.remove(paymentNumber);

        if (earlyPayment.isEmpty()) {
            resetAllEarlyPayments();
        } else {
            loan = Loan.builder()
                    .amount(loan.getAmount())
                    .term(loan.getTerm())
                    .rate(loan.getRate())
                    .earlyPayments(earlyPayment)
                    .build();

            tryCalculateLoanAmortization();
        }
    }

    /**
     * Resets all early payments and calculates amortization
     */
    private void resetAllEarlyPayments() {
        loan = Loan.builder()
                .amount(loan.getAmount())
                .term(loan.getTerm())
                .rate(loan.getRate())
                .earlyPayments(new HashMap<Integer, EarlyPayment>())
                .build();

        amortization = null;
        overPayment = null;

        TextView overPayment = findViewById(R.id.overPaymentWithEarly);
        overPayment.setText("");

        tryCalculateLoanAmortization();
    }

    /**
     * Handler on early payment add confirm
     * Makes an API call and rebuilds the result table
     *
     * @param paymentNumber Number of the payment in the schedule
     */
    private void earlyPaymentAddOnOKCallback(AlertDialog dialog, Integer paymentNumber) {
        EditText earlyPaymentAmountView = (EditText) dialog.findViewById(R.id.earlyPaymentAmount);

        setupRestoringBackgroundOnTextChange(earlyPaymentAmountView);
        boolean isValid = hasEmptyText(earlyPaymentAmountView, getResources().getColor(R.color.coolRed));

        if (isValid) {
            BigDecimal amount = new BigDecimal(earlyPaymentAmountView.getText().toString());

            EarlyPaymentRepeatingStrategy repeatingStrategy = EarlyPaymentRepeatingStrategy.SINGLE;// TODO Add radio button
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

            tryCalculateLoanAmortization();

            dialog.dismiss();
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
     * Sets visibility for all child for base layout
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