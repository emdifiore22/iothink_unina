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
    private String uid;
    private String idToken;
    private static FirebaseHelper firebaseHelper = null;

    private FirebaseHelper() {
        this.centraline_rest = new ArrayList<>();
        this.devices_rest = new ArrayList<>();
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

    /*
    public FirebaseHelper(DatabaseReference db, String uid) {
        this.db = db;
        this.centraline_rest = new ArrayList<>();
        this.devices_rest = new ArrayList<>();
        this.uid = uid;
        this.dbPath = db.toString();
    }
    */

    public boolean checkUser() throws ExecutionException, InterruptedException {

        String user = new RestRequest().execute(this.dbPath + "/users/" + this.uid + ".json?auth=" + this.idToken).get();
        boolean trovato = false;

        if(!user.equals("null")){
            trovato = true;
        }

        return trovato;
    }

    public ArrayList<Centralina> retrieve_hub() throws ExecutionException, InterruptedException {

        System.out.println("****DEBUG**** https://iothinkunina-a19a0.firebaseio.com/users/" + this.uid + "/centraline.json?auth=" + this.idToken);
        String centraline = new RestRequest().execute(this.dbPath + "/users/" + this.uid + "/centraline.json?auth=" + this.idToken).get();
        System.out.println("****DEBUG**** " + centraline);
        this.centraline_rest.clear();

        if(!centraline.equals("null")){
            JsonObject jsonCentraline = (JsonObject) new JsonParser().parse(centraline);

            System.out.println(centraline_rest.size());
            System.out.println("****DEBUG**** KeySet: " + jsonCentraline.keySet());

            for(String centralina: jsonCentraline.keySet()){
                JsonObject hub = (JsonObject) jsonCentraline.get(centralina);
                Centralina c = new Centralina(hub.get("nome").toString().replace("\"", ""));
                this.centraline_rest.add(c);
            }

            System.out.println("****DEBUG**** CENTRALINE_REST_SIZE: " + this.centraline_rest.size());
        }

        return this.centraline_rest;
    }

    public ArrayList<Device> retrieve_devices(String hub) throws ExecutionException, InterruptedException {
        System.out.println("****DEBUG**** https://iothinkunina-a19a0.firebaseio.com/users/" + this.uid + "/centraline/" + hub + "/devices.json");
        String devices = new RestRequest().execute(this.dbPath + "/users/" + this.uid + "/centraline/" + hub + "/devices.json?auth=" + this.idToken).get();

        if(!devices.equals("null")){

            JsonObject jsonDevices = (JsonObject) new JsonParser().parse(devices);

            this.devices_rest.clear();

            System.out.println(devices_rest.size());
            System.out.println("****DEBUG**** KeySet: " + jsonDevices.keySet());

            for(String bt_address_device: jsonDevices.keySet()){
                JsonObject device = (JsonObject) jsonDevices.get(bt_address_device);
                Device d = new Device(device.get("name").toString().replace("\"", ""),
                        device.get("bt_addr").toString().replace("\"", ""),
                        device.get("type").toString().replace("\"", ""),
                        device.get("uuid").toString().replace("\"", ""),
                        device.get("nomeCustom").toString().replace("\"", ""),
                        device.get("room").toString().replace("\"", ""),
                        device.get("centralina").toString().replace("\"", ""));
                this.devices_rest.add(d);
            }

            System.out.println("****DEBUG**** DEVICE_REST_SIZE: " + this.devices_rest.size());

        }

        return this.devices_rest;
    }
}
