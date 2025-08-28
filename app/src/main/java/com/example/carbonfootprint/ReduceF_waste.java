package com.example.carbonfootprint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
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

public class ReduceF_waste extends AppCompatActivity {

    Button btnIDidThis;
    ImageView btnProgress, gifImageView1, gifImageView2;
    ImageView homeBtn, searchBtn, progressBtn, connectBtn, rewardBtn; // Footer icons
    SharedPreferences sharedPreferences;
    Gson gson = new Gson();

    // Firebase variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private static final String PREF_NAME = "click_pref";
    private static final String CARD_LIST_KEY = "card_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reduce_food_waste_actioncard);

        // Initialize views
        btnIDidThis = findViewById(R.id.action_button);
        btnProgress = findViewById(R.id.progress);
        gifImageView1 = findViewById(R.id.gifImageView1);
        gifImageView2 = findViewById(R.id.gifImageView2);

        // Footer icons
        homeBtn = findViewById(R.id.home);
        searchBtn = findViewById(R.id.search);
        progressBtn = findViewById(R.id.progress);
        connectBtn = findViewById(R.id.connect);
        rewardBtn = findViewById(R.id.reward);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Load GIFs using Glide
        loadGif(gifImageView1, R.drawable.gif1);
        loadGif(gifImageView2, R.drawable.gif2);

        btnIDidThis.setOnClickListener(v -> {
            // Update SharedPreferences
            addOrUpdateCard(
                    "Reduce Food Waste",
                    "400g",
                    "25"
            );

            // Update Firebase with the new values
            updateFirebaseData();
        });

        btnProgress.setOnClickListener(v ->
                startActivity(new Intent(ReduceF_waste.this, Progress.class))
        );

        // ✅ Footer icon navigation (later you can update target activities)
        homeBtn.setOnClickListener(v -> {
            startActivity(new Intent(ReduceF_waste.this, Main1.class));
        });

        searchBtn.setOnClickListener(v -> {
            startActivity(new Intent(ReduceF_waste.this, SearchActivity.class));
        });

        progressBtn.setOnClickListener(v -> {
            startActivity(new Intent(ReduceF_waste.this, Progress.class));
        });

        connectBtn.setOnClickListener(v -> {
            startActivity(new Intent(ReduceF_waste.this, connect.class));
        });

        rewardBtn.setOnClickListener(v -> {
            startActivity(new Intent(ReduceF_waste.this, Reward.class));
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

    private void addOrUpdateCard(String title, String quantity, String count) {
        String json = sharedPreferences.getString(CARD_LIST_KEY, null);
        Type type = new TypeToken<ArrayList<CardModel>>() {}.getType();
        List<CardModel> cardList = json == null ? new ArrayList<>() : gson.fromJson(json, type);

        boolean cardFound = false;
        for (CardModel card : cardList) {
            if (card.getTitle().equals(title)) {
                int newProgress = card.getProgress() + 1;
                if (newProgress <= 4) {
                    card.setProgress(newProgress);
                }
                cardFound = true;
                break;
            }
        }

        if (!cardFound) {
            cardList.add(new CardModel(title, quantity, count, 1));
        }

        sharedPreferences.edit().putString(CARD_LIST_KEY, gson.toJson(cardList)).apply();
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
                    user = new User("", 0.4, 25);
                    mutableData.setValue(user);
                    return Transaction.success(mutableData);
                }

                // Update CO2 saved (convert 400g to kg = 0.4kg)
                user.setTotalCO2Saved(user.getTotalCO2Saved() + 0.4);

                // Update Talipoints
                user.setTaliPoints(user.getTaliPoints() + 25);

                mutableData.setValue(user);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Log.d("Firebase", "Transaction successfully committed");
                    // Redirect to completion page
                    Intent intent = new Intent(ReduceF_waste.this, ReduceF_waste_Done.class);
                    startActivity(intent);
                } else {
                    Log.e("Firebase", "Transaction failed: " + databaseError.getMessage());
                    // Show error message
                    Toast.makeText(ReduceF_waste.this, "Failed to save data. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
