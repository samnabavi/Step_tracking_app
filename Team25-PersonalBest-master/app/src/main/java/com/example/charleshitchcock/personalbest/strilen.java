package com.example.charleshitchcock.personalbest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class strilen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strilen);

        Button upButton = findViewById(R.id.updateButton);

        Log.d("debug1", "in strilen now");

        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText hfeet = findViewById(R.id.heightFeet);
                EditText hinch = findViewById(R.id.heightInch);

                //Check if user has entered feet and inch
                if ( hfeet.getText().toString().equals("") || (hinch.getText().toString().equals("")) ) {
                    Toast.makeText(strilen.this, "Please enter your height", Toast.LENGTH_SHORT).show();

                } else {
                    //Get feet and inch from user input

                    int feet = Integer.parseInt((hfeet.getText().toString()) );
                    int inch = Integer.parseInt((hinch.getText().toString()) );

                    //Calculate Stride Length based on feet and inch
                    float strideLen = (float) ((feet * 12 + inch) * 0.413);

                    SharedPreferences myStrideLen = getSharedPreferences("personal_best", MODE_PRIVATE);
                    SharedPreferences.Editor editor = myStrideLen.edit();

                    editor.putFloat("stride_len", strideLen);

                    editor.apply();
                    Toast.makeText(strilen.this, "Your stride length has been saved", Toast.LENGTH_SHORT).show();

                    TextView showStrideLen =  findViewById(R.id.showStrideLen);
                    String strideLenText = "Your stride length is " + (int)strideLen + " inches";
                    showStrideLen.setText(strideLenText);
                    findViewById(R.id.continueButton).setVisibility(View.VISIBLE);

                }
            }
        });
        findViewById(R.id.continueButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}

