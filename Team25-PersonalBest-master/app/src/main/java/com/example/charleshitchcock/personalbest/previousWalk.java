package com.example.charleshitchcock.personalbest;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class previousWalk extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_walk);

        SharedPreferences sharedPreferences = getSharedPreferences("personal_best", MODE_PRIVATE);
        long preStepcount = sharedPreferences.getLong("previous_step", 0);
        int walkTime = sharedPreferences.getInt("previous_time", 0);
        float speed = sharedPreferences.getFloat("previous_speed", 0);

        TextView timeElapsetxt = findViewById(R.id.timeElapsed);
        TextView stepcounttxt = findViewById(R.id.stepCount);
        TextView speedtxt = findViewById(R.id.speed);

        //set step
        String textStr = String.valueOf(preStepcount);
        stepcounttxt.setText(textStr);
        //set time
        textStr = String.format("%02d:%02d:%02d", (walkTime/3600), ((walkTime % 3600)/60), (walkTime % 60) );
        timeElapsetxt.setText(textStr);
        //set speed
        textStr = String.valueOf(speed) + " MPH";
        speedtxt.setText(textStr);


        findViewById(R.id.gobackButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
