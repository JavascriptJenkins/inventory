package com.techvvs.inventory.metrc.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating METRC API timestamps.
 */
public class MetrcUtil {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    /**
     * Generates the current UTC timestamp in ISO-8601 format.
     *
     * @return current timestamp as a string
     */
    public static String getCurrentUtcTimestamp() {
        return ISO_FORMATTER.format(Instant.now());
    }

    /**
     * Generates the UTC timestamp for N days ago.
     *
     * @param daysAgo number of days in the past
     * @return timestamp for N days ago as a string
     */
    public static String getUtcTimestampDaysAgo(int daysAgo) {
        Instant instant = Instant.now().minusSeconds(daysAgo * 86400L);
        return ISO_FORMATTER.format(instant);
    }

    /**
     * Generates the UTC timestamp for N hours ago.
     *
     * @param hoursAgo number of hours in the past
     * @return timestamp for N hours ago as a string
     */
    public static String getUtcTimestampHoursAgo(int hoursAgo) {
        Instant instant = Instant.now().minusSeconds(hoursAgo * 3600L);
        return ISO_FORMATTER.format(instant);
    }

    /**
     * Example method to get start and end timestamps for the last 24 hours.
     */
    public static void printLast24HoursRange() {
        String start = getUtcTimestampHoursAgo(24);
        String end = getCurrentUtcTimestamp();
        System.out.println("LastModifiedStart: " + start);
        System.out.println("LastModifiedEnd: " + end);
    }
}
