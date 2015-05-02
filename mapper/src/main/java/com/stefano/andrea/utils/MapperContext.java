package com.stefano.andrea.utils;

/**
 * MapperContext
 */
public class MapperContext {

    private static MapperContext context;
    private long idViaggio;
    private String nomeViaggio;
    private long idCitta;
    private String nomeCitta;
    private long idPosto;

    private MapperContext () {
        idViaggio = -1;
        idCitta = -1;
        idPosto = -1;
        nomeViaggio = null;
        nomeCitta = null;
    }

    public static MapperContext getInstance () {
        if (context == null)
            context = new MapperContext();
        return context;
    }

    public long getIdViaggio() {
        return idViaggio;
    }

    public void setIdViaggio(long idViaggio) {
        this.idViaggio = idViaggio;
    }

    public String getNomeViaggio() {
        return nomeViaggio;
    }

    public void setNomeViaggio(String nomeViaggio) {
        this.nomeViaggio = nomeViaggio;
    }

    public long getIdCitta() {
        return idCitta;
    }

    public void setIdCitta(long idCitta) {
        this.idCitta = idCitta;
    }

    public String getNomeCitta() {
        return nomeCitta;
    }

    public void setNomeCitta(String nomeCitta) {
        this.nomeCitta = nomeCitta;
    }

    public long getIdPosto() {
        return idPosto;
    }

    public void setIdPosto(long idPosto) {
        this.idPosto = idPosto;
    }
}
