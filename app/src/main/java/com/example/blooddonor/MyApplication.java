package com.example.blooddonor;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);  // ✅ This makes Firebase ready for all activities
    }
}
