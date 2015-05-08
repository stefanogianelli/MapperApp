package com.stefano.andrea.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Foto
 */
public class Foto implements Parcelable {

    private long id;
    private String path;
    private int data;
    private double latitudine;
    private double longitudine;
    private long idViaggio;
    private long idCitta;
    private long idPosto;
    private int idMediaStore;
    private int camera;

    public Foto () {
        this.id = -1;
        this.idPosto = -1;
        this.camera = 0;
    }

    public Foto (Parcel pc) {
        id = pc.readLong();
        path = pc.readString();
        data = pc.readInt();
        latitudine = pc.readDouble();
        longitudine = pc.readDouble();
        idViaggio = pc.readLong();
        idCitta = pc.readLong();
        idPosto = pc.readLong();
        idMediaStore = pc.readInt();
        camera = pc.readInt();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public long getIdViaggio() {
        return idViaggio;
    }

    public void setIdViaggio(long idViaggio) {
        this.idViaggio = idViaggio;
    }

    public long getIdCitta() {
        return idCitta;
    }

    public void setIdCitta(long idCitta) {
        this.idCitta = idCitta;
    }

    public long getIdPosto() {
        return idPosto;
    }

    public void setIdPosto(long idPosto) {
        this.idPosto = idPosto;
    }

    public int getIdMediaStore() {
        return idMediaStore;
    }

    public void setIdMediaStore(int idMediaStore) {
        this.idMediaStore = idMediaStore;
    }

    public int getCamera() {
        return camera;
    }

    public void setCamera(int camera) {
        this.camera = camera;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(path);
        dest.writeInt(data);
        dest.writeDouble(latitudine);
        dest.writeDouble(longitudine);
        dest.writeLong(idViaggio);
        dest.writeLong(idCitta);
        dest.writeLong(idPosto);
        dest.writeInt(idMediaStore);
        dest.writeInt(camera);
    }

    public static final Parcelable.Creator<Foto> CREATOR = new Parcelable.Creator<Foto>() {
        @Override
        public Foto createFromParcel(Parcel source) {
            return new Foto(source);
        }

        @Override
        public Foto[] newArray(int size) {
            return new Foto[size];
        }
    };

}