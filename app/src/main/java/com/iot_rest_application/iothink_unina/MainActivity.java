package com.iot_rest_application.iothink_unina;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.iot_rest_application.iothink_unina.utilities.FirebaseHelper;
import com.iot_rest_application.iothink_unina.utilities.centralina.CentralinaAdapter;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private CentralinaAdapter adapter;
    private RecyclerView rv;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_main);
        System.out.println("*****DEBUG***** MAIN ACTIVITY ON CREATE.");

        //SETUP RECYCLER VIEW
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("*****DEBUG***** MAIN ACTIVITY ON START.");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userUid = currentUser.getUid();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        firebaseHelper = new FirebaseHelper(db, userUid);
        //ADAPTER
        try {
            adapter = new CentralinaAdapter(this, firebaseHelper.retrieve_hub());

            if(adapter.getItemCount() == 0){
                TextView noHubLabel = (TextView) findViewById(R.id.noHubTextView);
                noHubLabel.setText("Nessun Hub registrato.");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        rv.setAdapter(adapter);
    }

    public void addCentralina(View view) {
        Intent intent = new Intent(this, AddCentralinaActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logOut:
                mAuth.signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}