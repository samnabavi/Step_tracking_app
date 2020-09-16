package com.example.charleshitchcock.personalbest;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charleshitchcock.personalbest.fitness.FakeStepUpdater;
import com.example.charleshitchcock.personalbest.fitness.FitnessMonthlyUpdater;
import com.example.charleshitchcock.personalbest.fitness.GoogleFitGSIAdapter;
import com.example.charleshitchcock.personalbest.fitness.StepUpdater;
import com.example.charleshitchcock.personalbest.fitness.GoogleFitAdapter;
import com.example.charleshitchcock.personalbest.fitness.WeeklyUpdater;
import com.example.charleshitchcock.personalbest.message.ChatList;
import com.example.charleshitchcock.personalbest.message.MessageActivity;
import com.example.charleshitchcock.personalbest.message.friendList;
import com.example.charleshitchcock.personalbest.services.FakeStepUpdaterService;
import com.example.charleshitchcock.personalbest.services.StepUpdaterService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class MenuActivity extends AppCompatActivity {

    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private final int REQUEST_CODE_STRILEN = System.identityHashCode(this) & 0xFFEE;
    TextView stepsTV;
    StepUpdater sg;
    public static int todayIs;
    public static boolean isShown = false;
    public static boolean test = false;
    public static int stepsadded = 0;
    public static long fakemillis = -1;
    public static boolean hasFriends = false;
    public static Calendar fakecalendar = null;
    EditText changedtime;
    FirebaseFirestore db;
    public static GoogleFitGSIAdapter wu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //this.setStepCount(1000);
        Log.d("debug1", "opening menu activity");

        db = FirebaseFirestore.getInstance();

        todayIs = getDayOfWeek();

        test = getIntent().getBooleanExtra("test", false);
        Log.d("debug1", "got intent");

        if (!test) {
            uploadMonthlyData();
        }

        // check shared preferences; if stride length hasn't been updated, we must set it
        SharedPreferences myStrideLen = getSharedPreferences("personal_best", MODE_PRIVATE);
        float l = myStrideLen.getFloat("stride_len", 1000);
        if (l == 1000) {
            Log.d("debug1", "before launching stride length");
            launchStriLeng();
            Log.d("debug1", "after launching stride length");
        } else {
            Log.d("debug1", "calling method to step updates ");
            beginStepUpdates();
        }

        Button setGoal = findViewById(R.id.updateGoalButton);

        setGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSetGoalActivity();
            }

        });

        Button weeklyButton = findViewById(R.id.weeklyButton);
        weeklyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchWeeklyGraph();
            }
        });

        Button monthlyButton = findViewById(R.id.monthlyGraph);
        monthlyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchMonthlyGraph();
            }
        });
      
        //Button and onclick for showing previous walk
        Button walkButton = findViewById(R.id.walkButton);
        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActiveWalk();
            }
        });

        Button preWalkButton = findViewById(R.id.showPreWalkButton);
        preWalkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPreWalk();
            }
        });

        Button addButton = findViewById(R.id.addButton);
        Button changeTimeButton = findViewById(R.id.changeTimeButton);
        changedtime = findViewById(R.id.changedtime);

        Button messageButton = findViewById(R.id.messageButton);
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMessage();
            }
        });

        Button frienddata = findViewById(R.id.addfriend);
        frienddata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchaddfriend();
            }
        });


        if (!test) {
            addButton.setVisibility(View.INVISIBLE);
            changeTimeButton.setVisibility(View.INVISIBLE);
            changedtime.setVisibility(View.INVISIBLE);
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepsadded += 500;
            }
        });

        changeTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fakemillis = Long.parseLong(changedtime.getText().toString());
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(fakemillis);
                fakecalendar = c;
            }
        });

        Log.d("debug1", "finished setting up the listeners");

    }

    public void uploadMonthlyData() {
        FitnessMonthlyUpdater fmu = new FitnessMonthlyUpdater(this, db);
        fmu.setup();
        fmu.updateMonthlyData();
    }

    public void checkForEncouragement() {
        if (hasFriends) {
            return;
        }
        SharedPreferences shrp = getSharedPreferences("personal_best", MODE_PRIVATE);
        long todaySteps = shrp.getLong("daily_steps", 0);
        int todayDay = MenuActivity.getDayOfWeek();
        //1_total = sunday
        String s = Integer.toString(((todayDay + 5) % 7 + 1));
        s = s + "_total";
        long yesterdaySteps = shrp.getLong(s,0);

        Log.d("debug2", "Checking for encouragement");
        Log.d("debug2", "Todays: " + todaySteps);
        Log.d("debug2", "Yesterdays: " + yesterdaySteps);

        if( todaySteps - yesterdaySteps > 500 && yesterdaySteps > 0) {
            //Toast
            Toast.makeText(MenuActivity.this, "Good job; you improved more than 500 steps from yesterday!", Toast.LENGTH_LONG).show();

            isShown = true;
            //todayIs = getDayOfWeek();
        }

    }

    public static void uploadSimpleName(GoogleSignInAccount gsi) {
        if (gsi != null) {
            Map<String, String> name = new HashMap<>();
            name.put("name", gsi.getGivenName());
            FirebaseFirestore.getInstance().collection("users").document(gsi.getEmail())
                    .set(name, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("debug5", "wrote name");
                }
            });
        }
    }

    public void checkForGoal() {
        //check every second to see if he was active in 3 days.
        SharedPreferences shrp = getSharedPreferences("personal_best", MODE_PRIVATE);
        SharedPreferences.Editor editor = shrp.edit();

        int inRowDays = shrp.getInt("days_in_row", 0);
        if( inRowDays >= 2) {

            AlertDialog.Builder notif = new AlertDialog.Builder(MenuActivity.this);
            notif.setMessage("Good Job! You completed your goal yesterday; do you want to upgrade your daily goal?");
            notif.setCancelable(true);

            notif.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            launchSetGoalActivity();
                            dialog.cancel();
                        }
                    });

            notif.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = notif.create();
            alert11.show();



            //Dialog msg
            editor.putInt("days_in_row", 0);
        } else if( inRowDays == 1) {

                AlertDialog.Builder notif = new AlertDialog.Builder(MenuActivity.this);
                notif.setMessage("Good Job; you completed your goal! Do you want to upgrade your daily goal?");
                notif.setCancelable(true);

                notif.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                launchSetGoalActivity();
                                dialog.cancel();
                            }
                        });

                notif.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = notif.create();
                alert11.show();



                //Dialog msg
                editor.putInt("days_in_row", 0);
        }
        editor.apply();

    }

    public void launchWeeklyGraph() {
        Intent intent = new Intent(this, WeeklyGraphActivity.class);
        Log.d("debug1", "starting weekly graph");
        startActivity(intent);
    }

    public void launchMonthlyGraph(){
        Intent intent = new Intent(this, MonthlyGraphActivity.class);
        Log.d("debug1", "starting monthly graph");
        startActivity(intent);
    }

    public void beginStepUpdates() {
        if (test) {
            sg = new FakeStepUpdater(this);
            sg.setup();
            new UpdateStepTask().execute(sg);
            Log.d("debug1", "starting service");
            startUpdaterService();
        } else {
            Log.d("debug1", "starting task");
            sg = new GoogleFitAdapter(this);
            sg.setup();
            sg.startRecording();
            new UpdateStepTask().execute(sg);
            Log.d("debug1", "starting service");
            startUpdaterService();
        }
    }

    public void startUpdaterService() {
        Intent intent;
        if (test) {
            intent = new Intent(this, FakeStepUpdaterService.class);
            intent.putExtra("test", test);
        } else {
            Log.d("debug2", "calling constructor for gsi");
            wu = new GoogleFitGSIAdapter(GoogleSignIn.getLastSignedInAccount(this), this, FirebaseFirestore.getInstance());
            intent = new Intent(this, StepUpdaterService.class);
            intent.putExtra("test", test);
        }
        startService(intent);
    }

    public class UpdateStepTask extends AsyncTask<StepUpdater, Long, Long> {

        protected Long doInBackground(StepUpdater... sgs) {
            Log.d("debug1", "reached thread");


            while(true) {
                try {
                    sg.updateStepCount();
                    publishProgress();
                    Log.d("debug1", "updating steps");
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.d("debug1", e.getMessage());
                }
            }
        }

        protected void onProgressUpdate(Long... progress) {
            if( todayIs != MenuActivity.getDayOfWeek()) {
                MenuActivity.stepsadded = 0;
                SharedPreferences shrp = getSharedPreferences("personal_best", MODE_PRIVATE);
                SharedPreferences.Editor editor = shrp.edit();
                editor.putLong("daily_steps", 0);
                editor.apply();
                isShown = false;
                todayIs = MenuActivity.getDayOfWeek();
            }
            if (!isShown) {
                checkForEncouragement();
            }
            checkForGoal();
        }

        protected void onPostExecute(Long result) {

        }
    }

    public void launchActiveWalk(){
        Intent intent = new Intent(this, planWalk.class);
        startActivity(intent);
    }

    public void launchMessage(){
        Intent intent = new Intent(this, ChatList.class);
        startActivity(intent);
    }
    public void launchaddfriend(){
        Intent intent = new Intent(this, friendList.class);
        startActivity(intent);
    }


    public void setStepCount(long steps) {
        stepsTV = (TextView) findViewById(R.id.stepsView);
        SharedPreferences shrp = getSharedPreferences("personal_best", MODE_PRIVATE);
        int l = shrp.getInt("goal", 5000);
        String textToSet = Long.toString(steps) + "/"+Integer.toString(l);
        stepsTV.setText(textToSet);
    }

    public void launchStriLeng(){
        Intent intent = new Intent(this, strilen.class);
        startActivityForResult(intent, REQUEST_CODE_STRILEN);
    }

    public void launchPreWalk(){
        Intent intent = new Intent(this, previousWalk.class);
        startActivity(intent);
    }

    public void launchSetGoalActivity() {
        Intent intent = new Intent(this, GoalSetupActivity.class);
        startActivity(intent);
    }
  
    public static int getDayOfWeek() {
        Calendar c = Calendar.getInstance();
        if (test) {
            return fakecalendar != null ? fakecalendar.get(Calendar.DAY_OF_WEEK) : c.get(Calendar.DAY_OF_WEEK);
        } else {
            return c.get(Calendar.DAY_OF_WEEK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  If authentication was required during google fit setup, this will be called after the user authenticates
        if (requestCode == REQUEST_CODE_STRILEN) {
            Log.d("debug1", "got stride length");
            beginStepUpdates();
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                sg.updateStepCount();
                Log.d("debug1", "signed in");
                new UpdateStepTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                Log.e("debug1", "ERROR, google fit result code: " + resultCode);
            }
        }
    }
}
