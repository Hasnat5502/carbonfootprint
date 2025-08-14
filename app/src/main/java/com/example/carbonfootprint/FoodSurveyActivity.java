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

public class FoodSurveyActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // UI Elements
    private RadioGroup meatFrequencyGroup, vegetarianDaysGroup, foodPurchaseGroup,
            organicProduceGroup, eatOutFrequencyGroup, foodWasteGroup, reusableGroup;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodservey); // Use the specified XML layout name

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        meatFrequencyGroup = findViewById(R.id.meatFrequencyGroup);
        vegetarianDaysGroup = findViewById(R.id.vegetarianDaysGroup);
        foodPurchaseGroup = findViewById(R.id.foodPurchaseGroup);
        organicProduceGroup = findViewById(R.id.organicProduceGroup);
        eatOutFrequencyGroup = findViewById(R.id.eatOutFrequencyGroup);
        foodWasteGroup = findViewById(R.id.foodWasteGroup);
        reusableGroup = findViewById(R.id.reusableGroup);
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
        return meatFrequencyGroup.getCheckedRadioButtonId() != -1 &&
                vegetarianDaysGroup.getCheckedRadioButtonId() != -1 &&
                foodPurchaseGroup.getCheckedRadioButtonId() != -1 &&
                organicProduceGroup.getCheckedRadioButtonId() != -1 &&
                eatOutFrequencyGroup.getCheckedRadioButtonId() != -1 &&
                foodWasteGroup.getCheckedRadioButtonId() != -1 &&
                reusableGroup.getCheckedRadioButtonId() != -1;
    }

    // Emission calculation helper methods
    private double getMeatFrequencyEmission() {
        int selectedId = meatFrequencyGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.neverMeatOption) return 0.0;
        if (selectedId == R.id.onceTwiceOption) return 0.8;
        if (selectedId == R.id.threeFourOption) return 1.6;
        if (selectedId == R.id.fivePlusOption) return 3.0;
        return 0;
    }

    private double getVegetarianDaysEmission() {
        int selectedId = vegetarianDaysGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.zeroDaysOption) return 2.5;
        if (selectedId == R.id.oneTwoDaysOption) return 1.8;
        if (selectedId == R.id.threeFiveDaysOption) return 1.0;
        if (selectedId == R.id.sixSevenDaysOption) return 0.3;
        return 0;
    }

    private double getFoodPurchaseEmission() {
        int selectedId = foodPurchaseGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.localMarketsOption) return 0.9;
        if (selectedId == R.id.supermarketsOption) return 1.3;
        if (selectedId == R.id.importedStoresOption) return 2.0;
        if (selectedId == R.id.onlineGroceryOption) return 1.7;
        return 0;
    }

    private double getOrganicProduceEmission() {
        int selectedId = organicProduceGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.alwaysOrganicOption) return 0.0;
        if (selectedId == R.id.sometimesOrganicOption) return 0.4;
        if (selectedId == R.id.rarelyOrganicOption) return 0.8;
        if (selectedId == R.id.neverOrganicOption) return 1.2;
        return 0;
    }

    private double getEatOutFrequencyEmission() {
        int selectedId = eatOutFrequencyGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.neverEatOutOption) return 0.0;
        if (selectedId == R.id.onceTwiceEatOutOption) return 0.6;
        if (selectedId == R.id.threeFiveEatOutOption) return 1.5;
        if (selectedId == R.id.moreThanFiveOption) return 3.0;
        return 0;
    }

    private double getFoodWasteEmission() {
        int selectedId = foodWasteGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.noWasteOption) return 0.0;
        if (selectedId == R.id.littleWasteOption) return 0.5;
        if (selectedId == R.id.someWasteOption) return 1.2;
        if (selectedId == R.id.lotWasteOption) return 2.5;
        return 0;
    }

    private double getReusableEmission() {
        int selectedId = reusableGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.alwaysReusableOption) return 0.0;
        if (selectedId == R.id.sometimesReusableOption) return 0.2;
        if (selectedId == R.id.rarelyReusableOption) return 0.5;
        if (selectedId == R.id.neverReusableOption) return 1.0;
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
        String meatFrequency = getSelectedRadioText(meatFrequencyGroup);
        String vegetarianDays = getSelectedRadioText(vegetarianDaysGroup);
        String foodPurchase = getSelectedRadioText(foodPurchaseGroup);
        String organicProduce = getSelectedRadioText(organicProduceGroup);
        String eatOutFrequency = getSelectedRadioText(eatOutFrequencyGroup);
        String foodWaste = getSelectedRadioText(foodWasteGroup);
        String reusable = getSelectedRadioText(reusableGroup);

        // Calculate emissions (kg CO₂e per week)
        double meatEmission = getMeatFrequencyEmission();
        double vegetarianEmission = getVegetarianDaysEmission();
        double purchaseEmission = getFoodPurchaseEmission();
        double organicEmission = getOrganicProduceEmission();
        double eatOutEmission = getEatOutFrequencyEmission();
        double wasteEmission = getFoodWasteEmission();
        double reusableEmission = getReusableEmission();

        // Calculate totals
        double weeklyEmissions = meatEmission + vegetarianEmission + purchaseEmission +
                organicEmission + eatOutEmission + wasteEmission + reusableEmission;
        double annualEmissions = (weeklyEmissions * 52) / 1000; // Convert to metric tons/year

        // Create survey data object with emissions
        SurveyData surveyData = new SurveyData(
                meatFrequency, vegetarianDays, foodPurchase, organicProduce,
                eatOutFrequency, foodWaste, reusable,
                meatEmission, vegetarianEmission, purchaseEmission,
                organicEmission, eatOutEmission, wasteEmission, reusableEmission,
                weeklyEmissions, annualEmissions
        );

        // Save to Firebase
        mDatabase.child("surveys").child("food").child(userId)
                .setValue(surveyData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Redirect to Others Survey (create this activity next)
                        startActivity(new Intent(FoodSurveyActivity.this, OthersSurveyActivity.class));
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
        public String meatFrequency;
        public String vegetarianDays;
        public String foodPurchase;
        public String organicProduce;
        public String eatOutFrequency;
        public String foodWaste;
        public String reusableContainers;

        // Emission values (kg CO₂e/week)
        public double meatEmission;
        public double vegetarianEmission;
        public double purchaseEmission;
        public double organicEmission;
        public double eatOutEmission;
        public double wasteEmission;
        public double reusableEmission;

        // Totals
        public double weeklyEmissions;   // kg CO₂e/week
        public double annualEmissions;   // metric tons CO₂e/year

        public long timestamp = System.currentTimeMillis();

        public SurveyData() {}  // Required for Firebase

        public SurveyData(String meatFrequency, String vegetarianDays, String foodPurchase,
                          String organicProduce, String eatOutFrequency, String foodWaste,
                          String reusableContainers,
                          double meatEmission, double vegetarianEmission, double purchaseEmission,
                          double organicEmission, double eatOutEmission, double wasteEmission,
                          double reusableEmission,
                          double weeklyEmissions, double annualEmissions) {
            // User answers
            this.meatFrequency = meatFrequency;
            this.vegetarianDays = vegetarianDays;
            this.foodPurchase = foodPurchase;
            this.organicProduce = organicProduce;
            this.eatOutFrequency = eatOutFrequency;
            this.foodWaste = foodWaste;
            this.reusableContainers = reusableContainers;

            // Emission values
            this.meatEmission = meatEmission;
            this.vegetarianEmission = vegetarianEmission;
            this.purchaseEmission = purchaseEmission;
            this.organicEmission = organicEmission;
            this.eatOutEmission = eatOutEmission;
            this.wasteEmission = wasteEmission;
            this.reusableEmission = reusableEmission;

            // Totals
            this.weeklyEmissions = weeklyEmissions;
            this.annualEmissions = annualEmissions;
        }
    }
}