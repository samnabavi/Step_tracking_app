package com.example.charleshitchcock.personalbest.fitness;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;

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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FakeStepUpdater implements StepUpdater {

    private final String TAG = "FakeStepUpdater";

    private MenuActivity activity;
    SharedPreferences daily;

    public void setup() {
    }

    public FakeStepUpdater(MenuActivity activity) {
        this.activity = activity;
        daily = activity.getSharedPreferences("personal_best", activity.MODE_PRIVATE);
    }

    public void startRecording() {
    }


    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    public void updateStepCount() {
        int total = MenuActivity.stepsadded;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        GoogleSignInAccount gsi = GoogleSignIn.getLastSignedInAccount(activity);
        SharedPreferences.Editor spe = daily.edit();
        spe.putLong("daily_steps", total);
        spe.apply();
        activity.setStepCount(total);
        Log.d("debug1", "Total steps: " + total);
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

