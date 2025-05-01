package com.example.blooddonor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {

    private List<Donor> donorList;

    public DonorAdapter(List<Donor> donorList) {
        this.donorList = donorList;
    }

    @Override
    public DonorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.donor_item, parent, false);
        return new DonorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DonorViewHolder holder, int position) {
        Donor donor = donorList.get(position);
        holder.nameTextView.setText(donor.getName());
        holder.bloodGroupTextView.setText(donor.getBloodGroup());
        holder.contactTextView.setText(donor.getContact());
    }

    @Override
    public int getItemCount() {
        return donorList.size();
    }

    public static class DonorViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView, bloodGroupTextView, contactTextView;

        public DonorViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.donor_name);
            bloodGroupTextView = view.findViewById(R.id.donor_blood_group);
            contactTextView = view.findViewById(R.id.donor_contact);
        }
    }
}
