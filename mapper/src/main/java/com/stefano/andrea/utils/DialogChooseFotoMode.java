package com.stefano.andrea.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DialogChooseFotoMode
 */
public class DialogChooseFotoMode {

    private static final String TAG = "DialogFoto";

    public static final int CAMERA_REQUEST = 0;
    public static final int GALLERY_PICTURE = 1;

    /**
     * Mostra il dialog per scegliere da dove acquisire la foto
     * @param activity L'activity sulla quale si vuole mostrare il dialog
     * @param imageUri L'uri dove si vuole salvare l'immagine (solo nel caso della fotocamera)
     */
    public static void mostraDialog (final Activity activity, final Uri imageUri) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle("Titolo");
        dialog.setMessage("Scegli foto");
        dialog.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("image/*");
                intent.putExtra("return-data", true);
                activity.startActivityForResult(intent, GALLERY_PICTURE);
            }
        });
        dialog.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (imageUri != null) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    activity.startActivityForResult(intent, CAMERA_REQUEST);
                } else {
                    Log.v(TAG, "URI dell'immagine non settata!");
                }
            }
        });
        dialog.show();
    }

    /**
     * Crea l'uri per l'immagine da salvare da fotocamera
     * @return L'uri dell'immagine
     */
    public static Uri getImageUri () {
        File photo;
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            photo = createFile("photo" + timestamp, ".jpg");
            photo.delete();
            return Uri.fromFile(photo);
        } catch (Exception e) {
            Log.v(TAG, "Impossibile creare il file!");
        }
        return null;
    }

    private static File createFile(String part, String ext) throws Exception {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        dir = new File(dir.getAbsolutePath() + "/Mapper/");
        if(!dir.exists()) {
            dir.mkdir();
        }
        return File.createTempFile(part, ext, dir);
    }

}
