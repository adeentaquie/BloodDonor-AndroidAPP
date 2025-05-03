package com.example.blooddonor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

public class DonationHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private DonationHistoryAdapter adapter;
    private List<DonationRecord> donationList = new ArrayList<>();
    private Map<String, String> userCache = new HashMap<>();

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_history);

        recyclerView = findViewById(R.id.recycler_donations);
        progressBar = findViewById(R.id.progress_donations);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new DonationHistoryAdapter(donationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchDonations();
    }

    private void fetchDonations() {
        String currentUserId = auth.getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);

        firestore.collection("donations")
                .whereEqualTo("donorId", currentUserId)  // Based on donorId now
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    donationList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        DonationRecord record = new DonationRecord();
                        record.setBloodGroup(doc.getString("bloodGroup"));
                        record.setHospital(doc.getString("hospital"));
                        record.setLocation(doc.getString("location"));
                        record.setDonorId(doc.getString("donorId"));
                        record.setRecipientId(doc.getString("recipientId"));
                        record.setRequestId(doc.getString("requestId"));
                        record.setStatus(doc.getString("status"));

                        Long timestamp = doc.getLong("donationDate");
                        if (timestamp != null) {
                            String formattedDate = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                    .format(new Date(timestamp));
                            record.setDonationDate(formattedDate);
                        }

                        // Fetch donor name from donorId (this user)
                        fetchUserName(record.getDonorId(), donorName -> {
                            record.setDonorName(donorName);

                            // Now fetch recipient name
                            fetchUserName(record.getRecipientId(), recipientName -> {
                                record.setRecipientName(recipientName);
                                donationList.add(record);
                                adapter.notifyDataSetChanged();
                            });
                        });
                    }

                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void fetchUserName(String userId, OnNameFetchedCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onNameFetched("Unknown");
            return;
        }

        if (userCache.containsKey(userId)) {
            callback.onNameFetched(userCache.get(userId));
        } else {
            firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String name = snapshot.getString("name");
                        if (name == null) name = "Unknown";
                        userCache.put(userId, name);
                        callback.onNameFetched(name);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DonationHistory", "Failed to fetch user name for ID: " + userId, e);
                        callback.onNameFetched("Unknown");
                    });
        }
    }

    interface OnNameFetchedCallback {
        void onNameFetched(String name);
    }
}
