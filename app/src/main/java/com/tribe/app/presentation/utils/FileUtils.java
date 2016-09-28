package com.tribe.app.presentation.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.StringDef;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

    private static String pathEnd = "/TribeMessage/Sent";

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

    public static File getCacheDir(Context context) {
        return context.getFilesDir();
    }
}
