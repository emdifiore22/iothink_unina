package com.iot_rest_application.iothink_unina.utilities.device;

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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iot_rest_application.iothink_unina.DevicesActivity;
import com.iot_rest_application.iothink_unina.R;
import com.iot_rest_application.iothink_unina.utilities.centralina.Centralina;
import com.iot_rest_application.iothink_unina.utilities.centralina.CentralinaAdapter;

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
                    System.out.println("****DEBUG**** HO PREMUTO");
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    String uid = user.getUid();

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users/" + uid + "/centraline/" + d.getCentralina() + "/cmd");
                    System.out.println("****DEBUG**** Database CMD reference : " + myRef);
                    final DatabaseReference ref_device_status = database.getReference("users/" + uid + "/centraline/" + d.getCentralina() + "/devices/" + d.getBt_addr() + "/status");
                    System.out.println("****DEBUG**** Database DEVICE_STATUS reference : " + ref_device_status);

                    /*
                    AlertDialog.Builder builder = new AlertDialog.Builder(c);

                    builder.setTitle("Caricamento");
                    builder.setCancelable(false);
                    builder.setMessage("Invio comando al dispositivo " + d.nomeCustom + "...");
                    final AlertDialog loadingDialog = builder.create();
                    loadingDialog.setCanceledOnTouchOutside(false);
                    */

                    final ProgressDialog progressDialog = new ProgressDialog(c);
                    progressDialog.setTitle("Caricamento");
                    progressDialog.setMessage("Loading...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);

                    if(isChecked){

                        holder.aSwitch.setClickable(false);
                        //Toast.makeText(DeviceAdapter.this.c, "Accensione " + d.nomeCustom , Toast.LENGTH_LONG).show();

                        progressDialog.show();  // to show

                        // OFF - Reset/OFF -> ON
                        System.out.println("****DEBUG**** CHECKED TRUE");

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

                        // Read from the database
                        ref_device_status.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                String value = dataSnapshot.getValue(String.class);

                                if(value.equals("off") || value.equals("reset/off") || value.equals("on")){

                                    //System.out.println("****DEBUG**** Comando ON inviato correttamente");

                                    holder.aSwitch.setClickable(true);

                                    if(value.equals("on")){
                                        setDeviceImage(holder, true);
                                        progressDialog.dismiss();  // to dismiss
                                    }

                                }else if (value.equals("error")){

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
                                // Failed to read value
                                //Log.w(TAG, "Failed to read value.", error.toException());
                            }
                        });

                    } else{
                        holder.aSwitch.setClickable(false);
                        //Toast.makeText(DeviceAdapter.this.c, "Spegnimento " + d.nomeCustom , Toast.LENGTH_LONG).show();

                        progressDialog.show();  // to show

                        // ON - Reset/ON -> OFF
                        System.out.println("****DEBUG**** CHECKED FALSE");

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

                        // Read from the database
                        ref_device_status.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                String value = dataSnapshot.getValue(String.class);

                                if(value.equals("on") || value.equals("reset/on") || value.equals("off")){

                                    //System.out.println("****DEBUG**** Comando OFF inviato correttamente");

                                    holder.aSwitch.setClickable(true);

                                    if(value.equals("off")){
                                        setDeviceImage(holder, false);
                                        progressDialog.dismiss();
                                    }

                                }else if (value.equals("error")){
                                    System.out.println("****DEBUG**** Centralina ha inviato error");

                                    Toast.makeText(DeviceAdapter.this.c, R.string.erroreDevice, Toast.LENGTH_SHORT).show();

                                    //scrittura sullo status del dispositivo
                                    ref_device_status.setValue("reset/on");
                                    holder.aSwitch.setChecked(true);
                                    holder.aSwitch.setClickable(true);
                                    progressDialog.dismiss();
                                    ref_device_status.removeEventListener(this);
                                }

                                //Log.d(TAG, "Value is: " + value);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                ref_device_status.removeEventListener(this);
                                // Failed to read value
                                //Log.w(TAG, "Failed to read value.", error.toException());
                            }
                        });
                    }
                }else{
                    System.out.println("****DEBUG**** CAMBIO SENZA PREMERE");
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

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

        /*System.out.println("SET DEVICE IMAGE: " + type);
        switch (type){
            case "Led":{
                if(cmd.equals("on")) dr = c.getResources().getDrawable(R.drawable.led_on);
                else if(cmd.equals("off"))  dr = c.getResources().getDrawable(R.drawable.led_off);
                break;
            }
            case "Lampadario":{
                if(cmd.equals("on")) dr = c.getResources().getDrawable(R.drawable.lampadario_on);
                else if(cmd.equals("off"))  dr = c.getResources().getDrawable(R.drawable.lampadario_off);
                break;
            }
            case "Porta":{
                if(cmd.equals("on")) dr = c.getResources().getDrawable(R.drawable.door_opened);
                else if(cmd.equals("off"))  dr = c.getResources().getDrawable(R.drawable.door_closed);
                break;
            }
            default:{
                if(cmd.equals("on")) dr = c.getResources().getDrawable(R.drawable.led_on);
                else if(cmd.equals("off"))  dr = c.getResources().getDrawable(R.drawable.led_off);
                break;
            }
        }
        */


    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

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
