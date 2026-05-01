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
 * S'encarrega de llegir imatges de la galeria, corregir la seva orientació (EXIF)
 * i guardar-les com a fitxers temporals optimitzats per a la pujada al servidor
 */
public class ImageUtils {

    /**
     * Crea un fitxer temporal a la memòria cau de l'aplicació a partir d'una URI
     * Processa la imatge per corregir la rotació si és necessari i la comprimeix
     *
     * @param context Context de l'aplicació
     * @param uri Identificador únic de la imatge seleccionada
     * @return Objecte File apuntant a la imatge processada, o null si hi ha error
     */
    public static File getFileFromUri(Context context, Uri uri) {
        try {
            // Obtenim el nom del fitxer original
            String fileName = getFileName(context, uri);

            // Creem un fitxer buit a la carpeta cache de l'app
            File tempFile = new File(context.getCacheDir(), fileName);

            // Llegim orientació (EXIF)
            // Obrim un Stream específic només per llegir les metadades abans de descodificar
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

            // Llegir imatge (els pixels)
            InputStream inputForDecode = context.getContentResolver().openInputStream(uri);
            if (inputForDecode == null) return null;

            Bitmap bitmap = BitmapFactory.decodeStream(inputForDecode);
            inputForDecode.close();

            if (bitmap == null) return null;

            // Aplicar rotació
            // Si l'EXIF diu que està girada, la girem
            bitmap = rotateBitmap(bitmap, orientation);

            // Guardar imatge corregida i comprimida
            FileOutputStream out = new FileOutputStream(tempFile);

            // Comprimim a JPG 80% per reduir mida mantenint qualitat
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

            out.close();

            return tempFile;

        } catch (Exception e) {
            Log.e("ImageUtils", "Error processant imatge: " + e.getMessage());
            return null;
        }
    }

    /**
     * Mètode auxiliar per rotar un Bitmap segons la seva etiqueta d'orientació EXIF
     *
     * @param bitmap  La imatge original en memòria
     * @param orientation El codi d'orientació EXIF
     * @return Un nou Bitmap rotat correctament, o l'original si no calia rotació
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
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
            default:
                return bitmap; // No cal fer res, retornem l'original
        }

        try {
            // Creem una nova imatge transformada segons la matriu de rotació
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            // Si s'ha creat una còpia nova, reciclem la vella per estalviar memòria
            if (rotated != bitmap) {
                bitmap.recycle();
            }
            return rotated;
        } catch (OutOfMemoryError e) {
            Log.e("ImageUtils", "Memòria insuficient per rotar la imatge");
            return bitmap;
        }
    }

    /**
     * Helper per extreure el nom real del fitxer a partir de la URI de contingut
     */
    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
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