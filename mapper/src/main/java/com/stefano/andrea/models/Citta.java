package com.stefano.andrea.models;

/**
 * Citta
 */
public class Citta {

    private long id;
    private String nome;
    private String nazione;
    private double latitudine;
    private double longitudine;

    public Citta(long id, String nome, String nazione, double latitudine, double longitudine) {
        this.id = id;
        this.nome = nome;
        this.nazione = nazione;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNazione() {
        return nazione;
    }

    public void setNazione(String nazione) {
        this.nazione = nazione;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }
}