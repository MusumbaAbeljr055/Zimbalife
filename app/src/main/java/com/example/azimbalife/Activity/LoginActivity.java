package com.example.azimbalife.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.azimbalife.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText loginUsername, loginPassword;
    Button loginButton;
    TextView signupRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ✅ Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("LoginSession", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        // --- Add this block to set status bar color ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.lavender));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        if (isLoggedIn) {
            String savedUsername = prefs.getString("username", null);
            if (savedUsername != null) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("username", savedUsername);
                startActivity(intent);
                finish();
                return;
            }
        }

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        setupPasswordToggle(loginPassword);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);

        loginButton.setOnClickListener(view -> {
            if (!validateUsername() | !validatePassword()) {
                // Invalid input
            } else {
                checkUser();
            }
        });

        signupRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    public Boolean validateUsername() {
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            loginUsername.setError("Username cannot be empty");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    loginUsername.setError(null);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);

                    if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
                        loginUsername.setError(null);

                        String nameFromDB = snapshot.child(userUsername).child("name").getValue(String.class);
                        String emailFromDB = snapshot.child(userUsername).child("email").getValue(String.class);
                        String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                        // Save login session
                        SharedPreferences.Editor editor = getSharedPreferences("LoginSession", MODE_PRIVATE).edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("username", usernameFromDB);
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("name", nameFromDB);
                        intent.putExtra("email", emailFromDB);
                        intent.putExtra("username", usernameFromDB);
                        intent.putExtra("password", passwordFromDB);
                        startActivity(intent);
                        finish(); // Prevent going back to login
                    } else {
                        loginPassword.setError("Invalid credentials");
                        loginPassword.requestFocus();
                    }
                } else {
                    loginUsername.setError("User does not exist");
                    loginUsername.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(EditText passwordEditText) {
        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2; // right drawable index

            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (passwordEditText.getCompoundDrawables()[DRAWABLE_END] != null) {
                    int drawableWidth = passwordEditText.getCompoundDrawables()[DRAWABLE_END].getBounds().width();
                    float touchAreaStart = passwordEditText.getWidth() - passwordEditText.getPaddingEnd() - drawableWidth;
                    if (event.getX() >= touchAreaStart) {
                        // Toggle password visibility
                        if (passwordEditText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                            // Show password
                            passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_baseline_lock_24,
                                    0,
                                    R.drawable.ic_eye_on,  // Use eye open icon here
                                    0
                            );
                        } else {
                            // Hide password
                            passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_baseline_lock_24,
                                    0,
                                    R.drawable.ic_eye_off,  // Use eye closed icon here
                                    0
                            );
                        }
                        // Move cursor to the end of text after toggle
                        passwordEditText.setSelection(passwordEditText.length());
                        return true; // event handled
                    }
                }
            }
            return false; // let others handle it
        });
    }

}
