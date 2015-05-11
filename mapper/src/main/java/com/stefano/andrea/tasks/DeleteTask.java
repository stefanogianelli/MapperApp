package com.stefano.andrea.tasks;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.DialogHelper;

import java.util.List;

/**
 * DeleteTask
 */
public class DeleteTask<T> extends AsyncTask<Integer, Void, Integer> {

    private interface DeleteInterface<T> {
        int cancellaItem (T item);
    }

    public interface DeleteAdapter<T> {
        void cancellaItem (T item);
        void notificaChange ();
    }

    public final static int CANCELLA_VIAGGIO = 0;
    public final static int CANCELLA_CITTA = 1;
    public final static int CANCELLA_POSTO = 2;
    public final static int CANCELLA_FOTO = 3;

    private final static int RESULT_OK = 10;
    private final static int RESULT_ERROR = 11;

    private ContentResolver mResolver;
    private DeleteAdapter mAdapter;
    private List<T> mList;
    private List<Integer> mSelectedItems;
    private Activity mActivity;

    public DeleteTask (Activity activity, DeleteAdapter adapter, List<T> list, List<Integer> selectedItems) {
        mResolver = activity.getContentResolver();
        mAdapter = adapter;
        mList = list;
        mSelectedItems = selectedItems;
        mActivity = activity;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        if (params.length == 1) {
            //assegno il delegate corretto
            DeleteInterface mDelegate;
            switch (params[0]) {
                case CANCELLA_VIAGGIO:
                    mDelegate = new CancellaViaggio();
                    break;
                case CANCELLA_CITTA:
                    mDelegate = new CancellaCitta();
                    break;
                case CANCELLA_POSTO:
                    mDelegate = new CancellaPosto();
                    break;
                case CANCELLA_FOTO:
                    mDelegate = new CancellaFoto();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            int count;
            for (int i = mList.size(); i >= 0; i--) {
                if (mSelectedItems.contains(i)) {
                    //cancello l'elemento
                    count = mDelegate.cancellaItem(mList.get(i));
                    //informo l'adapter dell'avvenuta cancellazione
                    if (count > 0) {
                        mAdapter.cancellaItem(mList.get(i));
                    }
                }
            }
            return RESULT_OK;
        }
        return RESULT_ERROR;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (result == RESULT_OK)
            mAdapter.notificaChange();
        if (result == RESULT_ERROR) {
            DialogHelper.showAlertDialog(mActivity, R.string.errore_eliminazione_titolo_dialog, R.string.errore_eliminazione_messaggio_dialog);
        }
    }

    /** Classe che si occupa dell'eliminazione dei viaggi */
    private class CancellaViaggio implements DeleteInterface<Viaggio> {
        @Override
        public int cancellaItem(Viaggio item) {
            Uri uri = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, item.getId());
            return mResolver.delete(uri, null, null);
        }
    }

    /** Classe che si occupa dell'eliminazione delle citta */
    private class CancellaCitta implements DeleteInterface<Citta> {
        @Override
        public int cancellaItem(Citta item) {
            Uri uri = ContentUris.withAppendedId(MapperContract.Citta.CONTENT_URI, item.getId());
            int count = mResolver.delete(uri, null, null);
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_FOTO));
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_MAPPA));
            return count;
        }
    }

    /** Classe che si occupa dell'eliminazione di un posto */
    private class CancellaPosto implements DeleteInterface<Posto> {
        @Override
        public int cancellaItem(Posto item) {
            Uri uri = ContentUris.withAppendedId(MapperContract.Posto.CONTENT_URI, item.getId());
            int count = mResolver.delete(uri, null, null);
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_FOTO));
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_MAPPA));
            return count;
        }
    }

    /** Classe che si occupa dell'eliminazione di un posto */
    private class CancellaFoto implements DeleteInterface<Foto> {
        @Override
        public int cancellaItem(Foto item) {
            //cancello il file se scattato dall'app
            if (item.getCamera() == 1) {
                String selection = MediaStore.Images.Media._ID + "=?";
                String [] selectionArgs = { Integer.toString(item.getIdMediaStore()) };
                mResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            }
            //cancello il riferimento dal database
            String selection = MapperContract.Foto.ID + "=?";
            String [] selectionArgs = {Long.toString(item.getId())};
            int count = mResolver.delete(MapperContract.Foto.CONTENT_URI, selection, selectionArgs);
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_CITTA));
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_MAPPA));
            return count;
        }
    }

}
