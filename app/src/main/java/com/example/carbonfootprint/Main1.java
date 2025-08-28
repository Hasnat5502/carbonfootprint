package com.example.carbonfootprint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Main1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main1);

        View connect = findViewById(R.id.connect);
        connect.setOnClickListener(v -> {
            startActivity(new Intent(this, connect.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Card1 click
        View card1 = findViewById(R.id.card1);
        card1.setOnClickListener(v -> {
            startActivity(new Intent(this, ReduceF_waste.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Card2 click
        View card2 = findViewById(R.id.Card2);
        card2.setOnClickListener(v -> {
            startActivity(new Intent(this, ShortWalkAction.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Card3 click
        CardView card3 = findViewById(R.id.card3);
        card3.setOnClickListener(v -> {
            startActivity(new Intent(this, BottolWater.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Card4 click
        View card4 = findViewById(R.id.card4);
        card4.setOnClickListener(v -> {
            startActivity(new Intent(this, Clean.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : "anonymous";
        DatabaseReference userRef = database.getReference("users").child(userId);

        // Inner layout click (Survey Card)
        LinearLayout innerLayout = findViewById(R.id.innerLinearLayout);
        innerLayout.setOnClickListener(v -> {
            // Check survey completion status
            userRef.child("survey_completed").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getValue(Boolean.class)) {
                                // Survey completed - go to Overall Dashboard
                                startActivity(new Intent(Main1.this, Overall.class));
                            } else {
                                // Survey not completed - start survey flow
                                startActivity(new Intent(Main1.this, TakeServey.class));
                            }
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // On error, default to starting survey
                            startActivity(new Intent(Main1.this, TakeServey.class));
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    }
            );
        });

        // User image click
        View userImage2 = findViewById(R.id.userImage2);
        userImage2.setOnClickListener(v -> {
            startActivity(new Intent(this, DashBoard.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Search icon click
        ImageView searchIcon = findViewById(R.id.hehe);
        searchIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Progress icon click
        ImageView progressIcon = findViewById(R.id.progress);
        progressIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, Progress.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Reward icon click - NEW CODE ADDED HERE
        ImageView rewardIcon = findViewById(R.id.Reward);
        rewardIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, Reward.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}