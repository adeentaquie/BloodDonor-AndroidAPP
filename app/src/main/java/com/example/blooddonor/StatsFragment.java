package com.example.blooddonor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StatsFragment extends Fragment {

    private TextView txtTotalDonations, txtLastDonation, txtBloodType;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_stats_fragment, container, false);

        txtTotalDonations = view.findViewById(R.id.txt_total_donations);
        txtLastDonation = view.findViewById(R.id.txt_last_donation);
        txtBloodType = view.findViewById(R.id.txt_blood_type);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadUserStats();

        return view;
    }

    private void loadUserStats() {
        String currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        // Load user blood group
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String bloodType = snapshot.getString("bloodGroup");
                    txtBloodType.setText("Blood Type: " + (bloodType != null ? bloodType : "-"));
                })
                .addOnFailureListener(e -> {
                    Log.e("StatsFragment", "Failed to load user blood type", e);
                    txtBloodType.setText("Blood Type: Error");
                });

        // Load donation stats
        db.collection("donations")
                .whereEqualTo("donorId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Donation> donations = querySnapshot.toObjects(Donation.class);

                    txtTotalDonations.setText("Total Donations: " + donations.size());

                    if (!donations.isEmpty()) {
                        Donation latest = Collections.max(donations, (d1, d2) -> {
                            long t1 = d1.getDonationDate() != null ? d1.getDonationDate() : 0;
                            long t2 = d2.getDonationDate() != null ? d2.getDonationDate() : 0;
                            return Long.compare(t1, t2);
                        });

                        txtLastDonation.setText("Last Donation: " + formatDate(latest.getDonationDate()));
                    } else {
                        txtLastDonation.setText("Last Donation: -");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("StatsFragment", "Failed to load donations", e);
                    txtTotalDonations.setText("Total Donations: Error");
                    txtLastDonation.setText("Last Donation: Error");
                });
    }

    private String formatDate(Long millis) {
        if (millis == null || millis == 0) return "-";
        Date date = new Date(millis);
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
    }

    // Model for donation documents
    public static class Donation {
        private Long donationDate;

        public Donation() {} // Required by Firestore

        public Long getDonationDate() {
            return donationDate;
        }

        public void setDonationDate(Long donationDate) {
            this.donationDate = donationDate;
        }
    }
}
