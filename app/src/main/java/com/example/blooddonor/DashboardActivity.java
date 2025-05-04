package com.example.blooddonor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    private Button btnPostRequest, btnFindDonors, btnDonationHistory, btnProfile, btnViewRequests;
    private Button btnManageRequests,btnLogout;

    private View mainContentLayout;
    private ProgressBar loadingIndicator;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Bind UI
        mainContentLayout = findViewById(R.id.main_content);
        loadingIndicator = findViewById(R.id.loading_indicator);

        btnPostRequest = findViewById(R.id.btn_post_request);
        btnFindDonors = findViewById(R.id.btn_find_donors);
        btnDonationHistory = findViewById(R.id.btn_donation_history);
        btnProfile = findViewById(R.id.btn_profile);
        btnViewRequests = findViewById(R.id.btn_view_requests);
        btnManageRequests = findViewById(R.id.btn_manage_requests);
        btnLogout = findViewById(R.id.btn_logout);

        // Hide content initially
        mainContentLayout.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String role = documentSnapshot.getString("role");

                Toast.makeText(this, "Welcome " + name + " (" + role + ")", Toast.LENGTH_SHORT).show();

                // Role-specific visibility
                if ("recipient".equalsIgnoreCase(role)) {
                    btnPostRequest.setVisibility(View.VISIBLE);
                    btnFindDonors.setVisibility(View.VISIBLE);
                    btnManageRequests.setVisibility(View.VISIBLE);
                    btnViewRequests.setVisibility(View.GONE);
                    btnDonationHistory.setVisibility(View.GONE);
                } else {
                    btnPostRequest.setVisibility(View.GONE);
                    btnFindDonors.setVisibility(View.GONE);
                    btnManageRequests.setVisibility(View.GONE);
                    btnViewRequests.setVisibility(View.VISIBLE);
                    btnDonationHistory.setVisibility(View.VISIBLE);

                }

                // Show content, hide loader
                loadingIndicator.setVisibility(View.GONE);
                mainContentLayout.setVisibility(View.VISIBLE);

                setupListeners();
            } else {
                Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
                loadingIndicator.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            loadingIndicator.setVisibility(View.GONE);
        });
    }

    private void setupListeners() {
        btnPostRequest.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, PostRequestActivity.class)));

        btnFindDonors.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, FindDonorActivity.class)));

        btnViewRequests.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ViewBloodRequestsActivity.class)));

        btnManageRequests.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ManageRequestsActivity.class)));

        btnDonationHistory.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, DonationHistoryActivity.class)));
//
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        });
    }
}
