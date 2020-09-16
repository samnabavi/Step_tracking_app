package com.example.charleshitchcock.personalbest.message;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.charleshitchcock.personalbest.MenuActivity;
import com.example.charleshitchcock.personalbest.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class friendList extends AppCompatActivity {

    public static final String TAG = "friendList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Button add = (Button) findViewById(R.id.btn_add);
        EditText email = (EditText) findViewById(R.id.email);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String toadd = email.getText().toString();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("users").document(toadd).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "got given name");
                                    if (task.getResult().getData() != null) {
                                        String name = (String) task.getResult().getData().get("name");
                                        Map<String, String> emailtoname = new HashMap<>();

                                        emailtoname.put(toadd, name);

                                        db.collection("users").document(GoogleSignIn.getLastSignedInAccount(getApplicationContext()).getEmail()).collection("friends").document("friendnames")
                                                .set(emailtoname, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "uploaded friend");
                                            }
                                        });
                                    }
                                }
                            }
                        });

            }
        });



        final ListView lv = (ListView) findViewById(R.id.lv);
        String[] names = new String[] {

        };
        final List<String> emails_list = new ArrayList<>();
        final List<String> names_list = new ArrayList<String>(Arrays.asList(names));
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, names_list);
        lv.setAdapter(arrayAdapter);
        FirebaseFirestore.getInstance().collection("users").document(GoogleSignIn.getLastSignedInAccount(this).getEmail())
                .collection("friends").document("friendnames").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "read friends");
                    Map<String, Object> temp = task.getResult().getData();
                    if (temp != null) {
                        for (String s : temp.keySet()) {
                            FirebaseFirestore.getInstance().collection("users").document(GoogleSignIn.getLastSignedInAccount(getApplicationContext()).getEmail())
                                    .collection("friends").document("friendnames").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().getData().containsKey(s)) {
                                            MenuActivity.hasFriends = true;
                                            emails_list.add(s);
                                            names_list.add((String) temp.get(s));
                                        }
                                        arrayAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item text from ListView
                String selectedItem = (String) parent.getItemAtPosition(position);

                // Display the selected item text on TextView
                Toast toast = Toast.makeText(getApplicationContext(), "You are clicking: "+selectedItem,
                        Toast.LENGTH_SHORT);

                toast.show();
                launchdata(emails_list.get(names_list.indexOf(selectedItem)));
            }
        });

    }
    public void launchdata(String email) {

        Intent intent = new Intent(this, FriendDataActivity.class);
        intent.putExtra("user", GoogleSignIn.getLastSignedInAccount(this).getEmail());
        intent.putExtra("other", email);
        startActivity(intent);

    }

}

