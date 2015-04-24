package com.stefano.andrea.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.stefano.andrea.activities.BuildConfig;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.adapters.CittaAdapter;
import com.stefano.andrea.helpers.CommonAlertDialog;
import com.stefano.andrea.models.Citta;
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
public class CreaCittaTask extends AsyncTask<String, Void, Integer> {

    private static final int RESULT_OK = 0;
    private static final int RESULT_ERROR = 1;

    private ContentResolver mResolver;
    private CittaAdapter mAdapter;
    private ProgressDialog mDialog;
    private Activity mActivity;
    private Context mContext;
    private Citta mCitta;

    public CreaCittaTask (Activity activity, ContentResolver resolver, CittaAdapter adapter, long idViaggio) {
        mResolver = resolver;
        mAdapter = adapter;
        mDialog = new ProgressDialog(activity);
        mActivity = activity;
        mContext = activity.getApplicationContext();
        mCitta = new Citta();
        mCitta.setIdViaggio(idViaggio);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.setMessage(mContext.getResources().getString(R.string.nuova_citta_loading_dialog));
        mDialog.show();
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        mDialog.dismiss();
        if (result == RESULT_ERROR) {
            new CommonAlertDialog(mActivity, R.string.errore_nuova_citta_title_dialog, R.string.errore_nuova_citta_message_dialog);
        }
    }

    @Override
    protected Integer doInBackground(String... params) {
        long id = -1;
        if (params.length == 2) {
            mCitta.setNome(params[0]);
            mCitta.setNazione(params[1]);
            //verifico se la citta esiste gia nel database
            this.getDatiCitta();
            if (mCitta.getIdCitta() == -1)
                //creo la nuova citta
                this.creaCitta();
            if (mCitta.getIdCitta() != -1) {
                ContentValues values = new ContentValues();
                values.put(MapperContract.Citta.ID_VIAGGIO, mCitta.getIdViaggio());
                values.put(MapperContract.Citta.ID_DATI_CITTA, mCitta.getIdCitta());
                values.put(MapperContract.Citta.PERCENTUALE, 0);
                Uri uri = mResolver.insert(MapperContract.Citta.CONTENT_URI, values);
                id = Long.parseLong(uri.getLastPathSegment());
                if (id != -1) {
                    mCitta.setId(id);
                    mAdapter.creaNuovaCitta(mCitta);
                    return RESULT_OK;
                }
            }
        }
        return RESULT_ERROR;
    }

    /**
     * Verifica se esistono nel database i dati di una citta
     */
    private void getDatiCitta () {
        String selection = MapperContract.DatiCitta.NOME + "=? AND " + MapperContract.DatiCitta.NAZIONE + "=?";
        String [] selectionArgs = {mCitta.getNome(), mCitta.getNazione()};
        Cursor c = mResolver.query(MapperContract.DatiCitta.CONTENT_URI, MapperContract.DatiCitta.PROJECTION_ALL, selection, selectionArgs, MapperContract.DatiCitta.DEFAULT_SORT);
        long id = -1;
        if (c != null && c.getCount() > 0) {
            c.moveToNext();
            id =  c.getLong(c.getColumnIndex(MapperContract.DatiCitta.ID));
            mCitta.setLatitudine(c.getLong(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE)));
            mCitta.setLongitudine(c.getLong(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE)));
            c.close();
        }
        mCitta.setIdCitta(id);
    }

    /**
     * Crea una nuova citta nel database
     */
    private void creaCitta () {
        ContentValues values = new ContentValues();
        values.put(MapperContract.DatiCitta.NOME, mCitta.getNome());
        values.put(MapperContract.DatiCitta.NAZIONE, mCitta.getNazione());
        JSONObject geoInfo = getGeocodeInformations();
        long idCitta = 1;
        if (geoInfo != null) {
            double latitudine = this.getLatitudine(geoInfo);
            double longitudine = this.getLongitudine(geoInfo);
            mCitta.setLatitudine(latitudine);
            mCitta.setLongitudine(longitudine);
            values.put(MapperContract.DatiCitta.LATITUDINE, latitudine);
            values.put(MapperContract.DatiCitta.LONGITUDINE, longitudine);
            if (BuildConfig.DEBUG)
                Log.v("CittaHelper", mCitta.getNome() + " - Lat: " + latitudine + " , Lng: " + longitudine);
            Uri uri = mResolver.insert(MapperContract.DatiCitta.CONTENT_URI, values);
            if (uri != null)
                idCitta = Long.parseLong(uri.getLastPathSegment());
        }
        mCitta.setIdCitta(idCitta);
    }

    private JSONObject getGeocodeInformations () {
        String indirizzo = mCitta.getNome() + "," + mCitta.getNazione();
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
