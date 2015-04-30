package com.stefano.andrea.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.stefano.andrea.models.Foto;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.tasks.InsertTask;
import com.stefano.andrea.utils.SavePhotoHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ModInfoFotoActivity extends ActionBarActivity {

    public final static String EXTRA_ID_VIAGGIO = "com.stefano.andrea.mapper.ModInfoFotoActivity.idViaggio";
    public final static String EXTRA_ID_CITTA = "com.stefano.andrea.mapper.ModInfoFotoActivity.idCitta";
    public final static String EXTRA_FOTO = "com.stefano.andrea.mapper.ModInfoFotoActivity.Foto";

    private static final String TAG = "MadInfoFoto";

    private Bitmap mImage;
    private ContentResolver mResolver;
    private long mIdViaggio;
    private long mIdCitta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_info_foto);
        mResolver = getContentResolver();
        Intent intent = getIntent();
        if (intent != null) {

            mImage = intent.getParcelableExtra(MainActivity.EXTRA_FOTO);
            mIdViaggio = intent.getLongExtra(EXTRA_ID_VIAGGIO, -1);
            mIdCitta = intent.getLongExtra(EXTRA_ID_CITTA, -1);
            /*if (intent.hasExtra(EXTRA_ID_VIAGGIO)) {
                mIdViaggio = intent.getLongExtra(EXTRA_ID_VIAGGIO, -1);
            }
            if (intent.hasExtra(EXTRA_ID_CITTA)) {
                mIdCitta = intent.getLongExtra(EXTRA_ID_CITTA, -1);
            }*/
        }
        //acquisisco riferimenti
        ImageView view = (ImageView) findViewById(R.id.thumb_mod_info_foto);
        TextView nomeViaggioView = (TextView) findViewById(R.id.txt_edit_viaggio_foto);
        TextView nomeCittaView = (TextView) findViewById(R.id.txt_edit_citta_foto);
        if (mImage != null) {
            view.setImageBitmap(mImage);
        }
        if (mIdViaggio != -1) {
            Uri viaggio = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, mIdViaggio);
            String [] projection = {MapperContract.Viaggio.NOME};
            Cursor cViaggio = mResolver.query(viaggio, projection, null, null, null);
            if (cViaggio != null && cViaggio.getCount() > 0) {
                cViaggio.moveToFirst();
                String nomeViaggio = cViaggio.getString(cViaggio.getColumnIndex(projection[0]));
                nomeViaggioView.setText(nomeViaggio);
            }
        }
        if (mIdCitta != -1) {
            Uri citta = ContentUris.withAppendedId(MapperContract.Citta.CONTENT_URI, mIdCitta);
            String [] projection = {MapperContract.DatiCitta.NOME};
            Cursor cCitta = mResolver.query(citta, projection, null, null, null);
            if (cCitta != null && cCitta.getCount() > 0) {
                cCitta.moveToFirst();
                String nomeCitta = cCitta.getString(cCitta.getColumnIndex(projection[0]));
                nomeCittaView.setText(nomeCitta);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mod_info_foto, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_salva_foto) {
            Foto foto = new Foto();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Uri uri = SavePhotoHelper.insertImage(mResolver, mImage, "mapper" + timestamp + ".jpg", "MapperApp");
            String path = getFotoPath(uri);
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Salvata foto in " + path);
            }
            foto.setPath(path);
            foto.setLatitudine(0);
            foto.setLongitudine(0);
            foto.setIdCitta(mIdCitta);
            new InsertTask<>(this, mResolver, null, foto).execute(InsertTask.INSERISCI_FOTO);
            //startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_annula_foto) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getFotoPath (Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null,null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();

        return path;
    }
}
