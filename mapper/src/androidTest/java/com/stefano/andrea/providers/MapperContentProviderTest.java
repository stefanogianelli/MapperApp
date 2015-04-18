package com.stefano.andrea.providers;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

/**
 * MapperContentProviderTest
 */
public class MapperContentProviderTest extends ProviderTestCase2<MapperContentProvider> {

    private static final String TAG = "ProviderTest";
    private ContentResolver provider;

    public MapperContentProviderTest(Class<MapperContentProvider> providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    public MapperContentProviderTest () {
        super(MapperContentProvider.class, "com.stefano.andrea.providers.MapperContentProvider");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        provider = getContext().getContentResolver();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInsertViaggio() throws Exception {
        ContentValues values = new ContentValues();
        values.put(MapperContract.Viaggio.NOME, "prova");
        Uri uri = provider.insert(MapperContract.Viaggio.CONTENT_URI, values);
        assertNotNull(uri);
    }

    public void testQueryViaggio() throws Exception {
        String [] projection = {MapperContract.Viaggio._ID, MapperContract.Viaggio.NOME};
        Uri uri = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, 1);
        Cursor cursor = provider.query(uri, projection, null, null, null);
        cursor.moveToNext();
        int index = cursor.getColumnIndex(MapperContract.Viaggio.NOME);
        Log.v(TAG, cursor.getString(index));
        assertEquals(cursor.getCount(), 1);
    }

    public void testDeleteViaggio () throws Exception {
        Uri uri = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, 1);
        int rows = provider.delete(uri, null, null);
        assertEquals(rows, 1);
    }
}
