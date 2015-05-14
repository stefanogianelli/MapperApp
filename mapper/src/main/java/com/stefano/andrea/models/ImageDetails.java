package com.stefano.andrea.models;

/**
 * ImageDetails
 */
public class ImageDetails {

    private int idMediaStore;
    private int data;
    private double latitudine;
    private double longitudine;
    private String mimeType;
    private String width;
    private String height;
    private int size;

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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
