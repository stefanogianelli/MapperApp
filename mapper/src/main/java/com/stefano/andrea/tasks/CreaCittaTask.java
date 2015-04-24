package com.stefano.andrea.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.stefano.andrea.activities.BuildConfig;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.CittaAdapter;
import com.stefano.andrea.providers.MapperContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * CreaCittaTask
 */
public class CreaCittaTask extends AsyncTask<String, Void, Uri> {

    private ContentResolver mResolver;
    private CittaAdapter mAdapter;
    private long mIdViaggio;
    private ProgressDialog mDialog;
    private Context mContext;

    public CreaCittaTask (Activity activity, ContentResolver resolver, CittaAdapter adapter, long idViaggio) {
        mResolver = resolver;
        mAdapter = adapter;
        mIdViaggio = idViaggio;
        mDialog = new ProgressDialog(activity);
        mContext = activity.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.setMessage(mContext.getResources().getString(R.string.nuova_citta_loading_dialog));
        mDialog.show();
    }

    @Override
    protected void onPostExecute(Uri uri) {
        super.onPostExecute(uri);
        mDialog.dismiss();
    }

    @Override
    protected Uri doInBackground(String... params) {
        if (params.length == 2) {
            String nome = params[0];
            String nazione = params[1];
            long idCitta = this.getDatiCitta(nome, nazione);
            if (idCitta == -1)
                idCitta = this.creaCitta(nome, nazione);
            if (idCitta != -1) {
                ContentValues values = new ContentValues();
                values.put(MapperContract.Citta.ID_VIAGGIO, mIdViaggio);
                values.put(MapperContract.Citta.ID_DATI_CITTA, idCitta);
                values.put(MapperContract.Citta.PERCENTUALE, 0);
                Uri uri = mResolver.insert(MapperContract.Citta.CONTENT_URI, values);
                long id = Long.parseLong(uri.getLastPathSegment());
                if (id != -1) {
                    //recupero informazioni sulla citta
                    Uri query = ContentUris.withAppendedId(MapperContract.DatiCitta.CONTENT_URI, idCitta);
                    String[] projetion = {MapperContract.DatiCitta.LATITUDINE, MapperContract.DatiCitta.LONGITUDINE};
                    Cursor c = mResolver.query(query, projetion, null, null, MapperContract.DatiCitta.DEFAULT_SORT);
                    if (c != null) {
                        c.moveToNext();
                        double lon = c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE));
                        double lat = c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE));
                        mAdapter.creaNuovaCitta(id, nome, nazione, lat, lon);
                        c.close();
                        return uri;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Verifica se esistono nel database i dati di una citta
     * @param nome Il nome della citta
     * @param nazione La nazione della citta
     * @return l'id della citta se esiste, altrimenti -1
     */
    private long getDatiCitta (String nome, String nazione) {
        String [] projection = {MapperContract.DatiCitta.ID};
        String selection = MapperContract.DatiCitta.NOME + "=? AND " + MapperContract.DatiCitta.NAZIONE + "=?";
        String [] selectionArgs = {nome, nazione};
        Cursor c = mResolver.query(MapperContract.DatiCitta.CONTENT_URI, projection, selection, selectionArgs, MapperContract.DatiCitta.DEFAULT_SORT);
        long id = -1;
        if (c != null && c.getCount() > 0) {
            c.moveToNext();
            id =  c.getLong(c.getColumnIndex(MapperContract.DatiCitta.ID));
            c.close();
        }
        return id;
    }

    /**
     * Crea una nuova citta nel database
     * @param nome Il nome della citta da creare
     * @param nazione La nazione nella quale si trova la citta
     * @return l'id della citta creata, altrimenti -1
     */
    private long creaCitta (final String nome, final String nazione) {
        ContentValues values = new ContentValues();
        values.put(MapperContract.DatiCitta.NOME, nome);
        values.put(MapperContract.DatiCitta.NAZIONE, nazione);
        JSONObject geoInfo = getGeocodeInformations(nome + "," + nazione);
        if (geoInfo != null) {
            values.put(MapperContract.DatiCitta.LATITUDINE, this.getLatitudine(geoInfo));
            values.put(MapperContract.DatiCitta.LONGITUDINE, this.getLongitudine(geoInfo));
            if (BuildConfig.DEBUG)
                Log.v("CittaHelper", nome + " - Lat: " + this.getLatitudine(geoInfo) + " , Lng: " + this.getLongitudine(geoInfo));
            Uri uri = mResolver.insert(MapperContract.DatiCitta.CONTENT_URI, values);
            if (uri != null)
                return Long.parseLong(uri.getLastPathSegment());
        }
        return -1;
    }

    private JSONObject getGeocodeInformations (String indirizzo) {
        try {
            HttpURLConnection conn;
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
