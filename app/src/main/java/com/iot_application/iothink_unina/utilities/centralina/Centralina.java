package com.iot_application.iothink_unina.utilities.centralina;

public class Centralina {

    private String nomeCustom;
    private String nomeHardware;
    private String cmd;

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

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
