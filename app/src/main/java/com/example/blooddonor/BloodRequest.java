package com.example.blooddonor;

public class BloodRequest {
    private String bloodGroup;
    private String urgency;
    private String location;
    private String contact;
    private long timestamp;
    private String userId;

    public BloodRequest() {
        // Default constructor for Firestore
    }

    public BloodRequest(String bloodGroup, String urgency, String location, String contact, long timestamp, String userId) {
        this.bloodGroup = bloodGroup;
        this.urgency = urgency;
        this.location = location;
        this.contact = contact;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Getters and setters
    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
