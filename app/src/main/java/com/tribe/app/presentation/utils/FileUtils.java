package com.tribe.app.presentation.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.StringDef;
import android.util.Log;
import android.widget.Toast;

import com.tribe.app.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 28/06/2016.
 */
@Singleton
public class FileUtils {

    @StringDef({VIDEO, PHOTO})
    public @interface MessageType {}

    public static final String VIDEO = "video";
    public static final String PHOTO = "photo";

    private static String pathEnd = "/Tribe/Sent";
    private static String pathEndTemp = "/Tribe/Sent/Temp";
    private static String pathSave = "/Tribe";


    @Inject
    public FileUtils() {
    }

    public static String generateFile(Context context, String id, @MessageType String type) {
        return getFile(context, id, type).getAbsolutePath();
    }

    public static File getFile(Context context, String id, @MessageType String type) {
        File endDir = new File(context.getFilesDir() + pathEnd);

        if (!endDir.exists()) {
            endDir.mkdirs();
        }

        return generateOutputFile(endDir, id, type);
    }

    public static File getFileTemp(Context context, String id, @MessageType String type) {
        File endDir = new File(context.getFilesDir() + pathEndTemp);

        if (!endDir.exists()) {
            endDir.mkdirs();
        }

        return generateOutputFile(endDir, id, type);
    }

    public static File generateOutputFile(File dir, String id, @MessageType String type) {
        return new File(dir, getTribeFilenameForId(id, type));
    }

    public static String getPathForId(Context context, String id, @MessageType String type) {
        File endDir = new File(getCacheDir(context) + pathEnd);
        return generateOutputFile(endDir, id, type).getAbsolutePath();
    }

    public static String getTribeFilenameForId(String id, @MessageType String type) {
        return id + (type == PHOTO ? ".jpeg" : ".mp4");
    }

    public static void delete(Context context, String id, @MessageType String type) {
        File endDir = new File(getCacheDir(context) + pathEnd);
        generateOutputFile(endDir, id, type).delete();
    }

    public static String generateIdForMessage() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void copyInputStreamToFile(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[10 * 1024];
        int len;

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        out.close();
        in.close();
    }

    public static File bitmapToFile(String name, Bitmap bitmap, Context context) {
        File f = new File(context.getCacheDir(), name);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmapToFile(bitmap, f);
    }

    public static File bitmapToFilePublic(String name, Bitmap bitmap, Context context) {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Tribe");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File f = new File(dir, name);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmapToFile(bitmap, f);
    }

    public static File bitmapToFile(Bitmap bitmap, File file) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.close();
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public static void saveToMediaStore(Context context, String path) {
        String title = "tribe_" + System.currentTimeMillis() + ".mp4";
        File saveDir = new File(Environment.getExternalStorageDirectory() + pathSave);
        if (!saveDir.exists()) saveDir.mkdirs();

        File file = new File(saveDir, title);
        copyFile(path, file.getAbsolutePath());

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.TITLE, title);
        values.put(MediaStore.Video.Media.DESCRIPTION, "");
        values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Video.VideoColumns.BUCKET_ID, file.toString().toLowerCase(context.getResources().getConfiguration().locale).hashCode());
        values.put(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(context.getResources().getConfiguration().locale));
        values.put("_data", file.getAbsolutePath());

        ContentResolver cr = context.getContentResolver();
        cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

        Toast.makeText(context, R.string.tribe_more_save_success, Toast.LENGTH_SHORT).show();
    }

    public static void copyFile(String inputPath, String outputPath) {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    public static File getCacheDir(Context context) {
        return context.getFilesDir();
    }
}
