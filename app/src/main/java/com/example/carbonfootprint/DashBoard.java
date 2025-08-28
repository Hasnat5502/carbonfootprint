package com.example.carbonfootprint;

import androidx.activity.EdgeToEdge;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashBoard extends AppCompatActivity {

    private TextView userName, monthYear, co2Saved, taliPoints;
    private View homeBar, travelBar, foodBar, othersBar;
    private DatabaseReference databaseRef;
    private String userId;

    private int categoriesLoaded = 0;
    private static final int TOTAL_CATEGORIES = 4;
    private float homeEmission = 0, travelEmission = 0, foodEmission = 0, othersEmission = 0;

    // Fixed bar height values
    private static final float BAR_HEIGHT_FOR_0_5_TONS = 100f;
    private static final float BAR_HEIGHT_FOR_1_0_TONS = 200f;
    private static final float BAR_HEIGHT_FOR_1_5_TONS = 300f;
    private static final float BAR_HEIGHT_FOR_2_0_TONS = 400f;
    private static final float BAR_HEIGHT_FOR_2_5_TONS = 500f;
    private static final float BAR_HEIGHT_FOR_3_0_TONS = 600f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        userName = findViewById(R.id.userName);
        monthYear = findViewById(R.id.monthYear);
        co2Saved = findViewById(R.id.co2Saved);
        taliPoints = findViewById(R.id.taliPoints);
        homeBar = findViewById(R.id.homeBar);
        travelBar = findViewById(R.id.travelBar);
        foodBar = findViewById(R.id.foodBar);
        othersBar = findViewById(R.id.othersBar);
        ImageButton backButton = findViewById(R.id.backButton);
        ImageButton settingsButton = findViewById(R.id.settingsButton);

        // Set static username
        userName.setText("Shihab");

        // Set current month/year
        setCurrentDate();

        // Firebase initialization
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Button listeners
        backButton.setOnClickListener(v -> finish());
        settingsButton.setOnClickListener(v -> openSettings());

        // Load user data with real-time updates (excluding name)
        loadUserData();

        // Load emission data
        loadEmissionData();
    }

    private void setCurrentDate() {
        String currentDate = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date());
        monthYear.setText(currentDate);
    }

    private void loadUserData() {
        // Use ValueEventListener for real-time updates (only CO2 and points, not name)
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Removed the firstName fetch here
                    Double co2Value = dataSnapshot.child("totalCO2Saved").getValue(Double.class);
                    Integer taliValue = dataSnapshot.child("taliPoints").getValue(Integer.class);

                    co2Saved.setText(String.format("%.2f kg ", co2Value != null ? co2Value : 0.0));
                    taliPoints.setText(String.valueOf(taliValue != null ? taliValue : 0));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "User data error: " + databaseError.getMessage());
            }
        });
    }

    // The rest of the code remains unchanged...
    private void loadEmissionData() {
        DatabaseReference surveysRef = FirebaseDatabase.getInstance().getReference("surveys");

        // Home emissions
        surveysRef.child("home").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("annualEmissions")) {
                    homeEmission = snapshot.child("annualEmissions").getValue(Float.class);
                    Log.d("Dashboard", "Home: " + homeEmission + " tons");
                }
                checkAllCategoriesLoaded();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Home error: " + error.getMessage());
                checkAllCategoriesLoaded();
            }
        });

        // Travel emissions
        surveysRef.child("travel").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("annualEmissions")) {
                    travelEmission = snapshot.child("annualEmissions").getValue(Float.class);
                    Log.d("Dashboard", "Travel: " + travelEmission + " tons");
                }
                checkAllCategoriesLoaded();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Travel error: " + error.getMessage());
                checkAllCategoriesLoaded();
            }
        });

        // Food emissions
        surveysRef.child("food").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("annualEmissions")) {
                    foodEmission = snapshot.child("annualEmissions").getValue(Float.class);
                    Log.d("Dashboard", "Food: " + foodEmission + " tons");
                }
                checkAllCategoriesLoaded();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Food error: " + error.getMessage());
                checkAllCategoriesLoaded();
            }
        });

        // Others emissions
        surveysRef.child("others").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("annualEmissions")) {
                    othersEmission = snapshot.child("annualEmissions").getValue(Float.class);
                    Log.d("Dashboard", "Others: " + othersEmission + " tons");
                }
                checkAllCategoriesLoaded();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Others error: " + error.getMessage());
                checkAllCategoriesLoaded();
            }
        });
    }

    private void checkAllCategoriesLoaded() {
        categoriesLoaded++;
        if (categoriesLoaded >= TOTAL_CATEGORIES) {
            updateChart();
        }
    }

    private void updateChart() {
        Log.d("Dashboard", "Updating chart with values: " +
                "Home=" + homeEmission + " tons, " +
                "Travel=" + travelEmission + " tons, " +
                "Food=" + foodEmission + " tons, " +
                "Others=" + othersEmission + " tons");

        // Set bar heights directly based on exact emission values
        setBarHeight(homeBar, homeEmission);
        setBarHeight(travelBar, travelEmission);
        setBarHeight(foodBar, foodEmission);
        setBarHeight(othersBar, othersEmission);
    }

    private void setBarHeight(View bar, float tons) {
        float heightDp;

        // Map emission values directly to bar heights
        if (tons <= 0.5f) {
            heightDp = BAR_HEIGHT_FOR_0_5_TONS * (tons / 0.5f);
        } else if (tons <= 1.0f) {
            heightDp = BAR_HEIGHT_FOR_1_0_TONS * (tons / 1.0f);
        } else if (tons <= 1.5f) {
            heightDp = BAR_HEIGHT_FOR_1_5_TONS * (tons / 1.5f);
        } else if (tons <= 2.0f) {
            heightDp = BAR_HEIGHT_FOR_2_0_TONS * (tons / 2.0f);
        } else if (tons <= 2.5f) {
            heightDp = BAR_HEIGHT_FOR_2_5_TONS * (tons / 2.5f);
        } else {
            heightDp = BAR_HEIGHT_FOR_3_0_TONS * (tons / 3.0f);
        }

        // Ensure minimum height for visibility
        if (heightDp < 10) heightDp = 10;

        // Cap at maximum height
        if (heightDp > BAR_HEIGHT_FOR_3_0_TONS) heightDp = BAR_HEIGHT_FOR_3_0_TONS;

        // Set the bar height
        bar.getLayoutParams().height = (int) heightDp;
        bar.requestLayout();

        Log.d("Dashboard", "Bar set to " + heightDp + "dp for " + tons + " tons");
    }

    private void openSettings() {
        // Implement settings activity
    }
}