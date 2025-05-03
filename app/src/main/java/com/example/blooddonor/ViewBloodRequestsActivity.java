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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class ViewBloodRequestsActivity extends AppCompatActivity {

    private Spinner bloodGroupSpinner;
    private Button viewRequestsBtn;
    private RecyclerView requestsRecyclerView;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
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
        auth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bloodGroupSpinner = findViewById(R.id.spinner_blood_group);
        viewRequestsBtn = findViewById(R.id.btn_view_requests);
        requestsRecyclerView = findViewById(R.id.recycler_view_requests);

        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bloodRequestList = new ArrayList<>();
        bloodRequestAdapter = new BloodRequestAdapter(bloodRequestList, this::showRequestDialog);
        requestsRecyclerView.setAdapter(bloodRequestAdapter);

        loadAvailableBloodGroups();  // ✅ Load only blood groups with open requests

        viewRequestsBtn.setOnClickListener(v -> fetchLocationAndRequests());
    }

    private void loadAvailableBloodGroups() {
        firestore.collection("blood_requests")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> bloodGroups = new HashSet<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String group = doc.getString("bloodGroup");
                        String status = doc.getString("status");
                        if (group != null && !"fulfilled".equalsIgnoreCase(status)) {
                            bloodGroups.add(group);
                        }
                    }
                    List<String> sortedGroups = new ArrayList<>(bloodGroups);
                    Collections.sort(sortedGroups);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, sortedGroups);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    bloodGroupSpinner.setAdapter(adapter);
                });
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
                        String status = document.getString("status");
                        if (status != null && status.equalsIgnoreCase("fulfilled")) {
                            continue;
                        }

                        BloodRequest request = new BloodRequest();
                        request.setId(document.getId());
                        request.setBloodGroup(document.getString("bloodGroup"));
                        request.setUrgency(document.getString("urgency"));
                        request.setLocation(document.getString("location"));
                        request.setContact(document.getString("contact"));
                        request.setHospital(document.getString("hospital"));
                        request.setLatitude(document.getString("latitude"));
                        request.setLongitude(document.getString("longitude"));
                        request.setUserId(document.getString("userId"));
                        request.setTimestamp(document.getLong("timestamp"));
                        request.setStatus(status);

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

        ((TextView) dialogView.findViewById(R.id.txt_hospital)).setText(request.getHospital());
        ((TextView) dialogView.findViewById(R.id.txt_blood_type)).setText("Blood Group: " + request.getBloodGroup());
        ((TextView) dialogView.findViewById(R.id.txt_urgency)).setText("Urgency: " + request.getUrgency());
        ((TextView) dialogView.findViewById(R.id.txt_location)).setText("Location: " + request.getLocation());
        ((TextView) dialogView.findViewById(R.id.txt_contact)).setText("Contact Number: " + request.getContact());
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
            String phone = request.getContact().replaceFirst("^0", "92");
            String message = "Hi, I’m a blood donor. I saw your request on the app. How can I help?";
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://wa.me/" + phone + "?text=" + Uri.encode(message)));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        });

        dialogView.findViewById(R.id.btn_accept).setOnClickListener(v -> {
            String donorId = auth.getCurrentUser().getUid();

            Map<String, Object> donation = new HashMap<>();
            donation.put("requestId", request.getId());
            donation.put("recipientId", request.getUserId());
            donation.put("donorId", donorId);
            donation.put("donationDate", System.currentTimeMillis());
            donation.put("hospital", request.getHospital());
            donation.put("location", request.getLocation());
            donation.put("bloodGroup", request.getBloodGroup());
            donation.put("status", "accepted");

            firestore.collection("donations")
                    .add(donation)
                    .addOnSuccessListener(docRef -> {
                        // ✅ Step 1: Update status of the request
                        firestore.collection("blood_requests")
                                .document(request.getId())
                                .update("status", "fulfilled")
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Request accepted and marked as fulfilled!", Toast.LENGTH_SHORT).show();

                                    // ✅ Step 2: Notify recipient via WhatsApp
                                    String phone = request.getContact().replaceFirst("^0", "92");
                                    String message = "Hi, I’ve accepted your blood request. Please get in touch!";
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("https://wa.me/" + phone + "?text=" + Uri.encode(message)));
                                    startActivity(intent);

                                    dialog.dismiss();
                                    fetchBloodRequests(); // 🔄 Refresh list to hide accepted request
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to mark as fulfilled: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

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
