package com.example.CafeteriaApp.Helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for common helper functions.
 */
public class Utils {

    private static final String DATE_FORMAT = "yyyy:MM:dd:HH:mm:ss";

    /**
     * Gets the current system date and time as a formatted string.
     * @return Formatted date string (e.g., "2023:10:27:14:30:05").
     */
    public static String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Gets a date string that is a certain number of days from now.
     * @param days Number of days to add.
     * @return Formatted date string.
     */
    public static String getDateStringWithOffset(int days) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return sdf.format(cal.getTime());
    }

    /**
     * Converts a formatted date string back into a long (milliseconds).
     * Useful for comparing dates.
     * @param dateString The date string in "yyyy:MM:dd:HH:mm:ss" format.
     * @return Time in milliseconds, or 0 if parsing fails.
     */
    public static long dateStringToLong(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
