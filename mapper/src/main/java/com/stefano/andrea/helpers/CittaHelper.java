package com.stefano.andrea.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.stefano.andrea.providers.MapperContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * CittaHelper
 */
public class CittaHelper {

    private ContentResolver mResolver;

    public CittaHelper (ContentResolver resolver) {
        mResolver = resolver;
    }

    /**
     * Verifica se esistono nel database i dati di una citta
     * @param nome Il nome della citta
     * @param nazione La nazione della citta
     * @return l'id della citta se esiste, altrimenti -1
     */
    public long getDatiCitta (String nome, String nazione) {
        String [] projection = {MapperContract.DatiCitta.ID};
        String selection = MapperContract.DatiCitta.NOME + "=? AND " + MapperContract.DatiCitta.NAZIONE + "=?";
        String [] selectionArgs = {nome, nazione};
        Cursor c = mResolver.query(MapperContract.DatiCitta.CONTENT_URI, projection, selection, selectionArgs, MapperContract.DatiCitta.DEFAULT_SORT);
        long id;
        if (c != null && c.getCount() > 0) {
            c.moveToNext();
            id =  c.getLong(c.getColumnIndex(MapperContract.DatiCitta.ID));
        } else {
            id = -1;
        }
        c.close();
        return id;
    }

    /**
     * Crea una nuova citta nel database
     * @param nome Il nome della citta da creare
     * @param nazione La nazione nella quale si trova la citta
     * @return l'id della citta creata, altrimenti -1
     */
    public long creaCitta (String nome, String nazione) {
        ContentValues values = new ContentValues();
        values.put(MapperContract.DatiCitta.NOME, nome);
        values.put(MapperContract.DatiCitta.NAZIONE, nazione);
        JSONObject loc = getGeocodeInformations(nome + "," + nazione);
        double latitudine = getLatitudine(loc);
        double longitudine = getLongitudine(loc);
        values.put(MapperContract.DatiCitta.LATITUDINE, latitudine);
        values.put(MapperContract.DatiCitta.LONGITUDINE, longitudine);
        Uri uri = mResolver.insert(MapperContract.DatiCitta.CONTENT_URI, values);
        if (uri != null)
            return Long.parseLong(uri.getLastPathSegment());
        else
            return -1;
    }

    private JSONObject getGeocodeInformations (String indirizzo) {
        try {
            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?address=" + indirizzo;

            URL url = new URL(googleMapUrl);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(
                    conn.getInputStream());
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
            String a = "";

            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray resultJsonArray = jsonObj.getJSONArray("results");
            JSONObject before_geometry_jsonObj = resultJsonArray.getJSONObject(0);
            JSONObject geometry_jsonObj = before_geometry_jsonObj.getJSONObject("geometry");
            return geometry_jsonObj.getJSONObject("location");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private double getLongitudine (JSONObject location) {
        double lng = 0;
        try {
            String lng_helper = location.getString("lng");
            lng = Double.valueOf(lng_helper);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lng;
    }

    private double getLatitudine (JSONObject location) {
        double lat = 0;
        try {
            String lat_helper = location.getString("lat");
            lat = Double.valueOf(lat_helper);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lat;
    }
}
