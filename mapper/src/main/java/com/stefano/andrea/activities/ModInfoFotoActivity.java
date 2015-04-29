package com.stefano.andrea.activities;

import android.content.ContentResolver;
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

import com.stefano.andrea.utils.SavePhotoHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ModInfoFotoActivity extends ActionBarActivity {

    private static final String TAG = "MadInfoFoto";

    private Bitmap image;
    private ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_info_foto);
        mResolver = getContentResolver();
        if (getIntent() != null) {
            image = getIntent().getExtras().getParcelable(MainActivity.EXTRA_FOTO);
            ImageView view = (ImageView) findViewById(R.id.thumb_mod_info_foto);
            view.setImageBitmap(image);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_salva_foto) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Uri uri = SavePhotoHelper.insertImage(mResolver, image, "mapper" + timestamp + ".jpg", "MapperApp");
            String path = getFotoPath(uri);
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Salvata foto in " + path);
            }
            startActivity(new Intent(this, MainActivity.class));
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
