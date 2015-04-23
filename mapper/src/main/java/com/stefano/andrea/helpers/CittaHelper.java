package com.stefano.andrea.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.stefano.andrea.activities.BuildConfig;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.tasks.GeocodeInformationTask;

import java.util.concurrent.ExecutionException;

/**
 * CittaHelper
 */
public class CittaHelper {

    private ContentResolver mResolver;
    private Context mContext;

    public CittaHelper (Context context, ContentResolver resolver) {
        mContext = context;
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
    public long creaCitta (final String nome, final String nazione) {
        ContentValues values = new ContentValues();
        values.put(MapperContract.DatiCitta.NOME, nome);
        values.put(MapperContract.DatiCitta.NAZIONE, nazione);
        try {
            LatLng coord = new GeocodeInformationTask(nome + "," + nazione).execute().get();
            values.put(MapperContract.DatiCitta.LATITUDINE, coord.latitude);
            values.put(MapperContract.DatiCitta.LONGITUDINE, coord.longitude);
            if (BuildConfig.DEBUG)
                Log.v("CittaHelper", nome + " - Lat: " + coord.latitude + " , Lng: " + coord.longitude);
            Uri uri = mResolver.insert(MapperContract.DatiCitta.CONTENT_URI, values);
            if (uri != null)
                return Long.parseLong(uri.getLastPathSegment());
        } catch (InterruptedException e) {
            //TODO: mostrare messaggio d'errore
            e.printStackTrace();
        } catch (ExecutionException e) {
            //TODO: mostrare messaggio d'errore
            e.printStackTrace();
        }
        return -1;
    }
}
