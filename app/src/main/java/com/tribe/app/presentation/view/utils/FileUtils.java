package com.tribe.app.presentation.view.utils;

import android.os.Environment;

import java.io.File;
import java.util.UUID;

/**
 * Created by tiago on 28/06/2016.
 */
public class FileUtils {

    private static File pathOrigin = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static String pathAudio = "/Tribe/Audio";
    private static String pathVideo = "/Tribe/Video";
    private static String pathEnd = "/Tribe/Sent";

    public static String generateFileForAudio(String id) {
        File audioDir = new File(pathOrigin + pathAudio);

        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        return generateOutputFile(audioDir, id);
    }

    public static String generateFileForVideo(String id) {
        File videoDir = new File(pathOrigin + pathVideo);

        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        return generateOutputFile(videoDir, id);
    }

    public static String generateFileEnd(String id) {
        File endDir = new File(pathOrigin + pathEnd);

        if (!endDir.exists()) {
            endDir.mkdirs();
        }

        return generateOutputFile(endDir, id);
    }

    public static String getPathForId(String id) {
        File endDir = new File(pathOrigin + pathEnd);
        return generateOutputFile(endDir, id);
    }

    public static String generateOutputFile(File dir, String id) {
        File finalDir = new File(dir, id + ".mp4");
        return finalDir.getAbsolutePath();
    }

    public static String generateIdForTribe() {
        return UUID.randomUUID().toString();
    }
}
