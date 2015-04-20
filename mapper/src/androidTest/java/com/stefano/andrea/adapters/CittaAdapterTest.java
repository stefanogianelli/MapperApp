package com.stefano.andrea.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.stefano.andrea.helpers.CittaHelper;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.providers.MapperContentProvider;
import com.stefano.andrea.providers.MapperContract;

import java.util.ArrayList;
import java.util.List;

/**
 * CittaAdapterTest
 */
public class CittaAdapterTest extends ProviderTestCase2<MapperContentProvider> {

    private final static String TAG = "CittaTest";
    private final static String VIAGGIO = "viaggio1";
    private final static String CITTA = "Roma";
    private final static String NAZIONE = "Italia";

    private MockContentResolver mResolver;
    private List<Citta> mListaCitta;
    private CittaAdapter mAdapter;

    public CittaAdapterTest() {
        super(MapperContentProvider.class, MapperContract.CONTENT_AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
        mListaCitta = new ArrayList<>();
        mAdapter = new CittaAdapter(mListaCitta, mResolver, new CittaAdapter.CittaOnClickListener() {
            @Override
            public void selezionataCitta(long id) {
                //do nothing
            }
        });
        //creo viaggio di prova
        ContentValues values = new ContentValues();
        values.put(MapperContract.Viaggio.NOME, VIAGGIO);
        mResolver.insert(MapperContract.Viaggio.CONTENT_URI, values);
        //creo citta di prova
        values.clear();
        values.put(MapperContract.DatiCitta.NOME, CITTA);
        values.put(MapperContract.DatiCitta.NAZIONE, NAZIONE);
        values.put(MapperContract.DatiCitta.LATITUDINE, 0);
        values.put(MapperContract.DatiCitta.LONGITUDINE, 0);
        mResolver.insert(MapperContract.DatiCitta.CONTENT_URI, values);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetCitta () throws Exception {
        CittaHelper helper = new CittaHelper(mResolver);
        long id = helper.getDatiCitta(CITTA, NAZIONE);
        assertEquals(id, 1);
        id = helper.getDatiCitta("Milano", "Italia");
        assertEquals(id, -1);
    }

    public void testCreaCitta() throws Exception {
        String nome = "Milano";
        String nazione = "Italia";
        CittaHelper helper = new CittaHelper(mResolver);
        long id = helper.creaCitta(nome, nazione);
        assertEquals(id, 2);
    }

    public void testName() throws Exception {
        ContentValues values = new ContentValues();
        values.put(MapperContract.Citta.ID_VIAGGIO, 1);
        values.put(MapperContract.Citta.ID_CITTA, 1);
        values.put(MapperContract.Citta.PERCENTUALE, 0);
        Uri uri = mResolver.insert(MapperContract.Citta.CONTENT_URI, values);
        assertNotNull(uri);
        assertEquals(uri.getLastPathSegment(), -1);
    }

    /*public void testNuovaCitta() throws Exception {
        String nome = "Milano";
        String nazione = "Italia";
        Uri uri = mAdapter.creaNuovaCitta(1, nome, nazione);
        assertNotNull(uri);
        assertNotSame(uri.getLastPathSegment(), "-1");
        Cursor c = mResolver.query(uri, MapperContract.Citta.PROJECTION_JOIN, null, null, MapperContract.Citta.DEFAULT_SORT);
        assertNotNull(c);
        c.moveToNext();
        String query = c.getString(c.getColumnIndex(MapperContract.DatiCitta.NOME));
        c.close();
        assertEquals(query, nome);
    }*/

    /*public void testInsertCittaEsistente () throws Exception {
        Uri uri = mAdapter.creaNuovaCitta(1, "Roma", "Italia");
        Log.v(TAG, uri.toString());
        assertNotNull(uri);
        mResolver.query(uri, MapperContract.)
    }*/

    /*public void testQueryJoinUri () throws Exception {
        Uri uri = ContentUris.withAppendedId(MapperContract.Citta.CONTENT_URI, 1);
        Cursor c = mResolver.query(uri, MapperContract.Citta.PROJECTION_JOIN, null, null, MapperContract.Citta.DEFAULT_SORT);
        assertNotNull(c);
        assertEquals(c.getCount(), 1);
        c.moveToNext();
        String nome = c.getString(c.getColumnIndex(MapperContract.DatiCitta.NOME));
        assertEquals(nome, CITTA);
        c.close();
    }*/
}
