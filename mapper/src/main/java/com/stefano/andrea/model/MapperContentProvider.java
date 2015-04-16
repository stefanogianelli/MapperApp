package com.stefano.andrea.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

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
    private SQLiteDatabase mDb;

    @Override
    public boolean onCreate() {
        mHelper = new MapperOpenHelper(getContext());
        mDb = mHelper.getWritableDatabase();
        if (mDb == null)
            return false;
        if (mDb.isReadOnly()) {
            mDb.close();
            mDb = null;
            return false;
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case VIAGGIO_LIST:
                break;
            case VIAGGIO_ITEM:
                queryBuilder.setTables(MapperContract.TABLE_VIAGGIO);
                queryBuilder.appendWhere(MapperContract.Viaggio._ID + "=" + uri.getLastPathSegment());
                break;
            case CITTA_LIST:
                break;
            case CITTA_ITEM:
                queryBuilder.setTables(MapperContract.TABLE_CITTA);
                queryBuilder.appendWhere(MapperContract.Citta._ID + "=" + uri.getLastPathSegment());
                break;
            case POSTO_LIST:
                break;
            case POSTO_ITEM:
                queryBuilder.setTables(MapperContract.TABLE_POSTO);
                queryBuilder.appendWhere(MapperContract.Posto._ID + "=" + uri.getLastPathSegment());
                break;
            case DATI_CITTA_LIST:
                break;
            case DATI_CITTA_ITEM:
                queryBuilder.setTables(MapperContract.TABLE_DATI_CITTA);
                queryBuilder.appendWhere(MapperContract.DatiCitta._ID + "=" + uri.getLastPathSegment());
                break;
            case LUOGO_LIST:
                break;
            case LUOGO_ITEM:
                queryBuilder.setTables(MapperContract.TABLE_LUOGO);
                queryBuilder.appendWhere(MapperContract.Luogo._ID + "=" + uri.getLastPathSegment());
                break;
            case FOTO_LIST:
                break;
            case FOTO_ITEM:
                queryBuilder.setTables(MapperContract.TABLE_FOTO);
                queryBuilder.appendWhere(MapperContract.Foto._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("URI non supportata " + uri);
        }
        Cursor cursor = queryBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
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
        long id;
        switch (URI_MATCHER.match(uri)) {
            case VIAGGIO_LIST:
                id = mDb.insert(MapperContract.TABLE_VIAGGIO, null, values);
                break;
            case CITTA_LIST:
                id = mDb.insert(MapperContract.TABLE_CITTA, null, values);
                break;
            case POSTO_LIST:
                id = mDb.insert(MapperContract.TABLE_POSTO, null, values);
                break;
            case DATI_CITTA_LIST:
                id = mDb.insert(MapperContract.TABLE_DATI_CITTA, null, values);
                break;
            case LUOGO_LIST:
                id = mDb.insert(MapperContract.TABLE_LUOGO, null, values);
                break;
            case FOTO_LIST:
                id = mDb.insert(MapperContract.TABLE_FOTO, null, values);
                break;
            default:
                throw new IllegalArgumentException("URI non supportata " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        String where;
        switch (URI_MATCHER.match(uri)) {
            case VIAGGIO_LIST:
                rowsDeleted = mDb.delete(MapperContract.TABLE_VIAGGIO, selection, selectionArgs);
                break;
            case VIAGGIO_ITEM:
                where = MapperContract.Viaggio._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsDeleted = mDb.delete(MapperContract.TABLE_VIAGGIO, where, selectionArgs);
                break;
            case CITTA_LIST:
                rowsDeleted = mDb.delete(MapperContract.TABLE_CITTA, selection, selectionArgs);
                break;
            case CITTA_ITEM:
                where = MapperContract.Citta._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsDeleted = mDb.delete(MapperContract.TABLE_CITTA, where, selectionArgs);
                break;
            case POSTO_LIST:
                rowsDeleted = mDb.delete(MapperContract.TABLE_POSTO, selection, selectionArgs);
                break;
            case POSTO_ITEM:
                where = MapperContract.Posto._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsDeleted = mDb.delete(MapperContract.TABLE_POSTO, where, selectionArgs);
                break;
            case DATI_CITTA_LIST:
                rowsDeleted = mDb.delete(MapperContract.TABLE_DATI_CITTA, selection, selectionArgs);
                break;
            case DATI_CITTA_ITEM:
                where = MapperContract.DatiCitta._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsDeleted = mDb.delete(MapperContract.TABLE_DATI_CITTA, where, selectionArgs);
                break;
            case LUOGO_LIST:
                rowsDeleted = mDb.delete(MapperContract.TABLE_LUOGO, selection, selectionArgs);
                break;
            case LUOGO_ITEM:
                where = MapperContract.Luogo._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsDeleted = mDb.delete(MapperContract.TABLE_LUOGO, where, selectionArgs);
                break;
            case FOTO_LIST:
                rowsDeleted = mDb.delete(MapperContract.TABLE_FOTO, selection, selectionArgs);
                break;
            case FOTO_ITEM:
                where = MapperContract.Foto._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsDeleted = mDb.delete(MapperContract.TABLE_FOTO, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI non supportata " + uri);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String where;
        int rowsUpdated;
        switch (URI_MATCHER.match(uri)) {
            case VIAGGIO_LIST:
                rowsUpdated = mDb.update(MapperContract.TABLE_VIAGGIO, values, selection, selectionArgs);
                break;
            case VIAGGIO_ITEM:
                where = MapperContract.Viaggio._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsUpdated = mDb.update(MapperContract.TABLE_VIAGGIO, values, where, selectionArgs);
                break;
            case CITTA_LIST:
                rowsUpdated = mDb.update(MapperContract.TABLE_CITTA, values, selection, selectionArgs);
                break;
            case CITTA_ITEM:
                where = MapperContract.Citta._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsUpdated = mDb.update(MapperContract.TABLE_CITTA, values, where, selectionArgs);
                break;
            case POSTO_LIST:
                rowsUpdated = mDb.update(MapperContract.TABLE_POSTO, values, selection, selectionArgs);
                break;
            case POSTO_ITEM:
                where = MapperContract.Posto._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsUpdated = mDb.update(MapperContract.TABLE_POSTO, values, where, selectionArgs);
                break;
            case DATI_CITTA_LIST:
                rowsUpdated = mDb.update(MapperContract.TABLE_DATI_CITTA, values, selection, selectionArgs);
                break;
            case DATI_CITTA_ITEM:
                where = MapperContract.DatiCitta._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsUpdated = mDb.update(MapperContract.TABLE_DATI_CITTA, values, where, selectionArgs);
                break;
            case LUOGO_LIST:
                rowsUpdated = mDb.update(MapperContract.TABLE_LUOGO, values, selection, selectionArgs);
                break;
            case LUOGO_ITEM:
                where = MapperContract.Luogo._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsUpdated = mDb.update(MapperContract.TABLE_LUOGO, values, where, selectionArgs);
                break;
            case FOTO_LIST:
                rowsUpdated = mDb.update(MapperContract.TABLE_FOTO, values, selection, selectionArgs);
                break;
            case FOTO_ITEM:
                where = MapperContract.Foto._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    where += " AND " + selection;
                rowsUpdated = mDb.update(MapperContract.TABLE_FOTO, values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI non supportata " + uri);
        }
        return rowsUpdated;
    }
}