package com.iot_rest_application.iothink_unina;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class AddCentralinaActivity extends AppCompatActivity {

    private BarcodeDetector detector;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private TextView message;
    private String barcode;
    private EditText hubName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_centralina);

        System.out.println("****DEBUG**** ADD CENTRALINA ACTIVITY ON CREATE");

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        message = (TextView) findViewById(R.id.detectedHubName);
        hubName = (EditText) findViewById(R.id.hubCustomName);

        detector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();

        if (!detector.isOperational()) {
            System.out.println("*****DEBUG***** Detector di codici a barre non attivabile");
            return;
        }

        cameraSource = new CameraSource.Builder(this, detector).setAutoFocusEnabled(true).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                activateCamera();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) { }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) { cameraSource.stop(); }
        });

        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() { }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> items = detections.getDetectedItems();
                if (items.size() != 0)
                    runOnUiThread(new Runnable() {
                        public void run() {
                            barcode = items.valueAt(0).displayValue;
                            //String[] barcodeTokenized = barcode.split("/");
                            String msg = "Centralina " +  barcode + " rilevata.";
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
            return;
        } else {
            try {
                cameraSource.start(surfaceView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addNewCentralina(View view) {

        /* Inserire altre operazioni eventuali, come la scrittura su Firebase */

        AddCentralinaActivity.this.finish();
    }
}