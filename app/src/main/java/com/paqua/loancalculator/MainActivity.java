package com.paqua.loancalculator;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.paqua.loancalculator.customshop.CustomLoanAdapter;
import com.paqua.loancalculator.dto.Loan;
import com.paqua.loancalculator.dto.LoanAmortization;
import com.paqua.loancalculator.storage.LoanStorage;
import com.paqua.loancalculator.util.Constant;
import com.paqua.loancalculator.util.CustomDateUtils;
import com.paqua.loancalculator.util.ErrorDialogUtils;
import com.paqua.loancalculator.util.LoanCommonUtils;
import com.paqua.loancalculator.util.OrientationUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.paqua.loancalculator.util.Constant.LOAN_AMORTIZATION_OBJECT;
import static com.paqua.loancalculator.util.Constant.LOAN_OBJECT;
import static com.paqua.loancalculator.util.Constant.USE_SAVED_DATA;
import static com.paqua.loancalculator.util.ValidationUtils.hasValidTerm;
import static com.paqua.loancalculator.util.ValidationUtils.setupRestoringBackgroundOnTextChange;
import static com.paqua.loancalculator.util.ValidationUtils.validateForEmptyText;
import static com.paqua.loancalculator.util.ValidationUtils.validateForZero;
import static com.paqua.loancalculator.util.ValidationUtils.validateInterestRate;

public class MainActivity extends AppCompatActivity {

