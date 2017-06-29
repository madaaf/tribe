package com.tribe.tribelivesdk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import timber.log.Timber;

/**
 * Created by tiago on 21/06/2017.
 */
// https://stackoverflow.com/questions/2467579/how-to-get-cpu-usage-statistics-on-android

public class CpuUtils {

  public static float readUsage() {
    try {
      RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
      String load = reader.readLine();

      String[] toks = load.split(" +");  // Split on one or more spaces

      long idle1 = Long.parseLong(toks[4]);
      long cpu1 = Long.parseLong(toks[2]) +
          Long.parseLong(toks[3]) +
          Long.parseLong(toks[5]) +
          Long.parseLong(toks[6]) +
          Long.parseLong(toks[7]) +
          Long.parseLong(toks[8]);

      try {
        Thread.sleep(360);
      } catch (Exception e) {
      }

      reader.seek(0);
      load = reader.readLine();
      reader.close();

      toks = load.split(" +");

      long idle2 = Long.parseLong(toks[4]);
      long cpu2 = Long.parseLong(toks[2]) +
          Long.parseLong(toks[3]) +
          Long.parseLong(toks[5]) +
          Long.parseLong(toks[6]) +
          Long.parseLong(toks[7]) +
          Long.parseLong(toks[8]);

      return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return 0;
  }

  /**
   * @return integer Array with 4 elements: user, system, idle and other cpu
   * usage in percentage.
   */
  public static int[] getCpuUsageStatistic() {
    String tempString = executeTop();

    tempString = tempString.replaceAll(",", "");
    tempString = tempString.replaceAll("User", "");
    tempString = tempString.replaceAll("System", "");
    tempString = tempString.replaceAll("IOW", "");
    tempString = tempString.replaceAll("IRQ", "");
    tempString = tempString.replaceAll("%", "");
    for (int i = 0; i < 10; i++) {
      tempString = tempString.replaceAll("  ", " ");
    }
    tempString = tempString.trim();
    String[] myString = tempString.split(" ");
    int[] cpuUsageAsInt = new int[myString.length];
    for (int i = 0; i < myString.length; i++) {
      myString[i] = myString[i].trim();
      try {
        cpuUsageAsInt[i] = Integer.parseInt(myString[i]);
      } catch (NumberFormatException ex) {
        cpuUsageAsInt[i] = 0;
      }
    }
    return cpuUsageAsInt;
  }

  public static String executeTop() {
    java.lang.Process p = null;
    BufferedReader in = null;
    String returnString = null;
    try {
      p = Runtime.getRuntime().exec("top -n 1");
      in = new BufferedReader(new InputStreamReader(p.getInputStream()));
      while (returnString == null || returnString.contentEquals("")) {
        returnString = in.readLine();
      }
    } catch (IOException e) {
      Timber.d("Error in getting first line of top");
      e.printStackTrace();
    } finally {
      try {
        in.close();
        p.destroy();
      } catch (IOException e) {
        Timber.d("Error in closing and destroying top process");
        e.printStackTrace();
      }
    }
    return returnString;
  }
}
