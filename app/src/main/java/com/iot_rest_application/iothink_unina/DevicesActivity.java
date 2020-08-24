package com.iot_rest_application.iothink_unina;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iot_rest_application.iothink_unina.utilities.FirebaseHelper;
import com.iot_rest_application.iothink_unina.utilities.device.Device;
import com.iot_rest_application.iothink_unina.utilities.device.DeviceAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class DevicesActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private DeviceAdapter adapter;
    private static RecyclerView rv;
    private String nomeCentralina;
    private Button ricercaButton;
    private FirebaseAuth mAuth;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        ricercaButton = (Button) findViewById(R.id.ricercaDispositiviButton);
        ricercaButton.setText(R.string.ricercaButtonStandard);
        ricercaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ricercaButton.setText(R.string.ricercaButtonInCorso);
                scansioneNuovoDispositivo();
            }
        });

        Bundle b = getIntent().getExtras();
        this.nomeCentralina = "";
        if(b != null) {
            this.nomeCentralina = b.getString("nomeCentralina");
        }

        System.out.println("****DEBUG**** DEVICES ACTIVITY ON CREATE");
        System.out.println("****DEBUG**** NOME CENTRALINA: " + this.nomeCentralina);

        //SETUP RECYCLER VIEW
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        firebaseHelper = FirebaseHelper.getInstance();

        //ADAPTER
        try {
            adapter = new DeviceAdapter(this, firebaseHelper.retrieve_devices(this.nomeCentralina));
            System.out.println("****DEBUG**** Dispositivi rilevati: " + adapter.getItemCount());
            TextView noDeviceLabel = (TextView) findViewById(R.id.noDeviceTextView);

            rv.setAdapter(adapter);
            /*
            if(adapter.getItemCount() == 0){
                noDeviceLabel.setText("Nessun device registrato.");
            }else{
                noDeviceLabel.setText("");
            }

             */

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("****DEBUG**** DEVICES ACTIVITY ON START");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("****DEBUG**** DEVICES ACTIVITY ON RESUME");
    }

    private void scansioneNuovoDispositivo() {
        System.out.println("****DEBUG**** SCANSIONE NUOVI DISPOSITIVI");

        // Inserimento del comando di discovery
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference cmdRef = database.getReference("users/" + this.uid + "/centraline/" + this.nomeCentralina + "/cmd");

        cmdRef.setValue(this.nomeCentralina + "/search/discover");

        // Lettura del dispositivo scansionato
        final DatabaseReference detectedDeviceRef = database.getReference("users/" + this.uid + "/centraline/" + this.nomeCentralina + "/detectedDevice");
        detectedDeviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                final Device device;
                device = dataSnapshot.getValue(Device.class);

                if(device != null){
                    System.out.println("****DEBUG**** Device rilevato: " + device.getBt_addr());
                    ricercaButton.setText(R.string.ricercaButtonStandard);

                    // Creazione del dialog per inserire il nuovo dispositivo
                    AlertDialog.Builder builder = new AlertDialog.Builder(DevicesActivity.this);
                    LayoutInflater inflater = DevicesActivity.this.getLayoutInflater();
                    final View view = inflater.inflate(R.layout.dialog_device, null);
                    builder.setView(view)
                            .setMessage(device.getBt_addr())
                            .setTitle("Device Rilevato");

                    final EditText nomeDispositivo = (EditText) view.findViewById(R.id.newDeviceName);
                    final Spinner roomSpinner = (Spinner) view.findViewById(R.id.roomSpinner);
                    ArrayAdapter<String> spinnerArrayAdapter = null;
                    try {

                        ArrayList<String> rooms = firebaseHelper.retrieve_rooms(DevicesActivity.this.nomeCentralina);
                        rooms.add(0, "Seleziona stanza");
                        spinnerArrayAdapter = new ArrayAdapter<>(DevicesActivity.this, android.R.layout.simple_spinner_dropdown_item, rooms );
                        roomSpinner.setAdapter(spinnerArrayAdapter);

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Impostazione del positive button
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            System.out.println("****DEBUG**** User clicked OK button");

                            String stanzaDispositivo = roomSpinner.getSelectedItem().toString();

                            if(!stanzaDispositivo.equals("Seleziona stanza")){
                                device.setCentralina(DevicesActivity.this.nomeCentralina);
                                device.setNomeCustom(nomeDispositivo.getText().toString());
                                device.setRoom(stanzaDispositivo);
                                device.setStatus("off");
                                DatabaseReference ref = database.getReference("users/" + DevicesActivity.this.uid + "/centraline/" + device.getCentralina() + "/devices/" + device.getBt_addr());

                                ref.setValue(device).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        try {
                                            adapter = new DeviceAdapter(DevicesActivity.this, firebaseHelper.retrieve_devices(DevicesActivity.this.nomeCentralina));
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        DevicesActivity.rv.setAdapter(adapter);
                                    }
                                });

                                detectedDeviceRef.removeValue();
                                cmdRef.setValue("idle");
                            }else{
                                Toast.makeText(DevicesActivity.this, R.string.select_room_failed, Toast.LENGTH_SHORT).show();
                            }
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

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_devices, menu);
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
            case R.id.addRoom:
                showAddRoomDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showAddRoomDialog(){
        // Creazione del dialog per inserire il nuovo dispositivo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_room, null);
        builder.setView(view).setTitle("Aggiungi stanza");

        // Impostazione del positive button
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Controlli ed inserimento su Firebase
                EditText roomNameView = (EditText) view.findViewById(R.id.roomName);
                String roomName = roomNameView.getText().toString().toLowerCase();
                System.out.println("****DEBUG**** Room: " + roomName );

                try {
                    ArrayList<String> rooms = firebaseHelper.retrieve_rooms(DevicesActivity.this.nomeCentralina);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users/" + uid + "/centraline/" + DevicesActivity.this.nomeCentralina + "/rooms");

                    if(rooms.isEmpty()){
                        System.out.println("****DEBUG**** ROOMS IS EMPTY");
                        myRef.child(roomName).setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(DevicesActivity.this, R.string.add_room_success, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        System.out.println("****DEBUG**** ROOMS IS NOT EMPTY");
                        if(!rooms.contains(roomName)){
                            myRef.child(roomName).setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(DevicesActivity.this, R.string.add_room_success, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            Toast.makeText(DevicesActivity.this, R.string.add_room_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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

}