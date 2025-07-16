package com.example.azimbalife.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.azimbalife.Adapter.CategoryAdapter;
import com.example.azimbalife.Adapter.TopDoctorAdapter;
import com.example.azimbalife.R;
import com.example.azimbalife.ViewModel.MainViewModel;
import com.example.azimbalife.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private ImageView logoutIcon;
    private String username; // current username

    // Auto-scroll variables
    private int currentDoctorPosition = 0;
    private boolean autoScrollEnabled = true;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;

    // Notification bell badge
    private TextView notificationBadge;
    private ImageView bellIcon;
    private int notificationCount = 5; // example initial count

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Find views for notification badge and bell
        notificationBadge = findViewById(R.id.notificationBadge);
        bellIcon = findViewById(R.id.imageView3);

        // Setup bell badge initial state
        updateBadge();

        // Bell icon click listener
        bellIcon.setOnClickListener(v -> {
            // Play built-in notification sound
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ringtone.play();

            // Show toast near bell icon
            showBadgeMessage(v);

            // Reset notification count and update badge
            notificationCount = 0;
            updateBadge();
        });

        // Get username passed via Intent or fallback
        username = getIntent().getStringExtra("username");
        if (username == null || username.isEmpty()) {
            username = "zimbalife"; // fallback username if needed
        }

        viewModel = new MainViewModel();

        loadUserProfile(username);
        initCategory();
        initTopDoctors();

        // Refresh Home Button
        binding.home.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Refreshing Home...", Toast.LENGTH_SHORT).show();
            if (binding.scrollView2 != null) {
                binding.scrollView2.post(() -> binding.scrollView2.fullScroll(View.FOCUS_UP));
            }
            initCategory();
            initTopDoctors();
        });

        // Profile Button
        binding.profileEdit.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        binding.textView4.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllDoctorsActivity.class);
            startActivity(intent);
        });

        binding.chat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);


        });

        // Logout
        logoutIcon = findViewById(R.id.logoutIcon);
        logoutIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences("LoginSession", MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void updateBadge() {
        if (notificationCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(String.valueOf(notificationCount));
        } else {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText("No");
        }
    }

    private void showBadgeMessage(View anchorView) {
        Toast toast = Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT);

        // Get location on screen of bell icon
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        int xOffset = location[0];
        int yOffset = location[1] - anchorView.getHeight();

        toast.setGravity(Gravity.TOP | Gravity.START, xOffset, yOffset);
        toast.show();
    }

    private void loadUserProfile(String username) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(username);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String usernameDb = snapshot.child("username").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    if (usernameDb != null) {
                        binding.textView.setText("Hi " + usernameDb);
                    }

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(MainActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.person_sharp_icon)
                                .circleCrop()
                                .into(binding.imageView2);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initTopDoctors() {
        binding.progressBarDoctor.setVisibility(View.VISIBLE);

        viewModel.loadDoctors().observe(this, doctorList -> {
            if (doctorList != null && !doctorList.isEmpty()) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                binding.doctorView.setLayoutManager(layoutManager);
                TopDoctorAdapter adapter = new TopDoctorAdapter(doctorList, this);
                binding.doctorView.setAdapter(adapter);

                // Auto-scroll setup
                startAutoScrollDoctors(doctorList.size());
            } else {
                binding.doctorView.setAdapter(null);
            }
            binding.progressBarDoctor.setVisibility(View.GONE);
        });
    }

    private void startAutoScrollDoctors(int itemCount) {
        currentDoctorPosition = 0;
        autoScrollHandler = new Handler();

        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!autoScrollEnabled || itemCount == 0) return;

                if (currentDoctorPosition >= itemCount) {
                    currentDoctorPosition = 0;
                }

                binding.doctorView.smoothScrollToPosition(currentDoctorPosition);
                currentDoctorPosition++;

                autoScrollHandler.postDelayed(this, 2000); // Scroll every 2 seconds
            }
        };

        autoScrollHandler.postDelayed(autoScrollRunnable, 2000);
    }

    private void initCategory() {
        binding.progressBarCat.setVisibility(View.VISIBLE);

        viewModel.loadCategory().observe(this, categoryList -> {
            if (categoryList != null && !categoryList.isEmpty()) {
                binding.cartView.setLayoutManager(
                        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                );
                binding.cartView.setAdapter(new CategoryAdapter(categoryList));
            } else {
                binding.cartView.setAdapter(null);
            }
            binding.progressBarCat.setVisibility(View.GONE);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoScrollEnabled = false;
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }
}
