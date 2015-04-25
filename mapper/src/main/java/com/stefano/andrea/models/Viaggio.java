package com.stefano.andrea.models;

/**
 * Viaggio
 */
public class Viaggio {

    private long id;
    private String nome;

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

    @Override
    public String toString() {
        return "Viaggio{" +
                "nome='" + nome + '\'' +
                '}';
    }
}