package com.example.blooddonor;

public class BloodRequest {
    private String bloodGroup;
    private String urgency;
    private String location;
    private String contact;
    private String hospital;  // Added hospital name field
    private String latitude;
    private String longitude;
    private long timestamp;
    private String userId;

    private double distanceKm;

    public BloodRequest() {
        // Required for Firestore
    }

    public BloodRequest(String bloodGroup, String urgency, String location, String contact,
                        String hospital, String latitude, String longitude, long timestamp, String userId) {
        this.bloodGroup = bloodGroup;
        this.urgency = urgency;
        this.location = location;
        this.contact = contact;
        this.hospital = hospital;  // Set hospital name
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getHospital() {  // Getter for hospital
        return hospital;
    }

    public void setHospital(String hospital) {  // Setter for hospital
        this.hospital = hospital;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
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

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
