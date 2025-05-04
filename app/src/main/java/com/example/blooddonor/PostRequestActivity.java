package com.example.blooddonor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class PostRequestActivity extends AppCompatActivity {

    private Spinner bloodGroupSpinner, urgencySpinner;
    private EditText hospitalInput, locationInput, contactInput, latitudeInput, longitudeInput;
    private Button submitBtn, selectLocationBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private static final int LOCATION_PICKER_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_request);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Bind views
        hospitalInput = findViewById(R.id.input_hospital);
        locationInput = findViewById(R.id.input_location);
        contactInput = findViewById(R.id.input_contact);
        latitudeInput = findViewById(R.id.input_latitude);
        longitudeInput = findViewById(R.id.input_longitude);
        bloodGroupSpinner = findViewById(R.id.spinner_blood_group);
        urgencySpinner = findViewById(R.id.spinner_urgency);
        submitBtn = findViewById(R.id.btn_submit_request);
        selectLocationBtn = findViewById(R.id.btn_select_location);

        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_groups, R.layout.spinner_item_white);
        bloodAdapter.setDropDownViewResource(R.layout.spinner_item_white);
        bloodGroupSpinner.setAdapter(bloodAdapter);

        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.urgency_levels, R.layout.spinner_item_white);
        urgencyAdapter.setDropDownViewResource(R.layout.spinner_item_white);
        urgencySpinner.setAdapter(urgencyAdapter);

        selectLocationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PostRequestActivity.this, LocationPickerActivity.class);
            startActivityForResult(intent, 101);
        });


        submitBtn.setOnClickListener(v -> submitRequest());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            hospitalInput.setText(data.getStringExtra("hospital"));
            locationInput.setText(data.getStringExtra("city"));
            latitudeInput.setText(data.getStringExtra("latitude"));
            longitudeInput.setText(data.getStringExtra("longitude"));

            // Disable editing
            hospitalInput.setEnabled(false);
            locationInput.setEnabled(false);
            latitudeInput.setEnabled(false);
            longitudeInput.setEnabled(false);
        }
    }


    private void submitRequest() {
        String bloodGroup = bloodGroupSpinner.getSelectedItem().toString();
        String urgency = urgencySpinner.getSelectedItem().toString();
        String hospital = hospitalInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String latitude = latitudeInput.getText().toString().trim();
        String longitude = longitudeInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();

        if (hospital.isEmpty() || location.isEmpty() || contact.isEmpty()
                || latitude.isEmpty() || longitude.isEmpty()) {
            Toast.makeText(this, "Please fill/select all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("bloodGroup", bloodGroup);
        request.put("urgency", urgency);
        request.put("hospital", hospital);
        request.put("location", location);
        request.put("latitude", latitude);
        request.put("longitude", longitude);
        request.put("contact", contact);
        request.put("timestamp", System.currentTimeMillis());
        request.put("userId", mAuth.getCurrentUser().getUid());
        request.put("status", "pending"); // ✅ Default status

        firestore.collection("blood_requests")
                .add(request)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Request submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
