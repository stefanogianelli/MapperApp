package com.stefano.andrea.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * MapperContentProvider
 */
public class MapperContentProvider extends ContentProvider {

    private static final int VIAGGIO_LIST = 1;
    private static final int VIAGGIO_ITEM = 2;
    private static final int CITTA_LIST = 5;
    private static final int CITTA_ITEM = 6;
    private static final int POSTO_LIST = 10;
    private static final int POSTO_ITEM = 11;
    private static final int DATI_CITTA_LIST = 15;
    private static final int DATI_CITTA_ITEM = 16;
    private static final int LUOGO_LIST = 20;
    private static final int LUOGO_ITEM = 21;
    private static final int FOTO_LIST = 25;
    private static final int FOTO_ITEM = 26;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_VIAGGIO, VIAGGIO_LIST);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_VIAGGIO + "/#", VIAGGIO_ITEM);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_CITTA, CITTA_LIST);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_CITTA + "/#", CITTA_ITEM);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_POSTO, POSTO_LIST);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_POSTO + "/#", POSTO_ITEM);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_DATI_CITTA, DATI_CITTA_LIST);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_DATI_CITTA + "/#", DATI_CITTA_ITEM);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_LUOGO, LUOGO_LIST);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_LUOGO + "/#", LUOGO_ITEM);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_FOTO, FOTO_LIST);
        URI_MATCHER.addURI(MapperContract.AUTHORITY, MapperContract.TABLE_FOTO + "/#", FOTO_ITEM);
    }

    private MapperOpenHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new MapperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case VIAGGIO_LIST:
                return MapperContract.Viaggio.CONTENT_TYPE;
            case VIAGGIO_ITEM:
                return MapperContract.Viaggio.CONTENT_ITEM_TYPE;
            case CITTA_LIST:
                return MapperContract.Citta.CONTENT_TYPE;
            case CITTA_ITEM:
                return MapperContract.Citta.CONTENT_ITEM_TYPE;
            case POSTO_LIST:
                return MapperContract.Posto.CONTENT_TYPE;
            case POSTO_ITEM:
                return MapperContract.Posto.CONTENT_ITEM_TYPE;
            case DATI_CITTA_LIST:
                return MapperContract.DatiCitta.CONTENT_TYPE;
            case DATI_CITTA_ITEM:
                return MapperContract.DatiCitta.CONTENT_ITEM_TYPE;
            case LUOGO_LIST:
                return MapperContract.Luogo.CONTENT_TYPE;
            case LUOGO_ITEM:
                return MapperContract.Luogo.CONTENT_ITEM_TYPE;
            case FOTO_LIST:
                return MapperContract.Foto.CONTENT_TYPE;
            case FOTO_ITEM:
                return MapperContract.Foto.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("URI non supportata " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
