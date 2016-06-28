package com.tribe.app.presentation.view.utils;

import android.os.Environment;

import java.io.File;

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

        String idVideo = "audio_" + id + "_" + System.currentTimeMillis();

        File audioFile = new File(audioDir, idVideo + ".mp4");
        return audioFile.getAbsolutePath();
    }

    public static String generateFileForVideo(String id) {
        File videoDir = new File(pathOrigin + pathVideo);

        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        String idVideo = "audio_" + id + "_" + System.currentTimeMillis();

        File audioFile = new File(videoDir, idVideo + ".mp4");
        return audioFile.getAbsolutePath();
    }

    public static String generateFileEnd(String id) {
        File endDir = new File(pathOrigin + pathEnd);

        if (!endDir.exists()) {
            endDir.mkdirs();
        }

        String idFile = "tribe_" + id + "_" + System.currentTimeMillis();

        File endFile = new File(endDir, idFile + ".mp4");
        return endFile.getAbsolutePath();
    }
}
