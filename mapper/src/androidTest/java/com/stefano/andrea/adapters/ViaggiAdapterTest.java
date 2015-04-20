package com.stefano.andrea.adapters;

import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.stefano.andrea.providers.MapperContentProvider;
import com.stefano.andrea.providers.MapperContract;

import java.util.ArrayList;
import java.util.List;

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
        mAdapter = new ViaggiAdapter(null, mResolver, new ViaggiAdapter.ViaggioOnClickListener() {
            @Override
            public void selezionatoViaggio(long id) {
                //do nothing
            }
        });
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

    public void testDeleteViaggio() throws Exception {
        int count = mAdapter.cancellaViaggio(1);
        assertEquals(count, 1);
    }

    public void testDeleteViaggi() throws Exception {
        List<Integer> removeIds = new ArrayList<>();
        removeIds.add(2);
        removeIds.add(3);
        removeIds.add(4);
        int count = mAdapter.cancellaViaggi(removeIds);
        assertEquals(count, 3);
    }
}