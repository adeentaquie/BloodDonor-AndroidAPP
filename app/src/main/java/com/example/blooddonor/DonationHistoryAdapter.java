package com.example.blooddonor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DonationHistoryAdapter extends RecyclerView.Adapter<DonationHistoryAdapter.ViewHolder> {

    private List<DonationRecord> donations;

    public DonationHistoryAdapter(List<DonationRecord> donations) {
        this.donations = donations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationRecord record = donations.get(position);

        holder.txtDonorName.setText("Donor: " + (record.getDonorName() != null ? record.getDonorName() : "Unknown"));
        holder.txtRecipientName.setText("Recipient: " + (record.getRecipientName() != null ? record.getRecipientName() : "Unknown"));
        holder.txtBloodGroup.setText("Blood Group: " + record.getBloodGroup());
        holder.txtHospital.setText("Hospital: " + record.getHospital());
        holder.txtLocation.setText("Location: " + record.getLocation());
        holder.txtDate.setText("Donated on: " + record.getDonationDate());
        holder.txtStatus.setText("Status: " + record.getStatus());
    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDonorName, txtRecipientName, txtBloodGroup, txtHospital, txtLocation, txtDate, txtStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDonorName = itemView.findViewById(R.id.txt_donor_name);
            txtRecipientName = itemView.findViewById(R.id.txt_recipient_name); // Make sure this exists in XML
            txtBloodGroup = itemView.findViewById(R.id.txt_blood_group);
            txtHospital = itemView.findViewById(R.id.txt_hospital);
            txtLocation = itemView.findViewById(R.id.txt_location);
            txtDate = itemView.findViewById(R.id.txt_date);
            txtStatus = itemView.findViewById(R.id.txt_status);
        }
    }
}
