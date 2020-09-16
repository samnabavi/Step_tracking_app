package com.example.charleshitchcock.personalbest.fitness;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.charleshitchcock.personalbest.MenuActivity;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.errorprone.annotations.Var;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class FitnessMonthlyUpdater implements  MonthlyDataUpdater {

    private final String TAG = "FitnessMonthlyUpdater";
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;

    Activity activity;
    FirebaseFirestore db;

    public FitnessMonthlyUpdater(Activity a, FirebaseFirestore db) {
        this.db = db;
        this.activity = a;
    }

    public void setup() {
        Log.d("TAG", "reached setup");
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        Log.d("TAG", "defined options");
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)) {
            Log.d("TAG", "requesting permissions");
            GoogleSignIn.requestPermissions(
                    activity, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(activity),
                    fitnessOptions);
        }
    }

    public void updateMonthlyData() {
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (lastSignedInAccount == null) {
            Log.d(TAG, "null last account");
            return;
        }
        Calendar cal = MenuActivity.fakecalendar != null ? MenuActivity.fakecalendar : Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -4);
//            cal.set(Calendar.HOUR_OF_DAY, 0);
//            cal.clear(Calendar.MINUTE);
//            cal.clear(Calendar.HOUR);
//            cal.clear(Calendar.MILLISECOND);
//            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest =
                new DataReadRequest.Builder()
                        // The data request can specify multiple data types to return, effectively
                        // combining multiple data queries into one call.
                        // In this example, it's very unlikely that the request is for several hundred
                        // datapoints each consisting of a few steps and a timestamp.  The more likely
                        // scenario is wanting to see how many steps were walked per day, for 7 days.
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                        // bucketByTime allows for a time span, whereas bucketBySession would allow
                        // bucketing by "sessions", which would need to be defined in code.
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();
        Task<DataReadResponse> response = Fitness.getHistoryClient(activity, lastSignedInAccount).readData(readRequest);
        response.addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
            @Override
            public void onSuccess(DataReadResponse dataReadResponse) {
                HashMap<String, Long> monthData = new HashMap<>();
                Log.i(TAG, "succeeded");
                dataReadResponse.getBuckets();
                int currentIndex = 0;
                for (Bucket b : dataReadResponse.getBuckets()) {
                    for (DataSet d : b.getDataSets()) {
                        if (!d.getDataPoints().isEmpty()) {
                            for (Field field : d.getDataPoints().get(0).getDataType().getFields()) {
                                long unintent = d.getDataPoints().get(0).getValue(field).asInt();
                                String key = cal.get(Calendar.MONTH) + "" + cal.get(Calendar.DATE) + "" + cal.get(Calendar.YEAR);
                                cal.add(Calendar.DAY_OF_YEAR, 1);
                                monthData.put(key, unintent);
                            }
                        }
                    }
                }
                db.collection("users").document(lastSignedInAccount.getEmail()).collection("data").document("unintentional")
                        .set(monthData, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "Error writing document", e);
                            }
                        });
            }
        });

    }




}
