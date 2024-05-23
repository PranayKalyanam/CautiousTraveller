package com.example.cautioustraveller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private final List<String> itemNames;
    private final List<String> itemImages;
    private final List<String> itemLocations;
    private final Context context;

    public SearchAdapter(List<String> itemNames, List<String> itemImages, List<String> itemLocations, Context context) {
        this.itemNames = itemNames;
        this.itemImages = itemImages;
        this.itemLocations = itemLocations;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.search_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return itemNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        TextView locationTextView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameSearch);
            locationTextView = itemView.findViewById(R.id.locationSearch);
            imageView = itemView.findViewById(R.id.imageView);
        }

        public void bind(int position) {
            userNameTextView.setText(itemNames.get(position));
            locationTextView.setText(itemLocations.get(position));
            if (itemImages.get(position) != null) {
                new LoadImageTask(imageView).execute(itemImages.get(position));
            }
        }
    }

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        LoadImageTask(ImageView imageView) {
            this.imageViewReference = new WeakReference<>(imageView);
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
