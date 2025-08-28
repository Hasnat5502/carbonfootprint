package com.example.carbonfootprint;

import androidx.activity.EdgeToEdge;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class CleanDone extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_clean_done);

        // Buttons
        Button actionButton = findViewById(R.id.action_button);
        Button actionButton2 = findViewById(R.id.action_button2);

        // Button 1: Navigate to Progress activity
        actionButton.setOnClickListener(v -> {
            startActivity(new Intent(this, Progress.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        // Button 2: Navigate to Search activity
        actionButton2.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        // Footer Navigation
        ImageView home = findViewById(R.id.home);
        ImageView search = findViewById(R.id.search);
        ImageView progress = findViewById(R.id.progress);
        ImageView connect = findViewById(R.id.connect);
        ImageView reward = findViewById(R.id.reward);

        home.setOnClickListener(v -> {
            startActivity(new Intent(this, Main1.class)); // Replace with your Home Activity
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        search.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        progress.setOnClickListener(v -> {
            startActivity(new Intent(this, Progress.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        connect.setOnClickListener(v -> {
            startActivity(new Intent(this, connect.class)); // Replace with your Connect page
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        reward.setOnClickListener(v -> {
            startActivity(new Intent(this, Reward.class)); // Replace with your Reward page
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}
