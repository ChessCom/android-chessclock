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
    private static final boolean VERBOSE = true;

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

        CountDownTimer.FinishCallback mFinishListener = new CountDownTimer.FinishCallback() {
            @Override
            public void onClockFinish() {

                chessGameRunning = false;
                mPlayerOneTimer.stop();
                mPlayerTwoTimer.stop();

                if (VERBOSE) Log.i(TAG, "#" + this.hashCode() + " Game finished.");
            }
        };
        mPlayerOneTimer.setFinishListener(mFinishListener);
        mPlayerTwoTimer.setFinishListener(mFinishListener);
    }

    @Override
    public void setupClock(TimeControlWrapper timeControlWrapper) {
        Args.checkForNull(timeControlWrapper);

        // Finish running game
        if (chessGameRunning) {
            if (VERBOSE) Log.d(TAG, "Finishing current timers.");
            // Reset the clock to new time controls
            resetClock();
            chessGameRunning = false;
        }

        mPlayerOneTimer.setTimeControl(timeControlWrapper.getTimeControlPlayerOne());
        mPlayerTwoTimer.setTimeControl(timeControlWrapper.getTimeControlPlayerTwo());

        if (VERBOSE) Log.d(TAG, "#" + this.hashCode() + " Time Control set.");
    }


    @Override
    public void pressClock(ClockPlayer player) {
        switch (player) {
            case ONE:
                if (VERBOSE) Log.v(TAG, "#" + this.hashCode() + " (1) pressed the clock.");
                pressPlayerClock(mPlayerOneTimer, mPlayerTwoTimer);
                break;
            case TWO:
                if (VERBOSE) Log.v(TAG, "#" + this.hashCode() + " (2) pressed the clock.");
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

            if (VERBOSE) Log.v(TAG, "#" + this.hashCode() + " paused the clock timers.");
        }
    }

    @Override
    public void resumeClock() {
        mPlayerOneTimer.resume();
        mPlayerTwoTimer.resume();
        if (VERBOSE) Log.v(TAG, "#" + this.hashCode() + " resumed the clock timers.");
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
        if (!opponentTimer.isFinished() && !playerTimer.isFinished()) {

            // Game already running? stop player timer and start the opponent timer.
            if (chessGameRunning) {
                playerTimer.stop();
                opponentTimer.start();

                if (VERBOSE) {
                    Log.d(TAG, "Move number: " + playerTimer.getTotalMoveCount() + ", time left: " + ClockTime.raw(playerTimer.getTime()).toReadableFormat());
                }
            }
            // First move: do not stop player clock to avoid invalid initial time increment.
            else {
                opponentTimer.start();
                chessGameRunning = true;

                if (VERBOSE) Log.i(TAG, "#" + this.hashCode() + " Game started.");
            }
        } else {
            if (VERBOSE) Log.w(TAG, "Discarded clock press due to Time Controls"
                    + " not available or game finished already.");
        }
    }

    @Override
    public boolean isClockStarted() {
        return mPlayerOneTimer.isStarted() || mPlayerTwoTimer.isStarted();
    }

    @Override
    public void setListeners(CountDownTimer.Callback playerOneCallback, CountDownTimer.Callback playerTwoCallback) {
        if (playerOneCallback != null) {
            if (VERBOSE)
                Log.d(TAG, "#" + this.hashCode() + " (1) registered listener: #" + playerOneCallback.hashCode() + ".");
        }
        mPlayerOneTimer.setClockTimerListener(playerOneCallback);

        if (playerTwoCallback != null) {
            if (VERBOSE) Log.d(TAG, "#" + this.hashCode()
                    + " (2) registered listener: #" + playerTwoCallback.hashCode() + ".");
        }
        mPlayerTwoTimer.setClockTimerListener(playerTwoCallback);
    }
}
