package com.example.blooddonor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class BloodRequestAdapter extends RecyclerView.Adapter<BloodRequestAdapter.BloodRequestViewHolder> {

    private List<BloodRequest> bloodRequestList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BloodRequest request);
    }

    public BloodRequestAdapter(List<BloodRequest> bloodRequestList, OnItemClickListener listener) {
        this.bloodRequestList = bloodRequestList;
        this.listener = listener;
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

        if (bloodRequest.getDistanceKm() > 0) {
            holder.distanceTextView.setText(String.format(Locale.getDefault(), "%.2f km away", bloodRequest.getDistanceKm()));
        } else {
            holder.distanceTextView.setText("");
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(bloodRequest));
    }

    @Override
    public int getItemCount() {
        return bloodRequestList.size();
    }

    public static class BloodRequestViewHolder extends RecyclerView.ViewHolder {
        public TextView bloodGroupTextView, urgencyTextView, locationTextView, contactTextView, distanceTextView;

        public BloodRequestViewHolder(View view) {
            super(view);
            bloodGroupTextView = view.findViewById(R.id.blood_group);
            urgencyTextView = view.findViewById(R.id.urgency);
            locationTextView = view.findViewById(R.id.location);
            contactTextView = view.findViewById(R.id.contact);
            distanceTextView = view.findViewById(R.id.distance);
        }
    }
}
