package com.iot_rest_application.iothink_unina.utilities;

import com.google.firebase.database.DatabaseReference;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iot_rest_application.iothink_unina.utilities.centralina.Centralina;
import com.iot_rest_application.iothink_unina.utilities.device.Device;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FirebaseHelper {

    private DatabaseReference db;
    private String dbPath;
    private ArrayList<Centralina> centraline_rest;
    private ArrayList<Device> devices_rest;
    private ArrayList<String> rooms;
    private String uid;
    private String idToken;
    private static FirebaseHelper firebaseHelper = null;

    private FirebaseHelper() {
        this.centraline_rest = new ArrayList<>();
        this.devices_rest = new ArrayList<>();
        this.rooms = new ArrayList<>();
    }

    public static FirebaseHelper getInstance() {
        if (firebaseHelper == null)
            firebaseHelper = new FirebaseHelper();

        return firebaseHelper;
    }

    public DatabaseReference getDb() {
        return db;
    }

    public void setDb(DatabaseReference db) {
        this.db = db;
        this.dbPath = db.toString();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public boolean checkUser() throws ExecutionException, InterruptedException {

        String user = new RestRequest().execute(this.dbPath + "/users/" + this.uid + ".json?auth=" + this.idToken).get();
        boolean trovato = false;

        if(!user.equals("null")){
            trovato = true;
        }

        return trovato;
    }

    public ArrayList<String> retrieve_rooms(String hub) throws ExecutionException, InterruptedException {
        System.out.println("****DEBUG**** https://iothinkunina-a19a0.firebaseio.com/users/" + this.uid + "/centraline/" + hub + "/rooms.json?auth=" + this.idToken);
        String rooms = new RestRequest().execute(this.dbPath + "/users/" + this.uid + "/centraline/" + hub + "/rooms.json?auth=" + this.idToken).get();
        System.out.println("****DEBUG**** " + rooms);

        this.rooms.clear();

        if(!rooms.equals("null")){
            JsonObject jsonCentraline = (JsonObject) new JsonParser().parse(rooms);

            System.out.println(this.rooms.size());
            System.out.println("****DEBUG**** KeySet: " + jsonCentraline.keySet());

            for(String room: jsonCentraline.keySet()){
                this.rooms.add(room.replace("\"", ""));
            }

            System.out.println("****DEBUG**** ROOM_SIZE: " + this.rooms.size());
        }

        return this.rooms;
    }
}
