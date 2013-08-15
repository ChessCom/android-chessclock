package com.chess.ui.engine.configs;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.04.13
 * Time: 15:55
 */
public class DailyGameConfig {

	private int daysPerMove;
	private boolean rated;
	private int minRating;
	private int maxRating;
	private int gameType;
	private String opponentName;

	public static class Builder{
		private int daysPerMove;
		private boolean rated;
		private int minRating;
		private int maxRating;
		private int gameType;
		private String opponentName;

		/**
		 * Create new Seek game with default values
		 */
		public Builder(){
			daysPerMove = 3;
			rated = true;
			gameType = 1;
		}

		public Builder setDaysPerMove(int daysPerMove) {
			this.daysPerMove = daysPerMove;
			return this;
		}

		public Builder setRated(boolean rated) {
			this.rated = rated;
			return this;
		}

		public Builder setGameType(int gameType) {
			this.gameType = gameType;
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

		public DailyGameConfig build(){
			return new DailyGameConfig(this);
		}
	}

	private DailyGameConfig(Builder builder) {
		this.daysPerMove = builder.daysPerMove;
		this.rated = builder.rated;
		this.gameType = builder.gameType;
		this.opponentName = builder.opponentName;
		this.minRating = builder.minRating;
		this.maxRating = builder.maxRating;
	}

	public int getDaysPerMove() {
		return daysPerMove;
	}

	public boolean isRated() {
		return rated;
	}

	public int getGameType() {
		return gameType;
	}

	public String getOpponentName() {
		return opponentName;
	}

	public void setOpponentName(String opponentName) {
		this.opponentName = opponentName;
	}

	public int getMinRating() {
		return minRating;
	}

	public int getMaxRating() {
		return maxRating;
	}
}
