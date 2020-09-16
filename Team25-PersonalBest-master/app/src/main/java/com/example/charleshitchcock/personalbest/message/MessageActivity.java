package com.example.charleshitchcock.personalbest.message;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.example.charleshitchcock.personalbest.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MessageActivity extends AppCompatActivity {
    public static final String CHAT_SERVICE_KEY = "CHAT_SERVICE_KEY";
    String TAG = MessageActivity.class.getSimpleName();

    String COLLECTION_KEY = "chats";//user name
    String DOCUMENT_KEY = "chat1";//friend name
    String MESSAGES_KEY = "messages";//
    String FROM_KEY = "from";

    String TEXT_KEY = "text";
    String TIMESTAMP_KEY = "timestamp";



    CollectionReference chat;
    String from;

    IChatService chatService;
    Boolean isSub = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        FirebaseApp.initializeApp(this);

        // user and receiver are the respective emails
        from = getIntent().getStringExtra("from");
        String user =  getIntent().getStringExtra("user");
        String receiver =   getIntent().getStringExtra("other");


        if(user.compareTo(receiver) > 0 ){
            DOCUMENT_KEY = user.substring(0,5) + receiver.substring(0,5);
        }
        else{
            DOCUMENT_KEY = receiver.substring(0,5)+user.substring(0,5);
        }





        chat = FirebaseFirestore.getInstance()
                .collection(COLLECTION_KEY)
                .document(DOCUMENT_KEY)
                .collection(MESSAGES_KEY);

        // @Before chatServiceFatory.put(Chatkey, fakeChatService);
        chatService = ChatServiceFactory.create(DOCUMENT_KEY, this);
        chatService.subscribeToNotificationsTopic();
        chatService.initMessageUpdateListener();

        findViewById(R.id.btn_send).setOnClickListener(view -> chatService.sendMessage());

        // EditText nameView = findViewById((R.id.user_name));
        // nameView.setText(from);

//        nameView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                from = s.toString();
//                sharedpreferences.edit().putString(FROM_KEY, from).apply();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
    }
}
