package com.chess.clock.engine;

import android.os.Parcel;
import android.os.Parcelable;

import com.chess.clock.engine.time.TimeControl;

public class TimeControlWrapper implements Parcelable, Cloneable{
  private TimeControl mTimeControlPlayerOne;
  private TimeControl mTimeControlPlayerTwo;
  private boolean mSameAsPlayerOne;

  public TimeControlWrapper(TimeControl playerOne, TimeControl playerTwo) {
    mTimeControlPlayerOne = playerOne;
    mTimeControlPlayerTwo = playerTwo;
    mSameAsPlayerOne = true;
  }

  private TimeControlWrapper(Parcel in) {
    mTimeControlPlayerOne = in.readParcelable(TimeControl.class.getClassLoader());
    mTimeControlPlayerTwo = in.readParcelable(TimeControl.class.getClassLoader());
    mSameAsPlayerOne = in.readParcelable(boolean.class.getClassLoader());
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

  @Override
  public Object clone() throws CloneNotSupportedException {
    TimeControlWrapper clone = (TimeControlWrapper) super.clone();

    // Clone StageManager object and set this clone as his listener.
    clone.mTimeControlPlayerOne = (TimeControl) mTimeControlPlayerOne.clone();
    clone.mTimeControlPlayerTwo = (TimeControl) mTimeControlPlayerTwo.clone();
    clone.mSameAsPlayerOne = mSameAsPlayerOne;

    return clone;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(mTimeControlPlayerOne, flags);
    dest.writeParcelable(mTimeControlPlayerTwo, flags);
  }

  public boolean isEqual(TimeControlWrapper wrapper) {
    return mTimeControlPlayerOne.isEqual(wrapper.getTimeControlPlayerOne()) &&
        mTimeControlPlayerTwo.isEqual(wrapper.getTimeControlPlayerTwo()) &&
        mSameAsPlayerOne == wrapper.isSameAsPlayerOne();
  }
}
