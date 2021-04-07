package com.wacked.smarttourist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wacked.smarttourist.R;

import java.util.ArrayList;

public class SignUpActivity extends AppCompatActivity {


    private static final String TAG = "SignUpActivity";

    private TextInputLayout emailUP;
    private TextInputLayout passwordUP;
    private TextInputLayout password_confUP;
    private Button signUpButton;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailUP = findViewById(R.id.SignUpTextInputEmail);
        passwordUP = findViewById(R.id.SignUpTextInputPassword);
        password_confUP = findViewById(R.id.SignUpTextInputConfirmPassword);
        signUpButton = findViewById(R.id.loginSignUpButton);

        auth = FirebaseAuth.getInstance();

    }


    private boolean validatePassw() {
        String passInput = passwordUP.getEditText().getText().toString().trim();
        String passConf = password_confUP.getEditText().getText().toString().trim();

        if (passConf.isEmpty()) {
            password_confUP.setError("Please, confirm Your password.");
        }

        if (passInput.isEmpty()) {
            passwordUP.setError("Please, input Your password.");

            return false;
        } else if (passInput.length() < 6) {
            passwordUP.setError("Password needs to be more than 6 characters length!");
            return false;

        } else if (!passInput.equals(passConf)) {
            String string = "Passwords does not match!";
            Toast toast = new Toast(this);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setText(string);
            toast.show();
            emailUP.setError("");
            password_confUP.setError("Not match.");
            return true;
        } else {
            emailUP.setError("");
            passwordUP.setError("");
            return true;
        }

    }


    private boolean validateEmail() {
        String emailInput = emailUP.getEditText().getText().toString().trim();

        if (emailInput.isEmpty()) {
            emailUP.setError("Please, input Your e-mail.");
            return false;
        } else {
            emailUP.setError("");
            return true;
        }

    }

    public void signUpUser(View view) {


        if (!validateEmail() | !validatePassw()) {
            return;
        }

        ArrayList<String> signUpInput = new ArrayList<>();
        signUpInput.add(emailUP.getEditText().getText().toString().trim());
        signUpInput.add(passwordUP.getEditText().getText().toString().trim());


        auth.createUserWithEmailAndPassword(signUpInput.get(0), signUpInput.get(1))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        startActivity(new Intent(SignUpActivity.this, MainMapsActivity.class));
                        FirebaseUser user = auth.getCurrentUser();
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();

                    }


                });
    }
}