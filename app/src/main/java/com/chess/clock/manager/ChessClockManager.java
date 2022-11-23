package com.chess.clock.manager;

import com.chess.clock.engine.ClockPlayer;
import com.chess.clock.engine.TimeControlWrapper;


public interface ChessClockManager {

    /**
     * Add new time controls to Chess Clock and resetTimeControl timer if needed.
     *
     * @param timeControlWrapper selected time control to set
     */
    void setupClock(TimeControlWrapper timeControlWrapper);

    /**
     * Notifies that ClockPlayer pressed the clock.
     */
    void pressClock(ClockPlayer player);

    /**
     * Pauses the global state of the chess clock.
     */
    void pauseClock();

    /**
     *
     * @param player selected clock player
     *
     * @return current time for selected player in milliseconds
     */
    long getTimeForPlayer(ClockPlayer player);
}

