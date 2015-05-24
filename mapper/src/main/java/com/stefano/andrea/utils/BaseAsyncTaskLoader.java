package com.stefano.andrea.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Generic {@link AsyncTaskLoader} which also registers and handles data changes via a {@link BroadcastReceiver}.
 *
 * @author jmardis
 * @link https://gist.github.com/jerrellmardis/5222580
 */
public abstract class BaseAsyncTaskLoader<D> extends AsyncTaskLoader<D> {

    private D mData;
    private DataObserverReceiver mDataObserverReceiver;
    private String mIntentFilterAction;

    public BaseAsyncTaskLoader(Context context) {
        super(context);
    }

    public BaseAsyncTaskLoader(Context ctx, String intentFilterAction) {
        this(ctx);
        mIntentFilterAction = intentFilterAction;
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

        if (mDataObserverReceiver == null && mIntentFilterAction != null && mIntentFilterAction.length() > 0) {
            mDataObserverReceiver = new DataObserverReceiver();
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

        if (mDataObserverReceiver != null) {
            getContext().unregisterReceiver(mDataObserverReceiver);
            mDataObserverReceiver = null;
        }
    }

    private class DataObserverReceiver extends BroadcastReceiver {

        final BaseAsyncTaskLoader<D> mLoader;

        public DataObserverReceiver () {
            mLoader = BaseAsyncTaskLoader.this;

            IntentFilter filter = new IntentFilter(mIntentFilterAction);
            mLoader.getContext().registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mLoader.onContentChanged();
        }

    }
}
