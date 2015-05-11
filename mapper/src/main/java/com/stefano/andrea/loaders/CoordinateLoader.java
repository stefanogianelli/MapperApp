package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.GeoInfo;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.BaseAsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * CoordinateLoader
 */
public class CoordinateLoader extends BaseAsyncTaskLoader<List<GeoInfo>> {

    public static final int ELENCO_CITTA = 1;
    public static final int ELENCO_POSTI = 2;

    private ContentResolver mResolver;
    private long mId;
    private int mType;

    public CoordinateLoader(Context context, long id, int type) {
        super(context, MapperIntent.UPDATE_MAPPA);
        mResolver = context.getContentResolver();
        mId = id;
        mType = type;
    }

    @Override
    public List<GeoInfo> loadInBackground() {
        List<GeoInfo> elenco = new ArrayList<>();
        Uri uri;
        String [] projection = new String[3];
        switch (mType) {
            case ELENCO_CITTA:
                uri = MapperContract.Citta.DETTAGLI_VIAGGIO_URI;
                projection[0] = MapperContract.DatiCitta.NOME;
                projection[1] = MapperContract.DatiCitta.LATITUDINE;
                projection[2] = MapperContract.DatiCitta.LONGITUDINE;
                break;
            case ELENCO_POSTI:
                uri = MapperContract.Posto.POSTI_IN_CITTA_URI;
                projection[0] = MapperContract.Luogo.NOME;
                projection[1] = MapperContract.Luogo.LATITUDINE;
                projection[2] = MapperContract.Luogo.LONGITUDINE;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        uri = ContentUris.withAppendedId(uri, mId);
        Cursor c = mResolver.query(uri, projection, null, null, null);
        while (c.moveToNext()) {
            String nome = c.getString(c.getColumnIndex(projection[0]));
            double lat = c.getDouble(c.getColumnIndex(projection[1]));
            double lon = c.getDouble(c.getColumnIndex(projection[2]));
            elenco.add(new GeoInfo(nome, lat, lon));
        }
        c.close();
        return elenco;
    }
}
