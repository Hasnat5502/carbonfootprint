package com.example.carbonfootprint;

public class User {
    public String firstName;
    public double totalCO2Saved;
    public int taliPoints;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String firstName, double totalCO2Saved, int taliPoints) {
        this.firstName = firstName;
        this.totalCO2Saved = totalCO2Saved;
        this.taliPoints = taliPoints;
    }

    // Getters and setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public double getTotalCO2Saved() {
        return totalCO2Saved;
    }

    public void setTotalCO2Saved(double totalCO2Saved) {
        this.totalCO2Saved = totalCO2Saved;
    }

    public int getTaliPoints() {
        return taliPoints;
    }

    public void setTaliPoints(int taliPoints) {
        this.taliPoints = taliPoints;
    }
}