package com.example.azimbalife.Activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.azimbalife.Adapter.TopDoctorAdapter;
import com.example.azimbalife.Domain.DoctorsModel;
import com.example.azimbalife.R;
import com.example.azimbalife.databinding.ActivityAllDoctorsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllDoctorsActivity extends AppCompatActivity {

    ActivityAllDoctorsBinding binding;
    List<DoctorsModel> doctorsList = new ArrayList<>();
    TopDoctorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllDoctorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set status bar color to match toolbar blue
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(0);
            }
        }

        // Toolbar setup
        binding.topAppBar.setTitle("Available Doctors");
        binding.topAppBar.setTitleTextColor(getResources().getColor(android.R.color.black));
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // RecyclerView layout (vertical scroll)
        binding.allDoctorRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set adapter
        adapter = new TopDoctorAdapter(doctorsList, this);
        binding.allDoctorRecyclerView.setAdapter(adapter);

        // Load doctor data
        loadAllDoctors();
    }

    private void loadAllDoctors() {
        binding.progressBarAll.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Doctors");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorsList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    DoctorsModel doctor = snap.getValue(DoctorsModel.class);
                    if (doctor != null) {
                        doctorsList.add(doctor);
                    }
                }
                adapter.notifyDataSetChanged();
                binding.progressBarAll.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarAll.setVisibility(View.GONE);
                Toast.makeText(AllDoctorsActivity.this, "Error loading doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
