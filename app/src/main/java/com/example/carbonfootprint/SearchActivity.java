package com.example.carbonfootprint;

import androidx.activity.EdgeToEdge;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        // Find all card views
        View Card1View = findViewById(R.id.Card1);
        View Card2View = findViewById(R.id.Card2);
        View Card3View = findViewById(R.id.Card3);
        View Card4View = findViewById(R.id.Card4);
        View main1View = findViewById(R.id.main1);


        // Find the progress icon in the footer
        ImageView progressIcon = findViewById(R.id.progress);

        // Card1: Reduce Food Waste
        Card1View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, ReduceF_waste.class));
            }
        });

        // Card2: Short Work Action
        Card2View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, ShortWalkAction.class));
            }
        });

        // Card3: Bottled Water
        Card3View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, BottolWater.class));
            }
        });

        // Card3: Bottled Water
        Card4View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, Clean.class));
            }
        });

        main1View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, Main1.class));
            }
        });



        // Progress Icon: Redirect to Progress activity
        progressIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, Progress.class));
            }
        });
    }
}