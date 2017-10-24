package com.tribe.app.presentation.utils;

import android.content.Context;
import com.tribe.app.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.inject.Inject;
import javax.inject.Named;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Created by tiago on 17/08/2016.
 */
public class DateUtils {

  private final SimpleDateFormat sdf;
  private Context context;

  @Inject public DateUtils(@Named("utcSimpleDate") SimpleDateFormat sdf, Context context) {
    this.sdf = sdf;
    this.context = context;
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

  public boolean isBefore(Date date1, Date date2) {
    return date1.before(date2);
  }

  public boolean isBefore(String date1, String date2) {
    return stringDateToDate(date1).before(stringDateToDate(date2));
  }

  public int getDiffDate(String date1, String date2) {
    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
    DateTime d1 = parser.parseDateTime(date1);
    DateTime d2 = parser.parseDateTime(date2);
    return Minutes.minutesBetween(d1, d2).getMinutes();
  }

  public String getDayId(String stringDate) {
    DateTimeZone timeZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser().withZone(timeZone);
    DateTime dt = parser.parseDateTime(stringDate);

    String year = String.valueOf(dt.getYear());
    String month = String.valueOf(dt.getMonthOfYear());
    String day = String.valueOf(dt.getDayOfMonth());
    return year + "/" + month + "/" + day;
  }

  public String getHourAndMinuteInLocal(String stringDate) {
    DateTimeZone timeZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser().withZone(timeZone);
    DateTime dt = parser.parseDateTime(stringDate);

    int h = dt.getHourOfDay();

    String suffix = "";
    if (isEnglish()) {
      if (dt.getHourOfDay() < 12) {
        suffix = " AM";
      } else {
        suffix = " PM";
        if (dt.getHourOfDay() != 12) h = dt.getHourOfDay() - 12;
      }
    }

    String minute = (dt.getMinuteOfHour() < 10) ? "0" + dt.getMinuteOfHour()
        : String.valueOf(dt.getMinuteOfHour());

    return h + ":" + minute + suffix;
  }

  public String getFormattedDayId(String stringDate) {
    if (isToday(stringDate)) {
      return context.getString(R.string.date_today);
    } else if (isYesterday(stringDate)) {
      return context.getString(R.string.date_yesterday);
    } else {
      DateTimeZone timeZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
      DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser().withZone(timeZone);
      DateTime dt = parser.parseDateTime(stringDate);

      String year = String.valueOf(dt.getYear());
      String month = (dt.getMonthOfYear() < 10) ? "0" + dt.getMonthOfYear()
          : String.valueOf(dt.getMonthOfYear());
      String day =
          (dt.getDayOfMonth() < 10) ? "0" + dt.getDayOfMonth() : String.valueOf(dt.getDayOfMonth());

      if (!isEnglish()) {
        return day + "/" + month + "/" + year;
      } else {
        return year + "/" + month + "/" + day;
      }
    }
  }

  private boolean isEnglish() {
    return Locale.getDefault().getISO3Language().equals(Locale.ENGLISH.getISO3Language());
  }

  private boolean isToday(String stringDate) {
    DateTime dt = new DateTime();
    int year = dt.getYear();
    int month = dt.getMonthOfYear();
    int day = dt.getDayOfMonth();

    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
    DateTime dateTimeHere = parser.parseDateTime(stringDate);
    return (dateTimeHere.getYear() == year
        && dateTimeHere.getMonthOfYear() == month
        && dateTimeHere.getDayOfMonth() == day);
  }

  private boolean isYesterday(String stringDate) {
    DateTime dt = new DateTime();
    int year = dt.getYear();
    int month = dt.getMonthOfYear();
    int day = dt.getDayOfMonth() - 1;

    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
    DateTime dateTimeHere = parser.parseDateTime(stringDate);
    return (dateTimeHere.getYear() == year
        && dateTimeHere.getMonthOfYear() == month
        && dateTimeHere.getDayOfMonth() == day);
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
