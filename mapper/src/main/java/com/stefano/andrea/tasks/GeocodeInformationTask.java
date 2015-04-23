package com.stefano.andrea.tasks;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * GeocodeInformationTask
 */
public class GeocodeInformationTask extends AsyncTask<Void, Void, LatLng> {

    private String mIndirizzo;

    public GeocodeInformationTask (String indirizzo) {
        mIndirizzo = indirizzo;
    }

    @Override
    protected LatLng doInBackground(Void... params) {
        JSONObject geoInfo = getGeocodeInformations(mIndirizzo);
        double latitudine = 0;
        double longitudine = 0;
        if (geoInfo != null) {
            latitudine = getLatitudine(geoInfo);
            longitudine = getLongitudine(geoInfo);
        }
        return new LatLng(latitudine, longitudine);
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
