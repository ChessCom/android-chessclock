package com.chess.ui.engine;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.04.13
 * Time: 6:55
 */
public class NewLiveGameConfig {
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

	public String getDefaultModeLabel(){
		return String.valueOf(initialTime) + " | " + bonusTime;
	}
}
