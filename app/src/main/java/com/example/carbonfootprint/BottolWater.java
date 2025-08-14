package com.example.carbonfootprint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BottolWater extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "click_pref";
    private static final String CARD_LIST_KEY = "card_list";
    private static final String ACTION_TITLE = "Drink Tap water instead of bottled";
    private static final String ACTION_QUANTITY = "200g";
    private static final String ACTION_POINTS = "+5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bottlewater);

        Button actionButton = findViewById(R.id.action_button);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Set static habit tracker message
        TextView habitTracker = findViewById(R.id.habit_tracker);
        habitTracker.setText("Track this 5 times to form a habit");

        // Set static points value
        TextView streakCountText = findViewById(R.id.streak_count_text);
        streakCountText.setText(ACTION_POINTS);

        // Load GIFs using Glide
        ImageView gifImageView1 = findViewById(R.id.gifImageView1);
        ImageView gifImageView2 = findViewById(R.id.gifImageView2);

        // Replace R.drawable.gif1 and R.drawable.gif3 with your actual GIF resources
        loadGif(gifImageView1, R.drawable.gif6);
        loadGif(gifImageView2, R.drawable.gif2);

        actionButton.setOnClickListener(v -> {
            saveCardData();
            startActivity(new Intent(BottolWater.this, BottolWaterDone.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
        List<CardModel> cardList = json == null ? new ArrayList<>() : new Gson().fromJson(json, type);

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
                .putString(CARD_LIST_KEY, new Gson().toJson(cardList))
                .apply();
    }
}