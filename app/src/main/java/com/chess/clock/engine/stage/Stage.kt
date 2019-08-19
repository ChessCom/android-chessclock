package com.chess.clock.engine.stage

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.VisibleForTesting

/**
 * A stage of a Time Control. One Time Control can have one or more stages. Every stage has a time
 * limit. Stages that are part of a multi-stage Time Control also have move count limit excluding
 * the last stage.
 */
class Stage : Parcelable, Cloneable {

    /**
     * Game Stage Type
     */
    private lateinit var mStageType: StageType
    /**
     * Game Stage State
     */
    private lateinit var mStageState: StageState
    /**
     * Registered Id used to identify the game stage after completion.
     *
     * @see .finishStage
     */
    private var mId: Int = 0
    /**
     * Stage duration in milliseconds
     */
    /**
     * @return The duration stage duration
     */
    /**
     * Set stage duration
     *
     * @param duration
     */
    var duration: Long = 0
    /**
     * Limited number of moves in the stage.
     */
    /**
     * @return The number of moves for this stage.
     */
    var totalMoves: Int = 0
        private set
    /**
     * Played moves in the stage.
     */
    /**
     * @return The current number of moves played in this stage.
     */
    var stageMoveCount: Int = 0
        private set
    /**
     * Listener used to dispatch stage finish event.
     */
    private var mOnStageEndListener: OnStageFinishListener? = null

    /**
     * @return The stage id.
     */
    /**
     * Set the id of the stage.
     *
     * @param id
     */
    var id: Int
        get() = mId
        set(id) {
            if (id in 0..2) {
                mId = id
            }
        }

    /**
     * @return StageType of this stage.
     */
    /**
     * Set the StageType of this stage.
     *
     * @param type StageType.
     */
    // Also reset total moves if type is StageTypeGame
    var stageType: StageType
        get() = mStageType
        set(type) {
            mStageType = type
            if (mStageType === StageTypeGame) {
                totalMoves = 0
            }
        }

    /**
     * @return Int array with {hour,minute,second}
     */
    val time: IntArray
        get() {
            val s = (duration / 1000).toInt() % 60
            val m = (duration / (1000 * 60) % 60).toInt()
            val h = (duration / (1000 * 60 * 60) % 24).toInt()

            return intArrayOf(h, m, s)
        }

    private val isStageFinished: Boolean
        get() = mStageState === StageEnded

    /**
     * @param id       Stage identifier.
     * @param duration Stage duration in milliseconds.
     * @param moves    Limited number of moves for the stage. If zero provided, Stage type will be GAME.
     * @throws java.lang.IllegalArgumentException if duration is not positive or moves is not positive.
     */
    constructor(id: Int, duration: Long, moves: Int) : this(id, duration) {
        this.totalMoves = moves
        this.mStageType = StageTypeMoves
        if (moves <= 0) {
            this.mStageType = StageTypeGame
        }
    }

    /**
     * @param id       Game stage identifier.
     * @param duration Stage duration in milliseconds.
     */
    constructor(id: Int, duration: Long) {
        this.mId = id
        this.duration = duration
        this.mStageType = StageTypeGame
        reset()
    }

    @VisibleForTesting
    constructor(parcel: Parcel) {
        this.readFromParcel(parcel)
    }

    /**
     * Set the number of moves for this stage.
     *
     * @param moves Number of moves.
     */
    fun setMoves(moves: Int) {
        totalMoves = moves
    }

    /**
     * Check if Stage object is equal to this one.
     *
     * @param stage Stage Object.
     * @return
     */
    fun isEqual(stage: Stage): Boolean {
        // ID
        if (mId != stage.id) {
            Log.i(TAG, "Ids not equal.")
            return false
        } else if (mStageType !== stage.stageType) {
            Log.i(TAG, "StageType not equal. " + mStageType.toString()
                    + " - " + stage.stageType.toString())
            return false
        } else if (duration != stage.duration) {
            Log.i(TAG, "Duration not equal. " + duration + " != " + stage.duration)
            return false
        } else if (totalMoves != stage.totalMoves) {
            Log.i(TAG, "Duration not equal.")
            return false
        } else if (mOnStageEndListener == null && stage.mOnStageEndListener != null) {
            Log.i(TAG, "listener:null != stage.listener:" + stage.mOnStageEndListener!!)
            return false
        } else if (mOnStageEndListener != null && stage.mOnStageEndListener == null) {
            Log.i(TAG, "listener:$mOnStageEndListener != stage.listener:null")
            return false
        } else {
            return true
        }// End listener
        // Moves
        // Duration
        // StageType
    }

