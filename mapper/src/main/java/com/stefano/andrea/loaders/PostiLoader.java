package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.stefano.andrea.models.Posto;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.BaseAsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * PostiLoader
 */
public class PostiLoader extends BaseAsyncTaskLoader<List<Posto>> {

    private ContentResolver mResolver;
    private long mIdCitta;

    public PostiLoader(Context context, long idCitta) {
        super(context);
        mResolver = context.getContentResolver();
        mIdCitta = idCitta;
    }

    @Override
    public List<Posto> loadInBackground() {
        List<Posto> elencoPosti = new ArrayList<>();
        Uri uri = ContentUris.withAppendedId(MapperContract.Posto.POSTI_IN_CITTA_URI, mIdCitta);
        Cursor c = mResolver.query(uri, MapperContract.Posto.PROJECTION_ALL, null, null, MapperContract.Posto.DEFAULT_SORT);
        while (c.moveToNext()) {
            Posto posto = new Posto();
            posto.setId(c.getLong(c.getColumnIndex(MapperContract.Posto.ID_POSTO)));
            posto.setIdCitta(c.getLong(c.getColumnIndex(MapperContract.Posto.ID_CITTA)));
            posto.setIdLuogo(c.getLong(c.getColumnIndex(MapperContract.Posto.ID_LUOGO)));
            posto.setVisitato(c.getInt(c.getColumnIndex(MapperContract.Posto.VISITATO)));
            posto.setNome(c.getString(c.getColumnIndex(MapperContract.Luogo.NOME)));
            posto.setLatitudine(c.getDouble(c.getColumnIndex(MapperContract.Luogo.LATITUDINE)));
            posto.setLongitudine(c.getDouble(c.getColumnIndex(MapperContract.Luogo.LONGITUDINE)));
            posto.setIdDatiCitta(c.getLong(c.getColumnIndex(MapperContract.Luogo.ID_CITTA)));
            elencoPosti.add(posto);
        }
        c.close();
        return elencoPosti;
    }
}
