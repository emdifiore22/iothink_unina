package com.iot_rest_application.iothink_unina.utilities.device;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot_rest_application.iothink_unina.R;

public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    //ImageView deviceImage;
    TextView nomeDispositivo;
    Switch aSwitch;
    ImageView deviceImage;
    private final Context context;

    public DeviceViewHolder(@NonNull View itemView) {
        super(itemView);

        //deviceImage = (ImageView) itemView.findViewById(R.id.deviceImage);
        nomeDispositivo = (TextView) itemView.findViewById(R.id.nomeDevice);
        aSwitch = (Switch) itemView.findViewById(R.id.switchComando);
        deviceImage = (ImageView) itemView.findViewById(R.id.deviceImage);
        context = itemView.getContext();

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        System.out.println("****DEBUG**** Selezionato dispositivo: " + this.nomeDispositivo.getText().toString());
        System.out.println("****DEBUG**** CONTEXT: " + context);
    }
}
