package com.iot_rest_application.iothink_unina.utilities.centralina;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iot_rest_application.iothink_unina.R;
import com.iot_rest_application.iothink_unina.utilities.device.Device;

import java.util.ArrayList;

public class CentralinaAdapter extends RecyclerView.Adapter<CentralinaViewHolder> implements Filterable {

    private Context c;
    private ArrayList<Centralina> centraline;
    private ArrayList<Centralina> centralineFull;

    public CentralinaAdapter(Context c, ArrayList<Centralina> centraline) {
        this.c = c;
        this.centraline = centraline;
        centralineFull = new ArrayList<>(centraline);
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

        holder.nomeCentralina.setText(c.getNomeCustom());
        holder.nomeHardwareCentralina.setText(c.getNomeHardware());
        setHubImage(holder);
    }

    private void setHubImage(final CentralinaViewHolder holder){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();

        // Create a storage reference from our app
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference islandRef = storageRef.child("users/" + uid + "/" + holder.nomeHardwareCentralina.getText().toString() + ".png");

        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                System.out.println("+***DEBUG**** Caricamento immagine effettuato con succeso");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.hubImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                System.out.println("+***DEBUG**** Caricamento immagine fallito");
                Drawable dr = c.getResources().getDrawable(R.drawable.home_icon);
                holder.hubImage.setImageDrawable(dr);
            }
        });
    }

    @Override
    public int getItemCount() {
        return centraline.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Centralina> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(centralineFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Centralina centralina : centralineFull) {
                    if (centralina.getNomeCustom().toLowerCase().contains(filterPattern)) {
                        filteredList.add(centralina);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            centraline.clear();
            centraline.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };
}
