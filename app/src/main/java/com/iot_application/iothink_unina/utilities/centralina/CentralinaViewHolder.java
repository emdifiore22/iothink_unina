package com.iot_application.iothink_unina.utilities.centralina;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.iot_application.iothink_unina.activities.DevicesActivity;
import com.iot_application.iothink_unina.R;

public class CentralinaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


    TextView nomeCustomCentralina;
    TextView nomeHardwareCentralina;
    ImageView hubImage;

    private final Context context;

    public CentralinaViewHolder(View itemView) {
        super(itemView);

        nomeCustomCentralina = (TextView) itemView.findViewById(R.id.nomeCentralina);
        nomeHardwareCentralina = (TextView) itemView.findViewById(R.id.nomeHardwareCentralina);
        hubImage = (ImageView) itemView.findViewById(R.id.hubImage);

        context = itemView.getContext();

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        System.out.println("****DEBUG**** Item selezionato: " + this.nomeCustomCentralina.getText());

        // Start dell'activity relativa alla lista dei devices relativi all'hub selezionato
        Intent intent = new Intent(context, DevicesActivity.class);
        Bundle b = new Bundle();
        b.putString("nomeCentralina", this.nomeHardwareCentralina.getText().toString());
        intent.putExtras(b);

        context.startActivity(intent);
    }
}
