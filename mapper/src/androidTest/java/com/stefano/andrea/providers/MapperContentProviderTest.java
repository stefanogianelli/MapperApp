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

    private static final String VIAGGIO = "test";
    private final static String CITTA = "Roma";
    private final static String NAZIONE = "Italia";

    private MockContentResolver mResolver;

    public MapperContentProviderTest () {
        super(MapperContentProvider.class, MapperContract.CONTENT_AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
        //Aggiungo viaggio di default
        ContentValues values = new ContentValues();
        values.put(MapperContract.Viaggio.NOME, VIAGGIO);
        mResolver.insert(MapperContract.Viaggio.CONTENT_URI, values);
        //Aggiungo dati citta di default
        values.clear();
        values.put(MapperContract.DatiCitta.NOME, CITTA);
        values.put(MapperContract.DatiCitta.LATITUDINE, 0);
        values.put(MapperContract.DatiCitta.LONGITUDINE, 0);
        mResolver.insert(MapperContract.DatiCitta.CONTENT_URI, values);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /*
    Tabella Viaggio
     */

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
        assertEquals(nome, VIAGGIO);
    }

    public void testDeleteViaggio () throws Exception {
        Uri uri = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, 1);
        int rows = mResolver.delete(uri, null, null);
        assertEquals(rows, 1);
    }

    /*
    Tabella Dati Citta
     */

    public void testNuoviDatiCitta() throws Exception {
        String nome = "Milano";
        String nazione = "Italia";
        ContentValues values = new ContentValues();
        values.put(MapperContract.DatiCitta.NOME, nome);
        values.put(MapperContract.DatiCitta.LATITUDINE, 0);
        values.put(MapperContract.DatiCitta.LONGITUDINE, 0);
        Uri uri = mResolver.insert(MapperContract.DatiCitta.CONTENT_URI, values);
        assertNotNull(uri);
        assertEquals(uri.getLastPathSegment(), "2");
    }

    public void testCancellazioneDatiCitta() throws Exception {
        //aggiungo nuova citta di prova
        ContentValues values = new ContentValues();
        values.put(MapperContract.DatiCitta.NOME, "provadelete");
        values.put(MapperContract.DatiCitta.LATITUDINE, 0);
        values.put(MapperContract.DatiCitta.LONGITUDINE, 0);
        Uri result = mResolver.insert(MapperContract.DatiCitta.CONTENT_URI, values);
        long id = Long.parseLong(result.getLastPathSegment());
        //aggiungo la citta al viaggio di default
        values.clear();
        values.put(MapperContract.Citta.ID_VIAGGIO, 1);
        values.put(MapperContract.Citta.ID_DATI_CITTA, id);
        Uri cittaInViaggio = mResolver.insert(MapperContract.Citta.CONTENT_URI, values);
        //elimino la citta appena creata
        mResolver.delete(cittaInViaggio, null, null);
        //verifico che i dati della citta siano stati cancellati
        Cursor c = mResolver.query(result, MapperContract.DatiCitta.PROJECTION_ALL, null, null, MapperContract.DatiCitta.DEFAULT_SORT);
        assertEquals(c.getCount(), 0);
    }
    
    /*
    Tabella Citta
     */

    public void testCreaCitta () throws Exception {
        ContentValues values = new ContentValues();
        values.put(MapperContract.Citta.ID_VIAGGIO, 1);
        values.put(MapperContract.Citta.ID_DATI_CITTA, 1);
        Uri uri = mResolver.insert(MapperContract.Citta.CONTENT_URI, values);
        assertNotNull(uri);
        assertEquals(uri.getLastPathSegment(), "1");
    }
}
