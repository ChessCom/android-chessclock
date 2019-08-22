package com.chess.clock.engine.time

import android.os.Parcel
import android.os.Parcelable
import com.chess.clock.engine.stage.Stage
import com.chess.clock.engine.stage.StageManager
import com.chess.clock.engine.stage.StageManagerListener

/**
 *
 *
 * This class represents the time control of a single player in a chess game. At the most basic
 * level, time controls can simply be expressed as the time each player has in order to make all of
 * his moves during a game. Commonly, these time controls are called “Game in X”. For instance, you
 * might have a time control named G/60 – shorthand for game in 60 minutes.
 *
 *
 *
 * <h3>Multiple Time Control Stages</h3>
 *
 *
 *
 *
 * Time controls may be broken up into multiple stages. This ensures that while a game might
 * be long, players are forced to reach a certain point of the game earlier. One multiple time
 * control format seen frequently in major tournaments is 40/120, G/60. This time control requires
 * players to make at least 40 moves in their first two hours of playing time, then gives each
 * player another hour with which to finish the remainder of the game.
 *
 *
 *
 * <h3>Increment and Time Delay</h3>
 *
 *
 *
 *
 * With an increment, players have time added to their clock after every move that’s completed,
 * thus ensuring that they’ll always have at least the increment time to make a move. Time delay
 * works slightly differently. Instead of adding time to the clock, a time delay creates a period
 * after your opponent moves during which your clock will not run.
 *
 */
open class TimeControl : Parcelable, Cloneable, StageManagerListener {

    lateinit var name: String
    var stageManager: StageManager? = null
        private set
    var timeIncrement: TimeIncrement? = null
        private set

    var stageManagerListener: StageManagerListener? = null

    constructor(name: String?, stages: Array<Stage>, time: TimeIncrement) {
        this.name = name.orEmpty()
        timeIncrement = time
        stageManager = StageManager(stages)

        stageManager?.stageManagerListener = this
    }

    private constructor(parcel: Parcel) {
        this.readFromParcel(parcel)
    }

    private fun readFromParcel(parcel: Parcel) {
        name = parcel.readString() ?: ""
        stageManager = parcel.readParcelable(StageManager::class.java.classLoader)
        stageManager?.stageManagerListener = this
        timeIncrement = parcel.readParcelable(TimeIncrement::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelable(stageManager, flags)
        parcel.writeParcelable(timeIncrement, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun onNewStageStarted(stage: Stage) {
        stageManagerListener?.onNewStageStarted(stage)
    }

    override fun onTotalMoveCountChange(moveCount: Int) {
        stageManagerListener?.onTotalMoveCountChange(moveCount)
    }

    fun isEqual(tc: TimeControl?): Boolean {

        return (name == tc?.name
                && stageManager?.isEqual(tc.stageManager) == true
                && timeIncrement?.isEqual(tc.timeIncrement) == true)
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): TimeControl {
        val clone = super.clone() as TimeControl

        // Clone StageManager object and set this clone as his listener.
        clone.stageManager = stageManager?.clone() as StageManager
        clone.stageManager?.stageManagerListener = clone

        // Clone TimeIncrement object
        clone.timeIncrement = timeIncrement?.clone()

        clone.stageManagerListener = null
        return clone
    }

    companion object CREATOR: Parcelable.Creator<TimeControl> {
        override fun createFromParcel(source: Parcel): TimeControl {
            return TimeControl(source)
        }

        override fun newArray(size: Int): Array<TimeControl?> {
            return arrayOfNulls(size)
        }
    }
}
