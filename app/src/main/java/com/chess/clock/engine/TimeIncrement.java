package com.chess.clock.engine;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.chess.clock.engine.time.TimeIncrementType;
import com.chess.clock.engine.time.TimeIncrementTypeKt;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.VisibleForTesting;

/**
 * A small amount of time that is added for each game addMove.
 */
public class TimeIncrement implements Parcelable, Cloneable {

    public static final Parcelable.Creator<TimeIncrement> CREATOR = new Parcelable.Creator<TimeIncrement>() {
        public TimeIncrement createFromParcel(Parcel source) {
            return new TimeIncrement(source);
        }

        public TimeIncrement[] newArray(int size) {
            return new TimeIncrement[size];
        }
    };
    private static final String TAG = TimeIncrement.class.getName();

    private TimeIncrementType incrementType;
    private long valueInMilliseconds;

    /**
     * @param incrementType Time control type.
     * @param valueInMilliseconds TimeIncrement value in milliseconds
     * @see TimeIncrementType
     */
    public TimeIncrement(TimeIncrementType incrementType, long valueInMilliseconds) {
        this.incrementType = incrementType;
        this.valueInMilliseconds = validateIncrementValue(valueInMilliseconds);
    }

    @VisibleForTesting
    public TimeIncrement(Parcel parcel) {
        this.readFromParcel(parcel);
    }

    /**
     * @return The increment type.
     */
    public TimeIncrementType getType() {
        return incrementType;
    }

    /**
     * Set the type of the time increment.
     *
     * @param type Type
     */
    public void setType(TimeIncrementType type) {
        incrementType = type;
    }

    /**
     * @return Increment time value in milliseconds.
     */
    public long getValue() {
        return valueInMilliseconds;
    }

    /**
     * Set the increment time value.
     *
     * @param valueInMilliseconds increment time in milliseconds.
     */
    public void setValue(long valueInMilliseconds) {
        this.valueInMilliseconds = valueInMilliseconds;
    }

    /**
     * @return Int array with {hour,minute,second}
     */
    public int[] getDuration() {
        int s = (int) (valueInMilliseconds / 1000) % 60;
        int m = (int) ((valueInMilliseconds / (1000 * 60)) % 60);
        int h = (int) ((valueInMilliseconds / (1000 * 60 * 60)) % 24);

        return new int[]{h, m, s};
    }

    /**
     * Check if TimeIncrement object is equal to this one.
     *
     * @param ti TimeIncrement Object.
     * @return True if relevant contents are equal.
     */
    public boolean isEqual(TimeIncrement ti) {

        if (valueInMilliseconds != ti.getValue()) {
            Log.i(TAG, "Value not equal.");
            return false;
        } else if (incrementType != ti.getType()) {
            Log.i(TAG, "Type not equal.");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get formated string ready to UI info display.
     *
     * @return String representing info content of TimeIncrement.
     */
    @NotNull
    public String toString() {
        String durationString = formatTime(getValue());
        return incrementType + ", " + durationString;
    }

    /**
     * @param time Player time in milliseconds.
     * @return Readable String format of time.
     */
    private String formatTime(long time) {

        int s = (int) (time / 1000) % 60;
        int m = (int) ((time / (1000 * 60)) % 60);
        int h = (int) ((time / (1000 * 60 * 60)) % 60);

        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /**
     * Avoid non valid increment values.
     *
     * @param value TimeIncrement value in milliseconds
     * @return TimeIncrement value or zero if negative.
     */
    private long validateIncrementValue(long value) {
        if (value < 0)
            return 0;
        return value;
    }

    private void readFromParcel(Parcel parcel) {
        incrementType = TimeIncrementTypeKt.fromInteger(parcel.readInt());
        valueInMilliseconds = parcel.readLong();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(TimeIncrementTypeKt.toInteger(incrementType));
        parcel.writeLong(valueInMilliseconds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public TimeIncrement clone() throws CloneNotSupportedException {
        return (TimeIncrement) super.clone();
    }
}