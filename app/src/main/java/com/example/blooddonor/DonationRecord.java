package com.example.blooddonor;

public class DonationRecord {
    private String bloodGroup;
    private String donorId;
    private String donorName;

    private String recipientId;
    private String recipientName;

    private String hospital;
    private String location;
    private String donationDate;
    private String requestId;
    private String status;

    // Getters and Setters
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDonationDate() { return donationDate; }
    public void setDonationDate(String donationDate) { this.donationDate = donationDate; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
