package com.example.carbonfootprint;

import androidx.activity.EdgeToEdge;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Clean extends AppCompatActivity {

    Button btnIDidThis;
    ImageView gifImageView1, gifImageView2;
    TextView quantityText, pointsText, habitTracker;
    SharedPreferences sharedPreferences;
    Gson gson = new Gson();

    // Firebase variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private static final String PREF_NAME = "click_pref";
    private static final String CARD_LIST_KEY = "card_list";
    private static final String ACTION_TITLE = "Take part in a local clean-up";
    private static final String ACTION_QUANTITY = "500g";
    private static final String ACTION_POINTS = "20";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_clean);

        // Footer Image Clicks
        ImageView home = findViewById(R.id.home);
        ImageView search = findViewById(R.id.search);
        ImageView progress = findViewById(R.id.progress);
        ImageView connect = findViewById(R.id.connect);
        ImageView reward = findViewById(R.id.reward);

        home.setOnClickListener(v -> startActivity(new Intent(Clean.this, Main1.class)));
        search.setOnClickListener(v -> startActivity(new Intent(Clean.this, SearchActivity.class)));
        progress.setOnClickListener(v -> startActivity(new Intent(Clean.this, Progress.class)));
        connect.setOnClickListener(v -> startActivity(new Intent(Clean.this, connect.class)));
        reward.setOnClickListener(v -> startActivity(new Intent(Clean.this, Reward.class)));

        // Initialize views
        btnIDidThis = findViewById(R.id.action_button);
        gifImageView1 = findViewById(R.id.gifImageView1);
        gifImageView2 = findViewById(R.id.gifImageView2);
        quantityText = findViewById(R.id.quantity_text);
        pointsText = findViewById(R.id.points_text);
        habitTracker = findViewById(R.id.habit_tracker);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Set static UI values
        quantityText.setText(ACTION_QUANTITY);
        pointsText.setText("+" + ACTION_POINTS);
        habitTracker.setText("Track this 5 times to form a habit");

        // Load GIFs using Glide
        loadGif(gifImageView1, R.drawable.gif5);
        loadGif(gifImageView2, R.drawable.gif3);

        btnIDidThis.setOnClickListener(v -> {
            // Update SharedPreferences
            saveCardData();

            // Update Firebase with the new values
            updateFirebaseData();
        });
    }

    // Helper method to load GIFs
    private void loadGif(ImageView imageView, int gifResourceId) {
        Glide.with(this)
                .asGif()
                .load(gifResourceId)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    private void saveCardData() {
        String json = sharedPreferences.getString(CARD_LIST_KEY, null);
        Type type = new TypeToken<ArrayList<CardModel>>(){}.getType();
        List<CardModel> cardList = json == null ? new ArrayList<>() : gson.fromJson(json, type);

        boolean found = false;
        for (CardModel card : cardList) {
            if (card.getTitle().equals(ACTION_TITLE)) {
                int currentProgress = card.getProgress();
                if (currentProgress < 4) {
                    card.setProgress(currentProgress + 1);
                }
                found = true;
                break;
            }
        }

        if (!found) {
            cardList.add(new CardModel(
                    ACTION_TITLE,
                    ACTION_QUANTITY,
                    ACTION_POINTS,
                    1  // Initial progress
            ));
        }

        sharedPreferences.edit()
                .putString(CARD_LIST_KEY, gson.toJson(cardList))
                .apply();
    }

    private void updateFirebaseData() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = mDatabase.child("Users").child(userId);

        userRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                User user = mutableData.getValue(User.class);
                if (user == null) {
                    // Create new user if doesn't exist
                    user = new User("", 0.5, 20); // 0.5kg CO2 saved, 20 points
                    mutableData.setValue(user);
                    return Transaction.success(mutableData);
                }

                // Update CO2 saved (convert 500g to kg = 0.5kg)
                user.setTotalCO2Saved(user.getTotalCO2Saved() + 0.5);

                // Update Talipoints
                user.setTaliPoints(user.getTaliPoints() + 20);

                mutableData.setValue(user);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Log.d("Firebase", "Transaction successfully committed");
                    // Redirect to completion page
                    Intent intent = new Intent(Clean.this, CleanDone.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    Log.e("Firebase", "Transaction failed: " + databaseError.getMessage());
                    // Show error message
                    Toast.makeText(Clean.this, "Failed to save data. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}