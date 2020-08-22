package com.iot_rest_application.iothink_unina.utilities.centralina;

public class Centralina {

    public String nome;
    public String bt_address;

    public Centralina() { }

    public Centralina(String nome){
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getBt_address() {
        return bt_address;
    }

    public void setBt_address(String bt_address) {
        this.bt_address = bt_address;
    }

}
