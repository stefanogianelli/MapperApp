package com.stefano.andrea.models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * GeoInfo
 */
public class GeoInfo implements ClusterItem {

    private Bitmap miniatura;
    private long id;
    private String nome;
    private double latitudine;
    private double longitudine;

    public Bitmap getMiniatura() {
        return miniatura;
    }

    public void setMiniatura(Bitmap miniatura) {
        this.miniatura = miniatura;
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

    @Override
    public LatLng getPosition() {
        return new LatLng(latitudine, longitudine);
    }

}
