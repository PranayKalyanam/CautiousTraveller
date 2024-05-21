package com.example.cautioustraveller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText email;
    private EditText password;
    private Button actionButton;
    private Button toggleButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean isSigningUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        actionButton = findViewById(R.id.action_button);
        toggleButton = findViewById(R.id.toggle_button);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set initial button texts
        actionButton.setText("Log In");
        toggleButton.setText("Don't have an account? Sign Up");

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();
                if (isSigningUp) {
                    String usernameText = username.getText().toString();
                    signUp(emailText, passwordText, usernameText);
                } else {
                    login(emailText, passwordText);
                }
            }
        });

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSigningUp = !isSigningUp;
                if (isSigningUp) {
                    actionButton.setText("Sign Up");
                    username.setVisibility(View.VISIBLE);
                    toggleButton.setText("Already have an account? Log In");
                } else {
                    actionButton.setText("Log In");
                    username.setVisibility(View.GONE);
                    toggleButton.setText("Don't have an account? Sign Up");
                }
            }
        });
    }

    private void signUp(String email, String password, String username) {
        // Check if email is in valid format
        if (!isValidEmail(email)) {
            Toast.makeText(MainActivity.this, "Invalid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("MainActivity", "createUserWithEmail:success");
                            saveUserData(email, username);
                            openHomePage();
                        } else {
                            Log.w("MainActivity", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("MainActivity", "signInWithEmail:success");
                            openHomePage();
                        } else {
                            Log.w("MainActivity", "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserData(String email, String username) {
        String userId = mAuth.getCurrentUser().getUid();
        User user = new User(email, username);
        db.collection("users").document(userId).set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("MainActivity", "DocumentSnapshot successfully written!");
                        } else {
                            Log.w("MainActivity", "Error writing document", task.getException());
                        }
                    }
                });
    }

    private void openHomePage() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    public static class User {
        public String email;
        public String username;
        public User(String email, String username) {
            this.email = email;
            this.username=username;
        }
    }
}
