package com.example.azimbalife.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.azimbalife.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditProfileActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int GALLERY_REQUEST_CODE = 1002;

    EditText editName, editEmail, editUsername, editPassword;
    Button saveButton;
    ImageView profileImageView, editImageButton;

    String nameUser, emailUser, usernameUser, passwordUser;
    DatabaseReference reference;
    StorageReference storageReference;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        reference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        saveButton = findViewById(R.id.saveButton);
        profileImageView = findViewById(R.id.profileImageView);
        editImageButton = findViewById(R.id.editImageButton);

        setupPasswordToggle(editPassword);
        showData();
        loadProfileImage();

        editImageButton.setOnClickListener(v -> showImagePickerDialog());

        saveButton.setOnClickListener(view -> {
            boolean textChanged = isNameChanged() || isPasswordChanged() || isEmailChanged();

            if (textChanged || imageUri != null) {
                if (imageUri != null) {
                    uploadImage(); // Upload image and update DB
                } else {
                    Toast.makeText(EditProfileActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                }
            } else {
                Toast.makeText(EditProfileActivity.this, "No Changes Found", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                } else {
                    openCamera();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, GALLERY_REQUEST_CODE);
                    } else {
                        openGallery();
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_REQUEST_CODE);
                    } else {
                        openGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // important!

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                imageUri = data.getData();
                Glide.with(this).load(imageUri).circleCrop().into(profileImageView);
            } else if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                if (data.getExtras() != null) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    profileImageView.setImageBitmap(photo);
                    // You can save this bitmap to file if you want to upload later
                    imageUri = null; // no Uri in this case
                }
            }
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference fileRef = storageReference.child(usernameUser + ".jpg");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        progressDialog.dismiss();
                        reference.child(usernameUser).child("imageUrl").setValue(uri.toString());
                        Toast.makeText(EditProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progressPercent = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progressPercent + "%");
                    });
        }
    }

    private void loadProfileImage() {
        reference.child(usernameUser).child("imageUrl").get().addOnSuccessListener(dataSnapshot -> {
            String imageUrl = dataSnapshot.getValue(String.class);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).circleCrop().into(profileImageView);
            }
        });
    }

    private boolean isNameChanged() {
        if (!nameUser.equals(editName.getText().toString())) {
            reference.child(usernameUser).child("name").setValue(editName.getText().toString());
            nameUser = editName.getText().toString();
            return true;
        }
        return false;
    }

    private boolean isEmailChanged() {
        if (!emailUser.equals(editEmail.getText().toString())) {
            reference.child(usernameUser).child("email").setValue(editEmail.getText().toString());
            emailUser = editEmail.getText().toString();
            return true;
        }
        return false;
    }

    private boolean isPasswordChanged() {
        if (!passwordUser.equals(editPassword.getText().toString())) {
            reference.child(usernameUser).child("password").setValue(editPassword.getText().toString());
            passwordUser = editPassword.getText().toString();
            return true;
        }
        return false;
    }

    public void showData() {
        Intent intent = getIntent();
        nameUser = intent.getStringExtra("name");
        emailUser = intent.getStringExtra("email");
        usernameUser = intent.getStringExtra("username");
        passwordUser = intent.getStringExtra("password");

        editName.setText(nameUser);
        editEmail.setText(emailUser);
        editUsername.setText(usernameUser);
        editPassword.setText(passwordUser);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(EditText passwordEditText) {
        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (passwordEditText.getCompoundDrawables()[DRAWABLE_END] != null) {
                    int drawableWidth = passwordEditText.getCompoundDrawables()[DRAWABLE_END].getBounds().width();
                    float touchAreaStart = passwordEditText.getWidth() - passwordEditText.getPaddingEnd() - drawableWidth;
                    if (event.getX() >= touchAreaStart) {
                        if (passwordEditText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                            passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, R.drawable.ic_eye_on, 0);
                        } else {
                            passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, R.drawable.ic_eye_off, 0);
                        }
                        passwordEditText.setSelection(passwordEditText.length());
                        return true;
                    }
                }
            }
            return false;
        });
    }
}
