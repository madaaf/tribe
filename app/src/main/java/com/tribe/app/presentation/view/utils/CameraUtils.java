package com.tribe.app.presentation.view.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

import java.util.List;

/**
 * Camera-related utility functions.
 */
public class CameraUtils {

    private static final String TAG = "TRIBE";

    /**
     * Attempts to find a preview size that matches the provided width and height (which
     * specify the dimensions of the encoded video).  If it fails to find a match it just
     * uses the default preview size for video.
     * <p>
     * TODO: should do a best-fit match, e.g.
     * https://github.com/commonsguy/cwac-camera/blob/master/camera/src/com/commonsware/cwac/camera/CameraUtils.java
     */
    public static void choosePreviewSize(Camera.Parameters parms, int width, int height) {
        final double ASPECT_TOLERANCE = 0.23;
        double targetRatio = (double) height / width;

        List<Camera.Size> sizes = parms.getSupportedPreviewSizes();

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        if (optimalSize != null) parms.setPreviewSize(optimalSize.width, optimalSize.height);
    }

    public static int chooseFixedPreviewFps(Camera.Parameters params, int desiredThousandFps) {
        List<int[]> supported = params.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            if ((entry[0] == entry[1]) && (entry[0] == desiredThousandFps)) {
                params.setPreviewFpsRange(entry[0], entry[1]);
                return entry[0];
            }
        }

        int[] tmp = new int[2];
        params.getPreviewFpsRange(tmp);
        int guess;
        if (tmp[0] == tmp[1]) {
            guess = tmp[0];
        } else {
            guess = tmp[1] / 2;
        }

        Log.d(TAG, "Couldn't find match for " + desiredThousandFps + ", using " + guess);
        return guess;
    }

    public static boolean isArcWelder() {
        return Build.BRAND.equalsIgnoreCase("chromium") &&
                Build.MANUFACTURER.equalsIgnoreCase("chromium");
    }

    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasCamera2(Context context) {
        if (context != null) return false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false;
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] idList = manager.getCameraIdList();
            boolean notNull = true;

            if (idList.length == 0) {
                notNull = false;
            } else {
                for (final String str : idList) {
                    if (str == null || str.trim().isEmpty()) {
                        notNull = false;
                        break;
                    }

                    final CameraCharacteristics characteristics = manager.getCameraCharacteristics(str);
                    final int supportLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        notNull = false;
                        break;
                    }
                }
            }

            return notNull;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}