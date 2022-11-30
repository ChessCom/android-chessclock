package com.chess.clock.engine;

public enum ClockPlayer {
    ONE, TWO;

    public boolean isFirstPlayer() {
        return this == ONE;
    }

    public static ClockPlayer ofBoolean(boolean playerOne) {
        return playerOne ? ONE : TWO;
    }
}
