package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

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

    private static final int MAX_THUMBNAIL = 7;

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
        String fotoSelection;
        String [] fotoProjection = new String[2];
        Uri itemUri;
        String itemSelection;
        String [] itemProjection = new String[3];
        fotoProjection[0] = MapperContract.Foto.ID_MEDIA_STORE;
        switch (mType) {
            case ELENCO_CITTA:
                fotoSelection = MapperContract.Foto.ID_VIAGGIO + "=?";
                fotoProjection[1] = MapperContract.Foto.ID_CITTA;
                itemUri = MapperContract.Citta.CONTENT_URI;
                itemSelection = MapperContract.Citta.ID_CITTA + "=?";
                itemProjection[0] = MapperContract.DatiCitta.NOME;
                itemProjection[1] = MapperContract.DatiCitta.LATITUDINE;
                itemProjection[2] = MapperContract.DatiCitta.LONGITUDINE;
                break;
            case ELENCO_POSTI:
                fotoSelection = MapperContract.Foto.ID_CITTA + "=?";
                fotoProjection[1] = MapperContract.Foto.ID_POSTO;
                itemUri = MapperContract.Posto.CONTENT_URI;
                itemSelection = MapperContract.Posto.ID_POSTO + "=?";
                itemProjection[0] = MapperContract.Luogo.NOME;
                itemProjection[1] = MapperContract.Luogo.LATITUDINE;
                itemProjection[2] = MapperContract.Luogo.LONGITUDINE;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        Cursor c = mResolver.query(MapperContract.Foto.CONTENT_URI, fotoProjection, fotoSelection,
                new String [] {Long.toString(mId)}, MapperContract.Foto.DEFAULT_SORT + " LIMIT " + MAX_THUMBNAIL);
        while (c.moveToNext()) {
            GeoInfo geoInfo = new GeoInfo();
            //Acquisisco miniatura
            int idMedia = c.getInt(c.getColumnIndex(fotoProjection[0]));
            geoInfo.setMiniatura(MediaStore.Images.Thumbnails.getThumbnail(mResolver, idMedia, MediaStore.Images.Thumbnails.MICRO_KIND, null));
            //Acquisco i dati della citta/posto
            geoInfo.setId(c.getLong(c.getColumnIndex(fotoProjection[1])));
            Cursor item = mResolver.query(itemUri, itemProjection, itemSelection, new String[]{Long.toString(geoInfo.getId())}, null);
            if (item.moveToFirst()) {
                geoInfo.setNome(item.getString(item.getColumnIndex(itemProjection[0])));
                geoInfo.setLatitudine(item.getDouble(item.getColumnIndex(itemProjection[1])));
                geoInfo.setLongitudine(item.getDouble(item.getColumnIndex(itemProjection[2])));
            }
            item.close();
            elenco.add(geoInfo);
        }
        c.close();
        return elenco;
    }
}
