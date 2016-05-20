package com.tribe.app.data.network.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TribeApiUtils {

    public static String generateTribeHash(String publicKey, String privateKey) {
        String tribeHash = "";

        try {
            String timeStamp = getUnixTimeStamp();
            String tribeData = timeStamp + privateKey + publicKey;

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] hash = messageDigest.digest(tribeData.getBytes());

            StringBuilder stringBuilder = new StringBuilder(2 * hash.length);

            for (byte b : hash)
                stringBuilder.append(String.format("%02x", b & 0xff));

            tribeHash = stringBuilder.toString();

        } catch (NoSuchAlgorithmException e) {

            System.out.println("[DEBUG]" + " TribeApiUtils generateTribeHash - " +
                    "NoSuchAlgorithmException");
        }

        return tribeHash;
    }

    public static String getUnixTimeStamp() {
        return String.valueOf(System.currentTimeMillis() / 1000L);
    }
}
