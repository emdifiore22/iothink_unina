package com.iot_rest_application.iothink_unina;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
        firebaseHelper = new FirebaseHelper(db, uid);

        //ADAPTER
        try {
            adapter = new DeviceAdapter(this, firebaseHelper.retrieve_devices(this.nomeCentralina));
            System.out.println("****DEBUG**** Dispositivi rilevati: " + adapter.getItemCount());

            if(adapter.getItemCount() == 0){
                TextView noDeviceLabel = (TextView) findViewById(R.id.noDeviceTextView);
                noDeviceLabel.setText("Nessun device registrato.");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        rv.setAdapter(adapter);

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
                    System.out.println("****DEBUG**** Device rilevato: " + device.getName());
                    ricercaButton.setText(R.string.ricercaButtonStandard);

                    // Creazione del dialog per inserire il nuovo dispositivo
                    AlertDialog.Builder builder = new AlertDialog.Builder(DevicesActivity.this);
                    LayoutInflater inflater = DevicesActivity.this.getLayoutInflater();
                    final View view = inflater.inflate(R.layout.dialog_device, null);
                    builder.setView(view)
                            .setMessage(device.getName())
                            .setTitle("Device Rilevato");

                    // Impostazione del positive button
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            System.out.println("****DEBUG**** User clicked OK button");
                            EditText nomeDispositivo = (EditText) view.findViewById(R.id.newDeviceName);
                            EditText stanzaDispositivo = (EditText) view.findViewById(R.id.newDeviceRoom);

                            device.setCentralina(DevicesActivity.this.nomeCentralina);
                            device.setNomeCustom(nomeDispositivo.getText().toString());
                            device.setRoom(stanzaDispositivo.getText().toString());

                            DatabaseReference ref = database.getReference("users/" + DevicesActivity.this.uid + "/centraline/hub1/devices/" + device.getBt_addr());

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

}