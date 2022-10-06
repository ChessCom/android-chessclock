package com.chess.clock.entities;

public final class ClockTime {
    public final int hours;
    public final int minutes;
    public final int seconds;
    public final long remainingTimeMs;

    public ClockTime(long timeMs) {

        // calibration
        int rest = (int) (timeMs % 1000);
        if (rest > 0 && timeMs > 0) {
            remainingTimeMs = timeMs + 1000;
        } else {
            remainingTimeMs = timeMs;
        }

        seconds = (int) (remainingTimeMs / 1000) % 60;
        minutes = (int) ((remainingTimeMs / (1000 * 60)) % 60);
        hours = (int) ((remainingTimeMs / (1000 * 60 * 60)));
    }
}
