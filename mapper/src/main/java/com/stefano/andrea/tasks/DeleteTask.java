package com.stefano.andrea.tasks;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.EventListener;
import com.stefano.andrea.activities.R;
import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.models.Citta;
import com.stefano.andrea.models.Foto;
import com.stefano.andrea.models.Posto;
import com.stefano.andrea.models.Viaggio;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.DialogHelper;

import java.util.Collections;
import java.util.List;

/**
 * DeleteTask
 */
public class DeleteTask<T> extends AsyncTask<Integer, Void, Integer> {

    private interface DeleteInterface<T> {
        int cancellaItem (T item);
    }

    public interface DeleteAdapter {
        void cancellaItems (List<Integer> items);
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
    private String mMessaggio;
    private EventListener mListener;

    public DeleteTask (Activity activity, DeleteAdapter adapter, List<T> list, List<Integer> selectedItems) {
        this(activity, adapter, list, selectedItems, null);
    }

    public DeleteTask (Activity activity, DeleteAdapter adapter, List<T> list, List<Integer> selectedItems, @Nullable EventListener listener) {
        mResolver = activity.getContentResolver();
        mAdapter = adapter;
        mList = list;
        mSelectedItems = selectedItems;
        mActivity = activity;
        mListener = listener;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        if (params.length == 1) {
            //assegno il delegate corretto
            DeleteInterface mDelegate;
            switch (params[0]) {
                case CANCELLA_VIAGGIO:
                    mDelegate = new CancellaViaggio();
                    mMessaggio = mActivity.getResources().getQuantityString(R.plurals.cancellazione_viaggio, mSelectedItems.size(), mSelectedItems.size());
                    break;
                case CANCELLA_CITTA:
                    mDelegate = new CancellaCitta();
                    mMessaggio = mActivity.getResources().getQuantityString(R.plurals.cancellazione_citta, mSelectedItems.size(), mSelectedItems.size());
                    break;
                case CANCELLA_POSTO:
                    mDelegate = new CancellaPosto();
                    mMessaggio = mActivity.getResources().getQuantityString(R.plurals.cancellazione_posto, mSelectedItems.size(), mSelectedItems.size());
                    break;
                case CANCELLA_FOTO:
                    mDelegate = new CancellaFoto();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            for (int i = mList.size() - 1; i >= 0; i--) {
                if (mSelectedItems.contains(i)) {
                    //cancello l'elemento
                    mDelegate.cancellaItem(mList.get(i));
                }
            }
            return RESULT_OK;
        }
        return RESULT_ERROR;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (result == RESULT_OK) {
            Collections.sort(mSelectedItems);
            mAdapter.cancellaItems(mSelectedItems);
            //mostro snackbar di conferma dell'operazione
            if (mListener != null)
                SnackbarManager.show(
                        Snackbar.with(mActivity)
                                .text(mMessaggio)
                                .eventListener(mListener));
        } else {
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
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_VIAGGIO));
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
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_VIAGGIO));
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_CITTA));
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
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_VIAGGIO));
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_CITTA));
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_MAPPA));
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_FOTO));
            return count;
        }
    }

}
