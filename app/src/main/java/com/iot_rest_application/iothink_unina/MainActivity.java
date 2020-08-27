package com.iot_rest_application.iothink_unina;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.iot_rest_application.iothink_unina.utilities.FirebaseHelper;
import com.iot_rest_application.iothink_unina.utilities.centralina.CentralinaAdapter;

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

        firebaseHelper = FirebaseHelper.getInstance();
        System.out.println("****DEBUG**** FIREBASE HELPER TOKEN ID: " + firebaseHelper.getIdToken());

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("/users/" + userUid + "/centraline");
        adapter = new CentralinaAdapter(this, dbRef);

        rv.setAdapter(adapter);

        TextView noHubLabel = (TextView) findViewById(R.id.noHubTextView);
        if(adapter.getItemCount() == 0){
            noHubLabel.setText("Nessun Hub registrato.");
        } else{
            noHubLabel.setText("");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_centralina, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.logOut:
                mAuth.signOut();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.addCentralina:
                showHubInstructions();
                return true;
            case R.id.action_search_centralina:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        adapter.getFilter().filter(newText);
                        return false;
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showHubInstructions(){
        // Creazione del dialog che mostra le istruzioni per l'inserimento di una nuova centralina
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.hub_instruction, null);
        builder.setView(view).setTitle("Istruzioni");

        // Impostazione del positive button
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Intent intent = new Intent(MainActivity.this, AddCentralinaActivity.class);
                startActivity(intent);
            }
        });

        // Impostazione del negative button
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                System.out.println("****DEBUG**** User cancelled the dialog");
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }
}