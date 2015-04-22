package com.stefano.andrea.adapters;

import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.stefano.andrea.providers.MapperContentProvider;
import com.stefano.andrea.providers.MapperContract;

/**
 * ViaggiAdapterTest
 */
public class ViaggiAdapterTest extends ProviderTestCase2<MapperContentProvider>{

    private MockContentResolver mResolver;
    private ViaggiAdapter mAdapter;

    public ViaggiAdapterTest () {
        super(MapperContentProvider.class, MapperContract.CONTENT_AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mResolver = getMockContentResolver();
        mAdapter = new ViaggiAdapter(null, null, null, mResolver);
        mAdapter.creaNuovoViaggio("viaggio1");
        mAdapter.creaNuovoViaggio("viaggio2");
        mAdapter.creaNuovoViaggio("viaggio3");
        mAdapter.creaNuovoViaggio("viaggio4");
    }

    public void testCreaNuovoViaggio() throws Exception {
        String nome = "Roma";
        Uri uri = mAdapter.creaNuovoViaggio(nome);
        Cursor c = mResolver.query(uri, MapperContract.Viaggio.PROJECTION_ALL, null, null, MapperContract.Viaggio.DEFAULT_SORT);
        c.moveToNext();
        String query = c.getString(c.getColumnIndex(MapperContract.Viaggio.NOME));
        assertEquals(query, nome);
        long id = c.getLong(c.getColumnIndex(MapperContract.Viaggio.ID_VIAGGIO));
        assertEquals(id, 5);
        c.close();
    }
}