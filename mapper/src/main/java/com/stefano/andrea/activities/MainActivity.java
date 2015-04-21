package com.stefano.andrea.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.stefano.andrea.adapters.ViaggiAdapter;
import com.stefano.andrea.loaders.ViaggiLoader;
import com.stefano.andrea.models.Viaggio;

import java.util.List;

public class MainActivity extends android.support.v7.app.ActionBarActivity implements LoaderManager.LoaderCallbacks<List<Viaggio>>, ViaggiAdapter.ViaggioOnClickListener {

    public final static String EXTRA_ID_VIAGGIO = "com.stefano.andrea.mainActivity.idViaggio";
    public final static String EXTRA_NOME_VIAGGIO = "com.stefano.andrea.mainActivitynomeViaggio";
    private final static int URL_LOADER = 0;

    private RecyclerView mRecyclerView;
    private ViaggiAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResolver = getContentResolver();
        getLoaderManager().initLoader(URL_LOADER, null, this).forceLoad();
        // Card layout
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter
        mAdapter = new ViaggiAdapter(mResolver, this);
        mRecyclerView.setAdapter(mAdapter);
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
    public void selezionatoViaggio(Viaggio viaggio) {
        Intent intent = new Intent(this, ActivityDettagliViaggio.class);
        intent.putExtra(EXTRA_ID_VIAGGIO, viaggio.getId());
        intent.putExtra(EXTRA_NOME_VIAGGIO, viaggio.getNome());
        startActivity(intent);
    }

    public void openDialogAddViaggio(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_add_viaggio, null))
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;
                        EditText nv = (EditText) d.findViewById(R.id.text_add_viaggio);
                        mAdapter.creaNuovoViaggio(nv.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public Loader<List<Viaggio>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                return new ViaggiLoader(this, mResolver);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Viaggio>> loader, List<Viaggio> data) {
        mAdapter.setListaViaggi(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Viaggio>> loader) {
        //do nothing
    }
}
