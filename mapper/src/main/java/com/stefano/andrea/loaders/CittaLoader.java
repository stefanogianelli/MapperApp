package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.stefano.andrea.models.Citta;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.BaseAsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * CittaLoader
 */
public class CittaLoader extends BaseAsyncTaskLoader<List<Citta>> {

    private ContentResolver mResolver;
    private long mIdViaggio;

    public CittaLoader(Context context, long idViaggio) {
        super(context);
        mResolver = context.getContentResolver();
        mIdViaggio = idViaggio;
    }

    @Override
    public List<Citta> loadInBackground() {
        List<Citta> elencoCitta = new ArrayList<>();
        Uri uri = ContentUris.withAppendedId(MapperContract.Citta.DETTAGLI_VIAGGIO_URI, mIdViaggio);
        Cursor c = mResolver.query(uri, MapperContract.Citta.PROJECTION_ALL, null, null, MapperContract.Citta.DEFAULT_SORT);
        while (c.moveToNext()) {
            Citta citta = new Citta();
            citta.setId(c.getLong(c.getColumnIndex(MapperContract.Citta.ID_CITTA)));
            citta.setIdCitta(c.getLong(c.getColumnIndex(MapperContract.Citta.ID_DATI_CITTA)));
            citta.setIdViaggio(c.getLong(c.getColumnIndex(MapperContract.Citta.ID_VIAGGIO)));
            citta.setNome(c.getString(c.getColumnIndex(MapperContract.DatiCitta.NOME)));
            citta.setNazione(c.getString(c.getColumnIndex(MapperContract.DatiCitta.NAZIONE)));
            citta.setLatitudine(c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE)));
            citta.setLongitudine(c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE)));
            citta.setCountPostiVisitati(c.getInt(c.getColumnIndex(MapperContract.Citta.POSTI_VISITATI)));
            citta.setCountPosti(c.getInt(c.getColumnIndex(MapperContract.Citta.COUNT_POSTI)));
            citta.setCountFoto(c.getInt(c.getColumnIndex(MapperContract.Citta.COUNT_FOTO)));
            elencoCitta.add(citta);
        }
        c.close();
        return elencoCitta;
    }
}
