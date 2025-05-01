package com.example.blooddonor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;

public class DashboardActivity extends AppCompatActivity {

    private Button btnPostRequest, btnFindDonors, btnDonationHistory, btnProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ScrollView rootLayout = findViewById(R.id.dashboard_root);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        rootLayout.startAnimation(fadeIn);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnPostRequest = findViewById(R.id.btn_post_request);
        btnFindDonors = findViewById(R.id.btn_find_donors);
        btnDonationHistory = findViewById(R.id.btn_donation_history);
        btnProfile = findViewById(R.id.btn_profile);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish(); // prevent access
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String role = documentSnapshot.getString("role");

                Toast.makeText(this, "Welcome " + name + " (" + role + ")", Toast.LENGTH_SHORT).show();

                if ("recipient".equalsIgnoreCase(role)) {
                    // Hide "Post Blood Request" for recipients
                    btnPostRequest.setVisibility(View.GONE);
                }

                // Set up button listeners
                setupListeners();
            } else {
                Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        btnPostRequest.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, PostRequestActivity.class)));

//        btnFindDonors.setOnClickListener(v ->
//                startActivity(new Intent(DashboardActivity.this, FindDonorActivity.class)));
//
//        btnDonationHistory.setOnClickListener(v ->
//                startActivity(new Intent(DashboardActivity.this, DonationHistoryActivity.class)));
//
//        btnProfile.setOnClickListener(v ->
//                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));
    }
}
