package com.example.charleshitchcock.personalbest.message;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charleshitchcock.personalbest.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServiceAdapter implements IChatService {
    private MessageActivity activity;
    private final String TAG = "ChatServiceAdapter";

    public ChatServiceAdapter(MessageActivity activity) {
        this.activity = activity;
    }

    @Override
    public void sendMessage() {
        if (activity.from == null || activity.from.isEmpty()) {
            Toast.makeText(activity, "Enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText messageView = activity.findViewById(R.id.text_message);

        Map<String, String> newMessage = new HashMap<>();
        newMessage.put(activity.FROM_KEY, activity.from);
        newMessage.put(activity.TEXT_KEY, messageView.getText().toString());

        activity.chat.add(newMessage).addOnSuccessListener(result -> {
            messageView.setText("");
        }).addOnFailureListener(error -> {
            Log.e(TAG, error.getLocalizedMessage());
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void initMessageUpdateListener() {
        activity.chat.orderBy(activity.TIMESTAMP_KEY, Query.Direction.ASCENDING)
                .addSnapshotListener((newChatSnapShot, error) -> {
                    if (error != null) {
                        Log.e(TAG, error.getLocalizedMessage());
                        return;
                    }

                    if (newChatSnapShot != null && !newChatSnapShot.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        List<DocumentChange> documentChanges = newChatSnapShot.getDocumentChanges();
                        documentChanges.forEach(change -> {
                            QueryDocumentSnapshot document = change.getDocument();
                            sb.append(document.get(activity.FROM_KEY));
                            sb.append(":\n");
                            sb.append(document.get(activity.TEXT_KEY));
                            sb.append("\n");
                            sb.append("---\n");
                        });


                        TextView chatView = activity.findViewById(R.id.chat);
                        chatView.append(sb.toString());
                    }
                });
    }

    @Override
    public void subscribeToNotificationsTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(activity.DOCUMENT_KEY)
                .addOnCompleteListener(task -> {
                            String msg = "Subscribed to notifications";
                            if (!task.isSuccessful()) {
                                msg = "Subscribe to notifications failed";
                            }
                            Log.d(TAG, msg);
                            activity.isSub = true;
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                        }
                );
    }

}
