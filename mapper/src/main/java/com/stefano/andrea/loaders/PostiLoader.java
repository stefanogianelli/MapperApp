package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.stefano.andrea.models.Posto;
import com.stefano.andrea.providers.MapperContract;

import java.util.ArrayList;
import java.util.List;

/**
 * PostiLoader
 */
public class PostiLoader extends AsyncTaskLoader<List<Posto>> {

    private ContentResolver mResolver;
    private long mIdCitta;
    private List<Posto> mElencoPosti;

    public PostiLoader(Context context, ContentResolver resolver, long idCitta) {
        super(context);
        mResolver = resolver;
        mIdCitta = idCitta;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mElencoPosti != null)
            deliverResult(mElencoPosti);
        if (mElencoPosti == null || takeContentChanged())
            forceLoad();
    }

    @Override
    public List<Posto> loadInBackground() {
        mElencoPosti = new ArrayList<>();
        Uri uri = ContentUris.withAppendedId(MapperContract.Posto.POSTI_IN_CITTA_URI, mIdCitta);
        Cursor c = mResolver.query(uri, MapperContract.Posto.PROJECTION_JOIN, null, null, MapperContract.Posto.DEFAULT_SORT);
        if (c != null) {
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
                mElencoPosti.add(posto);
            }
            c.close();
            return mElencoPosti;
        }
        return null;
    }
}
