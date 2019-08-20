package com.chess.clock.engine.stage

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.VisibleForTesting
import java.util.ArrayList

/**
 * Stage Manager of a Chess game.
 */
class StageManager : Parcelable, Cloneable, Stage.OnStageFinishListener {
    /**
     * Listener used to dispatch stage updates.
     */
    private var mStageManagerListener: StageManagerListener? = null
    /**
     * The stages of the game.
     */
    private var mStages: MutableList<Stage> = mutableListOf()
    /**
     * Current stage number of the game.
     */
    private var mCurrentStage: Int = 0
    /**
     * Total number of moves played in the game.
     */
    /**
     * @return The current move count of the game.
     */
    var totalMoveCount: Int = 0
        private set

    /**
     * @return The total number of stages in the game.
     */
    val totalStages: Int
        get() = mStages.size

    /**
     * @return The current stage being played.
     */
    val currentStage: Stage
        get() = mStages[mCurrentStage]

    /**
     * @return All stages
     */
    val stages: Array<Stage>
        get() = mStages.toTypedArray()

    /**
     * @param stages The game stages.
     */
    constructor(stages: Array<Stage>) {
        // Set this as listener of all stages.
        for (stage in stages) {
            mStages.add(stage)
            stage.setStageListener(this)
        }

        // Reset all stages, and set up listener for each one.
        reset()
    }

    @VisibleForTesting
    constructor(parcel: Parcel) {
        this.readFromParcel(parcel)

        for (stage in mStages) {
            stage.setStageListener(this)
        }

        // Reset all stages, and set up listener for each one.
        reset()
    }

    /**
     * Register a callback to be invoked when a stage has finished.
     *
     * @param listener The callback that will run
     */
    fun setStageManagerListener(listener: StageManagerListener) {
        mStageManagerListener = listener
    }

    /**
     * Add new Stage object
     */
    fun addNewStage() {
        when (totalStages) {
            0 -> addFirstStage()
            1 -> addSecondStage()
            2 -> addThirdStage()
            else -> return
        }
    }

    private fun addFirstStage() {
        val newStage = Stage(0, 300000)
        newStage.setStageListener(this)
        mStages.add(newStage)
    }

    private fun addSecondStage() {
        // Set first stage as type MOVES, with 1 move
        mStages[0].stageType = StageTypeMoves
        mStages[0].setMoves(20)

        val newStage = Stage(1, 300000)
        newStage.setStageListener(this)
        mStages.add(newStage)
    }

    private fun addThirdStage() {
        // Set second stage as Type MOVES, with 1 move each.
        mStages[1].stageType = StageTypeMoves
        mStages[1].setMoves(20)

        val newStage = Stage(2, 300000)
        newStage.setStageListener(this)
        mStages.add(newStage)
    }


    /**
     * Remove the stage from the list, update others stages type accordingly.
     *
     * @param removeStageIdx Stage index to be deleted.
     */
    fun removeStage(removeStageIdx: Int) {

        if (removeStageIdx !in 0 until totalStages) return

        val changingStage: Stage

        // Removing the last stage in a multi-stage setup
        if (removeStageIdx > 0 && removeStageIdx == totalStages - 1) {

            // The previous stage will become the new last stage.
            // Therefore it is set to StageTypeGame
            changingStage = mStages[removeStageIdx - 1]
            changingStage.stageType = StageTypeGame
        }

        mStages.removeAt(removeStageIdx)
        // Correctly update stage IDs
        mStages.forEachIndexed { index, stage ->
            stage.id = index
        }
    }

    /**
     * @param stageNumber A stage number of the game.
     * @return Time duration of the requested stage number or zero if stage number is invalid.
     */
    fun getStageDuration(stageNumber: Int): Long {
        return if (stageNumber in 0 until totalStages) {
            mStages[stageNumber].duration
        } else 0
    }

    /**
     * Check if StageManager object is equal to this one.
     *
     * @param sm StageManager object.
     * @return True if relevant contents are equal.
     */
    fun isEqual(sm: StageManager): Boolean {
        if (this.totalStages == sm.totalStages) {
            for (i in 0 until totalStages) {
                if (!mStages[i].isEqual(sm.stages[i]))
                    return false
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Performs a chess move in the current stage.
     */
    fun addMove() {

        try {
            // Add move to the current stage.
            mStages[mCurrentStage].addMove()

        } catch (e: Stage.GameStageException) {
            Log.e(TAG, e.message)
            e.printStackTrace()
        }

        // Update total move count.
        totalMoveCount += 1

        // Notify move count has been updated.
        mStageManagerListener?.onMoveCountUpdate(totalMoveCount)
    }

    /**
     * Reset current stages and total move count.
     */
    fun reset() {
        mCurrentStage = 0

        // Reset all stages.
        for (stage in mStages) {
            stage.reset()
        }

        totalMoveCount = 0
        mStageManagerListener?.onMoveCountUpdate(totalMoveCount)
    }

    private fun readFromParcel(parcel: Parcel) {
        mCurrentStage = parcel.readInt()
        totalMoveCount = parcel.readInt()
        mStages = ArrayList()
        parcel.readTypedList(mStages, Stage)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Log.d(TAG, "writeToParcel: " + mCurrentStage + ", " + totalMoveCount + ", " + mStages.size)
        parcel.writeInt(mCurrentStage)
        parcel.writeInt(totalMoveCount)
        parcel.writeTypedList(mStages)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Stage finish callback.
     *
     * @param stageFinishedNumber The identifier of the stage finished.
     * @throws java.lang.IllegalStateException if stage finished is not the current stage.
     */
    override fun onStageFinished(stageFinishedNumber: Int) {

        if (mCurrentStage != stageFinishedNumber) {
            throw IllegalStateException("Stage finished is not the current stage.")
        }

        mCurrentStage++

        // Check if there is more stages
        if (mCurrentStage < totalStages) {

            // Notify listener with new stage entering.
            mStageManagerListener?.onNewStageUpdate(mStages[mCurrentStage])
        }
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        val clone = super.clone() as StageManager

        // Clone each Stage object in the list
        val newList = ArrayList<Stage>()
        for (stage in mStages) {
            val clonedStage = stage.clone()
            clonedStage.setStageListener(clone)
            newList.add(clonedStage)
        }
        clone.mStages = newList
        clone.mStageManagerListener = null

        return clone
    }

    companion object CREATOR: Parcelable.Creator<StageManager> {
        override fun createFromParcel(source: Parcel): StageManager {
            return StageManager(source)
        }

        override fun newArray(size: Int): Array<StageManager?> {
            return arrayOfNulls(size)
        }

        private val TAG = StageManager::class.java.name
    }
}
