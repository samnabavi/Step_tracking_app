package com.example.charleshitchcock.personalbest.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;

import com.example.charleshitchcock.personalbest.MenuActivity;
import com.example.charleshitchcock.personalbest.fitness.FakeStepWeeklyUpdater;
import com.example.charleshitchcock.personalbest.fitness.GoogleFitGSIAdapter;
import com.example.charleshitchcock.personalbest.fitness.WeeklyUpdater;
import com.google.android.gms.auth.api.signin.GoogleSignIn;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FakeStepUpdaterService extends Service {

    WeeklyUpdater sg;
    boolean test;
    int dayofweek;
    boolean notisent;

    public FakeStepUpdaterService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sg = new FakeStepWeeklyUpdater(this);
        Thread uwt = new Thread(new UpdateWeeklyThread());
        uwt.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class UpdateWeeklyThread implements Runnable {

        @Override
        public void run() {
            Log.d("debug2", "started thread");
            Log.d("debug2", Boolean.toString(notisent));
            synchronized (this) {
                while (true) {
                    try {
                        sg.updateWeekly();
                        // check for days in a row
                        SharedPreferences shrp = getSharedPreferences("personal_best", MODE_PRIVATE);
                        SharedPreferences.Editor editor = shrp.edit();
                        int inRowDays = shrp.getInt("days_in_row", 0);

                        if(!shrp.getBoolean("notisent",false) && shrp.getLong("daily_steps",0)>=shrp.getInt("goal",5000)){
                            Log.d("notilog", "run: ");
                            editor.putBoolean("notisent",true);
                            editor.apply();
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "a")
                                    .setContentTitle("goal")
                                    .setContentText("reached goal")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                            notificationManager.notify(001, builder.build());
                        }



                        if (dayofweek != MenuActivity.getDayOfWeek()) {
                            editor.putBoolean("incremented_today", false);
                            editor.putLong("daily_steps", 0);
                            MenuActivity.stepsadded = 0;
                            editor.putBoolean("notisent",false);
                            editor.apply();
                        }
                        if (!shrp.getBoolean("incremented_today", false)) {
                            int goal = shrp.getInt("goal", 5000);
                            long daily_steps = shrp.getLong("daily_steps", 0);
                            if (daily_steps >= goal) {
                                editor.putBoolean("incremented_today", true);
                                editor.putInt("days_in_row", inRowDays + 1);
                                editor.apply();
                            } else if (inRowDays >= 2) {
                                editor.putInt("days_in_row", 0);
                                editor.apply();
                            }
                        }
                        dayofweek = MenuActivity.getDayOfWeek();
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
