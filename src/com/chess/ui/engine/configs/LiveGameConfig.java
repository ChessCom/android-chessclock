package com.chess.ui.engine.configs;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.04.13
 * Time: 6:55
 */
public class LiveGameConfig implements Parcelable {

	public static final int STANDARD = 0;
	public static final int BLITZ = 1;
	public static final int LIGHTNING = 2;

	public static final int RATING_STEP = 100;

	private boolean rated;
	private int minRating;
	private int maxRating;
	private int initialTime;
	private int bonusTime;
	private String opponentName;

	public static class Builder {
		private boolean rated;
		private int initialTime;
		private int bonusTime;
		private int minRating;
		private int maxRating;
		private String opponentName;
		private HashMap<Integer, Integer> timeModesMap;
		private int timeMode;

		/**
		 * Create new Seek game with default values
		 */
		public Builder() {
			rated = true;
			initialTime = 30;

			timeModesMap = new HashMap<Integer, Integer>();
			timeModesMap.put(0, STANDARD);    // 30		 = 0
			timeModesMap.put(4, STANDARD);    // 10		 = 1
			timeModesMap.put(1, BLITZ);       // 5 | 2	 = 2
			timeModesMap.put(2, BLITZ);       // 2 | 1	 = 3
			timeModesMap.put(5, BLITZ);       // 15 | 10 = 4
			timeModesMap.put(6, BLITZ);       // 5		 = 5
			timeModesMap.put(3, LIGHTNING);      // 3		 = 6
			timeModesMap.put(7, LIGHTNING);      // 1		 = 7
		}

		public Builder setRated(boolean rated) {
			this.rated = rated;
			return this;
		}

		public Builder setOpponentName(String opponentName) {
			this.opponentName = opponentName;
			return this;
		}

		public Builder setMinRating(int minRating) {
			this.minRating = minRating;
			return this;
		}

		public Builder setMaxRating(int maxRating) {
			this.maxRating = maxRating;
			return this;
		}

		/**
		 * This should be in sync with arrays_values resource file:
		 * 30		 = 0
		 * 10		 = 1
		 * 5 | 2	 = 2
		 * 2 | 1	 = 3
		 * 15 | 10	 = 4
		 * 5		 = 5
		 * 3		 = 6
		 * 1		 = 7
		 *
		 */
		public Builder setTimeFromMode(int mode){
			if (timeModesMap.get(mode) == STANDARD) {
				timeMode = STANDARD;
			} else if (timeModesMap.get(mode) == BLITZ) {
				timeMode = BLITZ;
			} else if (timeModesMap.get(mode) == LIGHTNING) {
				timeMode = LIGHTNING;
			}

			switch (mode){
				case 0:	initialTime = 30;	bonusTime = 0;	break;
				case 1:	initialTime = 10;	bonusTime = 0;	break;
				case 2:	initialTime = 5;	bonusTime = 2;	break;
				case 3:	initialTime = 2;	bonusTime = 1;	break;
				case 4:	initialTime = 15;	bonusTime = 10;	break;
				case 5:	initialTime = 5;	bonusTime = 0;	break;
				case 6:	initialTime = 3;	bonusTime = 0;	break;
				case 7:	initialTime = 1;	bonusTime = 0;	break;
			}
			return this;
		}

		public int getMinRating() {
			return minRating;
		}

		public int getMaxRating() {
			return maxRating;
		}

		public int getTimeMode() {
			return timeMode;
		}

		public LiveGameConfig build() {
			return new LiveGameConfig(this, false);
		}

		public LiveGameConfig build(boolean challengeFriend) {
			return new LiveGameConfig(this, challengeFriend);
		}
	}

	private LiveGameConfig(Builder builder, boolean challengeFriend) {
		this.rated = builder.rated;
		this.minRating = builder.minRating;
		this.maxRating = builder.maxRating;
		this.initialTime = builder.initialTime;
		this.bonusTime = builder.bonusTime;

		if (challengeFriend) {
			this.opponentName = builder.opponentName;
		}
	}

	public boolean isRated() {
		return rated;
	}

	public String getOpponentName() {
		return opponentName;
	}

	public int getMinRating() {
		return minRating;
	}

	public int getMaxRating() {
		return maxRating;
	}

	public int getInitialTime() {
		return initialTime;
	}

	public int getBonusTime() {
		return bonusTime;
	}

	public String getDefaultModeLabel() {
		return String.valueOf(initialTime) + " | " + bonusTime;
	}

	protected LiveGameConfig(Parcel in) {
		rated = in.readByte() != 0x00;
		minRating = in.readInt();
		maxRating = in.readInt();
		initialTime = in.readInt();
		bonusTime = in.readInt();
		opponentName = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (rated ? 0x01 : 0x00));
		dest.writeInt(minRating);
		dest.writeInt(maxRating);
		dest.writeInt(initialTime);
		dest.writeInt(bonusTime);
		dest.writeString(opponentName);
	}

	public static final Parcelable.Creator<LiveGameConfig> CREATOR = new Parcelable.Creator<LiveGameConfig>() {
		@Override
		public LiveGameConfig createFromParcel(Parcel in) {
			return new LiveGameConfig(in);
		}

		@Override
		public LiveGameConfig[] newArray(int size) {
			return new LiveGameConfig[size];
		}
	};
}
