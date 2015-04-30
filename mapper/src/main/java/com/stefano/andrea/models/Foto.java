package com.stefano.andrea.models;

import android.graphics.Bitmap;

/**
 * Foto
 */
public class Foto {

    private long id;
    private Bitmap image;
    private String path;
    private String title;
    private double latitudine;
    private double longitudine;
    private double idCitta;

    public Foto () { }

    public Foto(Bitmap image, String title) {
        this.image = image;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public double getIdCitta() {
        return idCitta;
    }

    public void setIdCitta(double idCitta) {
        this.idCitta = idCitta;
    }
}