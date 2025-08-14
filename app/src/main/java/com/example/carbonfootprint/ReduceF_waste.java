package com.example.carbonfootprint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReduceF_waste extends AppCompatActivity {

    Button btnIDidThis;
    ImageView btnProgress, gifImageView1, gifImageView2;
    SharedPreferences sharedPreferences;
    Gson gson = new Gson();

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

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Load GIFs using Glide
        loadGif(gifImageView1, R.drawable.gif1);  // Replace with your actual GIF resource
        loadGif(gifImageView2, R.drawable.gif2);  // Replace with your actual GIF resource

        btnIDidThis.setOnClickListener(v -> {
            addOrUpdateCard(
                    "Reduce Food Waste",
                    "400g",
                    "25"
            );
            // Redirect to Done page after updating data
            startActivity(new Intent(ReduceF_waste.this, ReduceF_waste_Done.class));
        });

        btnProgress.setOnClickListener(v ->
                startActivity(new Intent(ReduceF_waste.this, Progress.class))
        );
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
}