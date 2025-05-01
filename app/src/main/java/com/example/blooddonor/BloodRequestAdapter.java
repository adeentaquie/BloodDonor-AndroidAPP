package com.example.blooddonor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BloodRequestAdapter extends RecyclerView.Adapter<BloodRequestAdapter.BloodRequestViewHolder> {

    private List<BloodRequest> bloodRequestList;

    public BloodRequestAdapter(List<BloodRequest> bloodRequestList) {
        this.bloodRequestList = bloodRequestList;
    }

    @Override
    public BloodRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blood_request_item, parent, false);
        return new BloodRequestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BloodRequestViewHolder holder, int position) {
        BloodRequest bloodRequest = bloodRequestList.get(position);
        holder.bloodGroupTextView.setText(bloodRequest.getBloodGroup());
        holder.urgencyTextView.setText(bloodRequest.getUrgency());
        holder.locationTextView.setText(bloodRequest.getLocation());
        holder.contactTextView.setText(bloodRequest.getContact());
    }

    @Override
    public int getItemCount() {
        return bloodRequestList.size();
    }

    public static class BloodRequestViewHolder extends RecyclerView.ViewHolder {
        public TextView bloodGroupTextView, urgencyTextView, locationTextView, contactTextView;

        public BloodRequestViewHolder(View view) {
            super(view);
            bloodGroupTextView = view.findViewById(R.id.blood_group);
            urgencyTextView = view.findViewById(R.id.urgency);
            locationTextView = view.findViewById(R.id.location);
            contactTextView = view.findViewById(R.id.contact);
        }
    }
}
