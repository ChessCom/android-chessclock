package com.chess.ui.engine.configs;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.04.13
 * Time: 20:05
 */
public class NewCompGameConfig {
	private int compDelay;
	private int mode;

	public static class Builder{
		private int compDelay;
		private int mode;

		/**
		 * Create new Seek game with default values
		 */
		public Builder(){
			compDelay = 5;
		}

		public Builder setCompDelay(int compDelay) {
			this.compDelay = compDelay;
			return this;
		}

		public Builder setMode(int mode) {
			this.mode = mode;
			return this;
		}

		public NewCompGameConfig build(){
			return new NewCompGameConfig(this);
		}
	}

	private NewCompGameConfig(Builder builder) {
		this.compDelay = builder.compDelay;
		this.mode = builder.mode;
	}

	public int getCompDelay() {
		return compDelay;
	}

	public int getMode() {
		return mode;
	}
}
