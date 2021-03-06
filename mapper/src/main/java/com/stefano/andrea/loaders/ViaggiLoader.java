package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.BaseAsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * ViaggiLoader
 */
public class ViaggiLoader extends BaseAsyncTaskLoader<List<Viaggio>> {

    private ContentResolver mResolver;

    public ViaggiLoader(Context context) {
        super(context, MapperIntent.UPDATE_VIAGGIO);
        mResolver = context.getContentResolver();
    }

    @Override
    public List<Viaggio> loadInBackground() {
        List<Viaggio> elencoViaggi = new ArrayList<>();
        Cursor c = mResolver.query(MapperContract.Viaggio.CONTENT_URI, MapperContract.Viaggio.PROJECTION_ALL, null, null, MapperContract.Viaggio.DEFAULT_SORT);
        while (c.moveToNext()) {
            Viaggio viaggio = new Viaggio();
            viaggio.setId(c.getLong(c.getColumnIndex(MapperContract.Viaggio.ID_VIAGGIO)));
            viaggio.setNome(c.getString(c.getColumnIndex(MapperContract.Viaggio.NOME)));
            viaggio.setCountCitta(c.getInt(c.getColumnIndex(MapperContract.Viaggio.COUNT_CITTA)));
            viaggio.setCountPosti(c.getInt(c.getColumnIndex(MapperContract.Viaggio.COUNT_POSTI)));
            viaggio.setCountFoto(c.getInt(c.getColumnIndex(MapperContract.Viaggio.COUNT_FOTO)));
            Cursor foto = mResolver.query(MapperContract.Foto.CONTENT_URI,
                    new String [] {MapperContract.Foto.PATH},
                    MapperContract.Foto.ID_VIAGGIO + "=?",
                    new String [] {Long.toString(viaggio.getId())},
                    MapperContract.Foto.DEFAULT_SORT + " LIMIT 1");
            if (foto.moveToFirst())
                viaggio.setPathFoto(foto.getString(foto.getColumnIndex(MapperContract.Foto.PATH)));
            elencoViaggi.add(viaggio);
            foto.close();
        }
        c.close();
        return elencoViaggi;
    }
}
