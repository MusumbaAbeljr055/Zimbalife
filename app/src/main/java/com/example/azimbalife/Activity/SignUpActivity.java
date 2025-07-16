package com.example.azimbalife.Activity;



import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.azimbalife.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    EditText signupName, signupUsername, signupEmail, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        setupPasswordToggle(signupPassword);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupButton = findViewById(R.id.signup_button);

        signupButton.setOnClickListener(view -> {
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("Users"); // Use "Users" with capital U

            String name = signupName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String username = signupUsername.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();

            // --- Add this block to set status bar color ---
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.lavender));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }

            if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.contains(".") || username.contains("#") || username.contains("$") ||
                    username.contains("[") || username.contains("]")) {
                Toast.makeText(SignUpActivity.this, "Username contains invalid characters", Toast.LENGTH_LONG).show();
                return;
            }

            Helper helperClass = new Helper(name, email, username, password);

            reference.child(username).setValue(helperClass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });



        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
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