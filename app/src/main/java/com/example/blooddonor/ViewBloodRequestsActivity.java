package com.example.blooddonor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewBloodRequestsActivity extends AppCompatActivity {

    private Spinner bloodGroupSpinner;
    private Button viewRequestsBtn;
    private RecyclerView requestsRecyclerView;
    private FirebaseFirestore firestore;
    private BloodRequestAdapter bloodRequestAdapter;
    private List<BloodRequest> bloodRequestList;

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 0.0;
    private double currentLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_blood_requests);

        firestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bloodGroupSpinner = findViewById(R.id.spinner_blood_group);
        viewRequestsBtn = findViewById(R.id.btn_view_requests);
        requestsRecyclerView = findViewById(R.id.recycler_view_requests);

        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_groups, android.R.layout.simple_spinner_item);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupSpinner.setAdapter(bloodAdapter);

        bloodRequestList = new ArrayList<>();
        bloodRequestAdapter = new BloodRequestAdapter(bloodRequestList, this::showRequestDialog);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestsRecyclerView.setAdapter(bloodRequestAdapter);

        viewRequestsBtn.setOnClickListener(v -> fetchLocationAndRequests());
    }

    private void fetchLocationAndRequests() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                fetchBloodRequests();
            } else {
                Toast.makeText(this, "Could not fetch current location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBloodRequests() {
        String bloodGroup = bloodGroupSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(bloodGroup)) {
            Toast.makeText(this, "Please select a blood group", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("blood_requests")
                .whereEqualTo("bloodGroup", bloodGroup)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bloodRequestList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BloodRequest request = new BloodRequest();
                        request.setBloodGroup(document.getString("bloodGroup"));
                        request.setUrgency(document.getString("urgency"));
                        request.setLocation(document.getString("location"));
                        request.setContact(document.getString("contact"));
                        request.setHospital(document.getString("hospital"));  // Set the hospital name from Firestore
                        request.setLatitude(document.getString("latitude"));
                        request.setLongitude(document.getString("longitude"));
                        request.setUserId(document.getString("userId"));
                        request.setTimestamp(document.getLong("timestamp"));

                        try {
                            String latStr = request.getLatitude();
                            String lngStr = request.getLongitude();

                            if (latStr != null && lngStr != null && !latStr.isEmpty() && !lngStr.isEmpty()) {
                                double reqLat = Double.parseDouble(latStr);
                                double reqLng = Double.parseDouble(lngStr);

                                float[] results = new float[1];
                                Location.distanceBetween(currentLat, currentLng, reqLat, reqLng, results);
                                double distanceInKm = results[0] / 1000.0;
                                request.setDistanceKm(distanceInKm);

                                request.setLocation(request.getLocation() + String.format(" (%.1f km away)", distanceInKm));

                                bloodRequestList.add(request);
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    Collections.sort(bloodRequestList, Comparator.comparingDouble(BloodRequest::getDistanceKm));
                    bloodRequestAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void showRequestDialog(BloodRequest request) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_request_details, null);

        // Set hospital name
        ((TextView) dialogView.findViewById(R.id.txt_hospital)).setText(request.getHospital());
        // Set blood group
        ((TextView) dialogView.findViewById(R.id.txt_blood_type)).setText("Blood Group: " + request.getBloodGroup());
        // Set urgency
        ((TextView) dialogView.findViewById(R.id.txt_urgency)).setText("Urgency: " + request.getUrgency());
        // Set location
        ((TextView) dialogView.findViewById(R.id.txt_location)).setText("Location: " + request.getLocation());
        // Set contact number
        ((TextView) dialogView.findViewById(R.id.txt_contact)).setText("Contact Number: " + request.getContact());
        // Set distance (if applicable)
        ((TextView) dialogView.findViewById(R.id.txt_distance)).setText("Distance: " + request.getDistanceKm() + " km away");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btn_call).setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + request.getContact()));
            startActivity(callIntent);
        });

        dialogView.findViewById(R.id.btn_message).setOnClickListener(v -> {
            String phone = request.getContact().replaceFirst("^0", "92"); // Convert local number to international format
            String message = "Hi, I’m a blood donor. I saw your request on the app. How can I help?";
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://wa.me/" + phone + "?text=" + Uri.encode(message))); // Send message via WhatsApp
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndRequests();
        } else {
            Toast.makeText(this, "Location permission is required to sort by distance.", Toast.LENGTH_SHORT).show();
        }
    }
}
