package com.chess.clock.engine;

import android.os.Handler;
import android.util.Log;

import com.chess.clock.engine.time.TimeIncrement;
import com.chess.clock.engine.time.TimeIncrementBronstein;
import com.chess.clock.engine.time.TimeIncrementDelay;
import com.chess.clock.engine.time.TimeIncrementFischer;
import com.chess.clock.engine.time.TimeIncrementType;
import com.chess.clock.util.Args;

/**
 * Chess count down timer with time increment support. Provides support for multi-stage time
 * controls and their stage state updates. Uses Thread Handler as a stopwatch facility.
 */
public class CountDownTimer implements TimeControl.TimeControlListener {

    private static final String TAG = CountDownTimer.class.getName();
    /**
     * Count down timer built through Handler API.
     */
    final Handler handler = new Handler();
    /**
     * The interval along the way for each clock tick in milliseconds.
     */
    private final long mCountDownInterval;
    /**
     * Timer state
     */
    private TimerState mTimerState;
    /**
     * Listener used to dispatch timer updates.
     */
    private Callback mCallback;
    /**
     * Listener used to dispatch timer finish updates.
     */
    private FinishCallback mFinishCallback;
    /**
     * Time Control associated with this count down timer.
     */
    private TimeControl mTimeControl;
    /**
     * Current time on the clock in milliseconds.
     */
    private long mTime;
    private long mLastTickTime;
    final Runnable downCounter = new Runnable() {

        public void run() {

			/*
            The Handler's Runnable deliver time might suffer from a slight delay.
			That delay accumulated can generate large time drift. For that reason,
			the current tick time is saved and the elapsed time used as the decrement value.
			The last tick time is set to zero for each stop, pause and reset action.
			*/
            if (mLastTickTime > 0) {
                long elapsedTime = System.currentTimeMillis() - mLastTickTime;
                mTime -= elapsedTime;
            } else {
                // When last tick time is not available, update using fixed count down interval.
                mTime -= mCountDownInterval;
            }

            // Store current tick time
            mLastTickTime = System.currentTimeMillis();

            // Finish timer if zero or negative already.
            if (mTime <= 0) {

                // Notify UI of remaining time.
                if (mCallback != null) {
                    mCallback.onClockTimeUpdate(mTime);
                }

                finish();
            } else {

                // Notify UI of remaining time.
                if (mCallback != null) {
                    mCallback.onClockTimeUpdate(mTime);
                }

                // Continue count down timer cycle.
                handler.postDelayed(this, mCountDownInterval);
            }
        }
    };
    /**
     * Last forceStop time position in milliseconds.
     */
    private long mStopTime;
    /**
     * Last time start delayed was requested.
     */
    private long lastStartDelayTime;
    /**
     * Remaining delay time when resuming clock that was delaying its start.
     */
    private long mPendingDelayOnResume;

    /**
     * Creates a new and Count Down Timer.
     */
    public CountDownTimer(long countDownInterval) {
        Args.checkForPositive(countDownInterval);
        mCountDownInterval = countDownInterval;

        // Set state as Stopped on creation.
        resetTimeControl();
    }

    /**
     * Stops timer and sets new Time Control.
     *
     * @param timeControl New
     */
    public void setTimeControl(TimeControl timeControl) {
        Args.checkForNull(timeControl);

        forceStop();
        mTimeControl = timeControl;
        mTimeControl.setTimeControlListener(this);

        resetTimeControl();
    }

    /**
     * Register a callback to be invoked when the clock ticks or/and finishes.
     *
     * @param listener The callback that will run.
     */
    public void setClockTimerListener(Callback listener) {
        mCallback = listener;

        // After registering the listener, notify him the current time control state.
        notifyStatus();
    }

    /**
     * Register a callback to be invoked when the clock finishes.
     *
     * @param listener The callback that will run.
     */
    public void setFinishListener(FinishCallback listener) {
        mFinishCallback = listener;
        // Notify it it was already finished.
        if (mTimerState == TimerState.FINISHED) {
            mFinishCallback.onClockFinish();
        }
    }

