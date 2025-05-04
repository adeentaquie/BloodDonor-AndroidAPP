package com.example.blooddonor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class FindDonorActivity extends AppCompatActivity {

    private Spinner bloodGroupSpinner, citySpinner;
    private Button searchBtn, notifyBtn;
    private RecyclerView donorRecyclerView;

    private FirebaseFirestore firestore;

    private List<User> donorList;
    private DonorAdapter donorAdapter;

    private List<String> cityList;
    private ArrayAdapter<String> cityAdapter;

    private List<String> bloodGroupList;
    private ArrayAdapter<String> bloodGroupAdapter;

    private String selectedCity = "";
    private String selectedGroup = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_donor);

        firestore = FirebaseFirestore.getInstance();

        bloodGroupSpinner = findViewById(R.id.spinner_blood_group);
        citySpinner = findViewById(R.id.spinner_city);
        searchBtn = findViewById(R.id.btn_search);
        notifyBtn = findViewById(R.id.btn_notify_all);
        donorRecyclerView = findViewById(R.id.recycler_view_donors);

        donorList = new ArrayList<>();
        donorAdapter = new DonorAdapter(donorList);
        donorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        donorRecyclerView.setAdapter(donorAdapter);

        // Setup city spinner
        // Setup city spinner
        cityList = new ArrayList<>();
        cityAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_white, cityList);
        cityAdapter.setDropDownViewResource(R.layout.spinner_item_white);
        citySpinner.setAdapter(cityAdapter);
// Setup blood group spinner
        bloodGroupList = new ArrayList<>();
        bloodGroupAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_white, bloodGroupList);
        bloodGroupAdapter.setDropDownViewResource(R.layout.spinner_item_white);
        bloodGroupSpinner.setAdapter(bloodGroupAdapter);

        // Load data
        fetchCities();
        fetchBloodGroups();

        searchBtn.setOnClickListener(v -> {
            selectedCity = citySpinner.getSelectedItem().toString();
            selectedGroup = bloodGroupSpinner.getSelectedItem().toString();
            fetchDonors();
        });

        notifyBtn.setOnClickListener(v -> {
            if ("Select City".equalsIgnoreCase(selectedCity) || "Select Group".equalsIgnoreCase(selectedGroup)) {
                Toast.makeText(this, "Please select both blood group and city first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (donorList.isEmpty()) {
                Toast.makeText(this, "No donors to notify", Toast.LENGTH_SHORT).show();
                return;
            }

            String message = "Urgent blood request in " + selectedCity + " for " + selectedGroup + ". Please respond if you can donate.";

            for (User donor : donorList) {
                String phone = donor.getPhone().replaceFirst("^0", "92"); // Format to international
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://wa.me/" + phone + "?text=" + Uri.encode(message)));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "WhatsApp not available for " + donor.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchCities() {
        firestore.collection("users")
                .whereEqualTo("role", "donor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> citySet = new HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String city = doc.getString("city");
                        if (city != null && !city.trim().isEmpty()) {
                            citySet.add(city.trim());
                        }
                    }
                    cityList.clear();
                    cityList.add("Select City");
                    cityList.addAll(citySet);
                    Collections.sort(cityList);
                    cityAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load cities", Toast.LENGTH_SHORT).show());
    }

    private void fetchBloodGroups() {
        firestore.collection("users")
                .whereEqualTo("role", "donor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> groupSet = new HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String group = doc.getString("bloodGroup");
                        if (group != null && !group.trim().isEmpty()) {
                            groupSet.add(group.trim());
                        }
                    }
                    bloodGroupList.clear();
                    bloodGroupList.add("Select Group");
                    bloodGroupList.addAll(groupSet);
                    Collections.sort(bloodGroupList);
                    bloodGroupAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load blood groups", Toast.LENGTH_SHORT).show());
    }

    private void fetchDonors() {
        if ("Select Group".equalsIgnoreCase(bloodGroupSpinner.getSelectedItem().toString())
                || "Select City".equalsIgnoreCase(citySpinner.getSelectedItem().toString())) {
            Toast.makeText(this, "Please select both blood group and city", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users")
                .whereEqualTo("role", "donor")
                .whereEqualTo("bloodGroup", bloodGroupSpinner.getSelectedItem().toString())
                .whereEqualTo("city", citySpinner.getSelectedItem().toString())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    donorList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = new User();
                        user.setName(doc.getString("name"));
                        user.setBloodGroup(doc.getString("bloodGroup"));
                        user.setPhone(doc.getString("phone"));
                        user.setCity(doc.getString("city"));
                        donorList.add(user);
                    }
                    donorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch donors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
