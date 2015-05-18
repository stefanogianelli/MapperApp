package com.stefano.andrea.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * GeoInfo
 */
public class GeoInfo implements ClusterItem {

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

    @Override
    public LatLng getPosition() {
        return new LatLng(this.getLatitudine(),this.getLongitudine());
    }

}
