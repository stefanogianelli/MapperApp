package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;

import java.util.ArrayList;
import java.util.List;

/**
 * ViaggiLoader
 */
public class ViaggiLoader extends AsyncTaskLoader<List<Viaggio>> {

    private ContentResolver mResolver;
    private List<Viaggio> mViaggi;

    public ViaggiLoader(Context context, ContentResolver resolver) {
        super(context);
        mResolver = resolver;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mViaggi != null)
            deliverResult(mViaggi);
        if (mViaggi == null || takeContentChanged())
            this.forceLoad();
    }

    @Override
    public List<Viaggio> loadInBackground() {
        mViaggi = new ArrayList<>();
        Cursor c = mResolver.query(MapperContract.Viaggio.CONTENT_URI, MapperContract.Viaggio.PROJECTION_ALL, null, null, MapperContract.Viaggio.DEFAULT_SORT);
        if (c != null) {
            while (c.moveToNext()) {
                Viaggio viaggio = new Viaggio();
                viaggio.setId(c.getLong(c.getColumnIndex(MapperContract.Viaggio.ID_VIAGGIO)));
                viaggio.setNome(c.getString(c.getColumnIndex(MapperContract.Viaggio.NOME)));
                viaggio.setCountCitta(c.getInt(c.getColumnIndex(MapperContract.Viaggio.COUNT_CITTA)));
                viaggio.setCountPosti(c.getInt(c.getColumnIndex(MapperContract.Viaggio.COUNT_POSTI)));
                viaggio.setCountFoto(c.getInt(c.getColumnIndex(MapperContract.Viaggio.COUNT_FOTO)));
                mViaggi.add(viaggio);
            }
            c.close();
            return mViaggi;
        }
        return null;
    }
}
