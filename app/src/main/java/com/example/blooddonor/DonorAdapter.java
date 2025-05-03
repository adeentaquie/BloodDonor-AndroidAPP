package com.example.blooddonor;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;




public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.ViewHolder> {
    private List<User> donors;

    public DonorAdapter(List<User> donors) {
        this.donors = donors;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, bloodGroup, city, phone;
        public Button callBtn;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.donor_name);
            bloodGroup = view.findViewById(R.id.donor_blood_group);
            city = view.findViewById(R.id.donor_city);
            phone = view.findViewById(R.id.donor_phone);
            callBtn = view.findViewById(R.id.btn_call_donor);
        }
    }

    @Override
    public DonorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.donor_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User donor = donors.get(position);
        holder.name.setText(donor.getName());
        holder.bloodGroup.setText("Blood: " + donor.getBloodGroup());
        holder.city.setText("City: " + donor.getCity());
        holder.phone.setText("Phone: " + donor.getPhone());

        holder.callBtn.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + donor.getPhone()));
            v.getContext().startActivity(callIntent);
        });
    }

    @Override
    public int getItemCount() {
        return donors.size();
    }
}
