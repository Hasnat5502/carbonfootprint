package com.example.carbonfootprint;

import androidx.activity.EdgeToEdge;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ShortWorkActionDone extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shortwalk_done);

        // Button 1: Redirect to Progress page
        Button btnProgress = findViewById(R.id.action_button);
        btnProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShortWorkActionDone.this, Progress.class));
            }
        });

        // Button 2: Redirect to SearchPage
        Button btnSearchPage = findViewById(R.id.action_button2);
        btnSearchPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShortWorkActionDone.this, SearchActivity.class));
            }
        });

        // Footer Image Clicks
        ImageView home = findViewById(R.id.home);
        ImageView search = findViewById(R.id.search);
        ImageView progress = findViewById(R.id.progress);
        ImageView connect = findViewById(R.id.connect);
        ImageView reward = findViewById(R.id.reward);

        home.setOnClickListener(v -> startActivity(new Intent(ShortWorkActionDone.this, MainActivity.class)));
        search.setOnClickListener(v -> startActivity(new Intent(ShortWorkActionDone.this, SearchActivity.class)));
        progress.setOnClickListener(v -> startActivity(new Intent(ShortWorkActionDone.this, Progress.class)));
        connect.setOnClickListener(v -> startActivity(new Intent(ShortWorkActionDone.this, connect.class)));
        reward.setOnClickListener(v -> startActivity(new Intent(ShortWorkActionDone.this, Reward.class)));
    }
}
