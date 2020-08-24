package com.iot_rest_application.iothink_unina.utilities.centralina;

public class Centralina {

    private String nomeCustom;
    private String nomeHardware;
    private String bt_address;

    public Centralina() { }

    public Centralina(String nomeCu, String nomeHa){
        this.nomeCustom = nomeCu;
        this.nomeHardware = nomeHa;

    }

    public String getNomeCustom() {
        return nomeCustom;
    }

    public void setNomeCustom(String nomeCustom) {
        this.nomeCustom = nomeCustom;
    }

    public String getNomeHardware() {
        return nomeHardware;
    }

    public void setNomeHardware(String nomeHardware) {
        this.nomeHardware = nomeHardware;
    }

    public String getBt_address() {
        return bt_address;
    }

    public void setBt_address(String bt_address) {
        this.bt_address = bt_address;
    }
}
