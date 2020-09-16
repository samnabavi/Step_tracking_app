package com.example.charleshitchcock.personalbest;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.charleshitchcock.personalbest.MenuActivity.fakecalendar;
import static com.example.charleshitchcock.personalbest.MenuActivity.getDayOfWeek;

public class planWalk extends AppCompatActivity {

    private static String TAG = "PlanWalk";

    private int timeCounter = 0;
    private boolean isCancelled = false;
    private int dayofweekstarted;
    Calendar tempCalendar;

    long preWalksc;
    long activeSc;
    float stride_length;
    float speed;
    float walkDistance;

    private SharedPreferences sharedPreferences;
    Button cancelButton;

    private TextView scView;
    private TextView distanceView;
    private TextView speedView;
    private TextView timeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_walk);
        if (MenuActivity.test && MenuActivity.fakecalendar != null) {
            tempCalendar = MenuActivity.fakecalendar;
        } else {
            tempCalendar = Calendar.getInstance();
        }

        dayofweekstarted = MenuActivity.getDayOfWeek();

        cancelButton = findViewById(R.id.endWalkButton);
        scView = findViewById(R.id.stepCount);
        distanceView = findViewById(R.id.walkDistance);
        speedView = findViewById(R.id.speed);
        timeView = findViewById(R.id.timeElapsed);

        sharedPreferences = getSharedPreferences("personal_best", MODE_PRIVATE);
        stride_length = sharedPreferences.getFloat("stride_len",0);

        new AsyncTaskPlanWalk().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCancelled = true;

                Log.d("planWalk", "cancelling");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("previous_step", activeSc);
                editor.putInt("previous_time", timeCounter);
                editor.putFloat("previous_speed", speed);


                int dayOfWeek = getDayOfWeek();
                String activeSum = "active_"+dayOfWeek;
                String distanceSum = "distance_"+dayOfWeek;
                String timeSum = "time_"+dayOfWeek;

                long sum = sharedPreferences.getLong(activeSum, 0 ) +  activeSc;
                float distance = sharedPreferences.getFloat(distanceSum, 0 ) + walkDistance;
                int time = sharedPreferences.getInt(timeSum, 0) + timeCounter;

                Log.d("planWalk", "sharedprefs done");

                editor.putLong(activeSum,sum);
                editor.putFloat(distanceSum, distance);
                editor.putInt(timeSum, time);
                editor.apply();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                GoogleSignInAccount gsi = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

                Map<String, Long> intentData = new HashMap<>();
                Calendar cl = Calendar.getInstance();
                String key = cl.get(Calendar.MONTH) + "" + cl.get(Calendar.DATE) + "" + cl.get(Calendar.YEAR);
                intentData.put(key, sum);

                DocumentReference dr = db.collection("users").document(gsi.getEmail()).collection("data").document("intentional");
                dr.set(intentData, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnCanceledListener(new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                Log.i(TAG, "DocumentSnapshot canceled");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "Error writing document", e);
                            }
                        });

                finish();
            }
        });

        Button addIntentional = findViewById(R.id.addIntentionalButton);

        if (!MenuActivity.test) {
            addIntentional.setVisibility(View.INVISIBLE);
        }

        addIntentional.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activeSc += 500;
                MenuActivity.stepsadded += 500;
            }
        });

    }

    private class AsyncTaskPlanWalk extends AsyncTask<Void, Long, String> {

        @Override
        protected String doInBackground(Void... param){
            Log.d("debug2", "AsyncTask reached");
            //Get the step count before intentional walk so that we can calculate the sc during intentional walk
            preWalksc = sharedPreferences.getLong("daily_steps", 0);

            while(!isCancelled) {
                publishProgress(activeSc);
                try {
                    tempCalendar.add(Calendar.SECOND, 1);
                    MenuActivity.fakecalendar = tempCalendar;
                    MenuActivity.fakemillis = tempCalendar.getTimeInMillis();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                activeSc = sharedPreferences.getLong("daily_steps", 0) - preWalksc;

            }
            return "";
        }


        @Override
        protected void onProgressUpdate(Long... walkStep){
            if (dayofweekstarted != MenuActivity.getDayOfWeek() || new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()).equals("00:00:00") ) {
                cancelButton.performClick();
            }


            timeCounter++;

            scView.setText(String.valueOf(walkStep[0]));
            walkDistance = walkStep[0]*stride_length/63360;
            String textStr = String.valueOf(walkDistance) + " Miles";
            distanceView.setText(textStr);

            textStr = String.format("%02d:%02d:%02d", (timeCounter/3600), ((timeCounter % 3600)/60), (timeCounter % 60) );
            timeView.setText(textStr);


            speed = walkDistance/timeCounter*3600;
            textStr = String.valueOf(speed) + " MPH";
            speedView.setText(textStr);
        }

        @Override
        protected void onPostExecute(String result) {

        }

    }

}
