package com.example.carbonfootprint;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Set the content view to the SignUp layout XML
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        // Initialize the "Log in" TextView to make "Log in" clickable
        TextView loginText = findViewById(R.id.loginText);

        // Set the text with a clickable "Log in" part
        String text = "Have an account? Log in";

        SpannableString spannableString = new SpannableString(text);

        // Set the color of the "Log in" text to #1B4332 (dark green)
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(0xFF1B4332); // Hex color for dark green
        spannableString.setSpan(colorSpan, text.indexOf("Log in"), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Underline the "Log in" text
        UnderlineSpan underlineSpan = new UnderlineSpan();
        spannableString.setSpan(underlineSpan, text.indexOf("Log in"), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Create a ClickableSpan to handle the "Log in" text click
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // Navigate to the login page when "Log in" is clicked
                Intent intent = new Intent(SignUpActivity.this, SignIn.class);
                startActivity(intent);
            }
        };

        // Set the clickable part of the text ("Log in")
        spannableString.setSpan(clickableSpan, text.indexOf("Log in"), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the text on the TextView
        loginText.setText(spannableString);

        // Enable links to be clickable
        loginText.setMovementMethod(LinkMovementMethod.getInstance());

        // Ensure the default text color doesn't override the custom color
        loginText.setTextColor(0xFF000000);  // Set the default color (black) for the non-clickable text part

        // Add a listener to the sign-up button (assuming there's a button for creating the account)
        findViewById(R.id.createAccountButton).setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        // Get user input
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate user input
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the user account with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Account created successfully
                        String userId = mAuth.getCurrentUser().getUid();

                        // Create a User object
                        User newUser = new User(firstName, lastName, email);

                        // Store user data in Firebase Realtime Database
                        mDatabase.child("Users").child(userId).setValue(newUser)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Data stored successfully, navigate to the login screen
                                        Intent intent = new Intent(SignUpActivity.this, SignIn.class);
                                        startActivity(intent);
                                        finish();  // Close the signup screen
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Failed to store user data.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // User class for Firebase Realtime Database
    public static class User {
        public String firstName;
        public String lastName;
        public String email;

        public User() {
            // Default constructor required for Firebase
        }

        public User(String firstName, String lastName, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
    }
}
