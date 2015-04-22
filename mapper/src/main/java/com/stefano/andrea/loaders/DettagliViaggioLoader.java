package com.stefano.andrea.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.stefano.andrea.models.Citta;
import com.stefano.andrea.providers.MapperContract;

import java.util.ArrayList;
import java.util.List;

/**
 * DettagliViaggioLoader
 */
public class DettagliViaggioLoader extends AsyncTaskLoader<List<Citta>> {

    private ContentResolver mResolver;
    private long mIdViaggio;

    public DettagliViaggioLoader(Context context, ContentResolver resolver, long idViaggio) {
        super(context);
        mResolver = resolver;
        mIdViaggio = idViaggio;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        this.forceLoad();
    }

    @Override
    public List<Citta> loadInBackground() {
        List<Citta> list = new ArrayList<>();
        Uri uri = ContentUris.withAppendedId(MapperContract.Citta.DETTAGLI_VIAGGIO_URI, mIdViaggio);
        Cursor c = mResolver.query(uri, MapperContract.Citta.PROJECTION_JOIN, null, null, MapperContract.Citta.DEFAULT_SORT);
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndex(MapperContract.Citta.ID_CITTA));
            String nome = c.getString(c.getColumnIndex(MapperContract.DatiCitta.NOME));
            String nazione = c.getString(c.getColumnIndex(MapperContract.DatiCitta.NAZIONE));
            double latitudine = c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE));
            double longitudine = c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE));;
            list.add(new Citta(id, nome, nazione, latitudine, longitudine));
        }
        c.close();
        return list;
    }
}
