package com.example.blooddonor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostRequestActivity extends AppCompatActivity {

    private Spinner bloodGroupSpinner, urgencySpinner;
    private EditText hospitalInput, locationInput, contactInput, latitudeInput, longitudeInput;
    private Button submitBtn;
    private MapView mapView;
    private Marker selectedMarker;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // OSMDroid config (MUST come before setContentView)
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_request);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Bind views
        bloodGroupSpinner = findViewById(R.id.spinner_blood_group);
        urgencySpinner = findViewById(R.id.spinner_urgency);
        hospitalInput = findViewById(R.id.input_hospital);
        locationInput = findViewById(R.id.input_location);
        contactInput = findViewById(R.id.input_contact);
        latitudeInput = findViewById(R.id.input_latitude);
        longitudeInput = findViewById(R.id.input_longitude);
        submitBtn = findViewById(R.id.btn_submit_request);
        mapView = findViewById(R.id.osm_map);

        // Setup dropdowns for blood group and urgency
        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_groups, android.R.layout.simple_spinner_item);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupSpinner.setAdapter(bloodAdapter);

        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.urgency_levels, android.R.layout.simple_spinner_item);
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        urgencySpinner.setAdapter(urgencyAdapter);

        setupMap();
        submitBtn.setOnClickListener(v -> submitRequest());
    }

    private void setupMap() {
        mapView.setMultiTouchControls(true);
        GeoPoint defaultPoint = new GeoPoint(31.5204, 74.3587); // Lahore default
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(defaultPoint);

        selectedMarker = new Marker(mapView);
        selectedMarker.setTitle("Selected Location");
        mapView.getOverlays().add(selectedMarker);

        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                GeoPoint tappedPoint = (GeoPoint) mapView.getProjection()
                        .fromPixels((int) event.getX(), (int) event.getY());

                selectedMarker.setPosition(tappedPoint);
                selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.invalidate();

                // Get the city and coordinates from geocoder
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(
                            tappedPoint.getLatitude(), tappedPoint.getLongitude(), 1);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        locationInput.setText(address.getLocality()); // Set city
                        latitudeInput.setText(String.valueOf(tappedPoint.getLatitude())); // Set latitude
                        longitudeInput.setText(String.valueOf(tappedPoint.getLongitude())); // Set longitude
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        });

        // Request location permission if needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
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

        if (TextUtils.isEmpty(hospital) || TextUtils.isEmpty(location) || TextUtils.isEmpty(contact)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("bloodGroup", bloodGroup);
        request.put("urgency", urgency);
        request.put("hospital", hospital);
        request.put("location", location);
        request.put("latitude", latitude); // Store latitude
        request.put("longitude", longitude); // Store longitude
        request.put("contact", contact);
        request.put("timestamp", System.currentTimeMillis());
        request.put("userId", mAuth.getCurrentUser().getUid());

        firestore.collection("blood_requests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Request posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to post request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
