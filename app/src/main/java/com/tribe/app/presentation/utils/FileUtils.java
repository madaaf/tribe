package com.tribe.app.presentation.utils;

import android.os.Environment;

import java.io.File;
import java.util.UUID;

/**
 * Created by tiago on 28/06/2016.
 */
public class FileUtils {

    private static File pathOrigin = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static String pathAudio = "/TribeMessage/Audio";
    private static String pathVideo = "/TribeMessage/Video";
    private static String pathEnd = "/TribeMessage/Sent";

    public static String generateFileForAudio(String id) {
        File audioDir = new File(pathOrigin + pathAudio);

        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        return generateOutputFile(audioDir, id).getAbsolutePath();
    }

    public static String generateFileForVideo(String id) {
        File videoDir = new File(pathOrigin + pathVideo);

        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        return generateOutputFile(videoDir, id).getAbsolutePath();
    }

    public static String generateFileEnd(String id) {
        File endDir = new File(pathOrigin + pathEnd);

        if (!endDir.exists()) {
            endDir.mkdirs();
        }

        return generateOutputFile(endDir, id).getAbsolutePath();
    }

    public static File getFileEnd(String id) {
        File endDir = new File(pathOrigin + pathEnd);

        if (!endDir.exists()) {
            endDir.mkdirs();
        }

        return generateOutputFile(endDir, id);
    }

    public static String getPathForId(String id) {
        File endDir = new File(pathOrigin + pathEnd);
        return generateOutputFile(endDir, id).getAbsolutePath();
    }

    public static String getFilenameForId(String id) {
        return id + ".mp4";
    }

    public static void deleteTribe(String id) {
        File endDir = new File(pathOrigin + pathEnd);
        generateOutputFile(endDir, id).delete();
    }

    public static File generateOutputFile(File dir, String id) {
        File finalDir = new File(dir, getFilenameForId(id));
        return finalDir;
    }

    public static String generateIdForTribe() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
