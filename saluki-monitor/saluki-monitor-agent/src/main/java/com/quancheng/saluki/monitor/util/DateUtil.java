package com.quancheng.saluki.monitor.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private DateUtil(){

    }

    public static Date parse(String string) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(string);
        } catch (ParseException e) {
            throw new java.lang.IllegalArgumentException(e.getMessage(), e);
        }
    }
}
