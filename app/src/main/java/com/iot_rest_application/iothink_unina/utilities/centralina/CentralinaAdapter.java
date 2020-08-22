package com.iot_rest_application.iothink_unina.utilities.centralina;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iot_rest_application.iothink_unina.R;

import java.util.ArrayList;

public class CentralinaAdapter extends RecyclerView.Adapter<CentralinaViewHolder> {

    Context c;
    ArrayList<Centralina> centraline;

    public CentralinaAdapter(Context c, ArrayList<Centralina> centraline) {
        this.c = c;
        this.centraline = centraline;
    }

    @NonNull
    @Override
    public CentralinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.centralina_model, parent, false);
        return new CentralinaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CentralinaViewHolder holder, int position) {

        final Centralina c = centraline.get(position);

        holder.nomeCentralina.setText(c.nome);

    }

    @Override
    public int getItemCount() {
        return centraline.size();
    }
}
