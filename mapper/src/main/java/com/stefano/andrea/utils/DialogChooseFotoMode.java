package com.stefano.andrea.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

/**
 * DialogChooseFotoMode
 */
public class DialogChooseFotoMode {

    public static final int CAMERA_REQUEST = 0;
    public static final int GALLERY_PICTURE = 1;

    public static void mostraDialog (final Activity activity) {
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
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activity.startActivityForResult(intent, CAMERA_REQUEST);
            }
        });
        dialog.show();
    }

    private static File createFile(String part, String ext) throws Exception {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        dir = new File(dir.getAbsolutePath()+"/Mapper/");
        if(!dir.exists()) {
            dir.mkdir();
        }
        return File.createTempFile(part, ext, dir);
    }

}
