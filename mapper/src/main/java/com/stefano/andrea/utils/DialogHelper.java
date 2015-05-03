package com.stefano.andrea.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.stefano.andrea.activities.R;

/**
 * DialogsHelper
 */
public class DialogHelper {

    public static void showAlertDialog (Context context, int titleId, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

}
