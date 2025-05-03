package com.example.blooddonor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManageRequestAdapter extends RecyclerView.Adapter<ManageRequestAdapter.ViewHolder> {

    private List<BloodRequest> requestList;
    private OnRequestActionListener listener;

    public ManageRequestAdapter(List<BloodRequest> requestList, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    public interface OnRequestActionListener {
        void onDeleteRequest(BloodRequest request);
        void onChangeStatus(BloodRequest request);
    }

    @NonNull
    @Override
    public ManageRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageRequestAdapter.ViewHolder holder, int position) {
        BloodRequest request = requestList.get(position);
        holder.textHospital.setText(request.getHospital());
        holder.textStatus.setText("Status: " + (request.getStatus() != null ? request.getStatus() : "pending"));

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteRequest(request));
        holder.btnChangeStatus.setOnClickListener(v -> listener.onChangeStatus(request));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textHospital, textStatus;
        Button btnDelete, btnChangeStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textHospital = itemView.findViewById(R.id.text_hospital);
            textStatus = itemView.findViewById(R.id.text_status);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnChangeStatus = itemView.findViewById(R.id.btn_status);
        }
    }
}
