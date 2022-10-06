package com.chess.clock.engine;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.chess.clock.entities.ClockTime;

/**
 * A stage of a Time Control. One Time Control can have one or more stages. Every stage has a time
 * limit. Stages that are part of a multi-stage Time Control also have move count limit excluding
 * the last stage.
 */
public class Stage implements Parcelable, Cloneable {

    private final static String TAG = Stage.class.getName();
    public static final int STAGE_ONE_ID = 0;
    public static final int STAGE_TWO_ID = 1;
    public static final int STAGE_THREE_ID = 2;
    public static final int MAX_ALLOWED_STAGES_COUNT = 3;
    public static final int DEFAULT_STAGE_MOVES = 20;
    public static final int GAME_STAGE_MOVES = 0;
    public static final int STAGE_DURATION_5_MIN = 300000;

    public static final Parcelable.Creator<Stage> CREATOR = new Parcelable.Creator<Stage>() {
        public Stage createFromParcel(Parcel source) {
            return new Stage(source);
        }

        public Stage[] newArray(int size) {
            return new Stage[size];
        }
    };

    /**
     * Game Stage Type
     */
    private StageType mStageType;
    /**
     * Game Stage State
     */
    private StageState mStageState;
    /**
     * Registered Id used to identify the game stage after completion.
     *
     * @see #finishStage()
     */
    private int mId;
    /**
     * Stage duration in milliseconds
     */
    private long mDuration;
    /**
     * Limited number of moves in the stage.
     */
    private int mMoves;
    /**
     * Played moves in the stage.
     */
    private int mStageMoveCount;
    /**
     * Listener used to dispatch stage finish event.
     */
    private OnStageFinishListener mOnStageEndListener;

    /**
     * @param id       Stage identifier.
     * @param duration Stage duration in milliseconds.
     * @param moves    Limited number of moves for the stage. If zero provided, Stage type will be GAME.
     * @throws java.lang.IllegalArgumentException if duration is not positive or moves is not positive.
     */
    public Stage(int id, long duration, int moves) {
        this(id, duration);
        this.mMoves = moves;
        this.mStageType = StageType.MOVES;
    }

    /**
     * @param id       Game stage identifier.
     * @param duration Stage duration in milliseconds.
     */
    public Stage(int id, long duration) {
        this.mId = id;
        this.mDuration = duration;
        this.mStageType = StageType.GAME;
        reset();
    }

    private Stage(Parcel parcel) {
        this.readFromParcel(parcel);
    }

    /**
     * @return The stage id.
     */
    public int getId() {
        return mId;
    }

    /**
     * Set the id of the stage.
     *
     * @param id
     */
    public void setId(int id) {
        if (id >= 0 && id < MAX_ALLOWED_STAGES_COUNT) {
            mId = id;
        }
    }

    /**
     * @return The duration stage duration
     */
    public long getDuration() {
        return mDuration;
    }

    /**
     * Set stage duration
     *
     * @param duration
     */
    public void setDuration(long duration) {
        mDuration = duration;
    }

    /**
     * @return The number of moves for this stage.
     */
    public int getTotalMoves() {
        return mMoves;
    }

    /**
     * @return The current number of moves played in this stage.
     */
    public int getStageMoveCount() {
        return mStageMoveCount;
    }

    /**
     * Set the number of moves for this stage.
     *
     * @param moves Number of moves.
     */
    public void setMoves(int moves) {
        mMoves = moves;
    }

    /**
     * Check if Stage object is equal to this one.
     *
     * @param stage Stage Object.
     * @return
     */
    public boolean isEqual(Stage stage) {
        // ID
        if (mId != stage.getId()) {
            Log.i(TAG, "Ids not equal.");
            return false;
        }
        // StageType
        else if (mStageType.getValue() != stage.getStageType().getValue()) {
            Log.i(TAG, "StageType not equal. " + mStageType.getValue()
                    + " - " + stage.getStageType().getValue());
            return false;
        }
        // Duration
        else if (mDuration != stage.getDuration()) {
            Log.i(TAG, "Duration not equal. " + mDuration + " != " + stage.getDuration());
            return false;
        }
        // Moves
        else if (mMoves != stage.getTotalMoves()) {
            Log.i(TAG, "Duration not equal.");
            return false;
        }
        // End listener
        else if (mOnStageEndListener == null && stage.mOnStageEndListener != null) {
            Log.i(TAG, "listener:null != stage.listener:" + stage.mOnStageEndListener);
            return false;
        } else if (mOnStageEndListener != null && stage.mOnStageEndListener == null) {
            Log.i(TAG, "listener:" + mOnStageEndListener + " != stage.listener:null");
            return false;
        } else {
            return true;
        }
    }

    /**
     * @return StageType of this stage.
     */
    public StageType getStageType() {
        return mStageType;
    }

