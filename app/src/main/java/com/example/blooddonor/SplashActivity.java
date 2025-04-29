package com.example.blooddonor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
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
        ImageView bloodDrop1 = findViewById(R.id.blood_drop_1);
        ImageView bloodDrop2 = findViewById(R.id.blood_drop_2);
        ImageView bloodDrop3 = findViewById(R.id.blood_drop_3);
        ImageView logo = findViewById(R.id.splash_logo);
        TextView title = findViewById(R.id.splash_text);
        TextView footer = findViewById(R.id.splash_footer);
        TextView copyright = findViewById(R.id.splash_copyright);

        // Falling blood drop animations (independent)
        Animation dropAnim1 = createDropAnimation(0);
        Animation dropAnim2 = createDropAnimation(500);
        Animation dropAnim3 = createDropAnimation(1000);

        bloodDrop1.startAnimation(dropAnim1);
        bloodDrop2.startAnimation(dropAnim2);
        bloodDrop3.startAnimation(dropAnim3);

        // Fade-in animation for logo and text
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000);
        logo.startAnimation(fadeIn);
        title.startAnimation(fadeIn);
        footer.startAnimation(fadeIn);
        copyright.startAnimation(fadeIn);

        // Move to LoginActivity after splash
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DURATION);
    }

    private Animation createDropAnimation(int startOffset) {
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 1000);
        anim.setDuration(3000);
        anim.setStartOffset(startOffset);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setInterpolator(new LinearInterpolator());
        return anim;
    }
}
