package com.example.charleshitchcock.personalbest.message;

public interface IChatService {
    void sendMessage();
    void initMessageUpdateListener();
    void subscribeToNotificationsTopic();
}
