package com.chess.clock.engine.time

import android.os.Parcel
import android.os.Parcelable

import com.chess.clock.engine.stage.Stage
import com.chess.clock.engine.stage.StageManager
import com.chess.clock.engine.stage.StageManagerListener
import com.chess.clock.util.Args

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

    private val TAG = TimeControl::class.java.name

    /**
     * TimeControl identifier.
     */
    /**
     * The name identifier of the time control.
     *
     * @return Time control name.
     */
    /**
     * Set the name identifier of the time control.
     *
     * @param name Time control name.
     */
    lateinit var name: String

    /**
     * Stage Manager associated with Time Control.
     */
    /**
     * Gets the Stage manager associated with this time control.
     *
     * @return Stage manager associated with this time control.
     */
    lateinit var stageManager: StageManager
        private set

    /**
     * Time increment associated with Time Control.
     */
    /**
     * Gets the time increment associated with this time control.
     *
     * @return TimeIncrement associated with this TimeControl.
     */
    lateinit var timeIncrement: TimeIncrement
        private set

    /**
     * Listener used to dispatch time control update events.
     *
     * @see TimeControlListener
     */
    private var mTimeControlListener: TimeControlListener? = null

    /**
     * Simple constructor to use when creating a TimeControl.
     *
     * @param name   Name identifier.
     * @param stages stages of the TimeControl.
     * @param time   TimeIncrement object associated with the TimeControl.
     * @throws java.lang.NullPointerException if StageManager or TimeIncrement are not provided.
     */
    constructor(name: String, stages: Array<Stage>, time: TimeIncrement) {
        this.name = name
        timeIncrement = time
        stageManager = StageManager(stages)

        // Set up listener for Stage Manager.
        stageManager.setStageManagerListener(this)
    }

    private constructor(parcel: Parcel) {
        this.readFromParcel(parcel)
    }

    /**
     * Check if TimeControl object is equal to this one.
     *
     * @param tc TimeControl Object.
     * @return True if relevant contents are equal.
     */
    fun isEqual(tc: TimeControl): Boolean {

        return (name == tc.name
                && stageManager.isEqual(tc.stageManager)
                && timeIncrement.isEqual(tc.timeIncrement))
    }

    /**
     * Register a callback to be invoked when time control updates.
     *
     * @param listener The callback that will run
     */
    fun setTimeControlListener(listener: TimeControlListener) {
        Args.checkForNull(listener)
        mTimeControlListener = listener
    }

    private fun readFromParcel(parcel: Parcel) {
        name = parcel.readString() ?: ""
        stageManager = parcel.readParcelable(StageManager::class.java.classLoader)
        stageManager.setStageManagerListener(this)
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

    override fun onNewStageUpdate(stage: Stage) {
        mTimeControlListener?.onStageUpdate(stage)
    }

    override fun onMoveCountUpdate(moveCount: Int) {
        mTimeControlListener?.onMoveCountUpdate(moveCount)
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): TimeControl {
        val clone = super.clone() as TimeControl

        // Clone StageManager object and set this clone as his listener.
        clone.stageManager = stageManager.clone() as StageManager
        clone.stageManager.setStageManagerListener(clone)

        // Clone TimeIncrement object
        clone.timeIncrement = timeIncrement.clone()

        clone.mTimeControlListener = null
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
