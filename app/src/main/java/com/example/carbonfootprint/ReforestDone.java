package com.example.carbonfootprint;

import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ReforestDone extends AppCompatActivity {

    private TextView tvSpentPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reforestdone);

        tvSpentPoints = findViewById(R.id.tv_spent_points);

        // Get spent points from intent
        int spentPoints = getIntent().getIntExtra("spent_points", 0);

        // Show it in the TextView
        tvSpentPoints.setText(String.valueOf(spentPoints));
    }
}
