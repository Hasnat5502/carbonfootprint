package com.example.carbonfootprint;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carbonfootprint.R;
import com.example.carbonfootprint.TravelServey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeServey extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // UI Elements
    private SeekBar householdSeekBar;
    private TextView householdValue;
    private RadioGroup bedroomsGroup, heatingGroup, renewableGroup, appliancesGroup, laundryGroup;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homeservey); // Replace with your XML layout name

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        householdValue = findViewById(R.id.householdValue);
        householdSeekBar = findViewById(R.id.householdSeekBar);
        bedroomsGroup = findViewById(R.id.bedroomsGroup);
        heatingGroup = findViewById(R.id.heatingGroup);
        renewableGroup = findViewById(R.id.renewableGroup);
        appliancesGroup = findViewById(R.id.appliancesGroup);
        laundryGroup = findViewById(R.id.laundryGroup);
        submitButton = findViewById(R.id.submitButton);

        // SeekBar change listener
        householdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Progress 0-9 corresponds to 1-10 people
                householdValue.setText(String.valueOf(progress + 1));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

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
        return bedroomsGroup.getCheckedRadioButtonId() != -1 &&
                heatingGroup.getCheckedRadioButtonId() != -1 &&
                renewableGroup.getCheckedRadioButtonId() != -1 &&
                appliancesGroup.getCheckedRadioButtonId() != -1 &&
                laundryGroup.getCheckedRadioButtonId() != -1;
    }

    // Emission calculation helper methods
    private double getHouseholdEmission(int count) {
        switch (count) {
            case 1: return 3.5;
            case 2: return 2.0;
            case 3: return 1.5;
            case 4: return 1.25;
            case 5: return 1.1;
            case 6: return 1.0;
            case 7: return 0.95;
            case 8: return 0.9;
            case 9: return 0.87;
            default: return 0.85; // 10+
        }
    }

    private double getBedroomsEmission(String selection) {
        switch (selection) {
            case "Studio": return 1.0;
            case "1": return 1.5;
            case "2": return 2.2;
            case "3+": return 3.0;
            default: return 0;
        }
    }

    private double getHeatingEmission(String selection) {
        switch (selection) {
            case "Gas boiler": return 4.0;
            case "Gas condenser": return 3.6;
            case "Oil": return 5.5;
            case "Electricity": return 2.8;
            case "Ground-source heat pump": return 1.2;
            default: return 0;
        }
    }

    private double getRenewableEmission(String selection) {
        switch (selection) {
            case "Yes": return 0.0;
            case "No": return 1.5;
            case "Not sure": return 0.75;
            default: return 0;
        }
    }

    private double getAppliancesEmission(String selection) {
        switch (selection) {
            case "Always": return 0.0;
            case "Most of the time": return 0.3;
            case "Rarely": return 0.8;
            case "Never": return 1.5;
            default: return 0;
        }
    }

    private double getLaundryEmission(String selection) {
        switch (selection) {
            case "<2 times / week": return 0.5;
            case "3–4 times / week": return 1.0;
            case "5+ times / week": return 1.8;
            default: return 0;
        }
    }

    private void saveSurveyData() {
        // Get current user ID
        String userId = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : "anonymous";

        // Get answers
        int householdCount = Integer.parseInt(householdValue.getText().toString());
        String bedrooms = getSelectedRadioText(bedroomsGroup);
        String heating = getSelectedRadioText(heatingGroup);
        String renewable = getSelectedRadioText(renewableGroup);
        String appliances = getSelectedRadioText(appliancesGroup);
        String laundry = getSelectedRadioText(laundryGroup);

        // Calculate emissions (kg CO₂e per week)
        double householdEmission = getHouseholdEmission(householdCount);
        double bedroomsEmission = getBedroomsEmission(bedrooms);
        double heatingEmission = getHeatingEmission(heating);
        double renewableEmission = getRenewableEmission(renewable);
        double appliancesEmission = getAppliancesEmission(appliances);
        double laundryEmission = getLaundryEmission(laundry);

        // Calculate totals
        double weeklyEmissions = householdEmission + bedroomsEmission + heatingEmission +
                renewableEmission + appliancesEmission + laundryEmission;
        double annualEmissions = (weeklyEmissions * 52) / 1000; // Convert to metric tons/year

        // Create survey data object with emissions
        SurveyData surveyData = new SurveyData(
                householdCount, bedrooms, heating, renewable, appliances, laundry,
                householdEmission, bedroomsEmission, heatingEmission,
                renewableEmission, appliancesEmission, laundryEmission,
                weeklyEmissions, annualEmissions
        );

        // Save to Firebase
        mDatabase.child("surveys").child("home").child(userId)
                .setValue(surveyData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(HomeServey.this, TravelServey.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getSelectedRadioText(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        return radioButton.getText().toString();
    }

    // Updated data model class with emission values
    public static class SurveyData {
        // User selections
        public int householdCount;
        public String bedrooms;
        public String heatingSystem;
        public String renewableElectricity;
        public String appliancesUsage;
        public String laundryFrequency;

        // Emission values (kg CO₂e/week)
        public double householdEmission;
        public double bedroomsEmission;
        public double heatingEmission;
        public double renewableEmission;
        public double appliancesEmission;
        public double laundryEmission;

        // Totals
        public double weeklyEmissions;   // kg CO₂e/week
        public double annualEmissions;   // metric tons CO₂e/year

        public long timestamp = System.currentTimeMillis();

        public SurveyData() {}  // Required for Firebase

        public SurveyData(int householdCount, String bedrooms, String heatingSystem,
                          String renewableElectricity, String appliancesUsage,
                          String laundryFrequency,
                          double householdEmission, double bedroomsEmission,
                          double heatingEmission, double renewableEmission,
                          double appliancesEmission, double laundryEmission,
                          double weeklyEmissions, double annualEmissions) {
            // User answers
            this.householdCount = householdCount;
            this.bedrooms = bedrooms;
            this.heatingSystem = heatingSystem;
            this.renewableElectricity = renewableElectricity;
            this.appliancesUsage = appliancesUsage;
            this.laundryFrequency = laundryFrequency;

            // Emission values
            this.householdEmission = householdEmission;
            this.bedroomsEmission = bedroomsEmission;
            this.heatingEmission = heatingEmission;
            this.renewableEmission = renewableEmission;
            this.appliancesEmission = appliancesEmission;
            this.laundryEmission = laundryEmission;

            // Totals
            this.weeklyEmissions = weeklyEmissions;
            this.annualEmissions = annualEmissions;
        }
    }
}