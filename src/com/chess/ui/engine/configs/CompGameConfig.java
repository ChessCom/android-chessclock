package com.chess.ui.engine.configs;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.backend.statics.AppConstants;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.04.13
 * Time: 20:05
 */
public class CompGameConfig implements Parcelable {

	private int strength;
	private int mode;
	private boolean autoFlip;

	public static class Builder{
		private int strength;
		private int mode;
		private boolean autoFlip;


		/**
		 * Create new Seek game with default values
		 */
		public Builder(){
			mode = AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE;
			strength = 5;
		}

		public Builder setStrength(int strength) {
			this.strength = strength;
			return this;
		}

		public Builder setMode(int mode) {
			this.mode = mode;
			return this;
		}

		public Builder setAutoFlip(boolean autoFlip) {
			this.autoFlip = autoFlip;
			return this;
		}

		public CompGameConfig build(){
			return new CompGameConfig(this);
		}
	}

	private CompGameConfig(Builder builder) {
		this.strength = builder.strength;
		this.mode = builder.mode;
		this.autoFlip = builder.autoFlip;
	}

	public int getStrength() {
		return strength;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public void setAutoFlip(boolean autoFlip) {
		this.autoFlip = autoFlip;
	}

	protected CompGameConfig(Parcel in) {
		strength = in.readInt();
		mode = in.readInt();
		autoFlip = in.readByte() != 0x00;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(strength);
		dest.writeInt(mode);
		dest.writeByte((byte) (autoFlip ? 0x01 : 0x00));
	}

	public static final Parcelable.Creator<CompGameConfig> CREATOR = new Parcelable.Creator<CompGameConfig>() {
		@Override
		public CompGameConfig createFromParcel(Parcel in) {
			return new CompGameConfig(in);
		}

		@Override
		public CompGameConfig[] newArray(int size) {
			return new CompGameConfig[size];
		}
	};
}
