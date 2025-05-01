package com.example.blooddonor;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class FindDonorActivity extends AppCompatActivity {

    private Spinner bloodGroupSpinner;
    private EditText locationInput;
    private Button findDonorsBtn;
    private RecyclerView donorsRecyclerView;

    private FirebaseFirestore firestore;
    private DonorAdapter donorAdapter;
    private List<Donor> donorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_donor);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Bind views
        bloodGroupSpinner = findViewById(R.id.spinner_blood_group);
        locationInput = findViewById(R.id.input_location);
        findDonorsBtn = findViewById(R.id.btn_find_donors);
        donorsRecyclerView = findViewById(R.id.recycler_view_donors);

        // Setup blood group spinner
        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_groups, android.R.layout.simple_spinner_item);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupSpinner.setAdapter(bloodAdapter);

        // Initialize RecyclerView for displaying donors
        donorList = new ArrayList<>();
        donorAdapter = new DonorAdapter(donorList);
        donorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        donorsRecyclerView.setAdapter(donorAdapter);

        // Button click listener to fetch donors
        findDonorsBtn.setOnClickListener(v -> findDonors());
    }

    private void findDonors() {
        String bloodGroup = bloodGroupSpinner.getSelectedItem().toString();
        String location = locationInput.getText().toString().trim();

        if (TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Please enter your location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get coordinates based on the entered city/location
        GeoPoint userLocation = getUserLocationFromCity(location);
        if (userLocation != null) {
            // Fetch donors from Firestore based on location and blood group
            firestore.collection("donors")
                    .whereEqualTo("bloodGroup", bloodGroup)
                    .whereEqualTo("location", location)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        donorList.clear(); // Clear previous results
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Donor donor = document.toObject(Donor.class);
                            donorList.add(donor);  // Add donors to the list
                        }
                        donorAdapter.notifyDataSetChanged();  // Update the RecyclerView
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FindDonorActivity.this, "Error loading donors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Invalid location", Toast.LENGTH_SHORT).show();
        }
    }

    private GeoPoint getUserLocationFromCity(String city) {
        // Example: Use hardcoded coordinates for cities, or use reverse geocoding for real-time data
        if (city.equalsIgnoreCase("Lahore")) {
            return new GeoPoint(31.5204, 74.3587);
        }
        // Add more cities or use Geocoder for dynamic city-to-coordinates mapping
        return null;
    }
}
