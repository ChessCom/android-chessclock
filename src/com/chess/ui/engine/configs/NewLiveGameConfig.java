package com.chess.ui.engine.configs;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.StaticData;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.04.13
 * Time: 6:55
 */
public class NewLiveGameConfig implements Parcelable {
	private int userColor;
	private boolean rated;
	private int minRating;
	private int maxRating;
	private int initialTime;
	private int bonusTime;
	private String opponentName;

	public static class Builder {
		private int userColor;
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
		}

		public Builder setUserColor(int userColor) {
			this.userColor = userColor;
			return this;
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
			if(label.contains(StaticData.SYMBOL_SLASH)){// "5 | 2"),
				String[] params = label.split(RestHelper.SYMBOL_ITEM_SPLIT);
				initialTime = Integer.valueOf(params[0].trim());
				bonusTime = Integer.valueOf(params[1].trim());
			} else { // "10 min"),
				initialTime = Integer.valueOf(label);
			}
		}

		public NewLiveGameConfig build() {
			return new NewLiveGameConfig(this);
		}
	}

	private NewLiveGameConfig(Builder builder) {
		this.userColor = builder.userColor;
		this.rated = builder.rated;
		this.opponentName = builder.opponentName;
		this.minRating = builder.minRating;
		this.maxRating = builder.maxRating;
		this.initialTime = builder.initialTime;
		this.bonusTime = builder.bonusTime;
	}

	public int getUserColor() {
		return userColor;
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

	protected NewLiveGameConfig(Parcel in) {
		userColor = in.readInt();
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
		dest.writeInt(userColor);
		dest.writeByte((byte) (rated ? 0x01 : 0x00));
		dest.writeInt(minRating);
		dest.writeInt(maxRating);
		dest.writeInt(initialTime);
		dest.writeInt(bonusTime);
		dest.writeString(opponentName);
	}

	public static final Parcelable.Creator<NewLiveGameConfig> CREATOR = new Parcelable.Creator<NewLiveGameConfig>() {
		@Override
		public NewLiveGameConfig createFromParcel(Parcel in) {
			return new NewLiveGameConfig(in);
		}

		@Override
		public NewLiveGameConfig[] newArray(int size) {
			return new NewLiveGameConfig[size];
		}
	};
}
