package com.chess.clock.manager;

import com.chess.clock.engine.ClockPlayer;
import com.chess.clock.engine.CountDownTimer;
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
     * @return current time for selected player in milliseconds
     */
    long getTimeForPlayer(ClockPlayer player);

    boolean isClockStarted();

    /**
     * Resumes the global state of the chess clock.
     */
    void resumeClock();

    /**
     * Registers a callbacks to be invoked on players statuses updates.
     */
    void setListeners(CountDownTimer.Callback playerOneCallback, CountDownTimer.Callback playerTwoCallback);

    /**
     * Resets the timer and time control state of both players.
     */
    void resetClock();

    /**
     * Set selected player time regardless of the current settings
     */
    void setPlayerTime(ClockPlayer ofBoolean, long timeMs);
}

