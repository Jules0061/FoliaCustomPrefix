package com.foliaCustomPrefix.util;

import java.time.Duration;
import java.util.StringJoiner;

public final class TimeUtil {

    private TimeUtil() {
    }

    public static String formatDuration(long millis) {
        if (millis <= 0) {
            return "0 minutes";
        }
        Duration duration = Duration.ofMillis(millis);
        long days = duration.toDays();
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        if (days == 0 && hours == 0 && minutes == 0) {
            minutes = 1;
        }
        StringJoiner joiner = new StringJoiner(" ");
        if (days > 0) {
            joiner.add(days + (days == 1 ? " day" : " days"));
        }
        if (hours > 0) {
            joiner.add(hours + (hours == 1 ? " hour" : " hours"));
        }
        if (minutes > 0) {
            joiner.add(minutes + (minutes == 1 ? " minute" : " minutes"));
        }
        return joiner.toString();
    }
}
