package com.paqua.loancalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

                Integer term = Integer.parseInt(((EditText) findViewById(R.id.term)).getText().toString());

                RadioButton yearTerm = (RadioButton) findViewById(R.id.yearTermType);
                if (yearTerm.isChecked()) {
                    term *= 12;
                }

                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("loanAmount", ((EditText) findViewById(R.id.loanAmount)).getText().toString());
                intent.putExtra("interestRate", ((EditText) findViewById(R.id.interestRest)).getText().toString());
                intent.putExtra("monthTerm", term.toString()); // Convention: using string to pass all params for no reason at all

                startActivity(intent);
            }
        });
    }
}