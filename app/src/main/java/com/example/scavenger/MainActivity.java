package com.example.scavenger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_main);
        Intent results = new Intent(MainActivity.this,Results.class);
        TextView location = findViewById(R.id.use_my_location_button);
        location.setOnClickListener((View v)->{
                startActivity(results);
                });

    }
}
