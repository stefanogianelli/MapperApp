package com.stefano.andrea.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.DialogHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * InsertTask
 */
public class InsertTask<T> extends AsyncTask<Integer, Void, Integer> {

    private static final String TAG = "InsertTask";

    private interface InsertInterface {
        int insertItem ();
    }

    public interface InsertAdapter<T> {
        void insertItem (T item);
    }

    public final static int INSERISCI_VIAGGIO = 0;
    public final static int INSERISCI_CITTA = 1;
    public final static int INSERISCI_POSTO = 2;
    public final static int INSERISCI_FOTO = 3;

    private final static int RESULT_OK = 10;
    private final static int RESULT_ERROR = 11;

    private Activity mActivity;
    private Context mContext;
    private ContentResolver mResolver;
    private InsertAdapter mAdapter;
    private T mItem;
    private ProgressDialog mDialog;

    public InsertTask (Activity activity, InsertAdapter adapter, T item) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        mResolver = activity.getContentResolver();
        mAdapter = adapter;
        mItem = item;
        mDialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.setMessage(mContext.getResources().getString(R.string.inserimento_nuovo_elemento_dialog));
        mDialog.show();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        if (params.length == 1) {
            InsertInterface mDelegate;
            switch (params[0]) {
                case INSERISCI_VIAGGIO:
                    mDelegate = new InsertViaggio();
                    break;
                case INSERISCI_CITTA:
                    mDelegate = new InsertCitta();
                    break;
                case INSERISCI_POSTO:
                    mDelegate = new InsertPosto();
                    break;
                case INSERISCI_FOTO:
                    mDelegate = new InsertFoto();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            //aggiungo l'elemento nel database
            return mDelegate.insertItem();
        }
        return RESULT_ERROR;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        mDialog.dismiss();
        if (result == RESULT_OK) {
            //comunico l'avvenuto inserimento all'adapter
            if (mAdapter != null)
                mAdapter.insertItem(mItem);
        } else {
            //mostro dialog d'errore
            DialogHelper.showAlertDialog(mActivity, R.string.errore_inserimento_titolo_dialog, R.string.errore_inserimento_messaggio_dialog);
        }
    }

    /**
     * Classe che si oocupa dell'inserimento di un nuovo viaggio
     */
    private class InsertViaggio implements InsertInterface {

        private Viaggio viaggio;

        public InsertViaggio () {
            viaggio = (Viaggio) mItem;
        }

        @Override
        public int insertItem() {
            ContentValues values = new ContentValues();
            values.put(MapperContract.Viaggio.NOME, viaggio.getNome());
            Uri uri = mResolver.insert(MapperContract.Viaggio.CONTENT_URI, values);
            long id = Long.parseLong(uri.getLastPathSegment());
            if (id != -1) {
                viaggio.setId(id);
                return RESULT_OK;
            }
            return RESULT_ERROR;
        }
    }

    /**
     * Classe che si occupa dell'inserimento di una nuova citta
     */
    private class InsertCitta implements InsertInterface {

        private Citta citta;

        public InsertCitta () {
            citta = (Citta) mItem;
        }

