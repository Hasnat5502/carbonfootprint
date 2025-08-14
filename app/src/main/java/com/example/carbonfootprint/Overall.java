package com.example.carbonfootprint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;

public class Overall extends AppCompatActivity {

    private static final double MAX_BAR_VALUE = 2.50;
    private TextView homeValue, travelValue, foodValue, othersValue, overallText, overallValue;
    private View homeProgress, travelProgress, foodProgress, othersProgress;
    private double homeFootprint = 0.0;
    private double travelFootprint = 0.0;
    private double foodFootprint = 0.0;
    private double othersFootprint = 0.0;
    private DecimalFormat decimalFormat = new DecimalFormat("#.#");
    private FirebaseAuth mAuth;
    private int categoriesLoaded = 0;
    private final int TOTAL_CATEGORIES = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_overall);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize TextViews
        homeValue = findViewById(R.id.homeValue);
        travelValue = findViewById(R.id.travelValue);
        foodValue = findViewById(R.id.foodValue);
        othersValue = findViewById(R.id.othersValue);
        overallText = findViewById(R.id.overallText);
        overallValue = findViewById(R.id.overallValue);

        // Initialize progress bars
        homeProgress = findViewById(R.id.homeProgressFill);
        travelProgress = findViewById(R.id.travelProgressFill);
        foodProgress = findViewById(R.id.foodProgressFill);
        othersProgress = findViewById(R.id.othersProgressFill);

        // Set default values
        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchFootprintData();
    }

    private void fetchFootprintData() {
        String userId = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : "anonymous";

        // Reset counters and values
        categoriesLoaded = 0;
        homeFootprint = 0.0;
        travelFootprint = 0.0;
        foodFootprint = 0.0;
        othersFootprint = 0.0;

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("surveys");

        // Fetch each category separately
        fetchCategoryData(dbRef.child("home"), userId, "home");
        fetchCategoryData(dbRef.child("travel"), userId, "travel");
        fetchCategoryData(dbRef.child("food"), userId, "food");
        fetchCategoryData(dbRef.child("others"), userId, "others");
    }

    private void fetchCategoryData(DatabaseReference categoryRef, String userId, String categoryName) {
        categoryRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double footprint = 0.0;

                    if (snapshot.child("annualEmissions").exists()) {
                        footprint = getDoubleValue(snapshot.child("annualEmissions"));
                    }
                    else if (snapshot.child("footprint").exists()) {
                        footprint = getDoubleValue(snapshot.child("footprint"));
                    }
                    else if (snapshot.child("carbon_footprint").exists()) {
                        footprint = getDoubleValue(snapshot.child("carbon_footprint"));
                    }
                    else if (snapshot.child("home_footprint").exists()) {
                        footprint = getDoubleValue(snapshot.child("home_footprint"));
                    }

                    // Assign to correct category
                    switch (categoryName) {
                        case "home":
                            homeFootprint = footprint;
                            break;
                        case "travel":
                            travelFootprint = footprint;
                            break;
                        case "food":
                            foodFootprint = footprint;
                            break;
                        case "others":
                            othersFootprint = footprint;
                            break;
                    }

                    Log.d("Firebase", "Loaded " + categoryName + ": " + footprint);
                } else {
                    Log.d("Firebase", "No data for " + categoryName);
                }

                categoriesLoaded++;
                if (categoriesLoaded >= TOTAL_CATEGORIES) {
                    updateUI();
                    updateTotalFootprint();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading " + categoryName + ": " + error.getMessage());
                categoriesLoaded++;
                if (categoriesLoaded >= TOTAL_CATEGORIES) {
                    updateUI();
                }
            }
        });
    }

    private double getDoubleValue(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                Log.e("Conversion", "Cannot convert to double: " + value);
                return 0.0;
            }
        }
        Log.e("Conversion", "Unknown type: " + (value != null ? value.getClass().getSimpleName() : "null"));
        return 0.0;
    }

    private void updateUI() {
        homeValue.setText(formatValue(homeFootprint));
        travelValue.setText(formatValue(travelFootprint));
        foodValue.setText(formatValue(foodFootprint));
        othersValue.setText(formatValue(othersFootprint));

        // Calculate and display total
        double totalTons = homeFootprint + travelFootprint + foodFootprint + othersFootprint;
        overallText.setText("Overall " + formatValue(totalTons));
        overallValue.setText(getImpactDescription(totalTons));

        // Update progress bars after layout is measured
        updateProgressBarWithDelay();
    }

    private void updateTotalFootprint() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        if (!userId.equals("anonymous")) {
            double total = homeFootprint + travelFootprint + foodFootprint + othersFootprint;

            DatabaseReference totalRef = FirebaseDatabase.getInstance().getReference("surveys")
                    .child(userId)
                    .child("total_footprint");

            totalRef.setValue(total);
            Log.d("Firebase", "Updated total_footprint: " + total);
        }
    }

    private String formatValue(double value) {
        return value == 0 ? "0.0" : decimalFormat.format(value);
    }

    private String getImpactDescription(double totalTons) {
        if (totalTons <= 0) {
            return "Complete surveys to calculate your carbon impact";
        }

        int billboards = calculateBillboards(totalTons);
        return String.format("%s tons of CO2e would melt an area\nof arctic sea ice the size of %d %s",
                decimalFormat.format(totalTons),
                billboards,
                billboards == 1 ? "billboard" : "billboards");
    }

    private void updateProgressBarWithDelay() {
        homeProgress.post(() -> {
            updateProgressBar(homeProgress, homeFootprint);
            updateProgressBar(travelProgress, travelFootprint);
            updateProgressBar(foodProgress, foodFootprint);
            updateProgressBar(othersProgress, othersFootprint);
        });
    }

    private void updateProgressBar(View progressBar, double tonsValue) {
        float percentage;

        if (tonsValue <= 0) {
            percentage = 0.02f;
        } else {
            percentage = (float) (tonsValue / MAX_BAR_VALUE);
            if (percentage > 1) percentage = 1.0f;
        }

        ViewGroup parent = (ViewGroup) progressBar.getParent();
        int parentWidth = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        int progressWidth = (int) (parentWidth * percentage);

        ViewGroup.LayoutParams params = progressBar.getLayoutParams();
        params.width = progressWidth;
        progressBar.setLayoutParams(params);
    }

    private int calculateBillboards(double totalTons) {
        if (totalTons <= 0) return 0;

        double squareMeters = totalTons * 3;
        int billboards = (int) Math.ceil(squareMeters / 18);
        return Math.max(1, billboards);
    }

    @Override
    public void onBackPressed() {
        // Call super first to maintain framework behavior
        super.onBackPressed();

        // Add our custom navigation
        startActivity(new Intent(this, Main1.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}