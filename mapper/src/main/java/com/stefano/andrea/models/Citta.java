package com.stefano.andrea.models;

/**
 * Citta
 */
public class Citta {

    private long id;
    private long idCitta;
    private long idViaggio;
    private String nome;
    private String nazione;
    private double latitudine;
    private double longitudine;
    private int countPostiVisitati;
    private int countPosti;
    private int countFoto;

    public Citta () {
        this.id = -1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdCitta() {
        return idCitta;
    }

    public void setIdCitta(long idCitta) {
        this.idCitta = idCitta;
    }

    public long getIdViaggio() {
        return idViaggio;
    }

    public void setIdViaggio(long idViaggio) {
        this.idViaggio = idViaggio;
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

    public int getCountPostiVisitati() {
        return countPostiVisitati;
    }

    public void setCountPostiVisitati(int countPostiVisitati) {
        this.countPostiVisitati = countPostiVisitati;
    }

    public int getCountPosti() {
        return countPosti;
    }

    public void setCountPosti(int countPosti) {
        this.countPosti = countPosti;
    }

    public int getCountFoto() {
        return countFoto;
    }

    public void setCountFoto(int countFoto) {
        this.countFoto = countFoto;
    }
}