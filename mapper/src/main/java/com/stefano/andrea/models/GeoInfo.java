package com.stefano.andrea.models;

/**
 * GeoInfo
 */
public class GeoInfo {

    private String nome;
    private double latitudine;
    private double longitudine;

    public GeoInfo(String nome, double latitudine, double longitudine) {
        this.nome = nome;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
    }

    public String getNome() {
        return nome;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }
}
