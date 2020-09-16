package com.example.charleshitchcock.personalbest.services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;

import com.example.charleshitchcock.personalbest.GoalSetupActivity;
import com.example.charleshitchcock.personalbest.MenuActivity;
import com.example.charleshitchcock.personalbest.fitness.FakeStepWeeklyUpdater;
import com.example.charleshitchcock.personalbest.fitness.GoogleFitGSIAdapter;
import com.example.charleshitchcock.personalbest.fitness.WeeklyUpdater;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StepUpdaterService extends Service {

    GoogleFitGSIAdapter sg;
    int dayofweek;
    boolean notisent;

    public StepUpdaterService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("debug2", "started service");
        try {
            Log.d("debug2", "finished constructor");
            sg = MenuActivity.wu;
            Thread uwt = new Thread(new UpdateWeeklyThread());
            uwt.start();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("debug2", e.getMessage());
        }

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
            SharedPreferences shrp = getSharedPreferences("personal_best", MODE_PRIVATE);
            Log.d("debug2", Boolean.toString(shrp.getBoolean("notisent", false)));
            synchronized (this) {
                while (true) {
                    try {
                        while (sg.getGsi() == null) {
                            sg = new GoogleFitGSIAdapter(GoogleSignIn.getLastSignedInAccount(getApplicationContext()), getApplicationContext(), FirebaseFirestore.getInstance());
                            MenuActivity.uploadSimpleName(GoogleSignIn.getLastSignedInAccount(getApplicationContext()));
                            Thread.sleep(1000);
                        }
                        sg.updateWeekly();
                        // check for days in a row
                        shrp = getSharedPreferences("personal_best", MODE_PRIVATE);
                        SharedPreferences.Editor editor = shrp.edit();
                        if(!shrp.getBoolean("notisent",false) && shrp.getLong("daily_steps",0)>=shrp.getInt("goal",5000)){
                            Log.d("notilog", "run: ");
                            editor.putBoolean("notisent",true);
                            editor.apply();

                            Intent intent = new Intent(getApplicationContext(), GoalSetupActivity.class);
                            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "a")
                                    .setContentTitle("goal")
                                    .setContentText("reached goal")
                                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setAutoCancel(true)
                                    .setContentIntent(contentIntent);

                            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            nm.notify(NotificationManager.IMPORTANCE_DEFAULT, builder.build());
                            //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                            //notificationManager.notify(NotificationManager.IMPORTANCE_DEFAULT, builder.build());
                        }
                        int inRowDays = shrp.getInt("days_in_row", 0);
                        if (dayofweek != MenuActivity.getDayOfWeek()) {
                            editor.putBoolean("incremented_today", false);
                            editor.putLong("daily_steps", 0);
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
                        Thread.sleep(4000);
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
