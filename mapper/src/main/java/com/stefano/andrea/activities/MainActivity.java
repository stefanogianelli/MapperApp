package com.stefano.andrea.activities;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.stefano.andrea.adapters.ViaggiAdapter;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, ViaggiAdapter.ViaggioOnClickListener {

    private final static int URL_LOADER = 0;

    private RecyclerView mRecyclerView;
    private ViaggiAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ContentResolver mResolver;
    List<Viaggio> mListaViaggi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListaViaggi = new ArrayList<>();
        mResolver = getContentResolver();
        getLoaderManager().initLoader(URL_LOADER, null, this);
        // Card layout
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter
        mAdapter = new ViaggiAdapter(mListaViaggi, mResolver, this);
        mRecyclerView.setAdapter(mAdapter);

        // Floating button

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                return new CursorLoader(this, MapperContract.Viaggio.CONTENT_URI, MapperContract.Viaggio.PROJECTION_ALL, null, null, MapperContract.Viaggio._ID + " DESC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        while (data.moveToNext()) {
            long id = data.getLong(data.getColumnIndex(MapperContract.Viaggio._ID));
            String nome = data.getString(data.getColumnIndex(MapperContract.Viaggio.NOME));
            mListaViaggi.add(new Viaggio(id, nome));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListaViaggi.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void selezionatoViaggio(long id) {
        //TODO: creare intent per passare all'activity con i dettagli del viaggio
        Toast.makeText(this, "Click sul viaggio " + id, Toast.LENGTH_SHORT).show();
    }
}
