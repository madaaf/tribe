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

    @Inject
    public DateUtils(@Named("utcSimpleDate") SimpleDateFormat sdf) {
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
}
