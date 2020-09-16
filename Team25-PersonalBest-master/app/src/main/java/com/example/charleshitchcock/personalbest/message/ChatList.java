package com.example.charleshitchcock.personalbest.message;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.charleshitchcock.personalbest.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChatList extends AppCompatActivity {

    private final static String TAG = "ChatList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        final ListView lv = (ListView) findViewById(R.id.lv2);
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
                                            Log.d(TAG, s);
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
                launchchat(emails_list.get(names_list.indexOf(selectedItem)), selectedItem);
            }
        });
    }

    public void launchchat(String email, String from) {

        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("user", GoogleSignIn.getLastSignedInAccount(this).getEmail());
        intent.putExtra("other", email);
        intent.putExtra("from", from);
        startActivity(intent);

    }
}
