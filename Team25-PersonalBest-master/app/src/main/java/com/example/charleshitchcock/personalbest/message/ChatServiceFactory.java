package com.example.charleshitchcock.personalbest.message;

import android.util.Log;

public class ChatServiceFactory {
    private static final String TAG = "[ChatServiceFactory]";

    public static IChatService create(String key, MessageActivity activity) {
        Log.i(TAG, String.format("creating FitnessService with key %s", key));
        ChatServiceAdapter chatService = new ChatServiceAdapter(activity);
        return chatService;
    }

}
