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
import com.stefano.andrea.helpers.CommonAlertDialog;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * InsertTask
 */
public class InsertTask<T> extends AsyncTask<Integer, Void, Integer> {

    private interface InsertInterface {
        int insertItem ();
    }

    public interface InsertAdapter<T> {
        void insertItem (T item);
    }

    public final static int INSERISCI_VIAGGIO = 0;
    public final static int INSERISCI_CITTA = 1;
    public final static int INSERISCI_POSTO = 2;
    public final static int INSERISCI_FOTO = 3;

    private final static int RESULT_OK = 10;
    private final static int RESULT_ERROR = 11;

    private Activity mActivity;
    private Context mContext;
    private ContentResolver mResolver;
    private InsertAdapter mAdapter;
    private T mItem;
    private InsertInterface mDelegate;
    private ProgressDialog mDialog;

    public InsertTask (Activity activity, ContentResolver resolver, InsertAdapter adapter, T item) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        mResolver = resolver;
        mAdapter = adapter;
        mItem = item;
        mDialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.setMessage(mContext.getResources().getString(R.string.inserimento_nuovo_elemento_dialog));
        mDialog.show();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        if (params.length == 1) {
            switch (params[0]) {
                case INSERISCI_VIAGGIO:
                    mDelegate = new InsertViaggio();
                    break;
                case INSERISCI_CITTA:
                    mDelegate = new InsertCitta();
                    break;
                case INSERISCI_POSTO:
                    break;
                case INSERISCI_FOTO:
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            //aggiungo l'elemento nel database
            return mDelegate.insertItem();
        }
        return RESULT_ERROR;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        mDialog.dismiss();
        if (result == RESULT_OK) {
            //comunico l'avvenuto inserimento all'adapter
            mAdapter.insertItem(mItem);
        } else {
            //mostro dialog d'errore
            new CommonAlertDialog(mActivity, R.string.errore_inserimento_titolo_dialog, R.string.errore_inserimento_messaggio_dialog);
        }
    }

    /**
     * Classe che si oocupa dell'inserimento di un nuovo viaggio
     */
    private class InsertViaggio implements InsertInterface {

        private Viaggio viaggio;

        public InsertViaggio () {
            viaggio = (Viaggio) mItem;
        }

        @Override
        public int insertItem() {
            ContentValues values = new ContentValues();
            values.put(MapperContract.Viaggio.NOME, viaggio.getNome());
            Uri uri = mResolver.insert(MapperContract.Viaggio.CONTENT_URI, values);
            long id = Long.parseLong(uri.getLastPathSegment());
            if (uri != null && id != -1) {
                viaggio.setId(id);
                return RESULT_OK;
            }
            return RESULT_ERROR;
        }
    }

    /**
     * Classe che si occupa dell'inserimento di una nuova citta
     */
    private class InsertCitta implements InsertInterface {

        private Citta citta;

        public InsertCitta () {
            citta = (Citta) mItem;
        }

        @Override
        public int insertItem() {
            //verifico se la citta esiste gia nel database
            int res = this.getDatiCitta();
            if (res == RESULT_ERROR) {
                //creo la citta
                InsertDatiCitta datiCitta = new InsertDatiCitta();
                res = datiCitta.insertItem();
            }
            if (res == RESULT_OK) {
                ContentValues values = new ContentValues();
                values.put(MapperContract.Citta.ID_VIAGGIO, citta.getIdViaggio());
                values.put(MapperContract.Citta.ID_DATI_CITTA, citta.getIdCitta());
                values.put(MapperContract.Citta.PERCENTUALE, 0);
                Uri uri = mResolver.insert(MapperContract.Citta.CONTENT_URI, values);
                long id = Long.parseLong(uri.getLastPathSegment());
                if (id != -1) {
                    citta.setId(id);
                    return RESULT_OK;
                }
            }
            return RESULT_ERROR;
        }

        /**
         * Verifica se esistono nel database i dati di una citta
         */
        private int getDatiCitta () {
            String selection = MapperContract.DatiCitta.NOME + "=? AND " + MapperContract.DatiCitta.NAZIONE + "=?";
            String [] selectionArgs = {citta.getNome(), citta.getNazione()};
            Cursor c = mResolver.query(MapperContract.DatiCitta.CONTENT_URI, MapperContract.DatiCitta.PROJECTION_ALL, selection, selectionArgs, MapperContract.DatiCitta.DEFAULT_SORT);
            if (c != null && c.getCount() > 0) {
                c.moveToNext();
                citta.setIdCitta(c.getLong(c.getColumnIndex(MapperContract.DatiCitta.ID)));
                citta.setLatitudine(c.getLong(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE)));
                citta.setLongitudine(c.getLong(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE)));
                c.close();
                return RESULT_OK;
            }
            return RESULT_ERROR;
        }
    }

    private class InsertDatiCitta implements InsertInterface{

        private Citta citta;

        public InsertDatiCitta () {
            citta = (Citta) mItem;
        }

        @Override
        public int insertItem() {
            ContentValues values = new ContentValues();
            values.put(MapperContract.DatiCitta.NOME, citta.getNome());
            values.put(MapperContract.DatiCitta.NAZIONE, citta.getNazione());
            JSONObject geoInfo = getGeocodeInformations();
            if (geoInfo != null) {
                double latitudine = this.getLatitudine(geoInfo);
                double longitudine = this.getLongitudine(geoInfo);
                citta.setLatitudine(latitudine);
                citta.setLongitudine(longitudine);
                values.put(MapperContract.DatiCitta.LATITUDINE, latitudine);
                values.put(MapperContract.DatiCitta.LONGITUDINE, longitudine);
                if (BuildConfig.DEBUG)
                    Log.v("InsertTask", citta.getNome() + " - Lat: " + latitudine + " , Lng: " + longitudine);
                Uri uri = mResolver.insert(MapperContract.DatiCitta.CONTENT_URI, values);
                long idCitta = Long.parseLong(uri.getLastPathSegment());
                if (uri != null && idCitta != -1) {
                    citta.setIdCitta(idCitta);
                    return RESULT_OK;
                }
            }
            return RESULT_ERROR;
        }

        private JSONObject getGeocodeInformations () {
            String indirizzo = citta.getNome() + "," + citta.getNazione();
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
}
