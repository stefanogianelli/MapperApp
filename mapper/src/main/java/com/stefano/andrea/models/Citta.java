package com.stefano.andrea.models;

/**
 * Citta
 */
public class Citta {

    private long id;
    private String nome;
    private String nazione;
    private long latitudine;
    private long longitudine;

    public Citta(long id, String nome, String nazione, long latitudine, long longitudine) {
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

    public long getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(long latitudine) {
        this.latitudine = latitudine;
    }

    public long getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(long longitudine) {
        this.longitudine = longitudine;
    }
}
