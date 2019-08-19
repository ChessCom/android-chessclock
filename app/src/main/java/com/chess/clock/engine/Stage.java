package com.chess.clock.engine;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.chess.clock.engine.stage.StageBegan;
import com.chess.clock.engine.stage.StageEnded;
import com.chess.clock.engine.stage.StageIdle;
import com.chess.clock.engine.stage.StageState;
import com.chess.clock.engine.stage.StageStateKt;
import com.chess.clock.engine.stage.StageType;
import com.chess.clock.engine.stage.StageTypeGame;
import com.chess.clock.engine.stage.StageTypeKt;
import com.chess.clock.engine.stage.StageTypeMoves;

import androidx.annotation.VisibleForTesting;

/**
 * A stage of a Time Control. One Time Control can have one or more stages. Every stage has a time
 * limit. Stages that are part of a multi-stage Time Control also have move count limit excluding
 * the last stage.
 */
public class Stage implements Parcelable, Cloneable {

    private final static String TAG = Stage.class.getName();

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
        this.mStageType = StageTypeMoves.INSTANCE;
        if (moves <= 0) {
            this.mStageType = StageTypeGame.INSTANCE;
        }
    }

    /**
     * @param id       Game stage identifier.
     * @param duration Stage duration in milliseconds.
     */
    public Stage(int id, long duration) {
        this.mId = id;
        this.mDuration = duration;
        this.mStageType = StageTypeGame.INSTANCE;
        reset();
    }

    @VisibleForTesting
    public Stage(Parcel parcel) {
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
        if (id >= 0 && id < 3) {
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
        else if (mStageType != stage.getStageType()) {
            Log.i(TAG, "StageType not equal. " + mStageType.toString()
                    + " - " + stage.getStageType().toString());
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

        // Also reset total moves if type is StageTypeGame
        if (mStageType == StageTypeGame.INSTANCE) {
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
        if (mStageState == StageIdle.INSTANCE) {
            mStageState = StageBegan.INSTANCE;
            Log.d(TAG, "Stage " + mId + " began.");
        }

        mStageMoveCount++;
        Log.d(TAG, "Move added to Stage " + mId + ". Move count: " + mStageMoveCount);

        // Finish stage if last addMove was played.
        if (mStageType == StageTypeMoves.INSTANCE && !hasRemainingMoves()) {
            finishStage();
        }
    }

    /**
     * Reset Stage state and number of played moves.
     */
    public void reset() {
        mStageMoveCount = 0;
        mStageState = StageIdle.INSTANCE;
    }

    /**
     * Get formated string ready to UI info display.
     *
     * @return String representing info content of Stage.
     */
    public String toString() {

        String durationString = formatTime(getDuration());
        int moves = getTotalMoves();
        if (moves == 0) {
            return "Game in " + durationString;
        } else if (moves == 1) {
            return "1 move in " + durationString;
        } else {
            return moves + " moves in " + durationString;
        }
    }

    /**
     * @param time Player time in milliseconds.
     * @return Readable String format of time.
     */
    public String formatTime(long time) {

        int s = (int) (time / 1000) % 60;
        int m = (int) ((time / (1000 * 60)) % 60);
        int h = (int) ((time / (1000 * 60 * 60)) % 24);

        if (time >= 3600000) {
            return String.format("%02d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }

    /**
     * @return Int array with {hour,minute,second}
     */
    public int[] getTime() {
        int s = (int) (mDuration / 1000) % 60;
        int m = (int) ((mDuration / (1000 * 60)) % 60);
        int h = (int) ((mDuration / (1000 * 60 * 60)) % 24);

        return new int[]{h, m, s};
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

        mStageState = StageEnded.INSTANCE;
    }

    private boolean isStageFinished() {
        return mStageState == StageEnded.INSTANCE;
    }

    private boolean hasRemainingMoves() {
        return (mMoves - mStageMoveCount > 0);
    }

    private void readFromParcel(Parcel parcel) {
        mDuration = parcel.readLong();
        mId = parcel.readInt();
        mMoves = parcel.readInt();
        mStageMoveCount = parcel.readInt();
        mStageState = StageStateKt.stateFromInt(parcel.readInt());
        mStageType = StageTypeKt.typeFromInt(parcel.readInt());
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mDuration);
        parcel.writeInt(mId);
        parcel.writeInt(mMoves);
        parcel.writeInt(mStageMoveCount);
        parcel.writeInt(StageStateKt.toInteger(mStageState));
        parcel.writeInt(StageTypeKt.toInteger(mStageType));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public Stage clone() throws CloneNotSupportedException {
        Stage clone = (Stage) super.clone();
        clone.mStageState = mStageState;
        clone.mStageType = mStageType;
        clone.mOnStageEndListener = null;
        return clone;
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
    public class GameStageException extends Exception {
        public GameStageException(String message) {
            super(message);
        }
    }
}