    /**
     * Notify registered listener of current clock time, stage and move count. The following
     * methods will be called back: Callback.onClockTimeUpdate(long),
     * Callback.onMoveCountUpdate(int), Callback.onStageUpdate(Stage).
     */
    public void notifyStatus() {

        if (mCallback != null && mTimeControl != null) {
            mCallback.onClockTimeUpdate(getTime());
            mCallback.onMoveCountUpdate(mTimeControl.getStageManager().getTotalMoveCount());
            mCallback.onTotalStageNumber(mTimeControl.getStageManager().getTotalStages());
            mCallback.onStageUpdate(mTimeControl.getStageManager().getCurrentStage());
        }
    }

    /**
     * @return Current time in the clock.
     */
    public long getTime() {
        return mTime;
    }

    /**
     * Set count down time value.
     *
     * @param time Time position to be set in milliseconds.
     */
    private void setTime(long time) {
        // Avoid setting negative times.
        if (time >= 0) {
            mTime = time;
        } else {
            mTime = 0;
        }
    }

    public int getTotalMoveCount() {
        if (mTimeControl != null) {
            return mTimeControl.getStageManager().getTotalMoveCount();
        } else {
            Log.w(TAG, "Dropped total move count request due to time control not set."
                    + "returning 0 by default.");
            return 0;
        }
    }

    /**
     * @return The title of Time Control loaded
     */
    public String getTimeControlTitle() {
        return mTimeControl.getName();
    }

    /**
     * @return True if timer is on TimerState.FINISHED state and was not yet reset.
     */
    public boolean isFinished() {
        return mTimerState == TimerState.FINISHED;
    }

    /**
     * Start the clock.
     */
    public void start() {
        if (mTimeControl != null) {
            Log.d(TAG, "#" + this.hashCode() + " started.");

            // Only starts the clock if currently stopped (ignores state Finished)
            if (mTimerState == CountDownTimer.TimerState.STOPPED) {
                if (mTimeControl.getTimeIncrement().getType() instanceof TimeIncrementDelay) {
                    forceStartDelayed(mTimeControl.getTimeIncrement().getValue());
                } else {
                    forceStart();
                }
            }
        } else {
            Log.w(TAG, "Dropped start request due to time control not set."
                    + "returning null by default.");
        }
    }

    /**
     * Stop the clock and registers a move.
     */
    public void stop() {
        if (mTimeControl != null) {
            Log.d(TAG, "#" + this.hashCode() + " stopped at " + formatTime(getTime()) + ".");

            // Only stops the clock if currently running or paused
            if (mTimerState == TimerState.RUNNING || mTimerState == TimerState.PAUSED) {
                TimeIncrementType incrementType = mTimeControl.getTimeIncrement().getType();
                if (incrementType instanceof TimeIncrementFischer) {
                    forceStopAndIncrementFull(mTimeControl.getTimeIncrement().getValue());
                } else if (incrementType instanceof TimeIncrementBronstein) {
                    forceStopAndIncrementAtMost(mTimeControl.getTimeIncrement().getValue());
                } else {
                    forceStop();
                }

                // Increment total move count
                mTimeControl.getStageManager().addMove();
            }
        } else {
            Log.w(TAG, "Dropped stop request due to time control not set."
                    + "returning null by default.");
        }
    }

    /**
     * Timer pauses. Can only be restarted if {@link #resume()} is called.
     */
    public void pause() {
        // Do not pause if timer was already stopped.
        if (mTimerState == TimerState.RUNNING) {
            handler.removeCallbacks(downCounter);

            // Change timer state to STOPPED
            mTimerState = TimerState.PAUSED;

            // Reset last tick time
            mLastTickTime = 0;

            if (mTimeControl.getTimeIncrement().getType() == TimeIncrementDelay.INSTANCE) {

                // Pausing in the middle of a delay?
                long elapsedTime = System.currentTimeMillis() - lastStartDelayTime;
                if (elapsedTime < mTimeControl.getTimeIncrement().getValue()) {

                    mPendingDelayOnResume = mTimeControl.getTimeIncrement().getValue() - elapsedTime;
                    Log.i(TAG, "Pausing in the middle of delay, next resume will have delay: " + mPendingDelayOnResume);

                } else {
                    // reset
                    mPendingDelayOnResume = 0;
                }
            }

        } else {
            Log.d(TAG, "Pause request ignored. Timer is not running.");
        }
    }

