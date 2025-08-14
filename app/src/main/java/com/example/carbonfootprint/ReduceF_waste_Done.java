package com.example.carbonfootprint;

import androidx.activity.EdgeToEdge;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ReduceF_waste_Done extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reduce_food_waste_actioncard_done);

        // Button 1: Redirect to Progress page
        Button btnProgress = findViewById(R.id.action_button);
        btnProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReduceF_waste_Done.this, Progress.class));
            }
        });

        // Button 2: Redirect to SearchPage
        Button btnSearchPage = findViewById(R.id.action_button2);
        btnSearchPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReduceF_waste_Done.this, SearchActivity.class));
            }
        });


    }
}