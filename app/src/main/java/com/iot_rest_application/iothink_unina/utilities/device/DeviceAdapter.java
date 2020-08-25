package com.iot_rest_application.iothink_unina.utilities.device;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.iot_rest_application.iothink_unina.R;

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

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.device_model, parent, false);
        return new DeviceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        final Device d = devices.get(position);

        System.out.println("****DEBUG**** NOME CUSTOM: " +  d.getNomeCustom());
        holder.nomeDispositivo.setText(d.getNomeCustom());

        if(d.getStatus().equals("on")){
            holder.aSwitch.setChecked(true);
        }else if(d.getStatus().equals("off")){
            //holder.aSwitch.setChecked(true);
        }

        holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                String uid = user.getUid();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users/" + uid + "/centraline/" + d.getCentralina() + "/cmd");
                DatabaseReference ref_device_status = database.getReference("users/" + uid + "/centraline/" + d.getCentralina() + "/devices/" + d.getBt_addr() + "/status");
                if(isChecked){
                    // Inviare CMD: ON su Firebase
                    myRef.setValue(d.getBt_addr() + "/" + d.getUuid() + "/on");
                    ref_device_status.setValue("on");
                } else{
                    // Inviare CMD: OFF su Firebase
                    myRef.setValue(d.getBt_addr() + "/" + d.getUuid() + "/off");
                    ref_device_status.setValue("off");
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return devices.size();
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
