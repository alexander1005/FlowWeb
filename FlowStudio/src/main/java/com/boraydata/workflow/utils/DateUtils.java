package com.boraydata.workflow.utils;


import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String toDate(Long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return format.format(date);
    }
}
