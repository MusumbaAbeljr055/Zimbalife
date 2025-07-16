package com.example.azimbalife.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.azimbalife.Adapter.DateAdapter;
import com.example.azimbalife.Adapter.TimeAdapter;
import com.example.azimbalife.Domain.AppointmentModel;
import com.example.azimbalife.Domain.DoctorsModel;
import com.example.azimbalife.R;
import com.example.azimbalife.Activity.NotificationUtils;
import com.example.azimbalife.databinding.ActivityDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private DoctorsModel item;
    private String selectedDate = null;
    private String selectedTime = null;

    private String userName = "Patient Name";
    private String currentAppointmentId = null;

    private boolean isFavorite = false;
    private static final String PREFS_NAME = "favorites_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        item = (DoctorsModel) getIntent().getSerializableExtra("object");
        if (item == null) {
            Toast.makeText(this, "Doctor data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userName = getIntent().getStringExtra("username");
        if (userName == null) userName = "Patient Name";

        setVariable();
        dateInit();
        timeInit();

        isFavorite = loadFavoriteState();
        updateFavoriteIcon();

        binding.backbtn.setOnClickListener(v -> finish());

        binding.imageView9.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            updateFavoriteIcon();
            saveFavoriteState(isFavorite);

            Toast.makeText(this,
                    isFavorite ? "Added to Favorites" : "Removed from Favorites",
                    Toast.LENGTH_SHORT).show();
        });

        binding.button.setOnClickListener(v -> {
            if (selectedDate == null || selectedTime == null) {
                Toast.makeText(this, "Please select date and time for the appointment.", Toast.LENGTH_SHORT).show();
                return;
            }
            sendAppointmentEmail();
        });
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            binding.imageView9.setImageResource(R.drawable.favorite_red);
        } else {
            binding.imageView9.setImageResource(R.drawable.favorite_white);
        }
    }

    private void saveFavoriteState(boolean state) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(item.getName(), state);
        editor.apply();
    }

    private boolean loadFavoriteState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(item.getName(), false);
    }

    private void setVariable() {
        Glide.with(this)
                .load(item.getPicture())
                .placeholder(R.drawable.person_sharp_icon)
                .into(binding.img);

        binding.addressTxt.setText(item.getAddress());
        binding.nameTxt.setText(item.getName());
        binding.specialTxt.setText(item.getSpecial());
        binding.patiensTxt.setText(String.valueOf(item.getPatiens()));
        binding.experience.setText(item.getExpriense() + " Years");
    }

    private void dateInit() {
        binding.dateView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        DateAdapter dateAdapter = new DateAdapter(generateDate());
        binding.dateView.setAdapter(dateAdapter);
        dateAdapter.setOnItemClickListener(date -> selectedDate = date);
    }

    private void timeInit() {
        binding.timeView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        TimeAdapter timeAdapter = new TimeAdapter(generateTimeSlots());
        binding.timeView.setAdapter(timeAdapter);
        timeAdapter.setOnItemClickListener(time -> selectedTime = time);
    }

    private void sendAppointmentEmail() {
        String doctorEmail = item.getEmail();
        if (doctorEmail == null || doctorEmail.isEmpty()) {
            Toast.makeText(this, "Doctor email not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String subject = "Appointment Request from " + userName;
        String message = "Dear Dr. " + item.getName() + ",\n\n" +
                "You have a new appointment request.\n\n" +
                "Patient: " + userName + "\n" +
                "Specialization: " + item.getSpecial() + "\n" +
                "Date: " + selectedDate + "\n" +
                "Time: " + selectedTime + "\n\n" +
                "Please reply to confirm or reject this appointment.\n\n" +
                "Best regards,\nYour Health App";

        OkHttpClient client = new OkHttpClient();

        // Use exactly your Mailgun API key (copy-paste ONLY the key part starting with "key-")
        String apiKey = "key-70a5e10c0152503d6a0d426b1e8d9c97";  // <-- replace with YOUR exact key

        // Your Mailgun sandbox domain exactly as shown in dashboard
        String domain = "sandbox9532cbe1f35f49f2aed68d94874fccaf.mailgun.org";

        RequestBody formBody = new FormBody.Builder()
                // Use postmaster@domain as sender email
                .add("from", "Appointments <postmaster@" + domain + ">")
                .add("to", doctorEmail)
                .add("subject", subject)
                .add("text", message)
                .build();

        Request request = new Request.Builder()
                .url("https://api.mailgun.net/v3/" + domain + "/messages")
                .header("Authorization", Credentials.basic("api", apiKey))
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(DetailActivity.this, "Failed to send email: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                int statusCode = response.code();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(DetailActivity.this, "Email sent successfully", Toast.LENGTH_SHORT).show();
                        saveAppointmentToFirebase(doctorEmail, userName, selectedDate, selectedTime, item.getName(), item.getSpecial());
                    } else {
                        Toast.makeText(DetailActivity.this,
                                "Email failed: " + statusCode + " " + response.message() + "\n" + responseBody,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }




    private void saveAppointmentToFirebase(String doctorEmail, String patientName, String date, String time, String doctorName, String specialization) {
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        String appointmentId = appointmentsRef.push().getKey();

        if (appointmentId == null) {
            Toast.makeText(this, "Failed to generate appointment ID", Toast.LENGTH_SHORT).show();
            return;
        }

        currentAppointmentId = appointmentId;

        AppointmentModel appointment = new AppointmentModel(
                appointmentId,
                doctorEmail,
                patientName,
                date,
                time,
                doctorName,
                specialization,
                "pending"
        );

        appointmentsRef.child(appointmentId).setValue(appointment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Appointment request saved", Toast.LENGTH_SHORT).show();
                    listenForApproval(appointmentId);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save appointment: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void listenForApproval(String appointmentId) {
        DatabaseReference appointmentStatusRef = FirebaseDatabase.getInstance()
                .getReference("Appointments")
                .child(appointmentId)
                .child("status");

        appointmentStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if (status != null) {
                    if (status.equals("approved")) {
                        NotificationUtils.showNotification(DetailActivity.this, "Appointment Approved",
                                "Your appointment with Dr. " + item.getName() + " has been approved.");
                        Toast.makeText(DetailActivity.this, "Appointment Approved!", Toast.LENGTH_LONG).show();
                        appointmentStatusRef.removeEventListener(this);
                    } else if (status.equals("rejected")) {
                        NotificationUtils.showNotification(DetailActivity.this, "Appointment Rejected",
                                "Your appointment with Dr. " + item.getName() + " has been rejected.");
                        Toast.makeText(DetailActivity.this, "Appointment Rejected!", Toast.LENGTH_LONG).show();
                        appointmentStatusRef.removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public static List<String> generateDate() {
        List<String> date = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE/dd/MMM");
        for (int i = 0; i < 7; i++) {
            date.add(today.plusDays(i).format(formatter));
        }
        return date;
    }

    public static List<String> generateTimeSlots() {
        List<String> timeslots = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        for (int i = 0; i < 24; i += 2) {
            LocalTime time = LocalTime.of(i, 0);
            timeslots.add(time.format(formatter));
        }
        return timeslots;
    }
}
