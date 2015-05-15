package com.stefano.andrea.tasks;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.intents.MapperIntent;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.DialogHelper;

import java.util.List;

/**
 * UpdateTask
 */
public class UpdateTask extends AsyncTask<Integer, Void, Integer> {

    public interface UpdateAdapter {
        void UpdateItem (int position, String nome);
    }

    public final static int UPDATE_VIAGGIO = 0;
    public final static int UPDATE_FOTO = 1;

    private final static int RESULT_OK = 0;
    private final static int RESULT_ERROR = 1;

    private Activity mActivity;
    private ContentResolver mResolver;
    private int mPosition;
    private ContentValues mValues;
    private List<Integer> mElencoId;
    private UpdateAdapter mAdapter;
    private String mResultString = "";

    public UpdateTask (Activity activity, int position, ContentValues values, List<Integer> elencoId, UpdateAdapter adapter) {
        mActivity = activity;
        mResolver = activity.getContentResolver();
        mValues = values;
        mElencoId = elencoId;
        mAdapter = adapter;
        mPosition = position;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        if (params.length == 1) {
            Uri uri;
            String selection;
            String [] selectionArgs = new String[1];
            switch (params[0]) {
                case UPDATE_VIAGGIO:
                    selection = MapperContract.Viaggio.ID_VIAGGIO + "=?";
                    uri = MapperContract.Viaggio.CONTENT_URI;
                    mResultString = mValues.getAsString(MapperContract.Viaggio.NOME);
                    break;
                case UPDATE_FOTO:
                    selection = MapperContract.Foto.ID + "=?";
                    uri = MapperContract.Foto.CONTENT_URI;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            for (int i = 0; i < mElencoId.size(); i++) {
                selectionArgs[0] = mElencoId.get(i).toString();
                mResolver.update(uri, mValues, selection, selectionArgs);
            }
            return RESULT_OK;
        }
        return RESULT_ERROR;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (result == RESULT_OK) {
            mAdapter.UpdateItem(mPosition, mResultString);
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_VIAGGIO));
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_CITTA));
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_MAPPA));
            mActivity.sendBroadcast(new Intent(MapperIntent.UPDATE_FOTO));
        } else {
            //mostro dialog d'errore
            DialogHelper.showAlertDialog(mActivity, R.string.errore_inserimento_titolo_dialog, R.string.errore_inserimento_messaggio_dialog);
        }
    }
}
