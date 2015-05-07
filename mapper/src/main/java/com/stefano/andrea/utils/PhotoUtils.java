package com.stefano.andrea.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.stefano.andrea.activities.ModInfoFotoActivity;
import com.stefano.andrea.activities.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * PhotoUtils
 */
public class PhotoUtils {

    private static final String TAG = "DialogFoto";

    public static final int CAMERA_REQUEST = 0;
    private static final int GALLERY_PICTURE = 1;
    private static final int GALLERY_PICTURE_KITKAT = 2;

    private static final String PHOTO_PREFIX = "mapper_";
    private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    private static final String PHOTO_POSTFIX = ".jpg";
    private static final String FOLDER = "Mapper";

    /**
     * Mostra il dialog per scegliere da dove acquisire la foto
     * @param activity L'activity sulla quale si vuole mostrare il dialog
     * @param imageUri L'uri dove si vuole salvare l'immagine (solo nel caso della fotocamera)
     */
    public static void mostraDialog (final Activity activity, final Uri imageUri) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Tolgo lo spazio per il titolo di default
        dialog.setContentView(R.layout.fragment_add_foto);
        dialog.findViewById(R.id.action_fotocamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        activity.startActivityForResult(intent, CAMERA_REQUEST);
                    } else {
                        Toast.makeText(activity, activity.getApplicationContext().getResources().getString(R.string.foto_not_allowed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "URI dell'immagine non settata!");
                }
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.action_galleria).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    //pre-kitkat version
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    activity.startActivityForResult(intent, GALLERY_PICTURE);
                } else {
                    //kitkat version
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    activity.startActivityForResult(intent, GALLERY_PICTURE_KITKAT);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Avvia l'intent per il salvataggio della foto
     * @param activity L'activity di riferimento
     * @param requestCode Il codice di risposta
     * @param resultCode Il risultato dell'operazione
     * @param data I dati risultanti
     * @param imageUri L'uri dell'immagine
     * @param idViaggio L'id del viaggio
     * @param idCitta L'id della citta'
     */
    public static void startIntent (Activity activity, int requestCode, int resultCode, Intent data, Uri imageUri, long idViaggio, long idCitta) {
        startIntent(activity, requestCode, resultCode, data, imageUri, idViaggio, idCitta, -1);
    }

    /**
     * Avvia l'intent per il salvataggio della foto
     * @param activity L'activity di riferimento
     * @param requestCode Il codice di risposta
     * @param resultCode Il risultato dell'operazione
     * @param data I dati risultanti
     * @param imageUri L'uri dell'immagine
     * @param idViaggio L'id del viaggio
     * @param idCitta L'id della citta'
     * @param idPosto L'id del posto
     */
    public static void startIntent (Activity activity, int requestCode, int resultCode, Intent data, Uri imageUri, long idViaggio, long idCitta, long idPosto) {
        Intent intent = new Intent(activity, ModInfoFotoActivity.class);
        ArrayList<String> fotoUris = new ArrayList<>();
        if ((requestCode == GALLERY_PICTURE || requestCode == GALLERY_PICTURE_KITKAT) && resultCode == Activity.RESULT_OK) {
            //singola immagine
            if (data.getData() != null) {
                String path = data.getData().toString();
                if (requestCode == GALLERY_PICTURE_KITKAT) {
                    path = "file:/" + getGalleryPhotoPath(data.getData(), activity.getContentResolver());
                }
                fotoUris.add(path);
                intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, GALLERY_PICTURE);
            } else if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    String path = item.getUri().toString();
                    if (requestCode == GALLERY_PICTURE_KITKAT) {
                        path = "file://" + getGalleryPhotoPath(item.getUri(), activity.getContentResolver());
                    }
                    fotoUris.add(path);
                }
                intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, GALLERY_PICTURE);
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(imageUri);
            activity.sendBroadcast(mediaScanIntent);
            fotoUris.add(imageUri.toString());
            intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, CAMERA_REQUEST);
        }
        intent.putStringArrayListExtra(ModInfoFotoActivity.EXTRA_FOTO, fotoUris);
        if (idViaggio != -1)
            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_VIAGGIO, idViaggio);
        if (idCitta != -1)
            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_CITTA, idCitta);
        if (idPosto != -1)
            intent.putExtra(ModInfoFotoActivity.EXTRA_ID_POSTO, idPosto);
        activity.startActivity(intent);
    }

    /**
     * Restituisce il path corretto dell'immagine selezionata dalla galleria
     * @param imageUri L'uri dell'immagine acquisita dal document provider
     * @param resolver Il content resolver dell'applicazione
     * @return Il path correto dell'immagine
     */
    @TargetApi(19)
    private static String getGalleryPhotoPath (Uri imageUri, ContentResolver resolver) {
        String wholeID = DocumentsContract.getDocumentId(imageUri);
        String id = wholeID.split(":")[1];
        String [] projection = { MediaStore.Images.Media.DATA };
        String selection = MediaStore.Images.Media._ID + "=?";
        String [] selectionArgs = { id };
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        String filePath = null;
        int columnIndex = cursor.getColumnIndex(projection[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    /**
     * Create a file Uri for saving an image
     * @return L'uri del file creato
     * @throws IOException In caso di errori durante la creazione del file
     */
    public static Uri getOutputMediaFileUri() throws IOException {
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * Create a File for saving an image
     * @return Il file creato
     * @throws IOException In caso di errore durante la creazione del file
     */
    @SuppressLint("SimpleDateFormat")
    private static File getOutputMediaFile() throws IOException {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), FOLDER);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                throw new IOException();
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                PHOTO_PREFIX + timeStamp + PHOTO_POSTFIX);

        return mediaFile;
    }

}