    /**
     * Register a callback to be invoked when the stage has finished.
     *
     * @param listener The callback that will run
     */
    fun setStageListener(listener: OnStageFinishListener) {
        this.mOnStageEndListener = listener
    }

    /**
     * Performs a chess addMove in this game stage.
     *
     * @throws GameStageException
     */
    @Throws(GameStageException::class)
    fun addMove() {

        if (isStageFinished)
            throw GameStageException("Cannot perform addMove action after stage finished")

        // First addMove in the stage
        if (mStageState === StageIdle) {
            mStageState = StageBegan
            Log.d(TAG, "Stage $mId began.")
        }

        stageMoveCount++
        Log.d(TAG, "Move added to Stage $mId. Move count: $stageMoveCount")

        // Finish stage if last addMove was played.
        if (mStageType === StageTypeMoves && !hasRemainingMoves()) {
            finishStage()
        }
    }

    /**
     * Reset Stage state and number of played moves.
     */
    fun reset() {
        stageMoveCount = 0
        mStageState = StageIdle
    }

    /**
     * Get formated string ready to UI info display.
     *
     * @return String representing info content of Stage.
     */
    override fun toString(): String {

        val durationString = formatTime(duration)
        val moves = totalMoves
        return if (moves == 0) {
            "Game in $durationString"
        } else if (moves == 1) {
            "1 move in $durationString"
        } else {
            "$moves moves in $durationString"
        }
    }

    /**
     * @param time Player time in milliseconds.
     * @return Readable String format of time.
     */
    fun formatTime(time: Long): String {

        val s = (time / 1000).toInt() % 60
        val m = (time / (1000 * 60) % 60).toInt()
        val h = (time / (1000 * 60 * 60) % 24).toInt()

        return if (time >= 3600000) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    /**
     * Force finish stage state.
     */
    private fun finishStage() {
        Log.d(TAG, "Stage $mId finished. Reached $stageMoveCount move count.")

        // Notify stage finished
        if (mOnStageEndListener != null) {
            mOnStageEndListener!!.onStageFinished(mId)
        }

        mStageState = StageEnded
    }

    private fun hasRemainingMoves(): Boolean {
        return totalMoves - stageMoveCount > 0
    }

    private fun readFromParcel(parcel: Parcel) {
        duration = parcel.readLong()
        mId = parcel.readInt()
        totalMoves = parcel.readInt()
        stageMoveCount = parcel.readInt()
        mStageState = stateFromInt(parcel.readInt())
        mStageType = typeFromInt(parcel.readInt())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(duration)
        parcel.writeInt(mId)
        parcel.writeInt(totalMoves)
        parcel.writeInt(stageMoveCount)
        parcel.writeInt(mStageState.toInteger())
        parcel.writeInt(mStageType.toInteger())
    }

    override fun describeContents(): Int {
        return 0
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Stage {
        val clone = super.clone() as Stage
        clone.mStageState = mStageState
        clone.mStageType = mStageType
        clone.mOnStageEndListener = null
        return clone
    }

    /**
     * Interface definition for a callback to be invoked when the game stage has finished.
     */
    interface OnStageFinishListener {

        /**
         * Called when the stage has finished.
         *
         * @param stageFinishedNumber The identifier of the stage finished.
         */
        fun onStageFinished(stageFinishedNumber: Int)
    }

    /**
     * *********************************
     * Exceptions
     * *********************************
     */
    inner class GameStageException(message: String) : Exception(message)

    companion object CREATOR: Parcelable.Creator<Stage> {
        private val TAG = Stage::class.java.name

        override fun createFromParcel(source: Parcel): Stage {
            return Stage(source)
        }

        override fun newArray(size: Int): Array<Stage?> {
            return arrayOfNulls(size)
        }
    }
}