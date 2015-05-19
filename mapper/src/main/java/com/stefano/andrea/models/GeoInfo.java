package com.stefano.andrea.models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.List;

/**
 * GeoInfo
 */
public class GeoInfo implements ClusterItem {

    private List<Bitmap> miniature;
    private long id;
    private String nome;
    private double latitudine;
    private double longitudine;
    private int countFoto;

    public List<Bitmap> getMiniature() {
        return miniature;
    }

    public void setMiniature(List<Bitmap> miniature) {
        this.miniature = miniature;
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

    public int getCountFoto() {
        return countFoto;
    }

    public void setCountFoto(int countFoto) {
        this.countFoto = countFoto;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(latitudine, longitudine);
    }

}
