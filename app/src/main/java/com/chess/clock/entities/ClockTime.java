package com.chess.clock.entities;

import android.annotation.SuppressLint;

import com.chess.clock.R;

public final class ClockTime {
    public final int hours;
    public final int minutes;
    public final int seconds;
    public final long remainingTimeMs;

    public static int HOUR_MILLIS = 3600000;
    public static String CLOCK_FORMAT_HOURS = "%d:%02d:%02d";
    public static String CLOCK_FORMAT_MINUTES = "%d:%02d";

    private ClockTime(long timeMs) {
        remainingTimeMs = timeMs;
        seconds = (int) (timeMs / 1000) % 60;
        minutes = (int) ((timeMs / (1000 * 60)) % 60);
        hours = (int) ((timeMs / (1000 * 60 * 60)));
    }

    @SuppressLint("DefaultLocale")
    public String toReadableFormat() {
        if (atLeaseOneHourLeft()) {
            return String.format(CLOCK_FORMAT_HOURS, hours, minutes, seconds);
        } else {
            return String.format(CLOCK_FORMAT_MINUTES, minutes, seconds);
        }
    }

    public boolean atLeaseOneHourLeft() {
        return remainingTimeMs >= HOUR_MILLIS;
    }

    public static ClockTime calibrated(long timeMs) {
        // calibration
        long remainingTime = timeMs;
        int rest = (int) (timeMs % 1000);
        if (rest > 0 && timeMs > 0) {
            remainingTime = timeMs + 1000;
        }
        return new ClockTime(remainingTime);
    }

    public static ClockTime raw(long timeMs) {
        return new ClockTime(timeMs);
    }
}
