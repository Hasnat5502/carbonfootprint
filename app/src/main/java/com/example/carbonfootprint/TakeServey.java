package com.example.carbonfootprint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TakeServey extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_takeservey);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : "anonymous";
        DatabaseReference userRef = database.getReference("users").child(userId);

        // Check if survey is already completed
        userRef.child("survey_completed").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue(Boolean.class)) {
                            // Survey completed - redirect to Overall Dashboard
                            startActivity(new Intent(TakeServey.this, Overall.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Do nothing - stay on survey screen
                    }
                }
        );

        // Home card click
        CardView homeCard = findViewById(R.id.homeCard);
        homeCard.setOnClickListener(v -> {
            startActivity(new Intent(TakeServey.this, HomeServey.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}