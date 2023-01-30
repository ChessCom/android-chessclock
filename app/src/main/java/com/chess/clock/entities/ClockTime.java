package com.chess.clock.entities;

import android.annotation.SuppressLint;

public final class ClockTime {
    public static int HOUR_MILLIS = 3600000;
    public static String CLOCK_FORMAT_HOURS = "%d:%02d:%02d";
    public static String CLOCK_FORMAT_MINUTES = "%d:%02d";
    public final int hours;
    public final int minutes;
    public final int seconds;
    public final long remainingTimeMs;

    private ClockTime(long timeMs) {
        remainingTimeMs = timeMs;
        seconds = (int) (timeMs / 1000) % 60;
        minutes = (int) ((timeMs / (1000 * 60)) % 60);
        hours = (int) ((timeMs / (1000 * 60 * 60)));
    }

    public static boolean atLeastHourLeft(long timeMs) {
        return timeMs >= HOUR_MILLIS;
    }

    public static String calibratedReadableFormat(long timeMs) {
        long calibratedTime = calibrateTime(timeMs);
        if (atLeastHourLeft(calibratedTime)) {
            return String.format(
                    CLOCK_FORMAT_HOURS,
                    (int) ((calibratedTime / (1000 * 60 * 60))),
                    (int) ((calibratedTime / (1000 * 60)) % 60),
                    (int) (calibratedTime / 1000) % 60
            );
        } else {
            return String.format(
                    CLOCK_FORMAT_MINUTES,
                    (int) ((calibratedTime / (1000 * 60)) % 60),
                    (int) (calibratedTime / 1000) % 60
            );
        }
    }

    public static ClockTime calibrated(long timeMs) {
        // calibration
        long remainingTime = calibrateTime(timeMs);
        return new ClockTime(remainingTime);
    }

    private static long calibrateTime(long timeMs) {
        int rest = (int) (timeMs % 1000);
        if (rest > 0 && timeMs > 0) {
            return timeMs + 1000;
        }
        return timeMs;
    }

    public static ClockTime raw(long timeMs) {
        return new ClockTime(timeMs);
    }

    @SuppressLint("DefaultLocale")
    public String toReadableFormat() {
        if (atLeastOneHourLeft()) {
            return String.format(CLOCK_FORMAT_HOURS, hours, minutes, seconds);
        } else {
            return String.format(CLOCK_FORMAT_MINUTES, minutes, seconds);
        }
    }

    public int totalMinutes() {
        return hours * 60 + minutes;
    }

    public String toMinutesFormat() {
        return String.format(CLOCK_FORMAT_MINUTES, totalMinutes(), seconds);
    }

    public boolean atLeastOneHourLeft() {
        return atLeastHourLeft(remainingTimeMs);
    }
}
