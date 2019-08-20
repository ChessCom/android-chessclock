package com.chess.clock.engine.time

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.VisibleForTesting

/**
 * A small amount of time that is added for each game addMove.
 */
class TimeIncrement : Parcelable, Cloneable {

    /**
     * @return The increment type.
     */
    /**
     * Set the type of the time increment.
     *
     * @param type Type
     */
    lateinit var type: TimeIncrementType

    /**
     * @return Increment time value in milliseconds.
     */
    /**
     * Set the increment time value.
     *
     * @param valueInMilliseconds increment time in milliseconds.
     */
    var value: Long = 0

    /**
     * @return Int array with {hour,minute,second}
     */
    val duration: IntArray
        get() {
            val s = (value / 1000).toInt() % 60
            val m = (value / (1000 * 60) % 60).toInt()
            val h = (value / (1000 * 60 * 60) % 24).toInt()

            return intArrayOf(h, m, s)
        }

    /**
     * @param incrementType Time control type.
     * @param valueInMilliseconds TimeIncrement value in milliseconds
     * @see TimeIncrementType
     */
    constructor(incrementType: TimeIncrementType, valueInMilliseconds: Long) {
        this.type = incrementType
        this.value = validateIncrementValue(valueInMilliseconds)
    }

    @VisibleForTesting
    constructor(parcel: Parcel) {
        this.readFromParcel(parcel)
    }

    /**
     * Check if TimeIncrement object is equal to this one.
     *
     * @param ti TimeIncrement Object.
     * @return True if relevant contents are equal.
     */
    fun isEqual(ti: TimeIncrement?): Boolean {

        if (value != ti?.value) {
            Log.i(TAG, "Value not equal.")
            return false
        } else if (type !== ti.type) {
            Log.i(TAG, "Type not equal.")
            return false
        } else {
            return true
        }
    }

    /**
     * Get formated string ready to UI info display.
     *
     * @return String representing info content of TimeIncrement.
     */
    override fun toString(): String {
        val durationString = formatTime(value)
        return "$type, $durationString"
    }

    /**
     * @param time Player time in milliseconds.
     * @return Readable String format of time.
     */
    private fun formatTime(time: Long): String {

        val s = (time / 1000).toInt() % 60
        val m = (time / (1000 * 60) % 60).toInt()
        val h = (time / (1000 * 60 * 60) % 60).toInt()

        return String.format("%02d:%02d:%02d", h, m, s)
    }

    /**
     * Avoid non valid increment values.
     *
     * @param value TimeIncrement value in milliseconds
     * @return TimeIncrement value or zero if negative.
     */
    private fun validateIncrementValue(value: Long): Long {
        return if (value < 0) 0 else value
    }

    private fun readFromParcel(parcel: Parcel) {
        type = fromInteger(parcel.readInt())
        value = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type.toInteger())
        parcel.writeLong(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): TimeIncrement {
        return super.clone() as TimeIncrement
    }

    companion object CREATOR : Parcelable.Creator<TimeIncrement> {

        override fun createFromParcel(source: Parcel): TimeIncrement {
            return TimeIncrement(source)
        }

        override fun newArray(size: Int): Array<TimeIncrement?> {
            return arrayOfNulls(size)
        }

        private val TAG = TimeIncrement::class.java.name
    }
}