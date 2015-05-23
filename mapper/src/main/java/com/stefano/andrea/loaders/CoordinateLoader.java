package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.stefano.andrea.fragments.MappaFragment;
import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.GeoInfo;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.BaseAsyncTaskLoader;
import com.stefano.andrea.utils.MapperContext;

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
    private MapperContext mContext;

    public CoordinateLoader(Context context, long id, int type) {
        super(context, MapperIntent.UPDATE_MAPPA);
        mResolver = context.getContentResolver();
        mId = id;
        mType = type;
        mContext = MapperContext.getInstance();
    }

    @Override
    public List<GeoInfo> loadInBackground() {
        List<GeoInfo> elenco = new ArrayList<>();
        Uri itemUri;
        String [] itemProjection = new String[5];
        String fotoSelection;
        switch (mType) {
            case ELENCO_CITTA:
                itemUri = MapperContract.Citta.DETTAGLI_VIAGGIO_URI;
                itemProjection[0] = MapperContract.DatiCitta.NOME;
                itemProjection[1] = MapperContract.DatiCitta.LATITUDINE;
                itemProjection[2] = MapperContract.DatiCitta.LONGITUDINE;
                itemProjection[3] = MapperContract.Citta.COUNT_FOTO;
                itemProjection[4] = MapperContract.Citta.ID_CITTA;
                fotoSelection = MapperContract.Foto.ID_CITTA + "=?";
                break;
            case ELENCO_POSTI:
                itemUri = MapperContract.Posto.POSTI_IN_CITTA_URI;
                itemProjection[0] = MapperContract.Luogo.NOME;
                itemProjection[1] = MapperContract.Luogo.LATITUDINE;
                itemProjection[2] = MapperContract.Luogo.LONGITUDINE;
                itemProjection[3] = MapperContract.Posto.COUNT_FOTO;
                itemProjection[4] = MapperContract.Posto.ID_POSTO;
                fotoSelection = MapperContract.Foto.ID_POSTO + "=?";
                //carico anche i dettagli della citta
                Cursor c = mResolver.query(MapperContract.Citta.CONTENT_URI,
                        MapperContract.Citta.PROJECTION_ALL,
                        MapperContract.Citta.ID_CITTA + "=?",
                        new String [] {Long.toString(mContext.getIdCitta())},
                        null);
                if (c.moveToFirst()) {
                    GeoInfo citta = new GeoInfo();
                    citta.setId(MappaFragment.ID_CITTA);
                    citta.setNome(c.getString(c.getColumnIndex(MapperContract.DatiCitta.NOME)));
                    citta.setLatitudine(c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE)));
                    citta.setLongitudine(c.getDouble(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE)));
                    citta.setCountFoto(c.getInt(c.getColumnIndex(MapperContract.Citta.COUNT_FOTO)));
                    Cursor foto = mResolver.query(MapperContract.Foto.CONTENT_URI,
                            new String[]{MapperContract.Foto.ID_MEDIA_STORE},
                            MapperContract.Foto.ID_CITTA + "=?",
                            new String [] {Long.toString(c.getLong(c.getColumnIndex(MapperContract.Citta.ID_CITTA)))},
                            MapperContract.Foto.DEFAULT_SORT + " LIMIT 1");
                    List<Bitmap> miniature = new ArrayList<>();
                    if (foto.moveToFirst()) {
                        int idMediaStore = foto.getInt(foto.getColumnIndex(MapperContract.Foto.ID_MEDIA_STORE));
                        miniature.add(MediaStore.Images.Thumbnails.getThumbnail(mResolver, idMediaStore, MediaStore.Images.Thumbnails.MICRO_KIND, null));
                    }
                    citta.setMiniature(miniature);
                    foto.close();
                    elenco.add(citta);
                }
                c.close();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        itemUri = ContentUris.withAppendedId(itemUri, mId);
        Cursor c = mResolver.query(itemUri, itemProjection, null, null, null);
        while (c.moveToNext()) {
            GeoInfo geoInfo = new GeoInfo();
            geoInfo.setNome(c.getString(c.getColumnIndex(itemProjection[0])));
            geoInfo.setLatitudine(c.getDouble(c.getColumnIndex(itemProjection[1])));
            geoInfo.setLongitudine(c.getDouble(c.getColumnIndex(itemProjection[2])));
            geoInfo.setCountFoto(c.getInt(c.getColumnIndex(itemProjection[3])));
            geoInfo.setId(c.getLong(c.getColumnIndex(itemProjection[4])));
            //acquisisco miniature
            Cursor fotoCursor = mResolver.query(MapperContract.Foto.CONTENT_URI,
                    new String [] {MapperContract.Foto.ID_MEDIA_STORE}, fotoSelection,
                    new String [] {Long.toString(geoInfo.getId())} , MapperContract.Foto.DEFAULT_SORT + " LIMIT " + MAX_THUMBNAIL);
            List<Bitmap> miniature = new ArrayList<>();
            while (fotoCursor.moveToNext()) {
                int idMediaStore = fotoCursor.getInt(fotoCursor.getColumnIndex(MapperContract.Foto.ID_MEDIA_STORE));
                Bitmap miniatura = MediaStore.Images.Thumbnails.getThumbnail(mResolver, idMediaStore, MediaStore.Images.Thumbnails.MICRO_KIND, null);
                miniature.add(miniatura);
            }
            geoInfo.setMiniature(miniature);
            fotoCursor.close();
            elenco.add(geoInfo);
        }
        c.close();
        return elenco;
    }
}