    /**
     * Timer restarts if it was previously paused by {@link #pause()}.
     */
    public void resume() {
        if (mTimerState == TimerState.PAUSED) {

            Log.v(TAG, "Pending delay on resume: " + mPendingDelayOnResume);

            if (mPendingDelayOnResume > 0) {
                forceStartDelayed(mPendingDelayOnResume);
            } else {
                forceStart();
            }
        } else {
            Log.d(TAG, "Resume request ignored. Timer was not paused.");
        }
    }

    /**
     * Finish count down timer. This will also set time value to zero.
     */
    public void finish() {
        handler.removeCallbacks(downCounter);
        mTimerState = TimerState.FINISHED;
        setTime(0);

        // Notify clock finished (UI)
        if (mCallback != null) {
            mCallback.onClockFinish();
        }

        // Notify clock finished (Background component)
        if (mFinishCallback != null) {
            mFinishCallback.onClockFinish();
        }
    }

    /**
     * Resets the count down timer with initial time control values.
     */
    public void resetTimeControl() {

        if (mTimeControl != null) {
            // Reset stage manager
            mTimeControl.getStageManager().reset();

            // Set first stage duration on count down timer.
            long firstStageDuration = mTimeControl.getStageManager().getStageDuration(0 /* stage number */);
            forceReset(firstStageDuration);
        } else {
            Log.w(TAG, "Dropped reset Time Control command due to time control not set");
        }
    }

    /**
     * (Re)Starts the timer from last position.
     */
    private void forceStart() {
        if (mTimerState == TimerState.STOPPED || mTimerState == TimerState.PAUSED) {
            handler.postDelayed(downCounter, mCountDownInterval);
            mTimerState = TimerState.RUNNING;
        }
    }

    /**
     * Timer starts after delay period.
     * This is useful for {@linkplain TimeIncrement.Type#DELAY} increment type.
     *
     * @param delay Time delay to forceStart timer in milliseconds.
     */
    private void forceStartDelayed(long delay) {
        if (mTimerState == TimerState.STOPPED || mTimerState == TimerState.PAUSED) {
            handler.postDelayed(downCounter, delay);
            mTimerState = TimerState.RUNNING;

            lastStartDelayTime = System.currentTimeMillis();
        }
    }

    /**
     * Stops the timer and add full increment value.
     *
     * @param increment Time bonus to add.
     */
    private void forceStopAndIncrementFull(long increment) {
        // Only forceStop if currently running or paused.
        if (mTimerState == TimerState.RUNNING || mTimerState == TimerState.PAUSED) {
            addIncrement(increment);
            forceStop();
        }
    }

    /**
     * Stops the timer and add used portion of increment, at most the full increment value.
     * This is useful for the Bronstein increment type.
     *
     * @param increment Time bonus to add.
     */
    private void forceStopAndIncrementAtMost(long increment) {
        // Only stops if currently running or paused.
        if (mTimerState == TimerState.RUNNING || mTimerState == TimerState.PAUSED) {
            long elapsedTime = mStopTime - mTime;
            Log.d(TAG, "#" + this.hashCode() + " time since last stop: " + formatTime(elapsedTime));
            if (elapsedTime < increment) {
                addIncrement(elapsedTime);
            } else {
                addIncrement(increment);
            }
            forceStop();
        }
    }

    /**
     * Timer stops, the forceStop time is registered, time position is not cleaned.
     */
    private void forceStop() {
        // Do not increment if timer was already stopped.
        if (mTimerState == TimerState.RUNNING || mTimerState == TimerState.PAUSED) {
            handler.removeCallbacks(downCounter);

            // Force finish on zero if it went negative.
            if (mTime <= 0) {
                mTime = 0;
            }

            // Save last forceStop position
            mStopTime = mTime;

            // Change timer state to STOPPED
            mTimerState = TimerState.STOPPED;

            // Reset last tick time
            mLastTickTime = 0;

            // Notify time increment update.
            if (mCallback != null) {
                mCallback.onClockTimeUpdate(mTime);
            }
        }
    }

