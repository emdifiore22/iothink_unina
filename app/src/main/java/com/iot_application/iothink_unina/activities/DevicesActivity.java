package com.iot_application.iothink_unina.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iot_application.iothink_unina.R;
import com.iot_application.iothink_unina.utilities.device.Device;
import com.iot_application.iothink_unina.utilities.device.DeviceAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DevicesActivity extends AppCompatActivity {

    private DeviceAdapter adapter;
    private String nomeCentralina;
    private Button ricercaButton;
    private FirebaseAuth mAuth;
    private String uid;
    private FirebaseDatabase firebaseDatabase;
    private static final int TAKE_PIC_FROM_GALLERY = 1;
    private static final int CROP_ACTIVITY = 2;
    private static final int TAKE_PIC_FROM_CAMERA = 3;
    private final int REQUEST_PERMISSION_CAMERA = 4;
    private boolean roomEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        System.out.println("****DEBUG**** DEVICES ACTIVITY ON CREATE");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        ricercaButton = (Button) findViewById(R.id.ricercaDispositiviButton);
        ricercaButton.setText(R.string.ricercaButtonStandard);
        ricercaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Visualizzazione messaggio di ricerca in corso
                ricercaButton.setText(R.string.ricercaButtonInCorso);

                // Inizio ricerca nuovo dispositivo
                scansioneNuovoDispositivo();
            }
        });

        firebaseDatabase =  FirebaseDatabase.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();

    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("****DEBUG**** DEVICES ACTIVITY ON START");

        Bundle b = getIntent().getExtras();
        this.nomeCentralina = "";
        if(b != null) {
            this.nomeCentralina = b.getString("nomeCentralina");
        }

        System.out.println("****DEBUG**** NOME CENTRALINA: " + this.nomeCentralina);

        // Creazione RecyclerViewer per la visualizzazione dei dispositivi aggiuti dall'utente.
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("/users/" + uid + "/centraline/" + this.nomeCentralina + "/devices");
        adapter = new DeviceAdapter(this, dbRef);
        // Setup View
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Lettura stanze per la visualizzazione del dialog "Aggiungi Stanze" nel caso in cui queste non siano presenti.
        DatabaseReference db = this.firebaseDatabase.getReference("users/" + uid + "/centraline/" + nomeCentralina + "/");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.child("rooms").exists()){
                    System.out.println("****DEBUG**** ROOMS IS EMPTY");
                    roomEmpty = true;
                    showAddRoomDialog();
                }else{
                    roomEmpty = false;
                    System.out.println("****DEBUG**** ROOMS IS NOT EMPTY");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("****DEBUG**** DEVICES ACTIVITY ON RESUME");
    }

    private void scansioneNuovoDispositivo() {
        System.out.println("****DEBUG**** SCANSIONE NUOVI DISPOSITIVI");

        // Invio del comando di discovery alla centralina tramite Firebase
        final DatabaseReference cmdRef = this.firebaseDatabase.getReference("users/" + this.uid + "/centraline/" + this.nomeCentralina + "/cmd");
        cmdRef.setValue(this.nomeCentralina + "/search/discover");

        // Lettura del dispositivo scansionato
        final DatabaseReference detectedDeviceRef = this.firebaseDatabase.getReference("users/" + this.uid + "/centraline/" + this.nomeCentralina + "/detectedDevice");

        // Listener per la lettura di detectedDevices (inserito dalla centralina)
        detectedDeviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final Device device = dataSnapshot.getValue(Device.class);

                if(device != null){
                    // Caso: detectedDevice rilevato
                    System.out.println("****DEBUG**** Device rilevato: " + device.getBt_addr());
                    ricercaButton.setText(R.string.ricercaButtonStandard);

                    // Creazione del dialog per inserire il nuovo dispositivo
                    final AlertDialog.Builder builder = new AlertDialog.Builder(DevicesActivity.this);
                    LayoutInflater inflater = DevicesActivity.this.getLayoutInflater();
                    final View view = inflater.inflate(R.layout.dialog_device, null);
                    builder.setView(view)
                            .setMessage(device.getBt_addr())
                            .setTitle("Device Rilevato");

                    //Inizializzazione ArrayList per i nomi delle stanze
                    final ArrayList<String> rooms = new ArrayList<>();
                    rooms.add(0, "Seleziona stanza");

                    // Lettura delle stanze da Firebase
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference db = firebaseDatabase.getReference("users/" + uid + "/centraline/" + nomeCentralina + "/rooms");
                    db.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // Caricamento delle stanze
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                String room = postSnapshot.getKey().toString();
                                rooms.add(room);
                            }

                            // Impostazione dialog per l'inserimento del dispositivo
                            final EditText nomeDispositivo = (EditText) view.findViewById(R.id.newDeviceName);
                            final Spinner roomSpinner = (Spinner) view.findViewById(R.id.roomSpinner);
                            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(DevicesActivity.this, android.R.layout.simple_spinner_dropdown_item, rooms);
                            roomSpinner.setAdapter(spinnerArrayAdapter);

                            // Impostazione del positive button
                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    // User clicked OK button
                                    System.out.println("****DEBUG**** User clicked OK button");
                                    TextView noDeviceView = (TextView) findViewById(R.id.noDeviceTextView);
                                    noDeviceView.setText("");
                                    String stanzaDispositivo = roomSpinner.getSelectedItem().toString();

                                    if(!stanzaDispositivo.equals("Seleziona stanza")){
                                        device.setCentralina(DevicesActivity.this.nomeCentralina);
                                        device.setNomeCustom(nomeDispositivo.getText().toString());
                                        device.setRoom(stanzaDispositivo);
                                        device.setStatus("off");

                                        // Inserimento del nuovo disposivo e rimozione della struttura detectedDevice
                                        DatabaseReference ref = DevicesActivity.this.firebaseDatabase.getReference("users/" + DevicesActivity.this.uid + "/centraline/" + device.getCentralina() + "/devices/" + device.getBt_addr());
                                        ref.setValue(device);
                                        detectedDeviceRef.removeValue();

                                        // Impostazione comando centralina in idle.
                                        cmdRef.setValue("idle");
                                    }else{
                                        Toast.makeText(DevicesActivity.this, R.string.select_room_failed, Toast.LENGTH_SHORT).show();
                                    }

                                    dialog.dismiss();
                                }
                            });

                            // Impostazione del negative button
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    System.out.println("****DEBUG**** User cancelled the dialog");

                                    dialog.dismiss();
                                }
                            });

                            AlertDialog dialog = builder.create();

                            if(!((Activity) DevicesActivity.this).isFinishing()){
                                dialog.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

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
        // Creazione menu in Top Bar
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
                // Visualizzazione dialog aggiungi stanza
                showAddRoomDialog();
                return true;
            case R.id.addHubImage:

                // Visualizzazione menu per l'impostazione dell'immagine centralina
                showImagePickerDialog();
                return true;

            case R.id.action_search_device:

                // Impostazione meccanismo di ricerca dei dispositivi
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


    public void showImagePickerDialog(){
        final String[] options = {"Fotocamera", "Galleria"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scegli foto");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("****DEBUG**** È stato cliccato " + options[which]);

                if(options[which].equals("Fotocamera")){

                    // Caso: caricamento tramite fotocamera
                    if (ActivityCompat.checkSelfPermission(DevicesActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        System.out.println("*****DEBUG***** Dare all'applicazione l'autorizzazione ad accedere alla fotocamera del cellulare.");

                        // Visualizzazione dialog richiesta permessi fotocamera
                        ActivityCompat.requestPermissions(DevicesActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
                    } else {
                        pickImageFromCamera();
                    }

                }else{

                    // Caso: caricamento tramite galleria
                    pickImageFromGallery();
                }
            }
        });

        builder.show();
    }

    public void pickImageFromCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String[] mimeType = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType);
        startActivityForResult(intent, TAKE_PIC_FROM_CAMERA);
    }

    public void pickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeType = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType);
        startActivityForResult(intent, TAKE_PIC_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("****DEBUG**** Request code : " + requestCode);
        System.out.println("****DEBUG**** Result code : " + resultCode);
        Uri selectedImage;
        if(resultCode == Activity.RESULT_OK){
            // Caso: caricamento immagine avvenuto con successo
            Bundle extras = data.getExtras();
            switch (requestCode){
                case TAKE_PIC_FROM_GALLERY:
                    // Caso: immagine da galleria

                    selectedImage = data.getData();
                    try {
                        Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        Bitmap resized = Bitmap.createScaledBitmap(selectedBitmap, 300, 300, true);

                        // Caricamento immagine su Firebase Storage
                        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference imageRef = mStorageRef.child("users/" + uid + "/" + nomeCentralina + ".png");
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data_byte = baos.toByteArray();
                        UploadTask uploadTask = imageRef.putBytes(data_byte);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast toast = Toast.makeText(DevicesActivity.this, R.string.loadingImageSuccess, Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case TAKE_PIC_FROM_CAMERA:
                    // Caso: immagine da fotocamera

                    Bitmap selectedBitmap = extras.getParcelable("data");

                    // Caricamento immagine su Firebase Storage
                    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference imageRef = mStorageRef.child("users/" + uid + "/" + nomeCentralina + ".png");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] data_byte = baos.toByteArray();
                    UploadTask uploadTask = imageRef.putBytes(data_byte);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast toast = Toast.makeText(DevicesActivity.this, R.string.loadingImageSuccess, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });

                    break;
            }
        }else{
            Toast toast = Toast.makeText(this, R.string.loadingImageFailed, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void showAddRoomDialog(){
        // Creazione del dialog per inserire una nuova stanza
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_room, null);
        builder.setView(view).setTitle("Aggiungi stanza");

        // Impostazione del positive button
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // EditText per l'inserimento del nome della stanza da inserire
                EditText roomNameView = (EditText) view.findViewById(R.id.roomName);
                final String roomName = roomNameView.getText().toString().toLowerCase();
                System.out.println("****DEBUG**** Room: " + roomName );

                // Controlli ed inserimento su Firebase
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference dbRef = firebaseDatabase.getReference("users/" + uid + "/centraline/" + nomeCentralina + "/rooms");
                final ArrayList<String> rooms = new ArrayList<>();
                // Aggiunta listener per il prelievo delle stanze da Firebase
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            String room = postSnapshot.getKey().toString();
                            rooms.add(room);
                        }

                        if(rooms.isEmpty()){
                            System.out.println("****DEBUG**** ROOMS IS EMPTY");
                            dbRef.child(roomName).setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(DevicesActivity.this, R.string.add_room_success, Toast.LENGTH_SHORT).show();
                                    roomEmpty = false;
                                }
                            });

                        }else{
                            System.out.println("****DEBUG**** ROOMS IS NOT EMPTY");
                            if(!rooms.contains(roomName)){
                                // Caso: nome stanza non presente

                                // Aggiunta stanza
                                dbRef.child(roomName).setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(DevicesActivity.this, R.string.add_room_success, Toast.LENGTH_SHORT).show();
                                        roomEmpty = false;
                                    }
                                });
                            }else{
                                Toast.makeText(DevicesActivity.this, R.string.add_room_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

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

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // Ritorno a MainActivity per evitare la ricerca di un nuovo dispositivo senza aver mai inserito una stanza.
                if(roomEmpty){
                    DevicesActivity.this.finish();
                }
            }
        });

        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
    }
}