package com.chess.clock.engine;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.chess.clock.entities.ClockTime;

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
    /**
     * TimeIncrement type used
     */
    private Type mType;

    /**
     * Time Increment amount in milliseconds
     */
    private long mValue;

    /**
     * @param mType Time control TimeIncrement StageType.
     * @param value TimeIncrement value in milliseconds
     * @see TimeIncrement.Type
     */
    public TimeIncrement(Type mType, long value) {
        this.mType = mType;
        this.mValue = validateIncrementValue(value);
    }

    private TimeIncrement(Parcel parcel) {
        this.readFromParcel(parcel);
    }

    public static TimeIncrement defaultIncrement() {
        return new TimeIncrement(TimeIncrement.Type.FISCHER, 0);
    }

    /**
     * @return The increment type.
     */
    public Type getType() {
        return mType;
    }

    /**
     * Set the type of the time increment.
     *
     * @param type Type
     */
    public void setType(Type type) {
        mType = type;
    }

    /**
     * @return Increment time value in milliseconds.
     */
    public long getValue() {
        return mValue;
    }

    /**
     * @return Int array with {hour,minute,second}
     */
    public ClockTime getDuration() {
        return ClockTime.raw(mValue);
    }

    /**
     * Check if TimeIncrement object is equal to this one.
     *
     * @param ti TimeIncrement Object.
     * @return True if relevant contents are equal.
     */
    public boolean isEqual(TimeIncrement ti) {

        if (mValue != ti.getValue()) {
            Log.i(TAG, "Value not equal.");
            return false;
        } else if (mType.getValue() != ti.getType().getValue()) {
            Log.i(TAG, "Type not equal.");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get formatted string ready to UI info display.
     *
     * @return String representing info content of TimeIncrement.
     */
    @NonNull
    public String toString() {
        if (mType == Type.NONE) {
            return mType.typeName;
        } else {
            return mType.typeName + ", " + ClockTime.raw(mValue).toMinutesFormat();
        }
    }

    /**
     * Avoid non valid increment values.
     *
     * @param value TimeIncrement value in milliseconds
     * @return TimeIncrement value or zero if negative.
     */
    private long validateIncrementValue(long value) {
        return Math.max(0, value);
    }

    private void readFromParcel(Parcel parcel) {
        mType = Type.fromInteger(parcel.readInt());
        mValue = parcel.readLong();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mType.getValue());
        parcel.writeLong(mValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        TimeIncrement cloned = (TimeIncrement) super.clone();
        cloned.mType = Type.fromInteger(mType.getValue());
        return cloned;
    }

    /**
     * Time Control TimeIncrement StageType.
     */
    public enum Type {

        /**
         * The player's clock starts after the delay period.
         */
        DELAY(0, "Delay"),

        /**
         * Players receive the used portion of the increment at the end of each turn.
         */
        BRONSTEIN(1, "Bronstein"),

        /**
         * Players receive a full increment at the end of each turn.
         */
        FISCHER(2, "Fischer"),

        /**
         * No increment.
         */
        NONE(3, "None");

        private final int value;
        private final String typeName;

        Type(int value, String typeName) {
            this.value = value;
            this.typeName = typeName;
        }

        public static Type fromInteger(int type) {
            switch (type) {
                case 0:
                    return DELAY;
                case 1:
                    return BRONSTEIN;
                case 2:
                    return FISCHER;
                case 3:
                    return NONE;
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }
}