package com.chess.clock.engine.stage

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.VisibleForTesting
import java.util.ArrayList

class StageManager : Parcelable, Cloneable, OnStageFinishListener {

    private var mStages: MutableList<Stage> = mutableListOf()
    private var mCurrentStage: Int = 0

    var stageManagerListener: StageManagerListener? = null
    var totalMoveCount: Int = 0
        private set
    val amountOfStages
        get() = mStages.size
    val stages
        get() = mStages.toTypedArray()


    constructor(stages: Array<Stage>) {
        for (stage in stages) {
            mStages.add(stage)
            stage.onStageFinishListener = this
        }
        reset()
    }

    @VisibleForTesting
    constructor(parcel: Parcel) {
        this.readFromParcel(parcel)

        for (stage in mStages) {
            stage.onStageFinishListener = this
        }
        reset()
    }

    fun addNewStage() {
        when (amountOfStages) {
            0 -> addFirstStage()
            1 -> addSecondStage()
            2 -> addThirdStage()
            else -> return
        }
    }

    private fun addFirstStage() {
        val newStage = Stage(0, 300000)
        newStage.onStageFinishListener = this
        mStages.add(newStage)
    }

    private fun addSecondStage() {
        // Set first stage to StageTypeMoves
        mStages[0].setStageType(StageTypeMoves)
        mStages[0].totalMoveCount = 20

        val newStage = Stage(1, 300000)
        newStage.onStageFinishListener = this
        mStages.add(newStage)
    }

    private fun addThirdStage() {
        // Set second stage as StageTypeMoves
        mStages[1].setStageType(StageTypeMoves)
        mStages[1].totalMoveCount = 20

        val newStage = Stage(2, 300000)
        newStage.onStageFinishListener = this
        mStages.add(newStage)
    }

    fun removeStage(removeStageIdx: Int) {
        if (removeStageIdx !in 0 until amountOfStages) return

        val changingStage: Stage

        // Removing the last stage in a multi-stage setup
        if (removeStageIdx > 0 && removeStageIdx == amountOfStages - 1) {
            // The previous stage will become the new last stage.
            // Therefore it is set to StageTypeGame
            changingStage = mStages[removeStageIdx - 1]
            changingStage.setStageType(StageTypeGame)
        }

        mStages.removeAt(removeStageIdx)
        // Correctly update stage IDs
        mStages.forEachIndexed { index, stage ->
            stage.id = index
        }
    }

    fun getCurrentStage(): Stage? {
        if (mCurrentStage in 0 until amountOfStages) {
            return mStages[mCurrentStage]
        }
        return null
    }

    fun addMove() {
        try {
            mStages[mCurrentStage].addMove()
        } catch (e: GameStageException) {
            e.printStackTrace()
        }

        totalMoveCount += 1
        stageManagerListener?.onTotalMoveCountChange(totalMoveCount)
    }

    fun reset() {
        mCurrentStage = 0
        for (stage in mStages) {
            stage.reset()
        }

        totalMoveCount = 0
        stageManagerListener?.onTotalMoveCountChange(totalMoveCount)
    }

    override fun onStageFinished(stageFinishedNumber: Int) {
        if (mCurrentStage != stageFinishedNumber) {
            throw IllegalStateException("Stage finished is not the current stage.")
        }
        mCurrentStage++
        if (mCurrentStage < amountOfStages) {
            stageManagerListener?.onNewStageStarted(mStages[mCurrentStage])
        }
    }

    fun isEqual(sm: StageManager?): Boolean {
        if (this.amountOfStages == sm?.amountOfStages) {
            for (i in 0 until amountOfStages) {
                if (!mStages[i].isEqual(sm.stages[i]))
                    return false
            }
            return true
        } else {
            return false
        }
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

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        val clone = super.clone() as StageManager

        // Clone each Stage object in the list
        val newList = ArrayList<Stage>()
        for (stage in mStages) {
            val clonedStage = stage.clone()
            clonedStage.onStageFinishListener = clone
            newList.add(clonedStage)
        }
        clone.mStages = newList
        clone.stageManagerListener = null

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
