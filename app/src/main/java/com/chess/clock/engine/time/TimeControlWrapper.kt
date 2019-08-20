package com.chess.clock.engine.time

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.VisibleForTesting

class TimeControlWrapper : Parcelable, Cloneable {
    var timeControlPlayerOne: TimeControl?
    var timeControlPlayerTwo: TimeControl?
    var isSameAsPlayerOne: Boolean = false

    constructor(playerOne: TimeControl, playerTwo: TimeControl) {
        timeControlPlayerOne = playerOne
        timeControlPlayerTwo = playerTwo
        isSameAsPlayerOne = true
    }

    @VisibleForTesting
    constructor(parcel: Parcel) {
        timeControlPlayerOne = parcel.readParcelable(TimeControl::class.java.classLoader)
        timeControlPlayerTwo = parcel.readParcelable(TimeControl::class.java.classLoader)
        isSameAsPlayerOne = parcel.readByte().toInt() != 0
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): TimeControlWrapper {
        val clone = super.clone() as TimeControlWrapper

        // Clone StageManager object and set this clone as his listener.
        clone.timeControlPlayerOne = timeControlPlayerOne?.clone()
        clone.timeControlPlayerTwo = timeControlPlayerTwo?.clone()
        clone.isSameAsPlayerOne = isSameAsPlayerOne

        return clone
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(timeControlPlayerOne, flags)
        dest.writeParcelable(timeControlPlayerTwo, flags)
        dest.writeByte((if (isSameAsPlayerOne) 1 else 0).toByte())
    }

    fun isEqual(wrapper: TimeControlWrapper): Boolean {
        return timeControlPlayerOne?.isEqual(wrapper.timeControlPlayerOne) == true &&
                timeControlPlayerTwo?.isEqual(wrapper.timeControlPlayerTwo) == true &&
                isSameAsPlayerOne == wrapper.isSameAsPlayerOne
    }

    companion object CREATOR: Parcelable.Creator<TimeControlWrapper> {
        override fun createFromParcel(parcel: Parcel): TimeControlWrapper {
            return TimeControlWrapper(parcel)
        }

        override fun newArray(size: Int): Array<TimeControlWrapper?> {
            return arrayOfNulls(size)
        }
    }
}
