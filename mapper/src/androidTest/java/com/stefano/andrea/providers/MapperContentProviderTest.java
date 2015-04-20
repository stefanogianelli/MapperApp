package com.stefano.andrea.providers;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

/**
 * MapperContentProviderTest
 */
public class MapperContentProviderTest extends ProviderTestCase2<MapperContentProvider> {

    private static final String NOME = "test";

    private MockContentResolver mResolver;

    public MapperContentProviderTest () {
        super(MapperContentProvider.class, MapperContract.CONTENT_AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
        ContentValues values = new ContentValues();
        values.put(MapperContract.Viaggio.NOME, NOME);
        Uri uri = mResolver.insert(MapperContract.Viaggio.CONTENT_URI, values);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInsertViaggio() throws Exception {
        String nomeViaggio = "nuovo viaggio";
        ContentValues values = new ContentValues();
        values.put(MapperContract.Viaggio.NOME, nomeViaggio);
        Uri uri = mResolver.insert(MapperContract.Viaggio.CONTENT_URI, values);
        assertNotNull(uri);
        Cursor c = mResolver.query(uri, MapperContract.Viaggio.PROJECTION_ALL, null, null, MapperContract.Viaggio.DEFAULT_SORT);
        assertNotNull(c);
        c.moveToNext();
        String query = c.getString(c.getColumnIndex(MapperContract.Viaggio.NOME));
        c.close();
        assertEquals(query, nomeViaggio);
    }

    public void testQueryViaggio() throws Exception {
        Uri uri = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, 1);
        Cursor cursor = mResolver.query(uri, MapperContract.Viaggio.PROJECTION_ALL, null, null, null);
        assertNotNull(cursor);
        cursor.moveToNext();
        int index = cursor.getColumnIndex(MapperContract.Viaggio.NOME);
        String nome = cursor.getString(index);
        cursor.close();
        assertEquals(nome, NOME);
    }

    public void testDeleteViaggio () throws Exception {
        Uri uri = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, 1);
        int rows = mResolver.delete(uri, null, null);
        assertEquals(rows, 1);
    }
}
