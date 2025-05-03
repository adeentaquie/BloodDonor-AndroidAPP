package com.example.blooddonor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput, phoneInput, cityInput;
    private Button registerBtn;
    private TextView loginLink;
    private RadioGroup roleGroup;
    private RadioButton roleDonor, roleRecipient;
    private Spinner bloodGroupSpinner;
    private LinearLayout donorFieldsLayout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Bind views
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        phoneInput = findViewById(R.id.phone_input);
        cityInput = findViewById(R.id.city_input);
        bloodGroupSpinner = findViewById(R.id.spinner_blood_group);
        donorFieldsLayout = findViewById(R.id.donor_fields_layout);

        registerBtn = findViewById(R.id.register_btn);
        loginLink = findViewById(R.id.login_link);
        roleGroup = findViewById(R.id.role_group);
        roleDonor = findViewById(R.id.role_donor);
        roleRecipient = findViewById(R.id.role_recipient);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Set up spinner
        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_groups, android.R.layout.simple_spinner_item);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupSpinner.setAdapter(bloodAdapter);

        // Toggle donor fields visibility
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.role_donor) {
                donorFieldsLayout.setVisibility(View.VISIBLE);
            } else {
                donorFieldsLayout.setVisibility(View.GONE);
            }
        });

        registerBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String role = roleDonor.isChecked() ? "donor" : roleRecipient.isChecked() ? "recipient" : "";

            if (name.isEmpty()) {
                showToast("Please enter your name");
                return;
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Invalid email");
                return;
            }
            if (password.length() < 6) {
                showToast("Password must be at least 6 characters");
                return;
            }
            if (role.isEmpty()) {
                showToast("Please select a role");
                return;
            }

            // Donor-specific validation
            String phone = phoneInput.getText().toString().trim();
            String city = cityInput.getText().toString().trim();
            String bloodGroup = bloodGroupSpinner.getSelectedItem().toString();
            if ("donor".equals(role)) {
                if (phone.isEmpty() || city.isEmpty()) {
                    showToast("Please complete donor details");
                    return;
                }
            }

            // Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String userId = mAuth.getCurrentUser().getUid();

                        // Get FCM token first
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> {
                                    String token = task.isSuccessful() ? task.getResult() : "";

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", name);
                                    user.put("email", email);
                                    user.put("role", role);
                                    user.put("fcmToken", token);

                                    if ("donor".equals(role)) {
                                        user.put("phone", phone);
                                        user.put("city", city);
                                        user.put("bloodGroup", bloodGroup);
                                    }

                                    firestore.collection("users").document(userId)
                                            .set(user)
                                            .addOnSuccessListener(aVoid -> {
                                                showToast("Registration successful! Please log in.");
                                                startActivity(new Intent(this, LoginActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> showToast("Error: " + e.getMessage()));
                                });

                    })
                    .addOnFailureListener(e -> showToast("Registration failed: " + e.getMessage()));
        });

        loginLink.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
