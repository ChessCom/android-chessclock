package com.chess.clock.manager;

import android.util.Log;

import com.chess.clock.engine.ClockPlayer;
import com.chess.clock.engine.CountDownTimer;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.entities.ClockTime;
import com.chess.clock.util.Args;

public class ChessClockManagerImpl implements ChessClockManager {

    /**
     * Logs stuff
     */
    private static final String TAG = ChessClockManager.class.getName();

    /**
     * True if the game is on-going. Note: pause state still counts as game running.
     */
    private boolean chessGameRunning;

    /**
     * Count down timers for both players.
     */
    private final CountDownTimer mPlayerOneTimer;
    private final CountDownTimer mPlayerTwoTimer;


    public ChessClockManagerImpl() {
        chessGameRunning = false;

        long DEFAULT_COUNT_DOWN_INTERVAL_MS = 100;
        mPlayerOneTimer = new CountDownTimer(DEFAULT_COUNT_DOWN_INTERVAL_MS);
        mPlayerTwoTimer = new CountDownTimer(DEFAULT_COUNT_DOWN_INTERVAL_MS);

        CountDownTimer.FinishCallback mFinishListener = () -> {

            chessGameRunning = false;
            mPlayerOneTimer.stop();
            mPlayerTwoTimer.stop();

            log("Game finished.");
        };
        mPlayerOneTimer.setFinishListener(mFinishListener);
        mPlayerTwoTimer.setFinishListener(mFinishListener);
    }

    @Override
    public void setupClock(TimeControlWrapper timeControlWrapper) {
        Args.checkForNull(timeControlWrapper);

        // Finish running game
        if (chessGameRunning) {
            log("Finishing current timers.");
            // Reset the clock to new time controls
            resetClock();
            chessGameRunning = false;
        }

        mPlayerOneTimer.setTimeControl(timeControlWrapper.getTimeControlPlayerOne());
        mPlayerTwoTimer.setTimeControl(timeControlWrapper.getTimeControlPlayerTwo());

        log("Time Control set.");
    }


    @Override
    public void pressClock(ClockPlayer player) {
        switch (player) {
            case ONE:
                log("(1) pressed the clock.");
                pressPlayerClock(mPlayerOneTimer, mPlayerTwoTimer);
                break;
            case TWO:
                log("(2) pressed the clock.");
                pressPlayerClock(mPlayerTwoTimer, mPlayerOneTimer);
                break;
        }
    }

    @Override
    public void pauseClock() {
        // The pause instruction only affects the time control which has
        // the count down timer in the state CountDownTimer.TimerState.RUNNING.
        if (mPlayerOneTimer != null && mPlayerTwoTimer != null) {

            mPlayerOneTimer.pause();
            mPlayerTwoTimer.pause();

            log("Paused the clock timers.");
        }
    }

    @Override
    public void resumeClock() {
        mPlayerOneTimer.resume();
        mPlayerTwoTimer.resume();
        log("Resumed the clock timers.");
    }

    @Override
    public void resetClock() {
        mPlayerOneTimer.resetTimeControl();
        mPlayerTwoTimer.resetTimeControl();
        chessGameRunning = false;
    }

    @Override
    public long getTimeForPlayer(ClockPlayer player) {
        long time = 0L;
        switch (player) {
            case ONE:
                time = mPlayerOneTimer.getTime();
                break;
            case TWO:
                time = mPlayerTwoTimer.getTime();
                break;
        }
        return time;
    }

    @Override
    public void setPlayerTime(ClockPlayer player, long timeMs) {
        switch (player) {
            case ONE:
                mPlayerOneTimer.setTime(timeMs);
                break;
            case TWO:
                mPlayerTwoTimer.setTime(timeMs);
                break;
        }
    }


    /**
     * Notifies the Chess Clock Service that a player made a move.
     *
     * @param playerTimer   The player timer which made a move.
     * @param opponentTimer The opponent timer.
     */
    private void pressPlayerClock(CountDownTimer playerTimer, CountDownTimer opponentTimer) {

        // Ignore clock press if timers are not initiated or opponent timer already finished.
        if (opponentTimer.isNotFinished() && playerTimer.isNotFinished()) {

            // Game already running? stop player timer and start the opponent timer.
            if (chessGameRunning) {
                playerTimer.stop();
                opponentTimer.start();

                log("Move number: " + playerTimer.getTotalMoveCount() + ", time left: " + ClockTime.raw(playerTimer.getTime()).toReadableFormat());
            }
            // First move: do not stop player clock to avoid invalid initial time increment.
            else {
                opponentTimer.start();
                chessGameRunning = true;

                log("Game started.");
            }
        } else {
            log("Discarded clock press due to Time Controls not available or game finished already.");
        }
    }

    @Override
    public boolean isClockStarted() {
        return mPlayerOneTimer.isStarted() || mPlayerTwoTimer.isStarted();
    }

    @Override
    public void setListeners(CountDownTimer.Callback playerOneCallback, CountDownTimer.Callback playerTwoCallback) {
        if (playerOneCallback != null) {
            log("(1)registered listener: #" + playerOneCallback.hashCode());
        }
        mPlayerOneTimer.setClockTimerListener(playerOneCallback);

        if (playerTwoCallback != null) {
            log("(2) registered listener: #" + playerTwoCallback.hashCode());
        }
        mPlayerTwoTimer.setClockTimerListener(playerTwoCallback);
    }

    private void log(String message) {
        Log.d(TAG, message);
    }
}
