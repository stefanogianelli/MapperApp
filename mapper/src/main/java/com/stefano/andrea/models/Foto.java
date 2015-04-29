package com.stefano.andrea.models;

import android.graphics.Bitmap;

/**
 * Foto
 */
public class Foto {

    private Bitmap image;
    private String title;

    public Foto () { }

    public Foto(Bitmap image, String title) {
        this.image = image;
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}