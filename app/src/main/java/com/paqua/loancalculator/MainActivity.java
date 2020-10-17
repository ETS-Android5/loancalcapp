package com.paqua.loancalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.paqua.loancalculator.dto.Loan;
import com.paqua.loancalculator.util.Constant;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {
    private Button calculateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calculateButton = (Button) findViewById(R.id.calc);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateButtonOnClickCallback();
            }
        });
    }

    /**
     * Starts result activity
     */
    private void calculateButtonOnClickCallback() {
        // TODO Validation
        BigDecimal amount = new BigDecimal(((EditText) findViewById(R.id.loanAmount)).getText().toString());
        BigDecimal rate = new BigDecimal(((EditText) findViewById(R.id.interestRate)).getText().toString());
        int term = Integer.parseInt(((EditText) findViewById(R.id.term)).getText().toString());

        RadioButton yearTerm = (RadioButton) findViewById(R.id.yearTermType);
        if (yearTerm.isChecked()) {
            term *= 12;
        }

        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        Loan loan = Loan.builder()
                .amount(amount)
                .rate(rate)
                .term(term)
                .build();

        intent.putExtra(Constant.LOAN_OBJECT, loan);

        startActivity(intent);
    }
}