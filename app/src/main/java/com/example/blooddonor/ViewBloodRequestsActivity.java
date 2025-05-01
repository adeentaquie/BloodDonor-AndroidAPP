package com.example.blooddonor;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewBloodRequestsActivity extends AppCompatActivity {

    private Spinner bloodGroupSpinner;
    private Button viewRequestsBtn;
    private RecyclerView requestsRecyclerView;
    private FirebaseFirestore firestore;
    private BloodRequestAdapter bloodRequestAdapter;
    private List<BloodRequest> bloodRequestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_blood_requests);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Bind views
        bloodGroupSpinner = findViewById(R.id.spinner_blood_group);
        viewRequestsBtn = findViewById(R.id.btn_view_requests);
        requestsRecyclerView = findViewById(R.id.recycler_view_requests);

        // Setup the Spinner for blood group
        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_groups, android.R.layout.simple_spinner_item);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupSpinner.setAdapter(bloodAdapter);

        // Initialize RecyclerView for displaying blood requests
        bloodRequestList = new ArrayList<>();
        bloodRequestAdapter = new BloodRequestAdapter(bloodRequestList);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestsRecyclerView.setAdapter(bloodRequestAdapter);

        // Button click listener to fetch blood requests
        viewRequestsBtn.setOnClickListener(v -> fetchBloodRequests());
    }

    private void fetchBloodRequests() {
        String bloodGroup = bloodGroupSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(bloodGroup)) {
            Toast.makeText(this, "Please select a blood group", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch blood requests from Firestore based on the selected blood group
        firestore.collection("blood_requests")
                .whereEqualTo("bloodGroup", bloodGroup)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bloodRequestList.clear(); // Clear previous results

                    // Iterate through each document in the query result
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BloodRequest bloodRequest = document.toObject(BloodRequest.class);
                        bloodRequestList.add(bloodRequest);  // Add the blood request to the list
                    }

                    // Update the RecyclerView with new data
                    bloodRequestAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewBloodRequestsActivity.this, "Error loading blood requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
