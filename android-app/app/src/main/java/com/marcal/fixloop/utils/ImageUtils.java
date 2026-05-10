package com.marcal.fixloop.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Classe d'utilitat per a la gestió i processament d'imatges
 * S'encarrega de llegir imatges de la galeria, reduir-les, corregir
 * la seva orientació i guardar-les comprimides
 */
public class ImageUtils {

    public static File getFileFromUri(Context context, Uri uri) {
        try {
            // Obtenim el nom del fitxer original
            String fileName = getFileName(context, uri);

            // Creem un fitxer buit a la carpeta cache de l'app
            File tempFile = new File(context.getCacheDir(), fileName);

            // Llegim l'orientació (EXIF) abans de processar els píxels
            InputStream inputForExif = context.getContentResolver().openInputStream(uri);
            int orientation = ExifInterface.ORIENTATION_NORMAL;

            if (inputForExif != null) {
                try {
                    ExifInterface exif = new ExifInterface(inputForExif);
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                } catch (Exception e) {
                    Log.w("ImageUtils", "No s'ha pogut llegir EXIF: " + e.getMessage());
                } finally {
                    inputForExif.close();
                }
            }

            // Llegim la imatge original (sense compressió)
            InputStream inputForDecode = context.getContentResolver().openInputStream(uri);
            if (inputForDecode == null) return null;

            Bitmap originalBitmap = BitmapFactory.decodeStream(inputForDecode);
            inputForDecode.close();

            if (originalBitmap == null) return null;

            // Calculem l'escala per fer la foto més petita
            int maxWidth = 800;
            int maxHeight = 800;
            float scale = Math.min(((float)maxWidth / originalBitmap.getWidth()), ((float)maxHeight / originalBitmap.getHeight()));

            // 4. Creem una matriu per aplicar l'escala i la rotació
            Matrix matrix = new Matrix();

            // Apliquem la reducció si és necessari
            if (scale < 1) {
                matrix.postScale(scale, scale);
            }

            // Apliquem la rotació
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
            }

            //Creem la imatge final ja rotada i petita
            Bitmap finalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

            // Alliberem la imatge original de la memòria per no col·lapsar l'app
            if (finalBitmap != originalBitmap) {
                originalBitmap.recycle();
            }

            // Guardar la imatge comprimida
            FileOutputStream out = new FileOutputStream(tempFile);

            // Comprimim a JPG 80% per reduir mida mantenint una qualitat decent
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

            out.flush();
            out.close();

            return tempFile;

        } catch (Exception e) {
            Log.e("ImageUtils", "Error processant imatge: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper per extreure el nom real del fitxer a partir de la URI de contingut
     */
    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = "temp_image.jpg";
        }
        return result;
    }
}