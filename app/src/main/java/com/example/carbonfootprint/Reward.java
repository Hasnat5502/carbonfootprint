package com.example.carbonfootprint;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Reward extends AppCompatActivity {

    private TextView taliPointsTextView;
    private DatabaseReference databaseRef;
    private String userId;
    private CardView card1;  // declare the card

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reward);

        // Card1 click
        View card1 = findViewById(R.id.card1);
        card1.setOnClickListener(v -> {
            startActivity(new Intent(this, Reforest.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });// reward.xml

        // Initialize views
        taliPointsTextView = findViewById(R.id.taliPoints);
        card1 = findViewById(R.id.card1);

        // Card click → Reforest page
        card1.setOnClickListener(v -> {
            Intent intent = new Intent(Reward.this, Reforest.class);
            startActivity(intent);
        });

        // Firebase initialization
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Load Tailpoints data
        loadTailpointsData();
    }

    private void loadTailpointsData() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer taliValue = dataSnapshot.child("taliPoints").getValue(Integer.class);
                    taliPointsTextView.setText(taliValue != null ? String.valueOf(taliValue) : "0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Tailpoints data error: " + databaseError.getMessage());
                taliPointsTextView.setText("0");
                Toast.makeText(Reward.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
