package com.example.charleshitchcock.personalbest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.internal.FallbackServiceBroker;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class GoalSetupActivity extends AppCompatActivity {

    EditText goalPlace;
    Button doneButton;
    private static final String TAG = "GoalSetupActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_setup);

        doneButton = findViewById(R.id.done_btn);

        doneButton.setEnabled(true);

        goalPlace = findViewById(R.id.set_goal_space);

        //show the previous goal
        SharedPreferences shrp = getSharedPreferences("personal_best", MODE_PRIVATE);
        int l = shrp.getInt("goal", 5000);
        goalPlace.setText(Integer.toString(l));

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //for checking valid integer
                boolean flag = true;

                while (flag) {

                    SharedPreferences myGoal = getSharedPreferences("personal_best", MODE_PRIVATE);
                    SharedPreferences.Editor editor = myGoal.edit();
                    String newGoal = goalPlace.getText().toString();

                    int newGoalInt = 0;

                    try {
                        if (newGoal != null && newGoal.trim().length() > 0) {
                            newGoalInt = Integer.parseInt(newGoal);
                        }
                        flag = false;
                    } catch (NumberFormatException ex) {
                        Toast.makeText(GoalSetupActivity.this, "Please enter a valid goal.", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    //}

                    editor.putInt("goal", newGoalInt);

                    Map<String, Long> goalData = new HashMap<>();
                    GoogleSignInAccount gsi = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                    goalData.put("goal", Long.parseLong(Integer.toString(newGoalInt)));
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users").document(gsi.getEmail())
                    .set(goalData, SetOptions.merge())
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
                    editor.putBoolean("notisent",false);
                    editor.apply();
                    finish();
                }

            }

        });


    }

}
