package com.example.carbonfootprint;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OthersSurveyActivity extends AppCompatActivity {

    // Define all factor arrays at class level
    private static final double[] SCREEN_TIME_FACTORS = {0, 0, 0.3, 0.3, 0.6, 0.6, 1.0, 1.0, 1.5, 1.5, 2.0, 2.0, 3.0};
    private static final double[] SHOPPING_FREQ_FACTORS = {0.5, 1.5, 4.0, 8.0};
    private static final double[] RECYCLE_FACTORS = {0, 0.3, 0.7, 1.5};
    private static final double[] PLASTIC_FACTORS = {1.5, 0.8, 0.2, 0};
    private static final double[] ECO_BRAND_FACTORS = {0, 1.0, 0.5};
    private static final double[] COMPOST_FACTORS = {0, 0.5, 0.2};
    private static final double[] DISPOSAL_FACTORS = {0.1, 0.2, 1.0, 0.1};

    private SeekBar screenHoursSeekBar;
    private TextView screenHoursValue;
    private RadioGroup ecoBrandsGroup, shoppingFrequencyGroup, recycleGroup, plasticGroup, compostGroup, disposalGroup;
    private Button submitButton;

    private double annualEmissions = 0;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_othersservey);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : "anonymous";
        dbRef = FirebaseDatabase.getInstance().getReference("surveys");

        // Initialize views
        screenHoursValue = findViewById(R.id.screenHoursValue);
        screenHoursSeekBar = findViewById(R.id.screenHoursSeekBar);
        ecoBrandsGroup = findViewById(R.id.ecoBrandsGroup);
        shoppingFrequencyGroup = findViewById(R.id.shoppingFrequencyGroup);
        recycleGroup = findViewById(R.id.recycleGroup);
        plasticGroup = findViewById(R.id.plasticGroup);
        compostGroup = findViewById(R.id.compostGroup);
        disposalGroup = findViewById(R.id.disposalGroup);
        submitButton = findViewById(R.id.submitButton);

        // Set max to match array bounds
        screenHoursSeekBar.setMax(SCREEN_TIME_FACTORS.length - 1);

        // Setup seekbar listener
        screenHoursSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                screenHoursValue.setText(String.valueOf(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Submit button listener
        submitButton.setOnClickListener(v -> {
            if (validateSelections()) {
                calculateFootprint();
                saveSurveyData();
            } else {
                Toast.makeText(OthersSurveyActivity.this,
                        "Please answer all questions",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateSelections() {
        return ecoBrandsGroup.getCheckedRadioButtonId() != -1 &&
                shoppingFrequencyGroup.getCheckedRadioButtonId() != -1 &&
                recycleGroup.getCheckedRadioButtonId() != -1 &&
                plasticGroup.getCheckedRadioButtonId() != -1 &&
                compostGroup.getCheckedRadioButtonId() != -1 &&
                disposalGroup.getCheckedRadioButtonId() != -1;
    }

    private void calculateFootprint() {
        double weeklyEmissions = 0;  // Weekly emissions in kg CO₂e

        // Screen time - safe access
        int screenHours = screenHoursSeekBar.getProgress();
        if (screenHours >= 0 && screenHours < SCREEN_TIME_FACTORS.length) {
            weeklyEmissions += SCREEN_TIME_FACTORS[screenHours];
        } else {
            weeklyEmissions += SCREEN_TIME_FACTORS[SCREEN_TIME_FACTORS.length - 1];
        }

        // Eco brands
        int ecoBrandId = ecoBrandsGroup.getCheckedRadioButtonId();
        if (ecoBrandId == R.id.yesEcoOption) weeklyEmissions += ECO_BRAND_FACTORS[0];
        else if (ecoBrandId == R.id.noEcoOption) weeklyEmissions += ECO_BRAND_FACTORS[1];
        else if (ecoBrandId == R.id.sometimesEcoOption) weeklyEmissions += ECO_BRAND_FACTORS[2];

        // Shopping frequency
        int shoppingFreqId = shoppingFrequencyGroup.getCheckedRadioButtonId();
        if (shoppingFreqId == R.id.rarelyOption) weeklyEmissions += SHOPPING_FREQ_FACTORS[0];
        else if (shoppingFreqId == R.id.monthlyOption) weeklyEmissions += SHOPPING_FREQ_FACTORS[1];
        else if (shoppingFreqId == R.id.weeklyOption) weeklyEmissions += SHOPPING_FREQ_FACTORS[2];
        else if (shoppingFreqId == R.id.frequentlyOption) weeklyEmissions += SHOPPING_FREQ_FACTORS[3];

        // Recycle
        int recycleId = recycleGroup.getCheckedRadioButtonId();
        if (recycleId == R.id.alwaysRecycleOption) weeklyEmissions += RECYCLE_FACTORS[0];
        else if (recycleId == R.id.oftenRecycleOption) weeklyEmissions += RECYCLE_FACTORS[1];
        else if (recycleId == R.id.sometimesRecycleOption) weeklyEmissions += RECYCLE_FACTORS[2];
        else if (recycleId == R.id.neverRecycleOption) weeklyEmissions += RECYCLE_FACTORS[3];

        // Plastic use
        int plasticId = plasticGroup.getCheckedRadioButtonId();
        if (plasticId == R.id.alwaysPlasticOption) weeklyEmissions += PLASTIC_FACTORS[0];
        else if (plasticId == R.id.oftenPlasticOption) weeklyEmissions += PLASTIC_FACTORS[1];
        else if (plasticId == R.id.sometimesPlasticOption) weeklyEmissions += PLASTIC_FACTORS[2];
        else if (plasticId == R.id.neverPlasticOption) weeklyEmissions += PLASTIC_FACTORS[3];

        // Compost
        int compostId = compostGroup.getCheckedRadioButtonId();
        if (compostId == R.id.yesCompostOption) weeklyEmissions += COMPOST_FACTORS[0];
        else if (compostId == R.id.planningCompostOption) weeklyEmissions += COMPOST_FACTORS[1];
        else if (compostId == R.id.noCompostOption) weeklyEmissions += COMPOST_FACTORS[2];

        // Disposal
        int disposalId = disposalGroup.getCheckedRadioButtonId();
        if (disposalId == R.id.donateOption) weeklyEmissions += DISPOSAL_FACTORS[0];
        else if (disposalId == R.id.recycleOption) weeklyEmissions += DISPOSAL_FACTORS[1];
        else if (disposalId == R.id.throwOption) weeklyEmissions += DISPOSAL_FACTORS[2];
        else if (disposalId == R.id.saleOption) weeklyEmissions += DISPOSAL_FACTORS[3];

        // Convert to tonnes per year: (kg/week * 52) / 1000
        annualEmissions = (weeklyEmissions * 52) / 1000;
    }

    private void saveSurveyData() {
        try {
            // Create survey data object
            OthersSurveyData surveyData = new OthersSurveyData(
                    screenHoursSeekBar.getProgress(),
                    getSelectedRadioText(ecoBrandsGroup),
                    getSelectedRadioText(shoppingFrequencyGroup),
                    getSelectedRadioText(recycleGroup),
                    getSelectedRadioText(plasticGroup),
                    getSelectedRadioText(compostGroup),
                    getSelectedRadioText(disposalGroup),
                    annualEmissions
            );

            // Save to Firebase
            dbRef.child("others").child(userId)
                    .setValue(surveyData)
                    .addOnSuccessListener(aVoid -> {
                        // MARK SURVEY AS COMPLETED
                        FirebaseDatabaseHelper.markSurveyCompleted();

                        updateTotalFootprint();
                        Toast.makeText(this, "Data saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalFootprint() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("surveys").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double total = 0;

                // Sum all survey annual emissions
                if (dataSnapshot.child("home").exists()) {
                    total += getAnnualEmissions(dataSnapshot.child("home"));
                }
                if (dataSnapshot.child("food").exists()) {
                    total += getAnnualEmissions(dataSnapshot.child("food"));
                }
                if (dataSnapshot.child("travel").exists()) {
                    total += getAnnualEmissions(dataSnapshot.child("travel"));
                }
                if (dataSnapshot.child("others").exists()) {
                    total += getAnnualEmissions(dataSnapshot.child("others"));
                }

                // Save total footprint
                userRef.child("total_footprint").setValue(total)
                        .addOnSuccessListener(aVoid -> navigateToOverall())
                        .addOnFailureListener(e -> {
                            Toast.makeText(OthersSurveyActivity.this,
                                    "Total save failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            navigateToOverall();
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OthersSurveyActivity.this,
                        "Data read failed: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                navigateToOverall();
            }
        });
    }

    private double getAnnualEmissions(DataSnapshot snapshot) {
        if (snapshot.child("annualEmissions").exists()) {
            Double value = snapshot.child("annualEmissions").getValue(Double.class);
            return value != null ? value : 0;
        }
        return 0;
    }

    private void navigateToOverall() {
        startActivity(new Intent(this, Overall.class));
        finish();
    }

    private String getSelectedRadioText(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton radioButton = findViewById(selectedId);
            return radioButton.getText().toString();
        }
        return "Not answered";
    }

    // Data model class
    public static class OthersSurveyData {
        public int screenHours;
        public String ecoBrands;
        public String shoppingFrequency;
        public String recycling;
        public String plasticUsage;
        public String composting;
        public String disposalMethod;
        public double annualEmissions;
        public long timestamp;

        public OthersSurveyData() {}  // Required for Firebase

        public OthersSurveyData(int screenHours, String ecoBrands, String shoppingFrequency,
                                String recycling, String plasticUsage, String composting,
                                String disposalMethod, double annualEmissions) {
            this.screenHours = screenHours;
            this.ecoBrands = ecoBrands;
            this.shoppingFrequency = shoppingFrequency;
            this.recycling = recycling;
            this.plasticUsage = plasticUsage;
            this.composting = composting;
            this.disposalMethod = disposalMethod;
            this.annualEmissions = annualEmissions;
            this.timestamp = System.currentTimeMillis();
        }
    }
}