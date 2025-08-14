package com.example.carbonfootprint;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TravelServey extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // UI Elements
    private RadioGroup distanceGroup, transportGroup, vehicleTypeGroup,
            flightsGroup, carpoolGroup, rideHailingGroup, routePlanningGroup;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travelservey); // Use your XML layout name

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        distanceGroup = findViewById(R.id.distanceGroup);
        transportGroup = findViewById(R.id.transportGroup);
        vehicleTypeGroup = findViewById(R.id.vehicleTypeGroup);
        flightsGroup = findViewById(R.id.flightsGroup);
        carpoolGroup = findViewById(R.id.carpoolGroup);
        rideHailingGroup = findViewById(R.id.rideHailingGroup);
        routePlanningGroup = findViewById(R.id.routePlanningGroup);
        submitButton = findViewById(R.id.submitButton);

        // Submit button click listener
        submitButton.setOnClickListener(v -> {
            if (validateSelections()) {
                saveSurveyData();
            } else {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateSelections() {
        return distanceGroup.getCheckedRadioButtonId() != -1 &&
                transportGroup.getCheckedRadioButtonId() != -1 &&
                vehicleTypeGroup.getCheckedRadioButtonId() != -1 &&
                flightsGroup.getCheckedRadioButtonId() != -1 &&
                carpoolGroup.getCheckedRadioButtonId() != -1 &&
                rideHailingGroup.getCheckedRadioButtonId() != -1 &&
                routePlanningGroup.getCheckedRadioButtonId() != -1;
    }

    // Emission calculation helper methods
    private double getDistanceEmission() {
        int selectedId = distanceGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.noDriveOption) return 0.0;
        if (selectedId == R.id.lowDistanceOption) return 2.0;
        if (selectedId == R.id.mediumDistanceOption) return 5.0;
        if (selectedId == R.id.highDistanceOption) return 12.0;
        return 0;
    }

    private double getTransportEmission() {
        int selectedId = transportGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.walkOption) return 0.0;
        if (selectedId == R.id.bicycleOption) return 0.1;
        if (selectedId == R.id.publicTransportOption) return 1.2;
        if (selectedId == R.id.motorcycleOption) return 3.5;
        if (selectedId == R.id.carOption) return 8.0;
        return 0;
    }

    private double getVehicleTypeEmission() {
        int selectedId = vehicleTypeGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.electricOption) return 3.0;
        if (selectedId == R.id.hybridOption) return 5.5;
        if (selectedId == R.id.petrolOption) return 8.0;
        if (selectedId == R.id.dieselOption) return 7.2;
        if (selectedId == R.id.noVehicleOption) return 0.0;
        return 0;
    }

    private double getFlightsEmission() {
        int selectedId = flightsGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.noFlightsOption) return 0.0;
        if (selectedId == R.id.fewFlightsOption) return 2.0;
        if (selectedId == R.id.moderateFlightsOption) return 5.0;
        if (selectedId == R.id.manyFlightsOption) return 10.0;
        return 0;
    }

    private double getCarpoolEmission() {
        int selectedId = carpoolGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.alwaysCarpoolOption) return 0.3;
        if (selectedId == R.id.sometimesCarpoolOption) return 0.6;
        if (selectedId == R.id.rarelyCarpoolOption) return 0.9;
        if (selectedId == R.id.neverCarpoolOption) return 1.0;
        return 0;
    }

    private double getRideHailingEmission() {
        int selectedId = rideHailingGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.neverRideOption) return 0.0;
        if (selectedId == R.id.occasionallyOption) return 0.8;
        if (selectedId == R.id.weeklyOption) return 2.5;
        if (selectedId == R.id.dailyOption) return 6.0;
        return 0;
    }

    private double getRoutePlanningEmission() {
        int selectedId = routePlanningGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.yesPlanOption) return 0.9;
        if (selectedId == R.id.sometimesPlanOption) return 1.0;
        if (selectedId == R.id.noPlanOption) return 1.1;
        return 0;
    }

    private String getSelectedRadioText(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        return radioButton.getText().toString();
    }

    private void saveSurveyData() {
        // Get current user ID
        String userId = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : "anonymous";

        // Get answers
        String distance = getSelectedRadioText(distanceGroup);
        String transport = getSelectedRadioText(transportGroup);
        String vehicleType = getSelectedRadioText(vehicleTypeGroup);
        String flights = getSelectedRadioText(flightsGroup);
        String carpool = getSelectedRadioText(carpoolGroup);
        String rideHailing = getSelectedRadioText(rideHailingGroup);
        String routePlanning = getSelectedRadioText(routePlanningGroup);

        // Calculate emissions (kg CO₂e per week)
        double distanceEmission = getDistanceEmission();
        double transportEmission = getTransportEmission();
        double vehicleTypeEmission = getVehicleTypeEmission();
        double flightsEmission = getFlightsEmission();
        double carpoolEmission = getCarpoolEmission();
        double rideHailingEmission = getRideHailingEmission();
        double routePlanningEmission = getRoutePlanningEmission();

        // Calculate totals
        double weeklyEmissions = distanceEmission + transportEmission + vehicleTypeEmission +
                flightsEmission + carpoolEmission + rideHailingEmission +
                routePlanningEmission;
        double annualEmissions = (weeklyEmissions * 52) / 1000; // Convert to metric tons/year

        // Create survey data object with emissions
        SurveyData surveyData = new SurveyData(
                distance, transport, vehicleType, flights, carpool, rideHailing, routePlanning,
                distanceEmission, transportEmission, vehicleTypeEmission,
                flightsEmission, carpoolEmission, rideHailingEmission, routePlanningEmission,
                weeklyEmissions, annualEmissions
        );

        // Save to Firebase
        mDatabase.child("surveys").child("travel").child(userId)
                .setValue(surveyData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Redirect to Food Survey
                        startActivity(new Intent(TravelServey.this, FoodSurveyActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Data model class
    public static class SurveyData {
        // User selections
        public String distance;
        public String transport;
        public String vehicleType;
        public String flights;
        public String carpool;
        public String rideHailing;
        public String routePlanning;

        // Emission values (kg CO₂e/week)
        public double distanceEmission;
        public double transportEmission;
        public double vehicleTypeEmission;
        public double flightsEmission;
        public double carpoolEmission;
        public double rideHailingEmission;
        public double routePlanningEmission;

        // Totals
        public double weeklyEmissions;   // kg CO₂e/week
        public double annualEmissions;   // metric tons CO₂e/year

        public long timestamp = System.currentTimeMillis();

        public SurveyData() {}  // Required for Firebase

        public SurveyData(String distance, String transport, String vehicleType,
                          String flights, String carpool, String rideHailing,
                          String routePlanning,
                          double distanceEmission, double transportEmission,
                          double vehicleTypeEmission, double flightsEmission,
                          double carpoolEmission, double rideHailingEmission,
                          double routePlanningEmission,
                          double weeklyEmissions, double annualEmissions) {
            // User answers
            this.distance = distance;
            this.transport = transport;
            this.vehicleType = vehicleType;
            this.flights = flights;
            this.carpool = carpool;
            this.rideHailing = rideHailing;
            this.routePlanning = routePlanning;

            // Emission values
            this.distanceEmission = distanceEmission;
            this.transportEmission = transportEmission;
            this.vehicleTypeEmission = vehicleTypeEmission;
            this.flightsEmission = flightsEmission;
            this.carpoolEmission = carpoolEmission;
            this.rideHailingEmission = rideHailingEmission;
            this.routePlanningEmission = routePlanningEmission;

            // Totals
            this.weeklyEmissions = weeklyEmissions;
            this.annualEmissions = annualEmissions;
        }
    }
}