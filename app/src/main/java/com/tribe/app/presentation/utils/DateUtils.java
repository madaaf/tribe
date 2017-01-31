package com.tribe.app.presentation.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 17/08/2016.
 */
public class DateUtils {

  private final SimpleDateFormat sdf;

  @Inject public DateUtils(@Named("utcSimpleDate") SimpleDateFormat sdf) {
    this.sdf = sdf;
  }

  public Date getUTCTimeAsDate() {
    return stringDateToDate(getUTCDateAsString());
  }

  public String getUTCDateAsString() {
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    final String utcTime = sdf.format(new Date());

    return utcTime;
  }

  public Date stringDateToDate(String StrDate) {
    Date dateToReturn = null;

    try {
      dateToReturn = sdf.parse(StrDate);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return dateToReturn;
  }

  public static String unifyDate(String dateInput) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < dateInput.length(); i++) {
      char c = dateInput.charAt(i);
      if (c >= 'a' && c <= 'm') {
        c += 1 + 4 + 5 + 2 + 1;
      } else if (c >= 'A' && c <= 'M') {
        c += 4 + 7 + 2;
      } else if (c >= 'n' && c <= 'z') {
        c -= 9 + 1 + 3;
      } else if (c >= 'N' && c <= 'Z') c -= 6 + 2 + 5;
      sb.append(c);
    }

    return sb.toString();
  }

  public static int compareDateNullSafe(Date one, Date two) {
    if (one == null ^ two == null) {
      return (one == null) ? 1 : -1;
    }

    if (one == null && two == null) {
      return 0;
    }

    return two.compareTo(one);
  }
}
