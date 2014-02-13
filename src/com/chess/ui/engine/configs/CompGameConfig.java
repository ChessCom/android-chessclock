package com.chess.ui.engine.configs;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.statics.AppConstants;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.04.13
 * Time: 20:05
 */
public class CompGameConfig implements Parcelable {

	private int strength;
	/**
	 * Can be one of the following:
	 * GAME_MODE_COMPUTER_VS_PLAYER_WHITE = 0;
	 * GAME_MODE_COMPUTER_VS_PLAYER_BLACK = 1;
	 * GAME_MODE_2_PLAYERS = 2;
	 * GAME_MODE_COMPUTER_VS_COMPUTER = 3;
	 */
	private int mode;
	private String fen;

	public static class Builder {
		private int strength;
		private int mode;
		private String fen;

		/**
		 * Create new Seek game with default values
		 */
		public Builder() {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE;
			strength = 5;
			fen = null; // rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
		}

		public Builder setStrength(int strength) {
			this.strength = strength;
			return this;
		}

		public Builder setMode(int mode) {
			this.mode = mode;
			return this;
		}

		public Builder setFen(String fen) {
			this.fen = fen;
			return this;
		}

		public CompGameConfig build() {
			return new CompGameConfig(this);
		}
	}

	private CompGameConfig(Builder builder) {
		this.strength = builder.strength;
		this.mode = builder.mode;
		this.fen = builder.fen;
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

	public String getFen() {
		return fen;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	protected CompGameConfig(Parcel in) {
		strength = in.readInt();
		mode = in.readInt();
		fen = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(strength);
		dest.writeInt(mode);
		dest.writeString(fen);
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
