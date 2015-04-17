package com.stefano.andrea.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.stefano.andrea.utils.SelectionBuilder;

/**
 * MapperContentProvider
 */
public class MapperContentProvider extends ContentProvider {

    private MapperOpenHelper mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int VIAGGI = 100;
    private static final int VIAGGI_ID = 101;

    private static final int CITTA = 200;
    private static final int CITTA_ID = 201;

    private static final int POSTI = 300;
    private static final int POSTI_ID = 301;

    private static final int DATI_CITTA = 400;
    private static final int DATI_CITTA_ID = 401;

    private static final int LUOGHI = 500;
    private static final int LUOGHI_ID = 501;

    private static final int FOTO = 600;
    private static final int FOTO_ID = 601;


    private static UriMatcher buildUriMatcher () {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MapperContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MapperContract.Viaggio.TABLE_NAME, VIAGGI);
        matcher.addURI(authority, MapperContract.Viaggio.TABLE_NAME + "/*", VIAGGI_ID);

        matcher.addURI(authority, MapperContract.Citta.TABLE_NAME, CITTA);
        matcher.addURI(authority, MapperContract.Citta.TABLE_NAME + "/*", CITTA_ID);

        matcher.addURI(authority, MapperContract.Posto.TABLE_NAME, POSTI);
        matcher.addURI(authority, MapperContract.Posto.TABLE_NAME + "/*", POSTI_ID);

        matcher.addURI(authority, MapperContract.DatiCitta.TABLE_NAME, DATI_CITTA);
        matcher.addURI(authority, MapperContract.DatiCitta.TABLE_NAME + "/*", DATI_CITTA_ID);

        matcher.addURI(authority, MapperContract.Luogo.TABLE_NAME, LUOGHI);
        matcher.addURI(authority, MapperContract.Luogo.TABLE_NAME + "/*", LUOGHI_ID);

