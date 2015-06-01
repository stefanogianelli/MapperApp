package com.stefano.andrea.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.stefano.andrea.activities.R;
import com.stefano.andrea.providers.MapperContract;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ConsistencyService
 */
public class ConsistencyService extends IntentService {

    private static final String TAG = "ConsistencyService";
    private static final int NOTIFY_ID = 1;

    private static final String MAPPER_FOLDER = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES) + "/Mapper";

    private ContentResolver mResolver;
    private String mCondition;

    public ConsistencyService() {
        super(TAG);
        mCondition = "";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mResolver = getContentResolver();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int countFotoDB = 0;
        int countFotoFolder = 0;
        List<Long> fotoIndexes = new ArrayList<>();
        String [] projection = {MapperContract.Foto.ID, MapperContract.Foto.PATH};
        /*
         * fase 1: verifico che tutti i file delle foto esistano
         */
        Cursor fotoDB = mResolver.query(MapperContract.Foto.CONTENT_URI, projection, null, null, null);
        while (fotoDB.moveToNext()) {
            String path = fotoDB.getString(fotoDB.getColumnIndex(projection[1])).substring(7);
            if (!new File(path).exists()) {
                fotoIndexes.add(fotoDB.getLong(fotoDB.getColumnIndex(projection[0])));
                countFotoDB++;
            }
        }
        Log.d(TAG, "Trovata/e " + countFotoDB + " foto orfana/e nel DB");
        fotoDB.close();
        //costruisco la query per la delete
        for (int i = 0; i < fotoIndexes.size(); i++) {
            addWhere(fotoIndexes.get(i));
        }
        //elimino i riferimenti dal database
        if (!mCondition.isEmpty()) {
            mResolver.delete(MapperContract.Foto.CONTENT_URI, mCondition, null);
        }
        /*
         * fase 2: verifico che non siano rimaste foto orfane nella directory "Mapper"
         */
        List<String> dbPath = new ArrayList<>();
        List<String> folderPath = new ArrayList<>();
        String selection = MapperContract.Foto.CAMERA + "=?";
        String [] selectionArgs = {Integer.toString(1)};
        Cursor fotoMapper = mResolver.query(MapperContract.Foto.CONTENT_URI, projection, selection, selectionArgs, null);
        while (fotoMapper.moveToNext()) {
            dbPath.add(fotoMapper.getString(fotoMapper.getColumnIndex(projection[1])).substring(7));
        }
        fotoMapper.close();
        File dir = new File(MAPPER_FOLDER);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                folderPath.add(file.getAbsolutePath());
            }
            //verifico i file non presenti nel db
            folderPath.removeAll(dbPath);
            //elimino i file non piu' necessari
            for (String path : folderPath) {
                if (new File(path).delete())
                    countFotoFolder++;
                else
                    Log.e(TAG, "Errore durante l'eliminazione della foto: " + path);
            }
        }
        Log.d(TAG, "Trovato/i " + countFotoFolder + " file orfano/i nella cartella Mapper");
        /*
         * fase 3: invio notifica all'utente, se sono avvenute delle cancellazioni
         */
        if (countFotoDB > 0 || countFotoFolder > 0) {
            String [] messages = new String[2];
            if (countFotoDB > 0) {
                messages[0] = getResources().getQuantityString(R.plurals.rimosse_foto_db, countFotoDB, countFotoDB);
            }
            if (countFotoFolder > 0) {
                int i = 0;
                if (messages[0] != null) {
                    i = 1;
                }
                messages[i] = getResources().getQuantityString(R.plurals.rimosse_foto_folder, countFotoFolder, countFotoFolder);
            }
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.logo_statusbar)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(messages[0])
                            .setAutoCancel(true);
            if (messages[1] != null) {
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                for (String message : messages) {
                    inboxStyle.addLine(message);
                }
                builder.setStyle(inboxStyle);
            }
            PendingIntent notifyPIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
            builder.setContentIntent(notifyPIntent);
            NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notifyMgr.notify(NOTIFY_ID, builder.build());
        }
    }

    /**
     * Aggiunge una clausola where alla query per l'eliminazione delle foto
     * @param id L'id da aggiungere
     */
    private void addWhere (long id) {
        if (!mCondition.isEmpty()) {
            mCondition += " OR ";
        }
        mCondition += MapperContract.Foto.ID + "=" + longToString(id);
    }

    /**
     * Converte un long in una stringa
     * @param number Il numero da convertire
     * @return La stringa relativa al numero inserito
     */
    private String longToString (long number) {
        return Long.toString(number);
    }
}
