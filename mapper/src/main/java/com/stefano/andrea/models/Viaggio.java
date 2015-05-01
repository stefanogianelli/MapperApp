package com.stefano.andrea.models;

/**
 * Viaggio
 */
public class Viaggio {

    private long id;
    private String nome;
    private int countCitta;
    private int countPosti;
    private int countFoto;
    private String pathFoto;

    public Viaggio () {}

    public Viaggio (String nome) {
        this.nome = nome;
    }

    public Viaggio (long id, String nome) {
        this.id = id;
        this.nome = nome;
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

    public int getCountCitta() {
        return countCitta;
    }

    public void setCountCitta(int countCitta) {
        this.countCitta = countCitta;
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

    public String getPathFoto() {
        return pathFoto;
    }

    public void setPathFoto(String pathFoto) {
        this.pathFoto = pathFoto;
    }
}