package com.tribe.app.presentation.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.inject.Inject;
import javax.inject.Named;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

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

  public boolean isToday(String stringDate) {
    DateTime dt = new DateTime();  // current time
    int year = dt.getYear();
    int month = dt.getMonthOfYear();
    int day = dt.getDayOfMonth();

    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
    DateTime dateTimeHere = parser.parseDateTime(stringDate);
   /* return (dateTimeHere.getYear() == year
        && dateTimeHere.getMonthOfYear() == month
        && dateTimeHere.getDayOfMonth() == day);*/
   return true;
  }

  public String getHourAndMinuteInLocal(String stringDate) {
    DateTimeZone ok2 = DateTimeZone.forTimeZone(TimeZone.getDefault());
    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser().withZone(ok2);
    DateTime date = parser.parseDateTime(stringDate);
    return date.getHourOfDay() + ":" + date.getMinuteOfHour();
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
      return (one == null) ? -1 : 1;
    }

    if (one == null && two == null) {
      return 0;
    }

    return one.compareTo(two);
  }
}
