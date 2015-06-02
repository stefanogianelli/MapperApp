package com.stefano.andrea.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.dialogs.DialogHelper;
import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * InsertTask
 */
public class InsertTask<T> extends AsyncTask<Integer, Void, Integer> {

    private static final String TAG = "InsertTask";

    /**
     * Interfaccia che deve essere implementata dalle classi che si occupano dell'inserimento di un nuovo elemento
     */
    private interface InsertInterface {
        int insertItem ();
    }

    /**
     * Interfaccia di callback che viene richiamata al termine dell'inserimento, restituendo l'oggetto appena inserito
     * @param <T> La tipologia dell'elemento
     */
    public interface InsertAdapter<T> {
        void insertItem (T item);
    }

    public final static int INSERISCI_VIAGGIO = 0;
    public final static int INSERISCI_CITTA = 1;
    public final static int INSERISCI_POSTO = 2;
    public final static int INSERISCI_FOTO = 3;

    private final static int RESULT_OK = 10;
    private final static int RESULT_ERROR = 11;
    private final static int RESULT_NO_ACTION = 12;

    private Activity mActivity;
    private Context mContext;
    private ContentResolver mResolver;
    private InsertAdapter mAdapter;
    private T mItem;
    private ProgressDialog mDialog;
    private String mMessaggio;

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
                    mMessaggio = mActivity.getResources().getString(R.string.inserito_nuovo_viaggio);
                    break;
                case INSERISCI_CITTA:
                    mDelegate = new InsertCitta();
                    mMessaggio = mActivity.getResources().getString(R.string.inserita_nuova_citta);
                    break;
                case INSERISCI_POSTO:
                    mDelegate = new InsertPosto();
                    mMessaggio = mActivity.getResources().getString(R.string.inserito_nuovo_posto);
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
            //mostro snackbar di conferma dell'operazione
            Snackbar.make(mActivity.getCurrentFocus(), mMessaggio, Snackbar.LENGTH_SHORT).show();
        } else if (result == RESULT_NO_ACTION) {
            Snackbar.make(mActivity.getCurrentFocus(), mActivity.getString(R.string.elemento_presente), Snackbar.LENGTH_SHORT).show();
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
            //verifico se la citta' non sia gia' stata aggiunta al viaggio
            if (!checkExistence()) {
                //verifico se la citta' esiste gia nel database
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
            } else
                return RESULT_NO_ACTION;
        }

        /**
         * Verifica se la città che si vuole inserire esiste già
         * @return True se la città esiste, false altrimenti
         */
        private boolean checkExistence () {
            boolean res = false;
            String selection = MapperContract.DatiCitta.NOME + "=? AND " + MapperContract.DatiCitta.NAZIONE + "=? AND " + MapperContract.Citta.ID_VIAGGIO + "=?";
            String [] selectionArgs = {citta.getNome(), citta.getNazione(), Long.toString(citta.getIdViaggio())};
            Cursor c = mResolver.query(MapperContract.Citta.CONTENT_URI, MapperContract.Citta.PROJECTION_ALL, selection, selectionArgs, null);
            if (c.getCount() > 0) res = true;
            c.close();
            return res;
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
            //verifico se il posto non sia gia' stata aggiunto al viaggio
            if (!checkExistence()) {
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
            } else
                return RESULT_NO_ACTION;
        }

        /**
         * Verifica se il posto che si vuole inserire esiste già
         * @return True se il posto esiste, false altrimenti
         */
        private boolean checkExistence () {
            boolean res = false;
            String selection = MapperContract.Luogo.NOME + "=? AND " + MapperContract.Posto.ID_CITTA + "=?";
            String [] selectionArgs = {posto.getNome(), Long.toString(posto.getIdCitta())};
            Cursor c = mResolver.query(MapperContract.Posto.CONTENT_URI, MapperContract.Posto.PROJECTION_ALL, selection, selectionArgs, null);
            if (c.getCount() > 0) res = true;
            c.close();
            return res;
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
            Uri uri = mResolver.insert(MapperContract.Luogo.CONTENT_URI, values);
            long id = Long.parseLong(uri.getLastPathSegment());
            if (id != -1) {
                posto.setIdLuogo(id);
                return RESULT_OK;
            }
            return RESULT_ERROR;
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
        @SuppressLint("SimpleDateFormat")
        public int insertItem() {
            ContentValues values = new ContentValues();
            for (int i = 0; i < elencoFoto.size(); i++) {
                Foto foto = elencoFoto.get(i);
                values.clear();
                values.put(MapperContract.Foto.PATH, foto.getPath());
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
                values.put(MapperContract.Foto.INDIRIZZO, foto.getIndirizzo());
                String dataExif = "";
                try {
                    ExifInterface exif = new ExifInterface(foto.getPath().substring(7));
                    //APERTURE + EXPOSURE_TIME + ISO
                    String aperture = exif.getAttribute(ExifInterface.TAG_APERTURE);
                    String exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
                    String iso = exif.getAttribute(ExifInterface.TAG_ISO);
                    if (aperture != null && exposureTime != null && iso != null) {
                        String exifData = "F/" + aperture + " " + exposureTime + "m" + " ISO-" + iso;
                        values.put(MapperContract.Foto.EXIF, exifData);
                    }
                    String model = exif.getAttribute(ExifInterface.TAG_MODEL);
                    if (model != null)
                        values.put(MapperContract.Foto.MODEL, model);
                    //leggo la data, se presente
                    dataExif = exif.getAttribute(ExifInterface.TAG_DATETIME);
                } catch (IOException e) {
                    Log.e(TAG, "Impossibile leggere i dati EXIF - " + e.getMessage());
                }
                if (dataExif != null) {
                    //uso la data EXIF
                    try {
                        Date d = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(dataExif);
                        values.put(MapperContract.Foto.DATA, d.getTime());
                    } catch (ParseException e) {
                        //uso DATE_ADDED
                        values.put(MapperContract.Foto.DATA, foto.getData() * 1000);
                    }
                } else {
                    //uso DATE_ADDED
                    values.put(MapperContract.Foto.DATA, foto.getData() * 1000);
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
    }

}
