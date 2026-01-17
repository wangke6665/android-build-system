package com.shenma.tvlauncher.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GetTimeStamp {
    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        return new SimpleDateFormat(format).format(new Date(Long.valueOf(new StringBuilder(String.valueOf(seconds)).append("000").toString()).longValue()));
    }

    public static String date2TimeStamp(String date_str, String format) {
        try {
            return String.valueOf(new SimpleDateFormat(format).parse(date_str).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String timeStamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    public static void main(String[] args) {
        String timeStamp = timeStamp();
        //System.out.println("timeStamp=" + timeStamp);
        //System.out.println(System.currentTimeMillis());
        String date = timeStamp2Date(timeStamp, "yyyy-MM-dd HH:mm:ss");
        //System.out.println("date=" + date);
        //System.out.println(date2TimeStamp(date, "yyyy-MM-dd HH:mm:ss"));
    }
}
