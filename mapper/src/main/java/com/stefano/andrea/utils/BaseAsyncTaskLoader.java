package com.stefano.andrea.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.stefano.andrea.providers.MapperContract;

/**
 * Inspired by:
 * Generic {@link AsyncTaskLoader} which also registers and handles data changes via a {@link android.database.ContentObserver}.
 *
 * @author jmardis
 * @link https://gist.github.com/jerrellmardis/5222580
 */
public abstract class BaseAsyncTaskLoader<D> extends AsyncTaskLoader<D> {

    private D mData;
    private DataObserver mObserver;

    public BaseAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(D data) {
        if (isReset()) {
            return;
        }

        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (mObserver == null) {
            mObserver = new DataObserver(null);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mData != null) {
            mData = null;
        }

        if (mObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
    }

    private class DataObserver extends ContentObserver {

        private BaseAsyncTaskLoader mLoader;

        public DataObserver (Handler handler) {
            super(handler);
            mLoader = BaseAsyncTaskLoader.this;
            mLoader.getContext().getContentResolver().registerContentObserver(MapperContract.Foto.CONTENT_URI, false, this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            mLoader.onContentChanged();
        }
    }
}
