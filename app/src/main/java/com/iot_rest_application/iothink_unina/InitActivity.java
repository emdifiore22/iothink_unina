package com.iot_rest_application.iothink_unina;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.iot_rest_application.iothink_unina.utilities.FirebaseHelper;

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
            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful()){
                        String idToken = task.getResult().getToken();
                        String uid = user.getUid();
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

                        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
                        firebaseHelper.setDb(db);
                        firebaseHelper.setIdToken(idToken);
                        firebaseHelper.setUid(uid);

                        startActivity(new Intent(InitActivity.this, MainActivity.class));
                    }
                }
            });
        }
    }
}