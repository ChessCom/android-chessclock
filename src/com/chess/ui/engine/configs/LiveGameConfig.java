package com.chess.ui.engine.configs;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.Symbol;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.04.13
 * Time: 6:55
 */
public class LiveGameConfig implements Parcelable {
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

		/**
		 * Create new Seek game with default values
		 */
		public Builder() {
			rated = true;
			initialTime = 30;
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

		public void setTimeFromLabel(String label) {
			if(label.contains(Symbol.SLASH)){// "5 | 2"),
				String[] params = label.split(RestHelper.SYMBOL_ITEM_SPLIT);
				initialTime = Integer.valueOf(params[0].trim());
				bonusTime = Integer.valueOf(params[1].trim());
			} else { // "10 min"),
				initialTime = Integer.valueOf(label);
			}
		}

		public LiveGameConfig build() {
			return new LiveGameConfig(this);
		}
	}

	private LiveGameConfig(Builder builder) {
		this.rated = builder.rated;
		this.opponentName = builder.opponentName;
		this.minRating = builder.minRating;
		this.maxRating = builder.maxRating;
		this.initialTime = builder.initialTime;
		this.bonusTime = builder.bonusTime;
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
