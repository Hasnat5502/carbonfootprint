package com.example.carbonfootprint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Progress extends AppCompatActivity {
    LinearLayout cardContainer;
    SharedPreferences sharedPreferences;
    Gson gson = new Gson();
    public static final String PREF_NAME = "click_pref";
    public static final String CARD_LIST_KEY = "card_list";
    public static final String HABIT_LIST_KEY = "habit_list";
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_progress);

        View Card1View = findViewById(R.id.card1);
        View main1View = findViewById(R.id.main1);
        View btnMyHabitsView = findViewById(R.id.btnMyHabits);

        // Footer icons
        ImageView connectIcon = findViewById(R.id.connect);
        ImageView rewardIcon = findViewById(R.id.reward);

        btnMyHabitsView.setOnClickListener(v ->
                startActivity(new Intent(Progress.this, Habit.class))
        );

        main1View.setOnClickListener(v ->
                startActivity(new Intent(Progress.this, Main1.class))
        );

        Card1View.setOnClickListener(v ->
                startActivity(new Intent(Progress.this, SearchActivity.class))
        );

        // ✅ Footer click listeners
        connectIcon.setOnClickListener(v ->
                startActivity(new Intent(Progress.this, connect.class))
        );

        rewardIcon.setOnClickListener(v ->
                startActivity(new Intent(Progress.this, Reward.class))
        );

        cardContainer = findViewById(R.id.cardContainer);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Simulate loading with 1 second delay
            new Handler().postDelayed(() -> {
                transferCompletedCards(); // Transfer completed cards
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });

        loadCards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCards();
    }

    private void loadCards() {
        cardContainer.removeAllViews();
        String json = sharedPreferences.getString(CARD_LIST_KEY, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<CardModel>>() {}.getType();
            List<CardModel> cardList = gson.fromJson(json, type);

            for (int i = 0; i < cardList.size(); i++) {
                CardModel card = cardList.get(i);
                View cardView = getLayoutInflater().inflate(R.layout.card_design, null);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                if (i < cardList.size() - 1) {
                    params.bottomMargin = dpToPx(20);
                }

                cardView.setLayoutParams(params);

                TextView title = cardView.findViewById(R.id.cardTitle);
                TextView quantity = cardView.findViewById(R.id.cardQuantity);
                TextView count = cardView.findViewById(R.id.cardCount);
                SeekBar slider = cardView.findViewById(R.id.cardSlider);

                title.setText(card.getTitle());
                quantity.setText(card.getQuantity());
                count.setText("+" + card.getCount());
                slider.setMax(4);
                slider.setProgress(card.getProgress());

                slider.setTag(card.getId());

                slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        updateCardProgress((String) seekBar.getTag(), progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                cardContainer.addView(cardView);
            }
        }
    }

    private void updateCardProgress(String cardId, int progress) {
        List<CardModel> cardList = getCurrentProgressList();
        for (CardModel card : cardList) {
            if (card.getId().equals(cardId)) {
                card.setProgress(progress);
                break;
            }
        }
        saveCardList(cardList);
    }

    private void transferCompletedCards() {
        List<CardModel> progressList = getCurrentProgressList();
        List<CardModel> habitList = getHabitList();

        for (CardModel card : new ArrayList<>(progressList)) {
            if (card.getProgress() == 4) {
                progressList.remove(card);
                card.setProgress(4);
                habitList.add(card);
            }
        }

        saveCardList(progressList);
        saveHabitList(habitList);

        loadCards();
    }

    private List<CardModel> getCurrentProgressList() {
        String json = sharedPreferences.getString(CARD_LIST_KEY, null);
        Type type = new TypeToken<ArrayList<CardModel>>() {}.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    private List<CardModel> getHabitList() {
        String json = sharedPreferences.getString(HABIT_LIST_KEY, null);
        Type type = new TypeToken<ArrayList<CardModel>>() {}.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    private void saveCardList(List<CardModel> list) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CARD_LIST_KEY, gson.toJson(list));
        editor.apply();
    }

    private void saveHabitList(List<CardModel> list) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HABIT_LIST_KEY, gson.toJson(list));
        editor.apply();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
