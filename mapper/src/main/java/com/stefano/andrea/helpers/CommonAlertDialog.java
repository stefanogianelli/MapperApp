package com.stefano.andrea.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import com.stefano.andrea.activities.R;

/**
 * CommonAlertDialog
 */
public class CommonAlertDialog {

    private Activity mActivity;
    private int mTitleId;
    private int mMessageId;

    public CommonAlertDialog (Activity activity, int titleId, int messageId) {
        mActivity = activity;
        mTitleId = titleId;
        mMessageId = messageId;
        this.showDialog();
    }

    private void showDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mTitleId);
        builder.setMessage(mMessageId);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Dialog) dialog).dismiss();
            }
        });
        builder.create().show();
    }
}