    private Button calculateButton;
    private InterstitialAd interstitialAd;
    private Map<Integer, Map.Entry<Loan, LoanAmortization>> loanBySavedIndex;
    private boolean adIsDisabled = false;
    private BillingClient billingClient;
    private Map<String, SkuDetails> skuDetailsMap = new HashMap<>();
    private Calendar firstPaymentDate;
    private PurchasesUpdatedListener purchasesUpdateListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (int i = 0; i < list.size(); i++) {
                    String purchaseId = list.get(i).getSku();
                    if (TextUtils.equals(Constant.DISABLE_ADS_ID.value, purchaseId)) {
                        onPayComplete();
                    }
                }
            } else {
                adIsDisabled = false;
                findViewById(R.id.turnOffAds).setVisibility(View.VISIBLE);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OrientationUtils.lockOrientationPortrait(this);

        setContentView(R.layout.activity_main);
        initMobileAds();

        calculateButton = (Button) findViewById(R.id.calc);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateButtonOnClickCallback();
            }

        });

        initDisableAdsButton();

        initDatePickerButton();

        setupRestoringBackgroundOnTextChange((EditText) findViewById(R.id.loanAmount));
        setupRestoringBackgroundOnTextChange((EditText) findViewById(R.id.interestRate));
        setupRestoringBackgroundOnTextChange((EditText) findViewById(R.id.term));

        setMonthTermTypeOnChangeListener();

        initUnderlinedTextView();
        initSavedLoansView();
        initBillingClient();
    }

    /**
     * Callback for disable ads button
     *
     * Shows purchase dialog
     */
    private void initDisableAdsButton() {
        findViewById(R.id.turnOffAds).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initBillingClient();

                        SkuDetails skuDetails = skuDetailsMap.get(Constant.DISABLE_ADS_ID.value);

                        if (skuDetails != null) {
                            launchBilling(skuDetails.getSku());
                        }
                    }
                }
        );
    }

    /**
     * Callback for a click on date picker button on
     *
     * Shows dialog for choosing first payment date
     */
    private void initDatePickerButton() {
        findViewById(R.id.datePickerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
                alertDialogBuilder.setPositiveButton(getResources().getString(R.string.add_extra_payment_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog alertDialog = (AlertDialog) dialog;

                        DatePicker datePicker = alertDialog.findViewById(R.id.datePicker);
                        firstPaymentDate = CustomDateUtils.getDateFromDatePicker(datePicker);

                        TextView dateTextView = findViewById(R.id.firstPaymentDateTextView);
                        dateTextView.setVisibility(View.VISIBLE);
                        dateTextView.setText(CustomDateUtils.getDateFormatterForDisplayingToUser()
                                .format(firstPaymentDate.getTime())
                        );
                    }
                });

                alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel_extra_payment_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialogBuilder.setNeutralButton(getResources().getString(R.string.reset_extra_payment_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firstPaymentDate = null;

                        TextView dateTextView = findViewById(R.id.firstPaymentDateTextView);
                        dateTextView.setVisibility(View.INVISIBLE);
                        dateTextView.setText("");
                    }
                });

                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.first_payment_date_dialog, null);

                if (firstPaymentDate != null) {
                    DatePicker datePicker = layout.findViewById(R.id.datePicker);
                    datePicker.updateDate(firstPaymentDate.get(Calendar.YEAR), firstPaymentDate.get(Calendar.MONTH), firstPaymentDate.get(Calendar.DAY_OF_MONTH));
                }

                DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
                datePicker.setSpinnersShown(true);
                datePicker.setCalendarViewShown(false);

                alertDialogBuilder.setView(layout);

                alertDialogBuilder.create().show();
            }
        });
    }

    /**
     * Initializes billing client
     */
    private void initBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdateListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    requestSkuDetails();

                    List<Purchase> purchases = requestPurchases();

                    for (Purchase purchase : purchases) {
                       if (Constant.DISABLE_ADS_ID.value.equals(purchase.getSku()))  {
                           onPayComplete();
                       }
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                System.out.println("Billing service disconnected");
                billingClient.startConnection(this);
            }

        });
    }

    /**
     * Queries billing service for a sku for disabling ads
     */
    private void requestSkuDetails() {
        SkuDetailsParams.Builder skuDetailsParamsBuilder = SkuDetailsParams.newBuilder();
        List<String> skuList = new ArrayList<>();
        skuList.add(Constant.DISABLE_ADS_ID.value);
        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                    for (SkuDetails skuDetails : list) {
                        skuDetailsMap.put(skuDetails.getSku(), skuDetails);
                    }
                }
            }
        });
    }


    /**
     * Shows bulling flow
     * @param skuId skuId
     */
    private void launchBilling(@NonNull String skuId) {
        SkuDetails skuDetails = skuDetailsMap.get(skuId);

        if (skuDetails != null) {
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            BillingResult result = billingClient.launchBillingFlow(this, billingFlowParams);

            if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                ErrorDialogUtils.showSomethingWentWrongDialog(
                        MainActivity.this,
                        "Error while launching billing :(",
                        result.getResponseCode() + " " + result.getDebugMessage());
            }
        }
    }

    private void onPayComplete() {
        System.out.println("Payment is complete");
        adIsDisabled = true;
        findViewById(R.id.turnOffAds).setVisibility(View.INVISIBLE);
    }

    private List<Purchase> requestPurchases() {
        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        return purchasesResult.getPurchasesList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initSavedLoansView();
    }

    private void initUnderlinedTextView() {
        setUnderlineTextView((TextView) findViewById(R.id.turnOffAds));
        setUnderlineTextView((TextView) findViewById(R.id.faq));
    }

    /**
     * Sets underline paint flag
     * @param textView textView
     */
    private void setUnderlineTextView(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    /**
     * Fills spinner with saved loans and sets its callbacks
     */
    private void initSavedLoansView() {
        Spinner savedLoans = (Spinner)findViewById(R.id.savedLoans);

        List<String> items = refreshSavedLoans(savedLoans);

        savedLoans.setAdapter(new CustomLoanAdapter(this, items));
    }

    public List<String> refreshSavedLoans(Spinner savedLoans) {
        loanBySavedIndex = new HashMap<>(); // Always new

        List<String> items = new ArrayList<>();
        items.add(getResources().getString(R.string.saved_loans_text));

        Map<Loan, LoanAmortization> saved = LoanStorage.getAll(this);

        if (saved != null && !saved.isEmpty()) {
           int i = 1;
           for (Map.Entry<Loan, LoanAmortization> item : saved.entrySet()) {
               Loan loan = item.getKey();
               items.add(loan.getName() != null && !loan.getName().isEmpty()
                       ? loan.getNameWithCount()
                       : LoanCommonUtils.getDefaultLoanName(getApplicationContext())
               );

               loanBySavedIndex.put(i, item);

               i++;
           }
           savedLoans.setVisibility(View.VISIBLE);
        } else {
           savedLoans.setVisibility(View.INVISIBLE);
        }
        return items;
    }

    /**
     * Shows loaded amortization from preferences
     * @param position position from spinner
     */
    public void showLoadedLoanAmortization(int position) {
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);

        Loan loan = loanBySavedIndex.get(position).getKey();
        LoanAmortization amortization = loanBySavedIndex.get(position).getValue();

        intent.putExtra(LOAN_OBJECT.value, loan);
        intent.putExtra(LOAN_AMORTIZATION_OBJECT.value, amortization);
        intent.putExtra(USE_SAVED_DATA.value, Boolean.TRUE);

        startActivity(intent);
    }

    /**
     * Shows ads and calculates results
     */
    private void calculateButtonOnClickCallback() {
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(new AdRequest.Builder().build());
                calculateAndShow();
            }
        });

        if (interstitialAd.isLoaded() && !adIsDisabled) {
            interstitialAd.show();
        } else {
            // If ads did not load show amortization anyway
            calculateAndShow();
        }
    }

    /**
     * Starts result activity
     */
    private void calculateAndShow() {
        boolean isValidInput = true;

        EditText loanAmount = (EditText) findViewById(R.id.loanAmount);
        EditText interestRate = ((EditText) findViewById(R.id.interestRate));
        EditText loanTerm = ((EditText) findViewById(R.id.term));
        RadioButton monthTermType = (RadioButton) findViewById(R.id.monthTermType);

        int errorColor = getResources().getColor(R.color.coolRed);
        if (isValidInput) isValidInput = validateForEmptyText(loanAmount, errorColor);
        if (isValidInput) isValidInput = validateForZero(loanAmount, errorColor);
        if (isValidInput) isValidInput = validateForEmptyText(loanTerm, errorColor);
        if (isValidInput) isValidInput = validateForZero(loanTerm, errorColor);
        if (isValidInput) isValidInput = hasValidTerm(this, loanTerm, monthTermType.isChecked(), errorColor);
        if (isValidInput) isValidInput = validateInterestRate(interestRate, errorColor);
        if (isValidInput) isValidInput = validateForZero(interestRate, errorColor);

        if (isValidInput) {
            BigDecimal amount = new BigDecimal(loanAmount.getText().toString());
            BigDecimal rate = new BigDecimal(interestRate.getText().toString());
            int term = Integer.parseInt(loanTerm.getText().toString());

            RadioButton yearTerm = (RadioButton) findViewById(R.id.yearTermType);
            if (yearTerm.isChecked()) {
                term *= 12;
            }

            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            Loan loan = Loan.builder()
                    .uuid(UUID.randomUUID())
                    .nameCount(0)
                    .amount(amount)
                    .rate(rate)
                    .term(term)
                    .firstPaymentDate(firstPaymentDate != null
                            ? CustomDateUtils.getDateFormatterForApi().format(firstPaymentDate.getTime())
                            : null)
                    .build();

            intent.putExtra(LOAN_OBJECT.value, loan);
            intent.putExtra(USE_SAVED_DATA.value, Boolean.FALSE);

            startActivity(intent);
        }
    }

    /**
     * Sets max length for term on term type change
     */
    private void setMonthTermTypeOnChangeListener() {
        final RadioButton months = (RadioButton) findViewById(R.id.monthTermType);
        final EditText term = (EditText) findViewById(R.id.term);

        months.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int maxLength;
                if (isChecked) {
                    maxLength = 3;
                } else {
                    maxLength = 2;
                }
                term.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
            }
        });
    }

    private void initMobileAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3031881558720361/4919481152");
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    public Map<Integer, Map.Entry<Loan, LoanAmortization>> getLoanBySavedIndex() {
        return loanBySavedIndex;
    }

    public InterstitialAd getInterstitialAd() {
        return interstitialAd;
    }

    public boolean isAdDisabled() {
        return adIsDisabled;
    }
}