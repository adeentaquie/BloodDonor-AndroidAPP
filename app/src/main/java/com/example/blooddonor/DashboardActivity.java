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

public class DashboardActivity extends AppCompatActivity {

    private Button btnPostRequest, btnFindDonors, btnDonationHistory, btnProfile, btnViewRequests;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnPostRequest = findViewById(R.id.btn_post_request);
        btnFindDonors = findViewById(R.id.btn_find_donors);
        btnDonationHistory = findViewById(R.id.btn_donation_history);
        btnProfile = findViewById(R.id.btn_profile);
        btnViewRequests = findViewById(R.id.btn_view_requests);

        // Check if the user is logged in
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
                    btnPostRequest.setVisibility(View.VISIBLE);
                    btnViewRequests.setVisibility(View.GONE);
                } else {
                    btnPostRequest.setVisibility(View.GONE);
                    btnViewRequests.setVisibility(View.VISIBLE);
                }

                setupListeners();
            } else {
                Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        btnPostRequest.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, PostRequestActivity.class)));

        // Uncomment this only if FindDonorActivity is implemented
        // btnFindDonors.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, FindDonorActivity.class)));

        btnViewRequests.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ViewBloodRequestsActivity.class)));
    }
}
