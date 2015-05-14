package com.stefano.andrea.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Citta
 */
public class Citta implements Parcelable {

    private long id;
    private long idCitta;
    private long idViaggio;
    private String nome;
    private String nazione;
    private double latitudine;
    private double longitudine;
    private int countPostiVisitati;
    private int countPosti;
    private int countFoto;

    public Citta () {
        this.id = -1;
    }

    public Citta (Parcel pc) {
        id = pc.readLong();
        idCitta = pc.readLong();
        idViaggio = pc.readLong();
        nome = pc.readString();
        nazione = pc.readString();
        latitudine = pc.readDouble();
        longitudine = pc.readDouble();
        countPostiVisitati = pc.readInt();
        countPosti = pc.readInt();
        countFoto = pc.readInt();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdCitta() {
        return idCitta;
    }

    public void setIdCitta(long idCitta) {
        this.idCitta = idCitta;
    }

    public long getIdViaggio() {
        return idViaggio;
    }

    public void setIdViaggio(long idViaggio) {
        this.idViaggio = idViaggio;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNazione() {
        return nazione;
    }

    public void setNazione(String nazione) {
        this.nazione = nazione;
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

    public int getCountPostiVisitati() {
        return countPostiVisitati;
    }

    public void setCountPostiVisitati(int countPostiVisitati) {
        this.countPostiVisitati = countPostiVisitati;
    }

    public int getCountPosti() {
        return countPosti;
    }

    public void setCountPosti(int countPosti) {
        this.countPosti = countPosti;
    }

    public int getCountFoto() {
        return countFoto;
    }

    public void setCountFoto(int countFoto) {
        this.countFoto = countFoto;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(idCitta);
        dest.writeLong(idViaggio);
        dest.writeString(nome);
        dest.writeString(nazione);
        dest.writeDouble(latitudine);
        dest.writeDouble(longitudine);
        dest.writeInt(countPostiVisitati);
        dest.writeInt(countPosti);
        dest.writeInt(countFoto);
    }

    public static final Parcelable.Creator<Citta> CREATOR = new Parcelable.Creator<Citta>() {
        @Override
        public Citta createFromParcel(Parcel source) {
            return new Citta(source);
        }

        @Override
        public Citta[] newArray(int size) {
            return new Citta[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (o instanceof Citta) {
            if (this == o)
                return true;
            Citta c = (Citta) o;
            return this.nome.equals(c.getNome()) && this.nazione.equals(c.getNazione());
        } else
            return false;
    }
}