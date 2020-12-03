package com.wacked.smarttourist.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.wacked.smarttourist.R;

public class ChooseActivity extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null){
            startActivity(new Intent(ChooseActivity.this, MainMapsActivity.class));
        }
    }

    public void goToSignUp(View view) {
        startActivity(new Intent(ChooseActivity.this, SignUpActivity.class));
    }

    public void goToSignIn(View view) {
        startActivity(new Intent(ChooseActivity.this, SignInActivity.class));
    }

}