        matcher.addURI(authority, MapperContract.Foto.TABLE_NAME, FOTO);
        matcher.addURI(authority, MapperContract.Foto.TABLE_NAME + "/*", FOTO_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MapperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        String id;
        Cursor c;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case VIAGGI_ID:
                id = uri.getLastPathSegment();
                builder.where(MapperContract.Viaggio._ID + "=?", id);
            case VIAGGI:
                builder.table(MapperContract.Viaggio.TABLE_NAME).where(selection, selectionArgs);
                break;
            case CITTA_ID:
                id = uri.getLastPathSegment();
                builder.where(MapperContract.Citta._ID + "=?", id);
            case CITTA:
                builder.table(MapperContract.Citta.TABLE_NAME).where(selection, selectionArgs);
                break;
            case POSTI_ID:
                id = uri.getLastPathSegment();
                builder.where(MapperContract.Posto._ID + "=?", id);
            case POSTI:
                builder.table(MapperContract.Posto.TABLE_NAME).where(selection, selectionArgs);
                break;
            case DATI_CITTA_ID:
                id = uri.getLastPathSegment();
                builder.where(MapperContract.DatiCitta._ID + "=?", id);
            case DATI_CITTA:
                builder.table(MapperContract.DatiCitta.TABLE_NAME).where(selection, selectionArgs);
                break;
            case LUOGHI_ID:
                id = uri.getLastPathSegment();
                builder.where(MapperContract.Luogo._ID + "=?", id);
            case LUOGHI:
                builder.table(MapperContract.Luogo.TABLE_NAME).where(selection, selectionArgs);
                break;
            case FOTO_ID:
                id = uri.getLastPathSegment();
                builder.where(MapperContract.Foto._ID + "=?", id);
            case FOTO:
                builder.table(MapperContract.Foto.TABLE_NAME).where(selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("URI non supportata " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        c = builder.query(db, projection, sortOrder);
        c.setNotificationUri(ctx.getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case VIAGGI:
                return MapperContract.Viaggio.CONTENT_TYPE;
            case VIAGGI_ID:
                return MapperContract.Viaggio.CONTENT_ITEM_TYPE;
            case CITTA:
                return MapperContract.Citta.CONTENT_TYPE;
            case CITTA_ID:
                return MapperContract.Citta.CONTENT_ITEM_TYPE;
            case POSTI:
                return MapperContract.Posto.CONTENT_TYPE;
            case POSTI_ID:
                return MapperContract.Posto.CONTENT_ITEM_TYPE;
            case DATI_CITTA:
                return MapperContract.DatiCitta.CONTENT_TYPE;
            case DATI_CITTA_ID:
                return MapperContract.DatiCitta.CONTENT_ITEM_TYPE;
            case LUOGHI:
                return MapperContract.Luogo.CONTENT_TYPE;
            case LUOGHI_ID:
                return MapperContract.Luogo.CONTENT_ITEM_TYPE;
            case FOTO:
                return MapperContract.Foto.CONTENT_TYPE;
            case FOTO_ID:
                return MapperContract.Foto.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("URI non supportata " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        long id;
        Uri result;
        switch (match) {
            case VIAGGI:
                id = db.insert(MapperContract.Viaggio.TABLE_NAME, null, values);
                result = Uri.parse(MapperContract.Viaggio.CONTENT_URI + "/" + id);
                break;
            case CITTA:
                id = db.insert(MapperContract.Citta.TABLE_NAME, null, values);
                result = Uri.parse(MapperContract.Citta.CONTENT_URI + "/" + id);
                break;
            case POSTI:
                id = db.insert(MapperContract.Posto.TABLE_NAME, null, values);
                result = Uri.parse(MapperContract.Posto.CONTENT_URI + "/" + id);
                break;
            case DATI_CITTA:
                id = db.insert(MapperContract.DatiCitta.TABLE_NAME, null, values);
                result = Uri.parse(MapperContract.DatiCitta.CONTENT_URI + "/" + id);
                break;
            case LUOGHI:
                id = db.insert(MapperContract.Luogo.TABLE_NAME, null, values);
                result = Uri.parse(MapperContract.Luogo.CONTENT_URI + "/" + id);
                break;
            case FOTO:
                id = db.insert(MapperContract.Foto.TABLE_NAME, null, values);
                result = Uri.parse(MapperContract.Foto.CONTENT_URI + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("URI non supportata " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        String id;
        switch (match) {
            case VIAGGI:
                count = builder.table(MapperContract.Viaggio.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case VIAGGI_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.Viaggio.TABLE_NAME).where(MapperContract.Viaggio._ID + "=?", id).where(selection, selectionArgs).delete(db);
                break;
            case CITTA:
                count = builder.table(MapperContract.Citta.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case CITTA_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.Viaggio.TABLE_NAME).where(MapperContract.Viaggio._ID + "=?", id).where(selection, selectionArgs).delete(db);
                break;
            case POSTI:
                count = builder.table(MapperContract.Posto.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case POSTI_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.Posto.TABLE_NAME).where(MapperContract.Posto._ID + "=?", id).where(selection, selectionArgs).delete(db);
                break;
            case DATI_CITTA:
                count = builder.table(MapperContract.DatiCitta.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case DATI_CITTA_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.DatiCitta.TABLE_NAME).where(MapperContract.DatiCitta._ID + "=?", id).where(selection, selectionArgs).delete(db);
                break;
            case LUOGHI:
                count = builder.table(MapperContract.Luogo.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case LUOGHI_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.Luogo.TABLE_NAME).where(MapperContract.Luogo._ID + "=?", id).where(selection, selectionArgs).delete(db);
                break;
            case FOTO:
                count = builder.table(MapperContract.Foto.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case FOTO_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.Foto.TABLE_NAME).where(MapperContract.Foto._ID + "=?", id).where(selection, selectionArgs).delete(db);
                break;
            default:
                throw new UnsupportedOperationException("URI non supportata " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        String id;
        switch (match) {
            case VIAGGI:
                count = builder.table(MapperContract.Viaggio.TABLE_NAME).where(selection, selectionArgs).update(db, values);
                break;
            case VIAGGI_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.Viaggio.TABLE_NAME).where(MapperContract.Viaggio._ID + "=?", id).where(selection, selectionArgs).update(db, values);
                break;
            case CITTA:
                count = builder.table(MapperContract.Citta.TABLE_NAME).where(selection, selectionArgs).update(db, values);
                break;
            case CITTA_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.Citta.TABLE_NAME).where(MapperContract.Citta._ID + "=?", id).where(selection, selectionArgs).update(db, values);
                break;
            case POSTI:
                count = builder.table(MapperContract.Posto.TABLE_NAME).where(selection, selectionArgs).update(db, values);
                break;
            case POSTI_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.Posto.TABLE_NAME).where(MapperContract.Posto._ID + "=?", id).where(selection, selectionArgs).update(db, values);
                break;
            case DATI_CITTA:
                count = builder.table(MapperContract.DatiCitta.TABLE_NAME).where(selection, selectionArgs).update(db, values);
                break;
            case DATI_CITTA_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.DatiCitta.TABLE_NAME).where(MapperContract.DatiCitta._ID + "=?", id).where(selection, selectionArgs).update(db, values);
                break;
            case LUOGHI:
                count = builder.table(MapperContract.Luogo.TABLE_NAME).where(selection, selectionArgs).update(db, values);
                break;
            case LUOGHI_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.Luogo.TABLE_NAME).where(MapperContract.Luogo._ID + "=?", id).where(selection, selectionArgs).update(db, values);
                break;
            case FOTO:
                count = builder.table(MapperContract.Foto.TABLE_NAME).where(selection, selectionArgs).update(db, values);
                break;
            case FOTO_ID:
                id = uri.getLastPathSegment();
                count = builder.table(MapperContract.Foto.TABLE_NAME).where(MapperContract.Foto._ID + "=?", id).where(selection, selectionArgs).update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("URI non supportata " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

}