    /**
     * Stops count down timer, and sets the time value.
     *
     * @param millisUntilFinished Time position to be resetTimeControl in milliseconds.
     * @throws java.lang.IllegalArgumentException if millisUntilFinished is zero or negative.
     */
    private void forceReset(long millisUntilFinished) {
        Args.checkForZeroOrNegative(millisUntilFinished);

        // Stop on-going clock ticks.
        handler.removeCallbacks(downCounter);

        setTime(millisUntilFinished);
        mStopTime = mTime;
        mTimerState = TimerState.STOPPED;

        // Reset last tick time
        mLastTickTime = 0;

        // Reset pending time on resume
        mPendingDelayOnResume = 0;

        // Notify update.
        notifyStatus();
    }

    /**
     * @param time Player time in milliseconds.
     * @return Readable String format of time.
     */
    private String formatTime(long time) {
        int s = (int) (time / 1000) % 60;
        int m = (int) ((time / (1000 * 60)) % 60);
        int h = (int) ((time / (1000 * 60 * 60)) % 24);
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /**
     * Add full time increment on top of current time.
     * This is useful for Fischer increment type.
     *
     * @param increment Time increment in milliseconds.
     */
    private void addIncrement(long increment) {
        Log.v(TAG, "#" + this.hashCode() + " adding increment of " + formatTime(increment));
        mTime += increment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStageUpdate(Stage stage) {
        Args.checkForNull(stage);

        // Add time bonus of the new stage
        long stageDuration = stage.getDuration();
        addIncrement(stageDuration);

        // Update stop time is again required since time has been updated.
        mStopTime = getTime();

        Log.i(TAG, "#" + this.hashCode() + " stage " + stage.getId() + " added "
                + formatTime(stageDuration) + ", time left: " + formatTime(getTime()));

        if (mCallback != null) {
            mCallback.onStageUpdate(stage);
            mCallback.onClockTimeUpdate(mTime);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMoveCountUpdate(int moveCount) {
        if (mCallback != null) {
            mCallback.onMoveCountUpdate(moveCount);
        }
    }

    /**
     * The state mechanism is used to avoid adding time increment if forceStop button is
     * pressed while already stopped or finished.
     */
    public enum TimerState {

        /**
         * The count down timer is active.
         */
        RUNNING,

        /**
         * The count down timer is paused. Meaning it will respond to {@link #resume()}.
         */
        PAUSED,

        /**
         * The count down timer is inactive.
         */
        STOPPED,

        /**
         * The count down timer is finished.
         */
        FINISHED
    }

    /****************************************
     * Callbacks Definition.
     ***************************************/

    /**
     * Interface definition for a callback to be invoked when the UI elements should be updated.
     *
     * @see #onClockTimeUpdate(long)
     * @see #onMoveCountUpdate(int)
     * @see #onClockFinish()
     * @see #onStageUpdate(Stage)
     */
    public interface Callback {

        /**
         * Called when the timer updates.
         *
         * @param millisUntilFinished Time until finish in milliseconds.
         */
        public void onClockTimeUpdate(long millisUntilFinished);

        /**
         * Called when the time finishes.
         */
        public void onClockFinish();

        /**
         * Called when new game stage begins.
         *
         * @param stage The current game stage.
         */
        public void onStageUpdate(Stage stage);

        /**
         * Called when the move count is updated.
         */
        public void onMoveCountUpdate(int moves);

        /**
         * Called only on registering listener.
         *
         * @param stagesNumber Total number of stages.
         */
        public void onTotalStageNumber(int stagesNumber);
    }

    /**
     * Interface definition for a callback to be invoked when timer finishes.
     * This is a subset of {@link Callback} intended to be used by a background component.
     */
    public interface FinishCallback {
        /**
         * Called when the time finishes.
         */
        public void onClockFinish();
    }
}