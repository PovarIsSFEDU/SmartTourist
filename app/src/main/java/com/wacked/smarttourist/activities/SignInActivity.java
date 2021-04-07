package com.wacked.smarttourist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wacked.smarttourist.R;

import java.util.ArrayList;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private TextInputLayout email;
    private TextInputLayout password;
    private Button signInButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        email = findViewById(R.id.SignInTextInputEmail);
        password = findViewById(R.id.SignInTextInputPassword);
        signInButton = findViewById(R.id.loginSignInButton);
        auth = FirebaseAuth.getInstance();

    }

    private boolean validatePassw() {
        String passw = password.getEditText().getText().toString().trim();

        if (passw.isEmpty()) {
            password.setError("Please, input Your password.");
            return false;
        } else if (passw.length() < 6) {
            password.setError("Password needs to be more than 6 characters length!");
            return false;
        } else {
            password.setError("");
            return true;
        }

    }

    private boolean validateEmail() {
        String emailInput = email.getEditText().getText().toString().trim();

        if (emailInput.isEmpty()) {
            email.setError("Please, input Your e-mail.");
            return false;
        } else {
            email.setError("");
            return true;
        }

    }

    public void signInUser(View view) {

        if (!validateEmail() | !validatePassw()) {
            return;
        }

        ArrayList<String> signInInput = new ArrayList<>();
        signInInput.add(email.getEditText().getText().toString().trim());
        signInInput.add(password.getEditText().getText().toString().trim());


        auth.signInWithEmailAndPassword(signInInput.get(0), signInInput.get(1))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            startActivity(new Intent(SignInActivity.this, MainMapsActivity.class));
                            FirebaseUser user = auth.getCurrentUser();
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                });
    }
}