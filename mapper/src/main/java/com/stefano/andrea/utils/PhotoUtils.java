package com.stefano.andrea.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
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
    public static final int GALLERY_PICTURE = 1;

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
                        Toast.makeText(activity, "Impossibile scattare foto", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.v(TAG, "URI dell'immagine non settata!");
                }
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.action_galleria).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                activity.startActivityForResult(Intent.createChooser(intent,"Select Picture"), GALLERY_PICTURE);
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
        if (requestCode == PhotoUtils.GALLERY_PICTURE && resultCode == activity.RESULT_OK) {
            //singola immagine
            if (data.getData() != null) {
                intent = new Intent(activity, ModInfoFotoActivity.class);
                fotoUris.add(data.getData().toString());
                intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, PhotoUtils.GALLERY_PICTURE);
            } else if (data.getClipData() != null) {
                intent = new Intent(activity, ModInfoFotoActivity.class);
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    fotoUris.add(item.getUri().toString());
                }
                intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, PhotoUtils.GALLERY_PICTURE);
            }
        } else if (requestCode == PhotoUtils.CAMERA_REQUEST && resultCode == activity.RESULT_OK) {
            intent = new Intent(activity, ModInfoFotoActivity.class);
            fotoUris.add(imageUri.toString());
            intent.putExtra(ModInfoFotoActivity.EXTRA_TIPO_FOTO, PhotoUtils.CAMERA_REQUEST);
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
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File photo = createFile("mapper_" + timestamp, ".jpg");
        photo.delete();
        return Uri.fromFile(photo);
    }

    private static File createFile(String part, String ext) throws IOException {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        dir = new File(dir.getAbsolutePath() + "/Mapper/");
        if(!dir.exists()) {
            dir.mkdir();
        }
        return File.createTempFile(part, ext, dir);
    }

}
