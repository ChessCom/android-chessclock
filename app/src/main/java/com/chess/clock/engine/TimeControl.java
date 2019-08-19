package com.chess.clock.engine;

import android.os.Parcel;
import android.os.Parcelable;

import com.chess.clock.engine.time.TimeIncrement;
import com.chess.clock.util.Args;

/**
 * <p>
 * This class represents the time control of a single player in a chess game. At the most basic
 * level, time controls can simply be expressed as the time each player has in order to make all of
 * his moves during a game. Commonly, these time controls are called “Game in X”. For instance, you
 * might have a time control named G/60 – shorthand for game in 60 minutes.
 * </p>
 * <p/>
 * <h3>Multiple Time Control Stages</h3>
 * <p/>
 * <p>
 * Time controls may be broken up into multiple stages. This ensures that while a game might
 * be long, players are forced to reach a certain point of the game earlier. One multiple time
 * control format seen frequently in major tournaments is 40/120, G/60. This time control requires
 * players to make at least 40 moves in their first two hours of playing time, then gives each
 * player another hour with which to finish the remainder of the game.
 * </p>
 * <p/>
 * <h3>Increment and Time Delay</h3>
 * <p/>
 * <p>
 * With an increment, players have time added to their clock after every move that’s completed,
 * thus ensuring that they’ll always have at least the increment time to make a move. Time delay
 * works slightly differently. Instead of adding time to the clock, a time delay creates a period
 * after your opponent moves during which your clock will not run.
 * </p>
 */
public class TimeControl implements Parcelable, Cloneable, StageManager.StageManagerListener {

    public static final Parcelable.Creator<TimeControl> CREATOR = new Parcelable.Creator<TimeControl>() {
        public TimeControl createFromParcel(Parcel source) {
            return new TimeControl(source);
        }

        public TimeControl[] newArray(int size) {
            return new TimeControl[size];
        }
    };
    private static final String TAG = TimeControl.class.getName();
    /**
     * TimeControl identifier.
     */
    private String mName;

    /**
     * Stage Manager associated with Time Control.
     */
    private StageManager mStageManager;

    /**
     * Time increment associated with Time Control.
     */
    private TimeIncrement mTimeIncrement;

    /**
     * Listener used to dispatch time control update events.
     *
     * @see TimeControlListener
     */
    private TimeControlListener mTimeControlListener;

    /**
     * Simple constructor to use when creating a TimeControl.
     *
     * @param name   Name identifier.
     * @param stages stages of the TimeControl.
     * @param time   TimeIncrement object associated with the TimeControl.
     * @throws java.lang.NullPointerException if StageManager or TimeIncrement are not provided.
     */
    public TimeControl(String name, Stage[] stages, TimeIncrement time) {
        Args.checkForNull(stages);
        Args.checkForNull(time);

        mName = name;
        mTimeIncrement = time;
        mStageManager = new StageManager(stages);

        // Set up listener for Stage Manager.
        mStageManager.setStageManagerListener(this);
    }

    private TimeControl(Parcel parcel) {
        this.readFromParcel(parcel);
    }

    /**
     * Check if TimeControl object is equal to this one.
     *
     * @param tc TimeControl Object.
     * @return True if relevant contents are equal.
     */
    public boolean isEqual(TimeControl tc) {

        boolean equalNames = (mName == null && tc.getName() == null) ||
                mName.equals(tc.getName());

        return (equalNames
                && mStageManager.isEqual(tc.getStageManager())
                && mTimeIncrement.isEqual(tc.getTimeIncrement()));
    }

    /**
     * Register a callback to be invoked when time control updates.
     *
     * @param listener The callback that will run
     */
    public void setTimeControlListener(TimeControlListener listener) {
        Args.checkForNull(listener);
        mTimeControlListener = listener;
    }

    /**
     * The name identifier of the time control.
     *
     * @return Time control name.
     */
    public String getName() {
        return mName;
    }

    /**
     * Set the name identifier of the time control.
     *
     * @param name Time control name.
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Gets the time increment associated with this time control.
     *
     * @return TimeIncrement associated with this TimeControl.
     */
    public TimeIncrement getTimeIncrement() {
        return mTimeIncrement;
    }

    /**
     * Gets the Stage manager associated with this time control.
     *
     * @return Stage manager associated with this time control.
     */
    public StageManager getStageManager() {
        return mStageManager;
    }

    private void readFromParcel(Parcel parcel) {
        mName = parcel.readString();
        mStageManager = parcel.readParcelable(StageManager.class.getClassLoader());
        mStageManager.setStageManagerListener(this);
        mTimeIncrement = parcel.readParcelable(TimeIncrement.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mName);
        parcel.writeParcelable(mStageManager, flags);
        parcel.writeParcelable(mTimeIncrement, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * /**
     * {@inheritDoc}
     */
    @Override
    public void onNewStageUpdate(Stage stage) {
        Args.checkForNull(stage);

        if (mTimeControlListener != null) {
            mTimeControlListener.onStageUpdate(stage);
        }
    }

    /**
     * /**
     * {@inheritDoc}
     */
    @Override
    public void onMoveCountUpdate(int moveCount) {
        if (mTimeControlListener != null) {
            mTimeControlListener.onMoveCountUpdate(moveCount);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        TimeControl clone = (TimeControl) super.clone();

        // Clone StageManager object and set this clone as his listener.
        clone.mStageManager = (StageManager) mStageManager.clone();
        clone.mStageManager.setStageManagerListener(clone);

        // Clone TimeIncrement object
        clone.mTimeIncrement = (TimeIncrement) mTimeIncrement.clone();

        clone.mTimeControlListener = null;
        return clone;
    }

    /**
     * Interface definition for a callback to be invoked when the player clock gets updated.
     */
    public interface TimeControlListener {

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
    }
}
