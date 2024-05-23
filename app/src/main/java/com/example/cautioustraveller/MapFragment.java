package com.example.cautioustraveller;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapFragment extends Fragment {

    List<String> originalItemNames = new ArrayList<>();
    List<String> originalItemImages = new ArrayList<>();
    List<String> originalItemLocations = new ArrayList<>();
    List<String> filteredItemNames = new ArrayList<>();
    List<String> filteredItemImages = new ArrayList<>();
    List<String> filteredItemLocations = new ArrayList<>();
    private FirebaseFirestore firestore;
    RecyclerView rv;
    SearchAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        rv = view.findViewById(R.id.menuRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        setupSearchView(view);
        retrieveItems();
        return view;
    }

    private void setupSearchView(View view) {
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterItems(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterItems(newText);
                return true;
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterItems(String query) {
        filteredItemNames.clear();
        filteredItemImages.clear();
        filteredItemLocations.clear();

        for (int i = 0; i < originalItemNames.size(); i++) {
            if (originalItemLocations.get(i).toLowerCase().trim().contains(query.toLowerCase().trim())) {
                filteredItemNames.add(originalItemNames.get(i));
                filteredItemImages.add(originalItemImages.get(i));
                filteredItemLocations.add(originalItemLocations.get(i));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void retrieveItems() {

        firestore.collection("posts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String username = document.getString("username");
                            String imageUrl = document.getString("imageUrl");
                            String location = document.getString("location");
                            if (username != null && imageUrl != null && location != null) {
                                originalItemNames.add(username);
                                originalItemImages.add(imageUrl);
                                originalItemLocations.add(location);
                            }
                        }
                        showAllItems();
                    } else {
                        Toast.makeText(getContext(), "Failed to load posts: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAllItems() {
        filteredItemNames.clear();
        filteredItemImages.clear();
        filteredItemLocations.clear();
        filteredItemNames.addAll(originalItemNames);
        filteredItemImages.addAll(originalItemImages);
        filteredItemLocations.addAll(originalItemLocations);

        setupAdapter();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupAdapter() {
        if (adapter == null) {
            adapter = new SearchAdapter(filteredItemNames, filteredItemImages, filteredItemLocations, getContext());
            rv.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
}
