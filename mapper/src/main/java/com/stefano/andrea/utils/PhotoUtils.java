package com.stefano.andrea.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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
        Intent intent = null;
        ArrayList<String> fotoUris = new ArrayList<>();
        if ((requestCode == GALLERY_PICTURE || requestCode == GALLERY_PICTURE_KITKAT) && resultCode == Activity.RESULT_OK) {
            //singola immagine
            if (data.getData() != null) {
                intent = new Intent(activity, ModInfoFotoActivity.class);
                if (requestCode == GALLERY_PICTURE_KITKAT)
                    activity.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                fotoUris.add(data.getData().toString());
                intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, GALLERY_PICTURE);
            } else if (data.getClipData() != null) {
                intent = new Intent(activity, ModInfoFotoActivity.class);
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    if (requestCode == GALLERY_PICTURE_KITKAT)
                        activity.getContentResolver().takePersistableUriPermission(item.getUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    fotoUris.add(item.getUri().toString());
                }
                intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, GALLERY_PICTURE);
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            intent = new Intent(activity, ModInfoFotoActivity.class);
            fotoUris.add(imageUri.toString());
            intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, CAMERA_REQUEST);
        }
        if (intent != null) {
            intent.putStringArrayListExtra(ModInfoFotoActivity.EXTRA_FOTO, fotoUris);
            if (idViaggio != -1)
                intent.putExtra(ModInfoFotoActivity.EXTRA_ID_VIAGGIO, idViaggio);
            if (idCitta != -1)
                intent.putExtra(ModInfoFotoActivity.EXTRA_ID_CITTA, idCitta);
            activity.startActivity(intent);
        }
    }

    /**
     * Crea l'uri per l'immagine da salvare da fotocamera
     * @return L'uri dell'immagine
     */
    public static Uri getImageUri () throws IOException {
        String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
        File photo = createFile(PHOTO_PREFIX + timestamp);
        photo.delete();
        return Uri.fromFile(photo);
    }

    private static File createFile(String part) throws IOException {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        dir = new File(dir.getAbsolutePath() + "/" + FOLDER + "/");
        if(!dir.exists()) {
            dir.mkdir();
        }
        return File.createTempFile(part, PHOTO_POSTFIX, dir);
    }

}
