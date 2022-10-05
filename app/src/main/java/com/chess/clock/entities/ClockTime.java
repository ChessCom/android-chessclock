package com.chess.clock.entities;

public final class ClockTime {
    public final int hours;
    public final int minutes;
    public final int seconds;

    public ClockTime(long durationMs) {
        seconds = (int) (durationMs / 1000) % 60;
        minutes = (int) ((durationMs / (1000 * 60)) % 60);
        hours = (int) ((durationMs / (1000 * 60 * 60)));
    }
}
