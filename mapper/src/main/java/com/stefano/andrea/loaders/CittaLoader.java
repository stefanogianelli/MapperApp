package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.stefano.andrea.models.Citta;
import com.stefano.andrea.providers.MapperContract;

import java.util.ArrayList;
import java.util.List;

/**
 * CittaLoader
 */
public class CittaLoader extends AsyncTaskLoader<List<Citta>> {

    private ContentResolver mResolver;
    private long mIdViaggio;
    private List<Citta> mElencoCitta;

    public CittaLoader(Context context, ContentResolver resolver, long idViaggio) {
        super(context);
        mResolver = resolver;
        mIdViaggio = idViaggio;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mElencoCitta != null)
            deliverResult(mElencoCitta);
        if (mElencoCitta == null || takeContentChanged())
            this.forceLoad();
    }

    @Override
    public List<Citta> loadInBackground() {
        mElencoCitta = new ArrayList<>();
        Uri uri = ContentUris.withAppendedId(MapperContract.Citta.DETTAGLI_VIAGGIO_URI, mIdViaggio);
        Cursor c = mResolver.query(uri, MapperContract.Citta.PROJECTION_JOIN, null, null, MapperContract.Citta.DEFAULT_SORT);
        if (c != null) {
            while (c.moveToNext()) {
                Citta citta = new Citta();
                citta.setId(c.getLong(c.getColumnIndex(MapperContract.Citta.ID_CITTA)));
                citta.setIdCitta(c.getLong(c.getColumnIndex(MapperContract.Citta.ID_DATI_CITTA)));
                citta.setIdViaggio(c.getLong(c.getColumnIndex(MapperContract.Citta.ID_VIAGGIO)));
                citta.setNome(c.getString(c.getColumnIndex(MapperContract.DatiCitta.NOME)));
                citta.setNazione(c.getString(c.getColumnIndex(MapperContract.DatiCitta.NAZIONE)));
                citta.setLatitudine(c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE)));
                citta.setLongitudine(c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE)));
                citta.setPercentuale(c.getDouble(c.getColumnIndex(MapperContract.Citta.PERCENTUALE)));
                citta.setCountPosti(c.getInt(c.getColumnIndex(MapperContract.Citta.COUNT_POSTI)));
                mElencoCitta.add(citta);
            }
            c.close();
            return mElencoCitta;
        }
        return null;
    }
}
