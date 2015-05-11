package com.stefano.andrea.models;

/**
 * GeoInfo
 */
public class GeoInfo {

    private long id;
    private String nome;
    private double latitudine;
    private double longitudine;
    private int countFoto;

    public GeoInfo(long id, String nome, double latitudine, double longitudine, int countFoto) {
        this.id = id;
        this.nome = nome;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.countFoto = countFoto;
    }

    public long getId() {
        return id;
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

    public int getCountFoto() {
        return countFoto;
    }
}
