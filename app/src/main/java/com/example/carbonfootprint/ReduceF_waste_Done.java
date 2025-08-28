package com.example.carbonfootprint;

import androidx.activity.EdgeToEdge;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ReduceF_waste_Done extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reduce_food_waste_actioncard_done);

        // Footer Navigation
        ImageView home = findViewById(R.id.home);
        ImageView search = findViewById(R.id.search);
        ImageView progress = findViewById(R.id.progress);
        ImageView connect = findViewById(R.id.connect);
        ImageView reward = findViewById(R.id.reward);

        // Home → Main/Home page
        home.setOnClickListener(v -> {
            startActivity(new Intent(ReduceF_waste_Done.this, Main1.class));
        });

        // Search → Search page
        search.setOnClickListener(v -> {
            startActivity(new Intent(ReduceF_waste_Done.this, SearchActivity.class));
        });

        // Progress → Progress page
        progress.setOnClickListener(v -> {
            startActivity(new Intent(ReduceF_waste_Done.this, Progress.class));
        });

        // Connect → Connect page
        connect.setOnClickListener(v -> {
            startActivity(new Intent(ReduceF_waste_Done.this, connect.class));
        });

        // Reward → Reward page
        reward.setOnClickListener(v -> {
            startActivity(new Intent(ReduceF_waste_Done.this, Reward.class));
        });
    }
}
