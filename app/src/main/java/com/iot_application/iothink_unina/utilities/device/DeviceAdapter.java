package com.iot_application.iothink_unina.utilities.device;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iot_application.iothink_unina.R;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceViewHolder> implements Filterable {

    private Context c;
    private ArrayList<Device> devices;
    private ArrayList<Device> devicesFull;

    public DeviceAdapter(Context c, ArrayList<Device> devices) {
        this.c = c;
        this.devices = devices;
        devicesFull = new ArrayList<>(devices);
    }

    public DeviceAdapter(Context c, DatabaseReference dbRef) {
        this.c = c;
        this.devices = new ArrayList<>();
        devicesFull = new ArrayList<>();

        dbRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                DeviceAdapter.this.devices.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Device device = postSnapshot.getValue(Device.class);
                    System.out.println("****DEBUG**** Device BT ADDRESS: " + device.getBt_addr());
                    devices.add(device);
                    devicesFull.add(device);
                }

                TextView noDeviceLabel = (TextView) ((Activity) DeviceAdapter.this.c).findViewById(R.id.noDeviceTextView);

                if(devices.isEmpty()){
                    noDeviceLabel.setText("Nessun device registrato.");
                } else {
                    noDeviceLabel.setText("");
                }

                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.device_model, parent, false);
        return new DeviceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceViewHolder holder, int position) {
        final Device d = devices.get(position);

        System.out.println("****DEBUG**** NOME CUSTOM: " +  d.getNomeCustom());
        holder.nomeDispositivo.setText(d.getNomeCustom());
        holder.nomeStanza.setText(d.getRoom());

        // Inizializzazione switch sulla base dello stato prelevato da Firebase
        if(d.getStatus().equals("on") || d.getStatus().equals("reset/on")){
            holder.aSwitch.setChecked(true);
            setDeviceImage(holder, true);
        }else if(d.getStatus().equals("off") || d.getStatus().equals("reset/off")){
            holder.aSwitch.setChecked(false);
            setDeviceImage(holder, false);
        }

        holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                System.out.println("****DEBUG**** SWITCH CAMBIA STATO");

                if(buttonView.isPressed()){
                    // Caso: switch provocato dalla pressione dell'utente

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    String uid = user.getUid();

                    // Creazione delle reference
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users/" + uid + "/centraline/" + d.getCentralina() + "/cmd");
                    final DatabaseReference ref_device_status = database.getReference("users/" + uid + "/centraline/" + d.getCentralina() + "/devices/" + d.getBt_addr() + "/status");

                    // Creazione progress dialog di invio comando
                    final ProgressDialog progressDialog = new ProgressDialog(c);
                    progressDialog.setTitle("Caricamento");
                    progressDialog.setMessage("Loading...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);

                    if(isChecked){
                        // OFF - Reset/OFF -> ON
                        System.out.println("****DEBUG**** CHECKED TRUE");

                        holder.aSwitch.setClickable(false);
                        progressDialog.show();

                        // Inviare CMD: ON su Firebase
                        myRef.setValue(d.getBt_addr() + "/" + d.getUuid() + "/on").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    System.out.println("****DEBUG**** Invio comando ON effettuato");
                                }else{
                                    System.out.println("****DEBUG**** Invio comando ON non effettuato");
                                }
                            }
                        });

                        System.out.println("****DEBUG**** " + d.getBt_addr() + "/" + d.getUuid() + "/on");

                        // Lettura stato del dispositivo da Firebase
                        ref_device_status.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String status = dataSnapshot.getValue(String.class);

                                if(status.equals("off") || status.equals("reset/off") || status.equals("on")){
                                    // Caso: nessun errore verificato
                                    holder.aSwitch.setClickable(true);
                                    if(status.equals("on")){
                                        setDeviceImage(holder, true);
                                        progressDialog.dismiss();  // to dismiss
                                    }

                                }else if (status.equals("error")){
                                    // Caso: presenza di errori

                                    System.out.println("****DEBUG**** Centralina ha inviato error");

                                    Toast.makeText(DeviceAdapter.this.c, R.string.erroreDevice, Toast.LENGTH_SHORT).show();

                                    //scrittura sullo status del dispositivo
                                    ref_device_status.setValue("reset/off");
                                    holder.aSwitch.setChecked(false);
                                    holder.aSwitch.setClickable(true);
                                    progressDialog.dismiss();
                                    ref_device_status.removeEventListener(this);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                ref_device_status.removeEventListener(this);
                            }
                        });

                    } else{
                        // ON - Reset/ON -> OFF
                        System.out.println("****DEBUG**** CHECKED FALSE");

                        holder.aSwitch.setClickable(false);
                        progressDialog.show();  // to show

                        // Inviare CMD: OFF su Firebase
                        myRef.setValue(d.getBt_addr() + "/" + d.getUuid() + "/off").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    System.out.println("****DEBUG**** Invio comando OFF effettuato");
                                }else{
                                    System.out.println("****DEBUG**** Invio comando OFF non effettuato");
                                }
                            }
                        });

                        System.out.println("****DEBUG**** " + d.getBt_addr() + "/" + d.getUuid() + "/off");

                        // Lettura stato del dispositivo da Firebase
                        ref_device_status.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String status = dataSnapshot.getValue(String.class);

                                if(status.equals("on") || status.equals("reset/on") || status.equals("off")){

                                    holder.aSwitch.setClickable(true);

                                    if(status.equals("off")){
                                        setDeviceImage(holder, false);
                                        progressDialog.dismiss();
                                    }

                                }else if (status.equals("error")){
                                    System.out.println("****DEBUG**** Centralina ha inviato error");

                                    Toast.makeText(DeviceAdapter.this.c, R.string.erroreDevice, Toast.LENGTH_SHORT).show();

                                    //scrittura sullo status del dispositivo
                                    ref_device_status.setValue("reset/on");
                                    holder.aSwitch.setChecked(true);
                                    holder.aSwitch.setClickable(true);
                                    progressDialog.dismiss();
                                    ref_device_status.removeEventListener(this);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                ref_device_status.removeEventListener(this);
                            }
                        });
                    }
                }else{
                    // Caso: switch provocato dall'impossibilità di inviare il comando
                    System.out.println("****DEBUG**** CAMBIO SENZA PREMERE");
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    // Visualizzazione immagine led acceso/spento
    private void setDeviceImage(DeviceViewHolder holder, boolean status){
        Drawable dr;
        if(status){
            System.out.println("****DEBUG**** Set image on");
            dr = c.getResources().getDrawable(R.drawable.led_on);
        }else{
            System.out.println("****DEBUG**** Set image off");
            dr = c.getResources().getDrawable(R.drawable.led_off);
        }
        holder.deviceImage.setImageDrawable(dr);
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    // Implementazione funzione ricerca tra i dispositivi in base all'input dell'utente
    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Device> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(devicesFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Device device : devicesFull) {
                    if (device.getNomeCustom().toLowerCase().contains(filterPattern)) {
                        filteredList.add(device);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            devices.clear();
            devices.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };
}
