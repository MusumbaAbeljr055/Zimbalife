package com.example.azimbalife.Activity;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.azimbalife.Adapter.DoctorsAdapter;
import com.example.azimbalife.Domain.DoctorsModel;
import com.example.azimbalife.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DoctorsListActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerViewDoctors;
    private DoctorsAdapter adapter;
    private List<DoctorsModel> doctorsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors_list);

        searchView = findViewById(R.id.searchView);
        recyclerViewDoctors = findViewById(R.id.recyclerViewDoctors);
        recyclerViewDoctors.setLayoutManager(new LinearLayoutManager(this));

        doctorsList = new ArrayList<>();
        adapter = new DoctorsAdapter(doctorsList);
        recyclerViewDoctors.setAdapter(adapter);

        loadDoctorsFromFirebase();

        // Setup search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });

        adapter.setOnItemClickListener(doctor -> {
            // Handle item click: e.g., open doctor detail activity
            Toast.makeText(this, "Clicked: " + doctor.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadDoctorsFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Doctors");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    DoctorsModel doctor = ds.getValue(DoctorsModel.class);
                    if (doctor != null) {
                        doctorsList.add(doctor);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DoctorsListActivity.this, "Failed to load doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
