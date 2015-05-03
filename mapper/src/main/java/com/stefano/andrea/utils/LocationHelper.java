package com.stefano.andrea.utils;

import android.media.ExifInterface;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

/**
 * LocationHelper
 */
public class LocationHelper {

    /**
     * Recupera le coordinate geografiche associate ad una foto
     * @param filePath Il percorso dell'immagine
     * @return Le coordinate dell'immagine
     * @throws IOException Quando avviene un'errore nell'apertura del file
     */
    public static LatLng getCoordinatesFromExif (String filePath) throws IOException {
        double lat, lon;
        lat = lon = 0;
        ExifInterface exif = new ExifInterface(filePath);
        String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        if(latitude !=null && latitudeRef !=null && longitude != null && longitudeRef !=null) {
            if (latitudeRef.equals("N")) {
                lat = convertToDegree(latitude);
            } else {
                lat = 0 - convertToDegree(latitude);
            }
            if (longitudeRef.equals("E")) {
                lon = convertToDegree(longitude);
            } else {
                lon = 0 - convertToDegree(longitude);
            }
        }
        return new LatLng(lat, lon);
    }

    /**
     * Converte in gradi le coordinate ottenute dai dati exif
     * @param stringDMS La coordinata da convertire
     * @return Il valore della coordinata in gradi
     */
    private static double convertToDegree (String stringDMS) {
        double result = 0;
        String [] DMS = stringDMS.split(",", 3);

        String [] stringD = DMS[0].split("/", 2);
        Double D0 = Double.valueOf(stringD[0]);
        Double D1 = Double.valueOf(stringD[1]);
        Double FloatD = D0/D1;

        String [] stringM = DMS[1].split("/", 2);
        Double M0 = Double.valueOf(stringM[0]);
        Double M1 = Double.valueOf(stringM[1]);
        Double FloatM = M0/M1;

        String [] stringS = DMS[2].split("/", 2);
        Double S0 = Double.valueOf(stringS[0]);
        Double S1 = Double.valueOf(stringS[1]);
        Double FloatS = S0/S1;

        result = FloatD + (FloatM/60) + (FloatS/3600);

        return result;
    }


}
