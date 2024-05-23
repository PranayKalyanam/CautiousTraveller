package com.example.cautioustraveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize the fragment manager
        fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the default selected item to Feed
        bottomNavigationView.setSelectedItemId(R.id.navigation_feed);

        // Load the FeedFragment by default
        if (savedInstanceState == null) {
            loadFragment(new FeedFragment());
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_feed) {
                    selectedFragment = new FeedFragment();
                } else if (itemId == R.id.navigation_create) {
                    selectedFragment = new CreateFragment();
                } else if (itemId == R.id.navigation_search) {
                    selectedFragment = new MapFragment();
                }
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }
                return true;
            }
        });

        // Handle logout button click
        ImageButton logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the logout action
                mAuth.signOut();
                showLogoutSnackbar(v);

                // Redirect to login activity after a short delay
                logoutButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }, 1500); // 1.5 seconds delay
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    private void showLogoutSnackbar(View view) {
        Snackbar.make(view, "Logout successful", Snackbar.LENGTH_SHORT).show();
    }
}
