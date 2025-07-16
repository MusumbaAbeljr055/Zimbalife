package com.example.azimbalife.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.example.azimbalife.R;
import com.example.azimbalife.databinding.ActivitySplashBinding;

public class Splash extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // Make sure the progress bar is initially hidden
        binding.progressBar.setVisibility(View.GONE);

        binding.startBtn.setOnClickListener(v -> {
            // Show progress bar
            binding.progressBar.setVisibility(View.VISIBLE);

            // Hide the start button to prevent multiple taps
            binding.startBtn.setVisibility(View.GONE);

            // Delay 1 second before opening MainActivity
            new Handler().postDelayed(() -> {
                setTheme(R.style.Theme_AZimbalife);
                startActivity(new Intent(Splash.this, LoginActivity.class));
                finish();
            }, 1000); // 1 second delay
        });
    }
}

