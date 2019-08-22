package com.chess.clock.engine.time

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.VisibleForTesting

class TimeIncrement : Parcelable, Cloneable {

    lateinit var type: TimeIncrementType
    var valueInMilliseconds: Long = 0

    /**
     * @return Int array with {hour,minute,second}
     */
    val duration: IntArray
        get() {
            val s = (valueInMilliseconds / 1000).toInt() % 60
            val m = (valueInMilliseconds / (1000 * 60) % 60).toInt()
            val h = (valueInMilliseconds / (1000 * 60 * 60) % 24).toInt()

            return intArrayOf(h, m, s)
        }

    constructor(incrementType: TimeIncrementType, valueInMilliseconds: Long) {
        this.type = incrementType
        this.valueInMilliseconds = validateIncrementValue(valueInMilliseconds)
    }

    @VisibleForTesting
    constructor(parcel: Parcel) {
        this.readFromParcel(parcel)
    }

    fun isEqual(ti: TimeIncrement?): Boolean {
        return valueInMilliseconds == ti?.valueInMilliseconds
                && type == ti.type
    }

    override fun toString(): String {
        val durationString = formatTime(valueInMilliseconds)
        return "$type, $durationString"
    }

    private fun formatTime(time: Long): String {

        val s = (time / 1000).toInt() % 60
        val m = (time / (1000 * 60) % 60).toInt()
        val h = (time / (1000 * 60 * 60) % 60).toInt()

        return String.format("%02d:%02d:%02d", h, m, s)
    }

    private fun validateIncrementValue(value: Long): Long {
        return if (value < 0) 0 else value
    }

    private fun readFromParcel(parcel: Parcel) {
        type = TimeIncrementType.fromInteger(parcel.readInt())
        valueInMilliseconds = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type.toInteger())
        parcel.writeLong(valueInMilliseconds)
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
    }
}