        @Override
        public int insertItem() {
            //verifico se la citta esiste gia nel database
            int res = this.getDatiCitta();
            if (res == RESULT_ERROR) {
                //creo la citta
                InsertDatiCitta datiCitta = new InsertDatiCitta();
                res = datiCitta.insertItem();
            }
            if (res == RESULT_OK) {
                ContentValues values = new ContentValues();
                values.put(MapperContract.Citta.ID_VIAGGIO, citta.getIdViaggio());
                values.put(MapperContract.Citta.ID_DATI_CITTA, citta.getIdCitta());
                Uri uri = mResolver.insert(MapperContract.Citta.CONTENT_URI, values);
                long id = Long.parseLong(uri.getLastPathSegment());
                if (id != -1) {
                    citta.setId(id);
                    mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_VIAGGIO));
                    mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_MAPPA));
                    return RESULT_OK;
                }
            }
            return RESULT_ERROR;
        }

        /**
         * Verifica se esistono nel database i dati di una citta
         */
        private int getDatiCitta () {
            int result = RESULT_ERROR;
            String selection = MapperContract.DatiCitta.NOME + "=? AND " + MapperContract.DatiCitta.NAZIONE + "=?";
            String [] selectionArgs = {citta.getNome(), citta.getNazione()};
            Cursor c = mResolver.query(MapperContract.DatiCitta.CONTENT_URI, MapperContract.DatiCitta.PROJECTION_ALL, selection, selectionArgs, MapperContract.DatiCitta.DEFAULT_SORT + " LIMIT 1");
            if (c.moveToFirst()) {
                citta.setIdCitta(c.getLong(c.getColumnIndex(MapperContract.DatiCitta.ID)));
                citta.setLatitudine(c.getLong(c.getColumnIndex(MapperContract.DatiCitta.LATITUDINE)));
                citta.setLongitudine(c.getLong(c.getColumnIndex(MapperContract.DatiCitta.LONGITUDINE)));
                result = RESULT_OK;
            }
            c.close();
            return result;
        }
    }

    /**
     * Classe che si occupa dell'inserimento dei dati di una citta
     */
    private class InsertDatiCitta implements InsertInterface{

        private Citta citta;

        public InsertDatiCitta () {
            citta = (Citta) mItem;
        }

        @Override
        public int insertItem() {
            ContentValues values = new ContentValues();
            values.put(MapperContract.DatiCitta.NOME, citta.getNome());
            values.put(MapperContract.DatiCitta.NAZIONE, citta.getNazione());
            values.put(MapperContract.DatiCitta.LATITUDINE, citta.getLatitudine());
            values.put(MapperContract.DatiCitta.LONGITUDINE, citta.getLongitudine());
            Uri uri = mResolver.insert(MapperContract.DatiCitta.CONTENT_URI, values);
            long idCitta = Long.parseLong(uri.getLastPathSegment());
            if (idCitta != -1) {
                citta.setIdCitta(idCitta);
                return RESULT_OK;
            }
            return RESULT_ERROR;
        }

    }

    /**
     * Classe che si occupa dell'inserimento di un posto
     */
    private class InsertPosto implements InsertInterface {

        private Posto posto;

        public InsertPosto () {
            posto = (Posto) mItem;
        }

        @Override
        public int insertItem() {
            //verifico se il luogo e' gia' presente nel db
            int res = getLuogo();
            if (res == RESULT_ERROR) {
                //creo un nuovo luogo
                InsertLuogo helper = new InsertLuogo();
                res = helper.insertItem();
            }
            if (res == RESULT_OK) {
                ContentValues values = new ContentValues();
                values.put(MapperContract.Posto.ID_CITTA, posto.getIdCitta());
                values.put(MapperContract.Posto.ID_LUOGO, posto.getIdLuogo());
                Uri uri = mResolver.insert(MapperContract.Posto.CONTENT_URI, values);
                long id = Long.parseLong(uri.getLastPathSegment());
                if (id != -1) {
                    posto.setId(id);
                    mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_VIAGGIO));
                    mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_CITTA));
                    mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_MAPPA));
                    return RESULT_OK;
                }
            }
            return RESULT_ERROR;
        }

        private int getLuogo () {
            int result = RESULT_ERROR;
            String [] projection = {MapperContract.Luogo.ID};
            String selection = MapperContract.Luogo.NOME + "=?";
            String [] selectionArgs = {posto.getNome()};
            Cursor c = mResolver.query(MapperContract.Luogo.CONTENT_URI, projection, selection, selectionArgs, MapperContract.Luogo.DEFAULT_SORT + " LIMIT 1");
            if (c.moveToFirst()) {
                posto.setIdLuogo(c.getLong(c.getColumnIndex(MapperContract.Luogo.ID)));
                result = RESULT_OK;
            }
            c.close();
            return result;
        }

    }

    /**
     * Classe che si occupa di inserire un nuovo luogo
     */
    private class InsertLuogo implements InsertInterface {

        private Posto posto;

        public InsertLuogo () {
            posto = (Posto) mItem;
        }

        @Override
        public int insertItem() {
            ContentValues values = new ContentValues();
            values.put(MapperContract.Luogo.NOME, posto.getNome());
            values.put(MapperContract.Luogo.LATITUDINE, posto.getLatitudine());
            values.put(MapperContract.Luogo.LONGITUDINE, posto.getLongitudine());
            int res = getIdDatiCitta();
            if (res == RESULT_OK) {
                values.put(MapperContract.Luogo.ID_CITTA, posto.getIdDatiCitta());
                Uri uri = mResolver.insert(MapperContract.Luogo.CONTENT_URI, values);
                long id = Long.parseLong(uri.getLastPathSegment());
                if (id != -1) {
                    posto.setIdLuogo(id);
                    return RESULT_OK;
                }
            }
            return RESULT_ERROR;
        }

        private int getIdDatiCitta () {
            int result = RESULT_ERROR;
            String [] projection = {MapperContract.Citta.ID_DATI_CITTA};
            Uri query = ContentUris.withAppendedId(MapperContract.Citta.CONTENT_URI, posto.getIdCitta());
            Cursor c = mResolver.query(query, projection, null, null, MapperContract.Citta.DEFAULT_SORT + " LIMIT 1");
            if (c.moveToFirst()) {
                posto.setIdDatiCitta(c.getLong(c.getColumnIndex(MapperContract.Citta.ID_DATI_CITTA)));
                result = RESULT_OK;
            }
            c.close();
            return result;
        }
    }

    /**
     * Classe che si occupa dell'inserimento di una foto
     */
    private class InsertFoto implements InsertInterface {

        private List<Foto> elencoFoto;

        public InsertFoto () {
            elencoFoto = (List<Foto>) mItem;
        }

        @Override
        public int insertItem() {
            ContentValues values = new ContentValues();
            for (int i = 0; i < elencoFoto.size(); i++) {
                Foto foto = elencoFoto.get(i);
                values.clear();
                values.put(MapperContract.Foto.PATH, foto.getPath());
                values.put(MapperContract.Foto.DATA, getPhotoDate(foto.getPath()));
                values.put(MapperContract.Foto.LATITUDINE, foto.getLatitudine());
                values.put(MapperContract.Foto.LONGITUDINE, foto.getLongitudine());
                values.put(MapperContract.Foto.ID_VIAGGIO, foto.getIdViaggio());
                values.put(MapperContract.Foto.ID_CITTA, foto.getIdCitta());
                values.put(MapperContract.Foto.ID_MEDIA_STORE, foto.getIdMediaStore());
                values.put(MapperContract.Foto.CAMERA, foto.getCamera());
                values.put(MapperContract.Foto.MIME_TYPE, foto.getMimeType());
                values.put(MapperContract.Foto.WIDTH, foto.getWidth());
                values.put(MapperContract.Foto.HEIGHT, foto.getHeight());
                values.put(MapperContract.Foto.SIZE, foto.getSize());
                try {
                    ExifInterface exif = new ExifInterface(foto.getPath().substring(7));
                    //APERTURE + EXPOSURE_TIME + ISO
                    String exifData = "F/"  + exif.getAttribute(ExifInterface.TAG_APERTURE);
                    exifData += " " + exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) + "m";
                    exifData += " ISO-" + exif.getAttribute(ExifInterface.TAG_ISO);
                    values.put(MapperContract.Foto.EXIF, exifData);
                    values.put(MapperContract.Foto.MODEL, exif.getAttribute(ExifInterface.TAG_MODEL));
                } catch (IOException e) {
                    Log.e(TAG, "Impossibile leggere i dati EXIF - " + e.getMessage());
                }
                if (foto.getIdPosto() != -1)
                    values.put(MapperContract.Foto.ID_POSTO, foto.getIdPosto());
                Uri uri = mResolver.insert(MapperContract.Foto.CONTENT_URI, values);
                long id = Long.parseLong(uri.getLastPathSegment());
                if (id != -1) {
                    foto.setId(id);
                    mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_VIAGGIO));
                    mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_CITTA));
                    mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_MAPPA));
                    mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_FOTO));
                }
            }
            return RESULT_OK;
        }

        private long getPhotoDate (String path) {
            File file = new File(path.substring(7));
            if(file.exists()) {
                return file.lastModified();
            }
            return 0;
        }
    }

}
