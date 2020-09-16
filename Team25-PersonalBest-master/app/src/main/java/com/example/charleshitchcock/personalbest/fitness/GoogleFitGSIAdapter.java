package com.example.charleshitchcock.personalbest.fitness;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.charleshitchcock.personalbest.MenuActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GoogleFitGSIAdapter implements WeeklyUpdater {

    static final String TAG = "GoogleFitGSIAdapter";
    private GoogleSignInAccount gsi;
    Context c;
    FirebaseFirestore db;
    long toRet;

    public GoogleSignInAccount getGsi() {
        return gsi;
    }

    public GoogleFitGSIAdapter(GoogleSignInAccount gsi, Context c, FirebaseFirestore db) {
        Log.d(TAG, "in constructor");
        this.gsi = gsi;
        this.c = c;
        toRet = 0;
        this.db = db;
    }

    public void updateWeekly() {
        Log.d(TAG, "Getting current steps");
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();
        if (GoogleSignIn.hasPermissions(gsi, fitnessOptions)) {
            Fitness.getHistoryClient(c, gsi)
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataSet>() {
                                @Override
                                public void onSuccess(DataSet dataSet) {
                                    long total =
                                            dataSet.isEmpty()
                                                    ? 0
                                                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                    Log.d("debug3", "Total steps again: " + total);
                                    Map<String, Object> weekData = new HashMap<>();
                                    Calendar cl = Calendar.getInstance();
                                    String key = cl.get(Calendar.MONTH) + "" + cl.get(Calendar.DATE) + "" + cl.get(Calendar.YEAR);
                                    weekData.put(key, total);
                                    DocumentReference dr = db.collection("users").document(gsi.getEmail()).collection("data").document("unintentional");
                                    dr.set(weekData, SetOptions.merge())
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
                                    /*
                                    int dow = MenuActivity.getDayOfWeek();
                                    SharedPreferences sp = c.getSharedPreferences("personal_best", c.MODE_PRIVATE);
                                    SharedPreferences.Editor spe = sp.edit();
                                    spe.putLong(Integer.toString(dow)+"_total", total);
                                    spe.apply();
                                    Log.d("debug1", Integer.toString(dow)+"_total: "+Long.toString(total));
                                    */
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "There was a problem getting the step count.", e);
                                }
                            });
        }
    }
}
