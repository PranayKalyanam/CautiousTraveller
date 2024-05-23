package com.example.cautioustraveller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class FeedFragment extends Fragment {

    private LinearLayout feedLayout;
    private ProgressBar loadingProgressBar;
    private FirebaseFirestore firestore;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        loadPosts();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        feedLayout = view.findViewById(R.id.feed_layout);
        loadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        loadingProgressBar.setVisibility(View.VISIBLE); // Show progress bar immediately
        firestore = FirebaseFirestore.getInstance();

        return view;
    }



    private void loadPosts() {
        firestore.collection("posts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        loadingProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                String username = document.getString("username");
                                String caption = document.getString("caption");
                                String imageUrl = document.getString("imageUrl");
                                String location = document.getString("location");

                                View postView = getLayoutInflater().inflate(R.layout.item_post, null);
                                TextView userNameTextView = postView.findViewById(R.id.username_text_view);
                                TextView captionTextView = postView.findViewById(R.id.caption_text_view);
                                TextView locationTextView = postView.findViewById(R.id.location_text_view);
                                ImageView postImageView = postView.findViewById(R.id.post_image_view);

                                if (username != null) {
                                    userNameTextView.setText(username);
                                }
                                if (caption != null) {
                                    captionTextView.setText(caption);
                                }
                                if (imageUrl != null) {
                                    new LoadImageTask(postImageView).execute(imageUrl);
                                }
                                if (location != null) {
                                    locationTextView.setText(location);
                                }

                                feedLayout.addView(postView);
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load posts: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        LoadImageTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String imageUrl = strings[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
