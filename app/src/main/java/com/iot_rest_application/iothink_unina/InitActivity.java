package com.iot_rest_application.iothink_unina;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class InitActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        /* Se non esiste una sessione utente già aperta, gestire il login o la registrazione,
         altrimenti mostrare direttamente gli hub del particolare utente. */
        if(user == null){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    startActivity(new Intent(InitActivity.this, LoginActivity.class));
                }
            }, 1000);
        } else {
            startActivity(new Intent(InitActivity.this, MainActivity.class));
        }
    }
}