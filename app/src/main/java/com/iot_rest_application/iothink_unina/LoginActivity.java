package com.iot_rest_application.iothink_unina;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iot_rest_application.iothink_unina.utilities.FirebaseHelper;
import com.iot_rest_application.iothink_unina.utilities.RestRequest;

import java.util.concurrent.ExecutionException;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText email;
    private EditText pass;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.reg_button).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email_text);
        pass = findViewById(R.id.pass_text);

    }

    private void createAccountNormal() {
        if (email.getText().toString().isEmpty() || pass.getText().toString().isEmpty() || pass.getText().toString().length()<6) {
            if(email.getText().toString().isEmpty() || pass.getText().toString().isEmpty() )
                Toast.makeText(getApplicationContext(), R.string.error_log, Toast.LENGTH_SHORT).show();
            else Toast.makeText(getApplicationContext(), R.string.pwd_short, Toast.LENGTH_SHORT).show();

        } else {
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.i("Mount:reg", "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(LoginActivity.this,R.string.registration_success,
                                                            Toast.LENGTH_SHORT).show();
                                                    Log.d("Firebase Mail:", "Email sent.");
                                                }
                                            }
                                        });

                            } else {
                                // If sign up fails, display a message to the user.
                                Log.i("IoThink registration", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this,R.string.registration_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void signInNormal() {
        if (email.getText().toString().isEmpty() || pass.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.error_log, Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                final String uid = currentUser.getUid();
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference db = firebaseDatabase.getReference("users");

                                db.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(!snapshot.child(uid).exists()){
                                            System.out.println("****DEBUG**** NEW USER");
                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            DatabaseReference myRef = database.getReference("users/");
                                            myRef.child(uid).setValue(0);

                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        }else{
                                            System.out.println("****DEBUG**** OLD USER");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Mount:sing", "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, R.string.do_registration,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //finishAffinity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                signInNormal();
                break;
            case R.id.reg_button:
                createAccountNormal();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}

