package com.stefano.andrea.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * LocationHelper
 */
public class LocationHelper {

    public static JSONObject getGeocodeInformations (String query) throws IOException, JSONException {
        query = query.replace(" ", "%20");
        HttpURLConnection conn;
        StringBuilder jsonResults = new StringBuilder();
        String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?address=" + query;

        URL url = new URL(googleMapUrl);
        conn = (HttpURLConnection) url.openConnection();
        InputStreamReader in = new InputStreamReader(
                conn.getInputStream());
        int read;
        char[] buff = new char[1024];
        while ((read = in.read(buff)) != -1) {
            jsonResults.append(buff, 0, read);
        }

        JSONObject jsonObj = new JSONObject(jsonResults.toString());
        JSONArray resultJsonArray = jsonObj.getJSONArray("results");
        JSONObject before_geometry_jsonObj = resultJsonArray.getJSONObject(0);
        JSONObject geometry_jsonObj = before_geometry_jsonObj.getJSONObject("geometry");
        return geometry_jsonObj.getJSONObject("location");
    }

    public static double getLongitudine (JSONObject location) {
        double lng = 0;
        try {
            String lng_helper = location.getString("lng");
            lng = Double.valueOf(lng_helper);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lng;
    }

    public static double getLatitudine (JSONObject location) {
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
