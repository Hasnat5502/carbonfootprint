package com.example.carbonfootprint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class ShortWalkAction extends AppCompatActivity {
    public static final String ACTION_TITLE = "Walk for short distances";
    public static final String ACTION_QUANTITY = "400g";
    public static final String ACTION_POINTS = "5"; // Static points value

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "click_pref";
    private static final String CARD_LIST_KEY = "card_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shortwalk);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Load GIFs using Glide
        loadGif(R.id.gifImageView1, R.drawable.gif1);
        loadGif(R.id.gifImageView2, R.drawable.gif3);
        loadGif(R.id.gifImageView3, R.drawable.gif4);

        Button actionButton = findViewById(R.id.action_button);
        actionButton.setOnClickListener(v -> {
            addOrUpdateCard(
                    ACTION_TITLE,
                    ACTION_QUANTITY,
                    ACTION_POINTS
            );
            startActivity(new Intent(ShortWalkAction.this, ShortWorkActionDone.class));
            finish(); // Close current activity
        });
    }

    private void loadGif(int imageViewId, int gifResourceId) {
        ImageView imageView = findViewById(imageViewId);
        Glide.with(this)
                .asGif()
                .load(gifResourceId)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    private void addOrUpdateCard(String title, String quantity, String points) {
        String json = sharedPreferences.getString(CARD_LIST_KEY, null);
        Type type = new TypeToken<ArrayList<CardModel>>() {}.getType();
        List<CardModel> cardList = json == null ? new ArrayList<>() : new Gson().fromJson(json, type);

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
            cardList.add(new CardModel(title, quantity, points, 1));
        }

        sharedPreferences.edit().putString(CARD_LIST_KEY, new Gson().toJson(cardList)).apply();
    }
}