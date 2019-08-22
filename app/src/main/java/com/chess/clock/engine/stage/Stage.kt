package com.chess.clock.engine.stage

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.VisibleForTesting

/**
 * A stage of a Time Control. One Time Control can have one or more stages. Every stage has a time
 * limit. Stages that are part of a multi-stage Time Control also have move count limit excluding
 * the last stage.
 */
open class Stage : Parcelable, Cloneable {

    var id: Int = 0
        set(id) {
            if (id in 0..2) {
                field = id
            }
        }

    var durationInMilliseconds: Long = 0
    var totalMoveCount: Int = 0
    var playedMoveCount: Int = 0
        private set
    lateinit var stageType: StageType
        private set
    var onStageFinishListener: OnStageFinishListener? = null

    private lateinit var stageState: StageState


    constructor(id: Int, durationInMilliseconds: Long, totalMoveCount: Int) : this(id, durationInMilliseconds) {
        this.totalMoveCount = totalMoveCount
        if (totalMoveCount > 0) {
            this.stageType = StageTypeMoves
        }
    }

    constructor(id: Int, durationInMilliseconds: Long) {
        this.id = id
        this.durationInMilliseconds = durationInMilliseconds
        this.stageType = StageTypeGame
        reset()
    }

    @VisibleForTesting
    constructor(parcel: Parcel) {
        this.readFromParcel(parcel)
    }


    fun setStageType(stageType: StageType) {
        this.stageType = stageType
        if (stageType === StageTypeGame) {
            totalMoveCount = 0
        }
    }

    /**
     * @return Int array with {hour,minute,second}
     */
    val time: IntArray
        get() {
            val s = (durationInMilliseconds / 1000).toInt() % 60
            val m = (durationInMilliseconds / (1000 * 60) % 60).toInt()
            val h = (durationInMilliseconds / (1000 * 60 * 60) % 24).toInt()

            return intArrayOf(h, m, s)
        }

    fun isEqual(stage: Stage): Boolean {
        return id == stage.id
            && stageType == stage.stageType
            && durationInMilliseconds == stage.durationInMilliseconds
            && totalMoveCount == stage.totalMoveCount
            && (onStageFinishListener == null && stage.onStageFinishListener == null
                || onStageFinishListener != null && stage.onStageFinishListener != null
                )
    }

    @Throws(GameStageException::class)
    fun addMove() {
        if (isStageFinished())
            throw GameStageException("Cannot perform addMove action after stage finished")

        if (stageState === StageIdle) {
            // Adding the first ever move, so set the state to began.
            stageState = StageBegan
        }

        playedMoveCount++

        if (stageType === StageTypeMoves && !hasRemainingMoves()) {
            finishStage()
        }
    }

    fun reset() {
        playedMoveCount = 0
        stageState = StageIdle
    }

    override fun toString(): String {
        val durationString = formatTime(durationInMilliseconds)
        return when (val moves = totalMoveCount) {
            0 -> "Game in $durationString"
            1 -> "1 move in $durationString"
            else -> "$moves moves in $durationString"
        }
    }

    private fun formatTime(time: Long): String {

        val s = (time / 1000).toInt() % 60
        val m = (time / (1000 * 60) % 60).toInt()
        val h = (time / (1000 * 60 * 60) % 24).toInt()

        return if (time >= 3600000) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    private fun finishStage() {
        onStageFinishListener?.onStageFinished(id)
        stageState = StageEnded
    }

    private fun hasRemainingMoves() = totalMoveCount - playedMoveCount > 0

    private fun isStageFinished() = stageState === StageEnded

    private fun readFromParcel(parcel: Parcel) {
        durationInMilliseconds = parcel.readLong()
        id = parcel.readInt()
        totalMoveCount = parcel.readInt()
        playedMoveCount = parcel.readInt()
        stageState = stateFromInt(parcel.readInt())
        stageType = typeFromInt(parcel.readInt())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(durationInMilliseconds)
        parcel.writeInt(id)
        parcel.writeInt(totalMoveCount)
        parcel.writeInt(playedMoveCount)
        parcel.writeInt(stageState.toInteger())
        parcel.writeInt(stageType.toInteger())
    }

    override fun describeContents(): Int {
        return 0
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Stage {
        val clone = super.clone() as Stage
        clone.stageState = stageState
        clone.stageType = stageType
        clone.onStageFinishListener = null
        return clone
    }

    companion object CREATOR: Parcelable.Creator<Stage> {
        override fun createFromParcel(source: Parcel): Stage {
            return Stage(source)
        }

        override fun newArray(size: Int): Array<Stage?> {
            return arrayOfNulls(size)
        }
    }
}