    /**
     * Set the StageType of this stage.
     *
     * @param type StageType.
     */
    public void setStageType(StageType type) {
        mStageType = type;

        // Also reset total moves if type is GAME
        if (mStageType == StageType.GAME) {
            mMoves = 0;
        }
    }

    /**
     * Register a callback to be invoked when the stage has finished.
     *
     * @param listener The callback that will run
     */
    public void setStageListener(OnStageFinishListener listener) {
        this.mOnStageEndListener = listener;
    }

    /**
     * Performs a chess addMove in this game stage.
     *
     * @throws GameStageException
     */
    public void addMove() throws GameStageException {

        if (isStageFinished())
            throw new GameStageException("Cannot perform addMove action after stage finished");

        // First addMove in the stage
        if (mStageState == StageState.IDLE) {
            mStageState = StageState.BEGAN;
            Log.d(TAG, "Stage " + mId + " began.");
        }

        mStageMoveCount++;
        Log.d(TAG, "Move added to Stage " + mId + ". Move count: " + mStageMoveCount);

        // Finish stage if last addMove was played.
        if (mStageType == StageType.MOVES && !hasRemainingMoves()) {
            finishStage();
        }
    }

    /**
     * Reset Stage state and number of played moves.
     */
    public void reset() {
        mStageMoveCount = 0;
        mStageState = StageState.IDLE;
    }

    /**
     * @return Readable String format of time.
     */
    @SuppressLint("DefaultLocale")
    public String durationTimeFormatted() {
        long time = getDuration();

        ClockTime clockTime = ClockTime.raw(time);

        if (time >= 3600000) {
            return String.format("%02d:%02d:%02d", clockTime.hours, clockTime.minutes, clockTime.seconds);
        } else {
            return String.format("%02d:%02d", clockTime.minutes, clockTime.seconds);
        }
    }

    /**
     * @return Int array with {hour,minute,second}
     */
    public ClockTime getTime() {
        return ClockTime.raw(mDuration);
    }

    /**
     * Force finish stage state.
     */
    private void finishStage() {
        Log.d(TAG, "Stage " + mId + " finished. Reached " + mStageMoveCount + " move count.");

        // Notify stage finished
        if (mOnStageEndListener != null) {
            mOnStageEndListener.onStageFinished(mId);
        }

        mStageState = StageState.ENDED;
    }

    private boolean isStageFinished() {
        return mStageState == StageState.ENDED;
    }

    private boolean hasRemainingMoves() {
        return (mMoves - mStageMoveCount > 0);
    }

    private void readFromParcel(Parcel parcel) {
        mDuration = parcel.readLong();
        mId = parcel.readInt();
        mMoves = parcel.readInt();
        mStageMoveCount = parcel.readInt();
        mStageState = StageState.fromInteger(parcel.readInt());
        mStageType = StageType.fromInteger(parcel.readInt());
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mDuration);
        parcel.writeInt(mId);
        parcel.writeInt(mMoves);
        parcel.writeInt(mStageMoveCount);
        parcel.writeInt(mStageState.getValue());
        parcel.writeInt(mStageType.getValue());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Stage clone = (Stage) super.clone();
        clone.mStageState = StageState.fromInteger(mStageState.getValue());
        clone.mStageType = StageType.fromInteger(mStageType.getValue());
        clone.mOnStageEndListener = null;
        return clone;
    }

    /**
     * There are two stage types in a Time Control. The StageType.GAME is used for one-stage
     * Time Controls or the last stage of a multi-stage Time Control. The StageType.MOVES is used
     * for the remaining ones (multi-stage Time Controls besides the last stage).
     */
    public enum StageType {

        /**
         * Used for one-stage only type of game or the last stage of a multiple stage time control.
         */
        GAME(0),

        /**
         * Used for all stages of a the multi-stage time control, besides the last one.
         */
        MOVES(1);

        private final int value;

        StageType(int value) {
            this.value = value;
        }

        public static StageType fromInteger(int type) {
            switch (type) {
                case 0:
                    return GAME;
                case 1:
                    return MOVES;
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Game Stage State
     */
    private enum StageState {

        /**
         * The stage has not begun
         */
        IDLE(0),

        /**
         * The stage is on-going.
         */
        BEGAN(1),

        /**
         * The stage has finished.
         */
        ENDED(2);

        private final int value;

        StageState(int value) {
            this.value = value;
        }

        public static StageState fromInteger(int type) {
            switch (type) {
                case 0:
                    return IDLE;
                case 1:
                    return BEGAN;
                case 2:
                    return ENDED;
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Interface definition for a callback to be invoked when the game stage has finished.
     */
    public interface OnStageFinishListener {

        /**
         * Called when the stage has finished.
         *
         * @param stageFinishedNumber The identifier of the stage finished.
         */
        void onStageFinished(int stageFinishedNumber);
    }

    /**
     * *********************************
     * Exceptions
     * *********************************
     */
    public static class GameStageException extends Exception {
        public GameStageException(String message) {
            super(message);
        }
    }
}