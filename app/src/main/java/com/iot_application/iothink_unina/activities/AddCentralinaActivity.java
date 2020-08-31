package com.iot_application.iothink_unina.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.iot_application.iothink_unina.R;

import java.io.IOException;

public class AddCentralinaActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private TextView message;
    private String nomeHardwareCentralina;
    private EditText nomeUtenteCentralina;
    private String uid;
    private final int REQUEST_PERMISSION_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_centralina);

        System.out.println("****DEBUG**** ADD CENTRALINA ACTIVITY ON CREATE");

        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Inizializzazione utente corrente
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        message = (TextView) findViewById(R.id.detectedHubName);
        nomeUtenteCentralina = (EditText) findViewById(R.id.hubCustomName);

        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();

        if (!detector.isOperational()) {
            System.out.println("*****DEBUG***** Detector di codici a barre non attivabile.");
            return;
        }

        cameraSource = new CameraSource.Builder(this, detector).setAutoFocusEnabled(true).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                activateCamera();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> items = detections.getDetectedItems();
                if (items.size() != 0)
                    runOnUiThread(new Runnable() {
                        public void run() {
                            nomeHardwareCentralina = items.valueAt(0).displayValue;

                            // Get instance of Vibrator from current Context
                            Vibrator v = (Vibrator) getSystemService(AddCentralinaActivity.this.VIBRATOR_SERVICE);
                            // Vibrate for 400 milliseconds
                            v.vibrate(100);

                            String msg = "Centralina " + nomeHardwareCentralina + " rilevata.";
                            message.setText(msg);
                        }
                    });
            }
        });
    }

    private void activateCamera() {
        // Verifichiamo che sia stata concessa la permission CAMERA
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("*****DEBUG***** Dare all'applicazione l'autorizzazione ad accedere alla fotocamera del cellulare.");

            // Visualizzazione dialog per ottenere i permessi fotocamera
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);

        } else {
            try {
                // Avvio fotocamera per la lettura del QRCode
                cameraSource.start(surfaceView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AddCentralinaActivity.this, R.string.permissionGranted, Toast.LENGTH_SHORT).show();
                activateCamera();
            } else {
                Toast.makeText(AddCentralinaActivity.this, R.string.permissionNotGranted, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addNewCentralina(View view) {
        if(nomeHardwareCentralina != null){
            // Caso: QRCode rilevato

            if(!nomeUtenteCentralina.getText().toString().isEmpty()) {
                // Caso: Utente ha inserito il nome della centralina
                final String nomeUtenteCentralina = this.nomeUtenteCentralina.getText().toString();

                // Inserimento centralina nella sezione dell'utente
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("users/" + uid + "/centraline/" + nomeHardwareCentralina);

                myRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        currentData.child("cmd").setValue("idle");
                        currentData.child("nome").setValue(nomeUtenteCentralina);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                        // Ritorno a MainActivity
                        AddCentralinaActivity.this.finish();
                    }
                });

            } else {
                // Caso: Utente non ha inserito il nome della centralina
                Toast.makeText(AddCentralinaActivity.this,R.string.nomeUtenteCentralina, Toast.LENGTH_SHORT).show();
            }
        }else{
            // Caso: nessun QRCode scannerizzato
            Toast.makeText(AddCentralinaActivity.this,R.string.qrCodeNotFound, Toast.LENGTH_SHORT).show();
        }


    }
}