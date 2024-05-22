package com.example.cautioustraveller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateFragment extends Fragment {
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 1001;

    private EditText captionEditText;
    private EditText locationEditText; // New location input field
    private ImageView imageView;
    private Button takePhotoButton, uploadButton;
    private Uri selectedImageUri;
    private String currentPhotoPath;

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        captionEditText = view.findViewById(R.id.caption_edit_text);
        locationEditText = view.findViewById(R.id.location_edit_text); // Initialize the location input field
        imageView = view.findViewById(R.id.image_view);
        takePhotoButton = view.findViewById(R.id.take_photo_button);
        uploadButton = view.findViewById(R.id.upload_button);

        // Set up the take photo button click listener
        takePhotoButton.setOnClickListener(v -> requestCameraPermission());

        // Set up the upload button click listener
        uploadButton.setOnClickListener(v -> {
            String caption = captionEditText.getText().toString();
            String location = locationEditText.getText().toString(); // Get the location input
            uploadImageToFirebase(caption, location); // Pass location to the upload method
        });

        // Initialize Firebase Storage and Firestore
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firestore = FirebaseFirestore.getInstance();

        setupCameraPermissionLauncher();
        setupTakePhotoLauncher();

        return view;
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "Error occurred while creating the file", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            selectedImageUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.cautioustraveller.fileprovider",
                    photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
            takePhotoLauncher.launch(intent);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setupCameraPermissionLauncher() {
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(requireContext(), "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupTakePhotoLauncher() {
        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK) {
                        File f = new File(currentPhotoPath);
                        selectedImageUri = Uri.fromFile(f);
                        imageView.setImageURI(selectedImageUri);
                    }
                }
        );
    }

    private void uploadImageToFirebase(String caption, String location) { // Updated method signature
        if (selectedImageUri != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference fileReference = storageReference.child("posts/" + userId + "/" + selectedImageUri.getLastPathSegment());
            fileReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        savePostToFirestore(caption, location, imageUrl); // Pass location to Firestore method
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "Please select an image to upload", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePostToFirestore(String caption, String location, String imageUrl) { // Updated method signature
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get reference to the user document
        DocumentReference userRef = firestore.collection("users").document(userId);

        // Retrieve the username from the user document
        userRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        // Create the post object with username
                        Map<String, Object> post = new HashMap<>();
                        post.put("userId", userId);
                        post.put("username", username); // Add username to post
                        post.put("caption", caption);
                        post.put("location", location); // Add location to post
                        post.put("imageUrl", imageUrl);

                        // Add the post to the "posts" collection
                        firestore.collection("posts")
                                .add(post)
                                .addOnSuccessListener(documentReference -> {
                                    String postId = documentReference.getId(); // Get the ID of the newly created post

                                    // Update the "posts" array field of the user document
                                    userRef.update("posts", FieldValue.arrayUnion(postId))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(requireContext(), "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                                                // Optionally clear the UI elements after successful upload
                                                captionEditText.setText("");
                                                locationEditText.setText(""); // Clear location input
                                                imageView.setImageURI(null);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(requireContext(), "Failed to update user document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Failed to save post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(requireContext(), "User document does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to retrieve user document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
