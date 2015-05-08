package com.stefano.andrea.models;

/**
 * ImageDetails
 */
public class ImageDetails {

    private int idMediaStore;
    private int data;
    private double latitudine;
    private double longitudine;

    public ImageDetails () {
        idMediaStore = -1;
    }

    public int getIdMediaStore() {
        return idMediaStore;
    }

    public void setIdMediaStore(int idMediaStore) {
        this.idMediaStore = idMediaStore;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
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
