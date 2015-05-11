package com.stefano.andrea.tasks;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.providers.MapperContract;
import com.stefano.andrea.utils.DialogHelper;

/**
 * UpdateTask
 */
public class UpdateTask extends AsyncTask<Void, Void, Integer> {

    public interface UpdateAdapter {
        void UpdateItem (int position, String nome);
    }

    private final static int RESULT_OK = 0;
    private final static int RESULT_ERROR = 1;

    private Activity mActivity;
    private ContentResolver mResolver;
    private int mPosition;
    private long mId;
    private String mNome;
    private UpdateAdapter mAdapter;

    public UpdateTask (Activity activity, int position, long id, String nome, UpdateAdapter adapter) {
        mActivity = activity;
        mResolver = activity.getContentResolver();
        mId = id;
        mNome = nome;
        mAdapter = adapter;
        mPosition = position;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ContentValues values = new ContentValues();
        values.put(MapperContract.Viaggio.NOME, mNome);
        Uri uri = ContentUris.withAppendedId(MapperContract.Viaggio.CONTENT_URI, mId);
        int count = mResolver.update(uri, values, null, null);
        if (count == 1)
            return RESULT_OK;
        else
            return RESULT_ERROR;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (result == RESULT_OK) {
            mAdapter.UpdateItem(mPosition, mNome);
        } else {
            //mostro dialog d'errore
            DialogHelper.showAlertDialog(mActivity, R.string.errore_inserimento_titolo_dialog, R.string.errore_inserimento_messaggio_dialog);
        }
    }
}
