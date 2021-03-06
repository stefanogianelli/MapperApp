package com.stefano.andrea.models;

/**
 * Posto
 */
public class Posto {

    private long id;
    private long idCitta;
    private long idLuogo;
    private boolean visitato;
    private String nome;
    private double latitudine;
    private double longitudine;

    public Posto () {
        this.id = -1;
    }

    public Posto (long id, String nome) {
        this.id = id;
        this.nome = nome;
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

    public long getIdLuogo() {
        return idLuogo;
    }

    public void setIdLuogo(long idLuogo) {
        this.idLuogo = idLuogo;
    }

    public boolean isVisitato() {
        return visitato;
    }

    public int getVisitato () {
        if (visitato)
            return 1;
        else
            return 0;
    }

    public void setVisitato(boolean visitato) {
        this.visitato = visitato;
    }

    public void setVisitato (int visitato) {
        if (visitato == 1)
            this.setVisitato(true);
        else if (visitato == 0)
            this.setVisitato(false);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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
