package com.example.carbonfootprint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class connect extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connect); // Make sure footer is in this XML

        // Footer navigation setup
        ImageView home = findViewById(R.id.home);
        ImageView search = findViewById(R.id.search);
        ImageView progress = findViewById(R.id.progress);
        ImageView reward = findViewById(R.id.reward);

        // Home button → redirect to HomeActivity
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(connect.this, Main1.class));
            }
        });

        // Search button → redirect to SearchActivity
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(connect.this, SearchActivity.class));
            }
        });

        // Progress button → redirect to Progress Activity
        progress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(connect.this, Progress.class));
            }
        });

        // Connect button → we are already in Connect, so no need to reload
        ImageView connectBtn = findViewById(R.id.connect); // Make sure you add android:id="@+id/connect" in XML
        if (connectBtn != null) {
            connectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Stay on the same page (or reload if needed)
                }
            });
        }

        // Reward button → redirect to RewardActivity
        reward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(connect.this, Reward.class));
            }
        });
    }
}
