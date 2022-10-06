package com.chess.clock.entities;

public final class ClockTime {
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
