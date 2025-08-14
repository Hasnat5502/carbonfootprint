package com.example.carbonfootprint;
import androidx.activity.EdgeToEdge;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CleanDone extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_clean_done);

        Button actionButton = findViewById(R.id.action_button);
        Button actionButton2 = findViewById(R.id.action_button2);

        // Button 1: Navigate to Progress activity
        actionButton.setOnClickListener(v -> {
            startActivity(new Intent(this, Progress.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish(); // Close current activity if needed
        });

        // Button 2: Navigate to Search activity
        actionButton2.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish(); // Close current activity if needed
        });
    }
}