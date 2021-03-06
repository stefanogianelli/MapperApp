package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.BaseAsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * FotoLoader
 */
public class FotoLoader extends BaseAsyncTaskLoader<List<Foto>> {

    public static final int FOTO_VIAGGIO = 0;
    public static final int FOTO_CITTA = 1;
    public static final int FOTO_POSTO = 2;

    private ContentResolver mResolver;
    private long mId;
    private int selection;

    public FotoLoader(Context context,long id, int type) {
        super(context, MapperIntent.UPDATE_FOTO);
        mResolver = context.getContentResolver();
        mId = id;
        selection = type;
    }

    @Override
    public List<Foto> loadInBackground() {
        List<Foto> elencoFoto = new ArrayList<>();
        Uri uri;
        switch (selection) {
            case FOTO_VIAGGIO:
                uri = ContentUris.withAppendedId(MapperContract.Foto.FOTO_IN_VIAGGIO_URI, mId);
                break;
            case FOTO_CITTA:
                uri = ContentUris.withAppendedId(MapperContract.Foto.FOTO_IN_CITTA_URI, mId);
                break;
            case FOTO_POSTO:
                uri = ContentUris.withAppendedId(MapperContract.Foto.FOTO_IN_POSTO_URI, mId);
                break;
            default:
                throw new UnsupportedOperationException("Operazione non consentita");
        }
        Cursor c = mResolver.query(uri, MapperContract.Foto.PROJECTION_ALL, null, null, MapperContract.Foto.DEFAULT_SORT);
        while (c.moveToNext()) {
            Foto foto = new Foto();
            foto.setId(c.getLong(c.getColumnIndex(MapperContract.Foto.ID)));
            foto.setPath(c.getString(c.getColumnIndex(MapperContract.Foto.PATH)));
            foto.setData(c.getLong(c.getColumnIndex(MapperContract.Foto.DATA)));
            foto.setLatitudine(c.getDouble(c.getColumnIndex(MapperContract.Foto.LATITUDINE)));
            foto.setLongitudine(c.getDouble(c.getColumnIndex(MapperContract.Foto.LONGITUDINE)));
            foto.setIdViaggio(c.getLong(c.getColumnIndex(MapperContract.Foto.ID_VIAGGIO)));
            foto.setIdCitta(c.getLong(c.getColumnIndex(MapperContract.Foto.ID_CITTA)));
            foto.setIdPosto(c.getLong(c.getColumnIndex(MapperContract.Foto.ID_POSTO)));
            foto.setIdMediaStore(c.getInt(c.getColumnIndex(MapperContract.Foto.ID_MEDIA_STORE)));
            foto.setCamera(c.getInt(c.getColumnIndex(MapperContract.Foto.CAMERA)));
            foto.setModel(c.getString(c.getColumnIndex(MapperContract.Foto.MODEL)));
            foto.setMimeType(c.getString(c.getColumnIndex(MapperContract.Foto.MIME_TYPE)));
            foto.setWidth(c.getString(c.getColumnIndex(MapperContract.Foto.WIDTH)));
            foto.setHeight(c.getString(c.getColumnIndex(MapperContract.Foto.HEIGHT)));
            foto.setSize(c.getInt(c.getColumnIndex(MapperContract.Foto.SIZE)));
            foto.setExif(c.getString(c.getColumnIndex(MapperContract.Foto.EXIF)));
            foto.setIndirizzo(c.getString(c.getColumnIndex(MapperContract.Foto.INDIRIZZO)));
            //get the thumbnail
            foto.setThumbnail(MediaStore.Images.Thumbnails.getThumbnail(mResolver, foto.getIdMediaStore(), MediaStore.Images.Thumbnails.MINI_KIND, null));
            elencoFoto.add(foto);
        }
        c.close();
        return elencoFoto;
    }

}
