package com.stefano.andrea.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.stefano.andrea.models.Foto;
import com.stefano.andrea.providers.MapperContract;

import java.util.ArrayList;
import java.util.List;

/**
 * FotoLoader
 */
public class FotoLoader extends AsyncTaskLoader<List<Foto>> {

    public static final int FOTO_VIAGGIO = 0;
    public static final int FOTO_CITTA = 1;
    public static final int FOTO_POSTO = 2;

    private List<Foto> mElencoFoto;
    private ContentResolver mResolver;
    private long mId;
    private int selection;
    private FotoObserver mObserver;

    public FotoLoader(Context context, ContentResolver resolver, long id, int type) {
        super(context);
        mResolver = resolver;
        mId = id;
        selection = type;
        mObserver = new FotoObserver(null, this);
        mResolver.registerContentObserver(MapperContract.Foto.CONTENT_URI, false, mObserver);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mElencoFoto == null || takeContentChanged()) {
            forceLoad();
        } else if (mElencoFoto != null) {
            deliverResult(mElencoFoto);
        }
    }

    @Override
    public List<Foto> loadInBackground() {
        mElencoFoto = new ArrayList<>();
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
        if (c != null) {
            while (c.moveToNext()) {
                Foto foto = new Foto();
                foto.setId(c.getLong(c.getColumnIndex(MapperContract.Foto.ID)));
                foto.setPath(c.getString(c.getColumnIndex(MapperContract.Foto.PATH)));
                foto.setLatitudine(c.getDouble(c.getColumnIndex(MapperContract.Foto.LATITUDINE)));
                foto.setLongitudine(c.getDouble(c.getColumnIndex(MapperContract.Foto.LONGITUDINE)));
                foto.setIdViaggio(c.getLong(c.getColumnIndex(MapperContract.Foto.ID_VIAGGIO)));
                foto.setIdCitta(c.getLong(c.getColumnIndex(MapperContract.Foto.ID_CITTA)));
                foto.setIdPosto(c.getLong(c.getColumnIndex(MapperContract.Foto.ID_POSTO)));
                mElencoFoto.add(foto);
            }
            c.close();
        }
        return mElencoFoto;
    }

    @Override
    protected void onReset() {
        super.onReset();
        mResolver.unregisterContentObserver(mObserver);
    }

    private class FotoObserver extends ContentObserver {

        private FotoLoader mLoader;

        public FotoObserver(Handler handler, FotoLoader loader) {
            super(handler);
            mLoader = loader;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            mLoader.onContentChanged();
        }
    }
}
