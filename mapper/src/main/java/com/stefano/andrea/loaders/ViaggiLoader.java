package com.stefano.andrea.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;

import java.util.ArrayList;
import java.util.List;

/**
 * ViaggiLoader
 */
public class ViaggiLoader extends AsyncTaskLoader<List<Viaggio>> {

    private ContentResolver mResolver;

    public ViaggiLoader(Context context, ContentResolver resolver) {
        super(context);
        mResolver = resolver;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        this.forceLoad();
    }

    @Override
    public List<Viaggio> loadInBackground() {
        List<Viaggio> viaggi = new ArrayList<>();
        Cursor c = mResolver.query(MapperContract.Viaggio.CONTENT_URI, MapperContract.Viaggio.PROJECTION_ALL, null, null, MapperContract.Viaggio.DEFAULT_SORT);
        while (c.moveToNext()) {
            long id_viaggio = c.getLong(c.getColumnIndex(MapperContract.Viaggio.ID_VIAGGIO));
            String nome = c.getString(c.getColumnIndex(MapperContract.Viaggio.NOME));
            viaggi.add(new Viaggio(id_viaggio, nome));
        }
        c.close();
        return viaggi;
    }
}
