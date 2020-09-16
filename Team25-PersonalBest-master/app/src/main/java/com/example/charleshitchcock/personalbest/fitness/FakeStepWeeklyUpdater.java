package com.example.charleshitchcock.personalbest.fitness;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.charleshitchcock.personalbest.MenuActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FakeStepWeeklyUpdater  implements WeeklyUpdater {

    Context c;

    private final String TAG = "FakeStepWeeklyUpdater";

    private MenuActivity activity;

    public FakeStepWeeklyUpdater(Context c) {
        Log.d("debug2", "in constructor");
        this.c = c;
    }

    public void updateWeekly() {
        int total = MenuActivity.stepsadded;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        GoogleSignInAccount gsi = GoogleSignIn.getLastSignedInAccount(activity);
        Log.d("debug3", "Total steps again: " + total);
        int dow = MenuActivity.getDayOfWeek();
        SharedPreferences sp = c.getSharedPreferences("personal_best", c.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putLong(Integer.toString(dow)+"_total", total);
        spe.apply();
        Log.d("debug1", Integer.toString(dow)+"_total: "+Long.toString(total));
        Map<String, Object> weekData = new HashMap<>();
        Calendar cl = MenuActivity.fakecalendar;
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

    }
}
