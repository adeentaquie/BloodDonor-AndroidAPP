package com.example.blooddonor;

public class Donor {
    private String name;
    private String bloodGroup;
    private String location;
    private String contact;

    // Constructors, Getters, and Setters
    public Donor() {}

    public Donor(String name, String bloodGroup, String location, String contact) {
        this.name = name;
        this.bloodGroup = bloodGroup;
        this.location = location;
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
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
}
