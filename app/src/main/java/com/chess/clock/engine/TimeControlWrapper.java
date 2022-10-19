package com.chess.clock.engine;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Random;

public class TimeControlWrapper implements Parcelable, Cloneable {
    private TimeControl mTimeControlPlayerOne;
    private TimeControl mTimeControlPlayerTwo;

    private boolean mSameAsPlayerOne;
    private long id;
    private int order;

    public TimeControlWrapper(
            long id,
            int order,
            TimeControl playerOne,
            TimeControl playerTwo
    ) {
        this.id = id;
        this.order = order;
        mTimeControlPlayerOne = playerOne;
        mTimeControlPlayerTwo = playerTwo;
        mSameAsPlayerOne = true;
    }

    private TimeControlWrapper(Parcel in) {
        id = in.readLong();
        order = in.readInt();
        mTimeControlPlayerOne = in.readParcelable(TimeControl.class.getClassLoader());
        mTimeControlPlayerTwo = in.readParcelable(TimeControl.class.getClassLoader());
        mSameAsPlayerOne = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(order);
        dest.writeParcelable(mTimeControlPlayerOne, flags);
        dest.writeParcelable(mTimeControlPlayerTwo, flags);
        dest.writeByte((byte) (mSameAsPlayerOne ? 1 : 0));
    }


    public static final Creator<TimeControlWrapper> CREATOR = new Creator<TimeControlWrapper>() {
        @Override
        public TimeControlWrapper createFromParcel(Parcel in) {
            return new TimeControlWrapper(in);
        }

        @Override
        public TimeControlWrapper[] newArray(int size) {
            return new TimeControlWrapper[size];
        }
    };

    public TimeControl getTimeControlPlayerOne() {
        return mTimeControlPlayerOne;
    }

    public void setTimeControlPlayerOne(TimeControl timeControl) {
        mTimeControlPlayerOne = timeControl;
    }

    public TimeControl getTimeControlPlayerTwo() {
        return mTimeControlPlayerTwo;
    }

    public void setTimeControlPlayerTwo(TimeControl timeControl) {
        mTimeControlPlayerTwo = timeControl;
    }

    public boolean isSameAsPlayerOne() {
        return mSameAsPlayerOne;
    }

    public void setSameAsPlayerOne(boolean sameAsPlayerOne) {
        mSameAsPlayerOne = sameAsPlayerOne;
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        TimeControlWrapper clone = (TimeControlWrapper) super.clone();

        // Clone StageManager object and set this clone as his listener.
        clone.mTimeControlPlayerOne = (TimeControl) mTimeControlPlayerOne.clone();
        clone.mTimeControlPlayerTwo = (TimeControl) mTimeControlPlayerTwo.clone();
        clone.mSameAsPlayerOne = mSameAsPlayerOne;
        clone.order = order;
        clone.id = id;
        return clone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isEqual(TimeControlWrapper wrapper) {
        return mTimeControlPlayerOne.isEqual(wrapper.getTimeControlPlayerOne()) &&
                mTimeControlPlayerTwo.isEqual(wrapper.getTimeControlPlayerTwo()) &&
                mSameAsPlayerOne == wrapper.isSameAsPlayerOne() &&
                id == wrapper.id &&
                order == wrapper.order;
    }

    public boolean bothUsersHaveAtLeastOneStage() {
        if (mSameAsPlayerOne) {
            return mTimeControlPlayerOne.getStageManager().getTotalStages() > 0;
        } else {
            return mTimeControlPlayerOne.getStageManager().getTotalStages() > 0 && mTimeControlPlayerTwo.getStageManager().getTotalStages() > 0;
        }
    }

    public long getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int newOrder) {
        this.order = newOrder;
    }
}
