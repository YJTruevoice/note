package com.note.utils;

import android.content.Context;
import android.provider.Settings;

import com.note.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by user on 2015/8/14.
 */
public class DateTimeUtil {

    private static final Object sLocaleLock = new Object();
    private static Locale sIs24HourLocale;
    private static boolean sIs24Hour;

    public static final DateFormat getTimeFormat(Context context){
        boolean b24 = is24HoursFormat(context);
        int res;

        if (b24) {
            res = R.string.twenty_four_hour_time_format;
        } else {
            res = R.string.twelve_hour_time_format;
        }

        return new SimpleDateFormat(context.getString(res));
    }

    public static final boolean is24HoursFormat(Context context){

        String value = Settings.System.getString(context.getContentResolver(),
                Settings.System.TIME_12_24);

        if (value == null) {
            Locale locale = context.getResources().getConfiguration().locale;

            synchronized (sLocaleLock) {
                if (sIs24HourLocale != null && sIs24HourLocale.equals(locale)) {
                    return sIs24Hour;
                }
            }

            java.text.DateFormat natural =
                    java.text.DateFormat.getTimeInstance(
                            java.text.DateFormat.LONG, locale);

            if (natural instanceof SimpleDateFormat) {
                SimpleDateFormat sdf = (SimpleDateFormat) natural;
                String pattern = sdf.toPattern();

                if (pattern.indexOf('H') >= 0) {
                    value = "24";
                } else {
                    value = "12";
                }
            } else {
                value = "12";
            }

            synchronized (sLocaleLock) {
                sIs24HourLocale = locale;
                sIs24Hour = !value.equals("12");
            }
        }
        boolean b24 =  !(value == null || value.equals("12"));
        return b24;
    }

    public static final DateFormat getDateFormat(Context context){


//        String value = Settings.System.getString(context.getContentResolver(),
//                Settings.System.DATE_FORMAT);

        return new SimpleDateFormat("yyyy-MM-dd");
    }

    private static DateFormat getDateFormatForSetting(Context context, String value) {
        String format = getDateFormatStringForSetting(context, value);

        return new SimpleDateFormat(format);
    }

    private static String getDateFormatStringForSetting(Context context, String value) {
        if (value != null) {
            int month = value.indexOf('M');
            int day = value.indexOf('d');
            int year = value.indexOf('y');

            if (month >= 0 && day >= 0 && year >= 0) {
                String template = context.getString(R.string.numeric_date_template);
                if (year < month && year < day) {
                    if (month < day) {
                        value = String.format(template, "yyyy", "MM", "dd");
                    } else {
                        value = String.format(template, "yyyy", "dd", "MM");
                    }
                } else if (month < day) {
                    if (day < year) {
                        value = String.format(template, "MM", "dd", "yyyy");
                    } else { // unlikely
                        value = String.format(template, "MM", "yyyy", "dd");
                    }
                } else { // day < month
                    if (month < year) {
                        value = String.format(template, "dd", "MM", "yyyy");
                    } else { // unlikely
                        value = String.format(template, "dd", "yyyy", "MM");
                    }
                }

                return value;
            }
        }

        value = context.getString(R.string.numeric_date_format);
        return value;
    }


}
