package com.chess.ui.engine.configs;

import com.chess.backend.statics.AppConstants;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.04.13
 * Time: 20:05
 */
public class CompGameConfig {
	private int strength;
	private int mode;

	public static class Builder{
		private int strength;
		private int mode;

		/**
		 * Create new Seek game with default values
		 */
		public Builder(){
			mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
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

		public CompGameConfig build(){
			return new CompGameConfig(this);
		}
	}

	private CompGameConfig(Builder builder) {
		this.strength = builder.strength;
		this.mode = builder.mode;
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
}
