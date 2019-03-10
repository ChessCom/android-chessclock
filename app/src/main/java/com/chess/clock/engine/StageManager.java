package com.chess.clock.engine;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Stage Manager of a Chess game.
 */
public class StageManager implements Parcelable, Cloneable, Stage.OnStageFinishListener {

    public static final Parcelable.Creator<StageManager> CREATOR = new Parcelable.Creator<StageManager>() {
        public StageManager createFromParcel(Parcel source) {
            return new StageManager(source);
        }

        public StageManager[] newArray(int size) {
            return new StageManager[size];
        }
    };
    private static final String TAG = StageManager.class.getName();
    /**
     * Listener used to dispatch stage updates.
     */
    StageManagerListener mStageManagerListener;
    /**
     * The stages of the game.
     */
    private ArrayList<Stage> mStages;
    /**
     * Current stage number of the game.
     */
    private int mCurrentStage;
    /**
     * Total number of moves played in the game.
     */
    private int mMoveCount;

    /**
     * @param stages The game stages.
     */
    public StageManager(Stage[] stages) {

        mStages = new ArrayList<Stage>();

        // Set this as listener of all stages.
        for (Stage stage : stages) {
            mStages.add(stage);
            stage.setStageListener(this);
        }

        // Reset all stages, and set up listener for each one.
        reset();
    }

    private StageManager(Parcel parcel) {
        this.readFromParcel(parcel);

        for (Stage stage : mStages) {
            stage.setStageListener(this);
        }

        // Reset all stages, and set up listener for each one.
        reset();
    }

    /**
     * Register a callback to be invoked when a stage has finished.
     *
     * @param listener The callback that will run
     */
    public void setStageManagerListener(StageManagerListener listener) {
        mStageManagerListener = listener;
    }

    /**
     * @return The total number of stages in the game.
     */
    public int getTotalStages() {
        return mStages.size();
    }

    /**
     * @return The current stage being played.
     */
    public Stage getCurrentStage() {
        return mStages.get(mCurrentStage);
    }

    /**
     * @return All stages
     */
    public Stage[] getStages() {
        return mStages.toArray(new Stage[mStages.size()]);
    }

    /**
     * Add new Stage object
     */
    public void addNewStage() {

        if (getTotalStages() < 3) {

            if (getTotalStages() == 1) {

                // Set first stage as type MOVES, with 1 move
                mStages.get(0).setStageType(Stage.StageType.MOVES);
                mStages.get(0).setMoves(20);

                Stage newStage = new Stage(1, 300000);
                newStage.setStageListener(this);
                mStages.add(newStage);

            } else if (getTotalStages() == 2) {

                // Set second stage as Type MOVES, with 1 move each.
                mStages.get(1).setStageType(Stage.StageType.MOVES);
                mStages.get(1).setMoves(20);

                Stage newStage = new Stage(2, 300000);
                newStage.setStageListener(this);
                mStages.add(newStage);
            }
        }
    }

    /**
     * Remove the stage from the list, update others stages type accordingly.
     *
     * @param removeStageIdx Stage index to be deleted.
     */
    public void removeStage(int removeStageIdx) {

        if (removeStageIdx != -1) {

            Stage changingStage;

            // Removing the middle and last one
            if (removeStageIdx > 0 && removeStageIdx == (getTotalStages() - 1)) {

                // Change the previous one
                changingStage = mStages.get(removeStageIdx - 1);
                changingStage.setMoves(0);
                changingStage.setStageType(Stage.StageType.GAME);
            }
            // Remove the middle one
            else if (removeStageIdx == 1) {

                // Change the next one
                changingStage = mStages.get(removeStageIdx + 1);
                changingStage.setId(removeStageIdx);
            }

            mStages.remove(removeStageIdx);
        }
    }

    /**
     * @return The current move count of the game.
     */
    public int getTotalMoveCount() {
        return mMoveCount;
    }

    /**
     * @param stageNumber A stage number of the game.
     * @return Time duration of the requested stage number or zero if stage number is invalid.
     */
    public long getStageDuration(int stageNumber) {
        if (stageNumber >= 0 && stageNumber < getTotalStages()) {
            return mStages.get(stageNumber).getDuration();
        }
        return 0;
    }

    /**
     * Check if StageManager object is equal to this one.
     *
     * @param sm StageManager object.
     * @return True if relevant contents are equal.
     */
    public boolean isEqual(StageManager sm) {
        if (this.getTotalStages() == sm.getTotalStages()) {
            for (int i = 0; i < getTotalStages(); i++) {
                if (!mStages.get(i).isEqual(sm.getStages()[i]))
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Performs a chess move in the current stage.
     */
    public void addMove() {

        try {
            // Add move to the current stage.
            mStages.get(mCurrentStage).addMove();

        } catch (Stage.GameStageException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        // Update total move count.
        mMoveCount += 1;

        // Notify move count has been updated.
        if (mStageManagerListener != null) {
            mStageManagerListener.onMoveCountUpdate(mMoveCount);
        }
    }

    /**
     * Reset current stages and total move count.
     */
    public void reset() {
        mCurrentStage = 0;

        // Reset all stages.
        for (Stage stage : mStages) {
            stage.reset();
        }

        mMoveCount = 0;
        if (mStageManagerListener != null) {
            mStageManagerListener.onMoveCountUpdate(mMoveCount);
        }
    }

    private void readFromParcel(Parcel parcel) {
        mCurrentStage = parcel.readInt();
        mMoveCount = parcel.readInt();
        mStages = new ArrayList<Stage>();
        parcel.readTypedList(mStages, Stage.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        Log.d(TAG, "writeToParcel: " + mCurrentStage + ", " + mMoveCount + ", " + mStages.size());
        parcel.writeInt(mCurrentStage);
        parcel.writeInt(mMoveCount);
        parcel.writeTypedList(mStages);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Stage finish callback.
     *
     * @param stageFinishedNumber The identifier of the stage finished.
     * @throws java.lang.IllegalStateException if stage finished is not the current stage.
     */
    @Override
    public void onStageFinished(int stageFinishedNumber) {

        if (mCurrentStage != stageFinishedNumber) {
            throw new IllegalStateException("Stage finished is not the current stage.");
        }

        mCurrentStage++;

        // Check if there is more stages
        if (mCurrentStage < getTotalStages()) {

            // Notify listener with new stage entering.
            if (mStageManagerListener != null) {
                mStageManagerListener.onNewStageUpdate(mStages.get(mCurrentStage));
            }
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        StageManager clone = (StageManager) super.clone();

        // Clone each Stage object in the list
        ArrayList<Stage> newList = new ArrayList<Stage>();
        for (Stage stage : mStages) {
            Stage clonedStage = (Stage) stage.clone();
            clonedStage.setStageListener(clone);
            newList.add(clonedStage);
        }
        clone.mStages = newList;
        clone.mStageManagerListener = null;

        return clone;
    }

    /**
     * Interface definition for a callback to be invoked when the Time Control stages gets updated.
     */
    public interface StageManagerListener {

        /**
         * Called when the first stage begins or new one enters.
         *
         * @param stage The new stage.
         */
        public void onNewStageUpdate(Stage stage);

        /**
         * Called when total move count is updated.
         */
        public void onMoveCountUpdate(int moveCount);
    }
}
