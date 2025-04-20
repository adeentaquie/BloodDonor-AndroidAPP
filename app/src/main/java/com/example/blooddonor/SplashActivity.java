package com.example.blooddonor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find blood drop image views
        ImageView bloodDrop1 = findViewById(R.id.blood_drop_1);
        ImageView bloodDrop2 = findViewById(R.id.blood_drop_2);
        ImageView bloodDrop3 = findViewById(R.id.blood_drop_3);

        // Create the translate animation for each drop
        Animation fallAnimation = new TranslateAnimation(0, 0, 0, 1000); // Move from top to bottom
        fallAnimation.setDuration(3000); // Animation duration
        fallAnimation.setRepeatCount(Animation.INFINITE); // Repeat forever for a continuous fall
        fallAnimation.setInterpolator(new LinearInterpolator());

        // Start animations for each drop at different times
        bloodDrop1.startAnimation(fallAnimation);
        fallAnimation.setStartOffset(0);

        bloodDrop2.startAnimation(fallAnimation);
        fallAnimation.setStartOffset(500); // Delay for 500ms for drop 2

        bloodDrop3.startAnimation(fallAnimation);
        fallAnimation.setStartOffset(1000); // Delay for 1000ms for drop 3


        ImageView logo = findViewById(R.id.splash_logo);
        TextView slogan = findViewById(R.id.splash_footer);
        TextView copyright = findViewById(R.id.splash_copyright);


    }

}