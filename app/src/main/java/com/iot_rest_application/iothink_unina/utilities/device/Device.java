package com.iot_rest_application.iothink_unina.utilities.device;

public class Device {

    public String name;
    public String bt_addr;
    public String type;
    public String uuid;
    public String nomeCustom;
    public String room;
    public String centralina;

    public Device() {}

    public Device(String bt_addr) {
        this.bt_addr = bt_addr;
    }

    public Device(String name, String bt_addr, String type, String uuid, String nomeCustom, String room, String centralina) {
        this.name = name;
        this.bt_addr = bt_addr;
        this.type = type;
        this.uuid = uuid;
        this.nomeCustom = nomeCustom;
        this.room = room;
        this.centralina = centralina;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBt_addr() {
        return bt_addr;
    }

    public void setBt_addr(String bt_addr) {
        this.bt_addr = bt_addr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNomeCustom() {
        return nomeCustom;
    }

    public void setNomeCustom(String nomeCustom) {
        this.nomeCustom = nomeCustom;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getCentralina() {
        return centralina;
    }

    public void setCentralina(String centralina) {
        this.centralina = centralina;
    }
}
