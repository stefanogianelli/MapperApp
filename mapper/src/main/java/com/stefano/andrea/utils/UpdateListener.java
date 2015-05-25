package com.stefano.andrea.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.github.snowdream.android.app.AbstractUpdateListener;
import com.github.snowdream.android.app.DownloadTask;
import com.github.snowdream.android.app.UpdateInfo;
import com.stefano.andrea.activities.R;

import java.io.File;

/**
 * UpdateListener
 */
public class UpdateListener extends AbstractUpdateListener {

    private static final String TAG = "UpdateListener";

    private NotificationManager notificationManager = null;
    private NotificationCompat.Builder notificationBuilder = null;

    @Override
    public void onShowUpdateUI(final UpdateInfo info) {
        if (info == null) {
            return;
        }

        Context context = getContext();
        if (context != null) {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getText(R.string.autoupdate_update_tips))
                    .setMessage(getUpdateTips(info))
                    .setPositiveButton(context.getText(R.string.autoupdate_confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            informUpdate(info);
                        }
                    })
                    .setNegativeButton(context.getText(R.string.autoupdate_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            informCancel(info);
                        }
                    })
                    .setCancelable(false)
                    .create();
            dialog.show();
        }
    }

    @Override
    public void onShowNoUpdateUI() {
        Log.v(TAG, "Ultima versione");
        //elimino la cartella temporanea dei download, se presente
        File downloadDir = new File(Environment.getExternalStorageDirectory(), "/snowdream");
        if (downloadDir.exists()) {
            boolean res = deleteDirectory(downloadDir);
            Log.v(TAG, "Cancellata directory temporanea con esito: " + res);
        }
    }

    @Override
    public void onShowUpdateProgressUI(final UpdateInfo info, final DownloadTask task, final int progress) {
        Context context = getContext();
        if (context != null && task != null && info != null) {
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
            String contentTitle = info.getAppName();
            String contentText = new StringBuffer().append(progress)
                    .append("%").toString();
            int smallIcon = context.getApplicationInfo().icon;
            if (notificationManager == null) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }

            if (notificationBuilder == null) {
                notificationBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(smallIcon)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);
            }
            notificationBuilder.setContentText(contentText);
            notificationBuilder.setProgress(100, progress, false);
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    @Override
    public void ExitApp() {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private boolean deleteDirectory (File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }
}
