package com.example.blooddonor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.blooddonor.R;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private EditText inputName, inputAbout, inputCurrentPass, inputNewPass;
    private com.google.android.material.imageview.ShapeableImageView imgProfile;
    private Button btnSave, btnChangeImage;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private Uri selectedImageUri;

    public AccountFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_account_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputName = view.findViewById(R.id.input_name);
        inputAbout = view.findViewById(R.id.input_about);
        inputCurrentPass = view.findViewById(R.id.input_current_password);
        inputNewPass = view.findViewById(R.id.input_new_password);
        imgProfile = view.findViewById(R.id.img_profile);
        btnSave = view.findViewById(R.id.btn_save_changes);
        btnChangeImage = view.findViewById(R.id.btn_change_image);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadUserProfile();

        btnChangeImage.setOnClickListener(v -> pickImage());
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void loadUserProfile() {
        String userId = mAuth.getUid();
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        inputName.setText(snapshot.getString("name"));
                        inputAbout.setText(snapshot.getString("about"));

                        String imageUrl = snapshot.getString("profilePicUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(getContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .into(imgProfile);
                        }
                    }
                });
    }

    private void saveChanges() {
        String name = inputName.getText().toString().trim();
        String about = inputAbout.getText().toString().trim();
        String currentPass = inputCurrentPass.getText().toString().trim();
        String newPass = inputNewPass.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            inputName.setError("Enter name");
            return;
        }

        String userId = mAuth.getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("about", about);

        firestore.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show());

        if (!TextUtils.isEmpty(currentPass) && !TextUtils.isEmpty(newPass)) {
            reauthenticateAndChangePassword(currentPass, newPass);
        }

        if (selectedImageUri != null) {
            uploadToImgBB(selectedImageUri);
        }
    }

    private void reauthenticateAndChangePassword(String currentPass, String newPass) {
        FirebaseUser user = mAuth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    user.updatePassword(newPass)
                            .addOnSuccessListener(aVoid1 -> Toast.makeText(getContext(), "Password changed", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Password change failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Re-authentication failed", Toast.LENGTH_SHORT).show());
    }

    private void uploadToImgBB(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);

            String apiKey = "f16db61e4ea3216780a7feb7ca4d5a20";
            String url = "https://api.imgbb.com/1/upload?key=" + apiKey;

            RequestQueue queue = Volley.newRequestQueue(requireContext());

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String imageUrl = jsonObject.getJSONObject("data").getString("url");

                            // Save URL to Firestore
                            String userId = FirebaseAuth.getInstance().getUid();
                            FirebaseFirestore.getInstance().collection("users").document(userId)
                                    .update("profilePicUrl", imageUrl)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show());

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Failed to parse ImgBB response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("image", encodedImage);
                    return params;
                }
            };

            queue.add(request);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Image processing failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                imgProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
