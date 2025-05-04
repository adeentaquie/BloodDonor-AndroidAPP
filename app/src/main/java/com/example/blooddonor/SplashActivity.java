package com.example.blooddonor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FirebaseApp.initializeApp(this);

        // References to views
        ImageView bloodDrop2 = findViewById(R.id.blood_drop_2);
        ImageView logo = findViewById(R.id.splash_logo);
        TextView title = findViewById(R.id.splash_text);
        TextView footer = findViewById(R.id.splash_footer);
        TextView copyright = findViewById(R.id.splash_copyright);

        // Load animations from anim folder
        Animation dropAnimation = AnimationUtils.loadAnimation(this, R.anim.drop_fall);
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_scale);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Start animations
        bloodDrop2.startAnimation(dropAnimation);
        logo.startAnimation(scaleAnimation);
        title.startAnimation(fadeInAnimation);
        footer.startAnimation(fadeInAnimation);
        copyright.startAnimation(fadeInAnimation);

        // Move to LoginActivity after splash
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}