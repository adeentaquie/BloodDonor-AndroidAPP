package com.example.blooddonor;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marker;
    private static final LatLng DEFAULT_LOCATION = new LatLng(31.5204, 74.3587); // Lahore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        // Initialize Places
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.getDefault());
        }

        // Setup Autocomplete Fragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (place.getLatLng() != null) {
                    updateSelection(place.getLatLng(), place.getName());
                }
            }

            @Override
            public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                Toast.makeText(LocationPickerActivity.this, "Search error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Load map
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 14));

        // Show hospitals
        fetchNearbyHospitals(DEFAULT_LOCATION);

        // Tap on map
        mMap.setOnMapClickListener(latLng -> updateSelection(latLng, null));
    }

    private void updateSelection(LatLng latLng, String placeName) {
        if (mMap == null) return;

        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected"));

        String hospital = placeName;
        String city = "";

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {
                Address addr = addresses.get(0);
                if (hospital == null) hospital = addr.getFeatureName();
                city = addr.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent result = new Intent();
        result.putExtra("hospital", hospital != null ? hospital : "Unknown");
        result.putExtra("city", city);
        result.putExtra("latitude", String.valueOf(latLng.latitude));
        result.putExtra("longitude", String.valueOf(latLng.longitude));
        setResult(RESULT_OK, result);
        finish();
    }

    private void fetchNearbyHospitals(LatLng center) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                + "?location=" + center.latitude + "," + center.longitude
                + "&radius=5000"
                + "&type=hospital"
                + "&key=" + getString(R.string.google_maps_key);

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);

                JSONObject response = new JSONObject(sb.toString());
                JSONArray results = response.getJSONArray("results");

                runOnUiThread(() -> {
                    for (int i = 0; i < results.length(); i++) {
                        try {
                            JSONObject place = results.getJSONObject(i);
                            JSONObject loc = place.getJSONObject("geometry").getJSONObject("location");
                            double lat = loc.getDouble("lat");
                            double lng = loc.getDouble("lng");
                            String name = place.getString("name");

                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
