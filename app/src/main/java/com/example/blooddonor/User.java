package com.example.blooddonor;

public class User {
    private String name;
    private String bloodGroup;
    private String phone;
    private String city;

    public User() {}

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
