package com.iot_application.iothink_unina.utilities.centralina;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iot_application.iothink_unina.R;

import java.util.ArrayList;

public class CentralinaAdapter extends RecyclerView.Adapter<CentralinaViewHolder> implements Filterable {

    private Context c;
    private ArrayList<Centralina> centraline;
    private ArrayList<Centralina> centralineFull;


    public CentralinaAdapter(Context c, DatabaseReference dbRef) {
        this.c = c;
        this.centraline = new ArrayList<>();
        centralineFull = new ArrayList<>();

        // Aggiunta listener per la lettura delle centraline dal database
        dbRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                // Re-inizializzazione del ArrayList centraline
                CentralinaAdapter.this.centraline.clear();

                // Caricamento ArrayList centraline
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    String nomeHardware = postSnapshot.getKey().toString();
                    String nomeCustom = postSnapshot.child("nome").getValue().toString();

                    System.out.println("****DEBUG**** nomeHardware: " + nomeHardware);
                    System.out.println("****DEBUG**** nomeCustom: " + nomeCustom);

                    Centralina centralina = new Centralina(nomeCustom, nomeHardware);
                    centraline.add(centralina);
                    centralineFull.add(centralina);
                }

                // Visualizzazione messaggio "nessun hub registrato" nel caso in cui non ci siano centraline
                TextView noHubLabel = (TextView) ((Activity) CentralinaAdapter.this.c).findViewById(R.id.noHubTextView);
                if(centraline.isEmpty()){
                    noHubLabel.setText("Nessun centralina registrato.");
                } else {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    noHubLabel.setText("Centraline di \n" + currentUser.getEmail());
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
    public CentralinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.centralina_model, parent, false);
        return new CentralinaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CentralinaViewHolder holder, int position) {

        final Centralina c = centraline.get(position);

        // Visualizzazione nomeCustomCentralina e nomeHardwareCentralina
        holder.nomeCustomCentralina.setText(c.getNomeCustom());
        holder.nomeHardwareCentralina.setText(c.getNomeHardware());

        // Caricamento immagine associata alla centralina
        setHubImage(holder);
    }

    private void setHubImage(final CentralinaViewHolder holder){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();

        // Caricamento immagine centralina da Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("users/" + uid + "/" + holder.nomeHardwareCentralina.getText().toString() + ".png");

        final long ONE_MEGABYTE = 1024 * 1024;

        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                System.out.println("+***DEBUG**** Caricamento immagine effettuato con successo");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.hubImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

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

    // Implementazione funzione ricerca tra le centraline in base all'input dell'utente
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
