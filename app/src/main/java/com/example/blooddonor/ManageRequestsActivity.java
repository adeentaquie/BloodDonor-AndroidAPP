package com.example.blooddonor;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ManageRequestsActivity extends AppCompatActivity implements ManageRequestAdapter.OnRequestActionListener {

    private RecyclerView recyclerView;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private List<BloodRequest> myRequests;
    private ManageRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_requests);

        recyclerView = findViewById(R.id.recycler_manage_requests);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        myRequests = new ArrayList<>();
        adapter = new ManageRequestAdapter(myRequests, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchMyRequests();
    }

    private void fetchMyRequests() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("blood_requests")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myRequests.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        BloodRequest request = doc.toObject(BloodRequest.class);
                        if (request != null) {
                            request.setId(doc.getId());
                            myRequests.add(request);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDeleteRequest(BloodRequest request) {
        firestore.collection("blood_requests")
                .document(request.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Request deleted", Toast.LENGTH_SHORT).show();
                    fetchMyRequests();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onChangeStatus(BloodRequest request) {
        String[] statuses = {"pending", "fulfilled"};
        int checked = request.getStatus() != null && request.getStatus().equals("fulfilled") ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle("Change Request Status")
                .setSingleChoiceItems(statuses, checked, null)
                .setPositiveButton("Update", (dialog, which) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String newStatus = statuses[selectedPosition];
                    firestore.collection("blood_requests")
                            .document(request.getId())
                            .update("status", newStatus)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                                fetchMyRequests();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
