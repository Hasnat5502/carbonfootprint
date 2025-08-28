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

public class ShortWalkAction extends AppCompatActivity {
    public static final String ACTION_TITLE = "Walk for short distances";
    public static final String ACTION_QUANTITY = "400g";
    public static final String ACTION_POINTS = "5"; // Static points value

    Button btnIDidThis;
    ImageView gifImageView1, gifImageView2, gifImageView3;

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
        setContentView(R.layout.activity_shortwalk);

        // Initialize views
        btnIDidThis = findViewById(R.id.action_button);
        gifImageView1 = findViewById(R.id.gifImageView1);
        gifImageView2 = findViewById(R.id.gifImageView2);
        gifImageView3 = findViewById(R.id.gifImageView3);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Load GIFs
        loadGif(gifImageView1, R.drawable.gif1);
        loadGif(gifImageView2, R.drawable.gif3);
        loadGif(gifImageView3, R.drawable.gif4);

        btnIDidThis.setOnClickListener(v -> {
            addOrUpdateCard(ACTION_TITLE, ACTION_QUANTITY, ACTION_POINTS);
            updateFirebaseData();
        });

        // ---------------- Footer Navigation ----------------
        ImageView home = findViewById(R.id.home);
        ImageView search = findViewById(R.id.search);
        ImageView progress = findViewById(R.id.progress);
        ImageView connect = findViewById(R.id.connect);
        ImageView reward = findViewById(R.id.reward);

        home.setOnClickListener(v ->
                startActivity(new Intent(ShortWalkAction.this, Main1.class)));

        search.setOnClickListener(v ->
                startActivity(new Intent(ShortWalkAction.this, SearchActivity.class)));

        progress.setOnClickListener(v ->
                startActivity(new Intent(ShortWalkAction.this, Progress.class)));

        connect.setOnClickListener(v ->
                startActivity(new Intent(ShortWalkAction.this, connect.class)));

        reward.setOnClickListener(v ->
                startActivity(new Intent(ShortWalkAction.this, Reward.class)));
    }

    private void loadGif(ImageView imageView, int gifResourceId) {
        Glide.with(this)
                .asGif()
                .load(gifResourceId)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    private void addOrUpdateCard(String title, String quantity, String points) {
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
            cardList.add(new CardModel(title, quantity, points, 1));
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
                    user = new User("", 0.4, 5);
                    mutableData.setValue(user);
                    return Transaction.success(mutableData);
                }

                user.setTotalCO2Saved(user.getTotalCO2Saved() + 0.4);
                user.setTaliPoints(user.getTaliPoints() + 5);

                mutableData.setValue(user);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Log.d("Firebase", "Transaction successfully committed");
                    Intent intent = new Intent(ShortWalkAction.this, ShortWorkActionDone.class);
                    startActivity(intent);
                } else {
                    Log.e("Firebase", "Transaction failed: " + databaseError.getMessage());
                    Toast.makeText(ShortWalkAction.this, "Failed to save data. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
