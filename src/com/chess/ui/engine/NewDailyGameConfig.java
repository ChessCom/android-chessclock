package com.chess.ui.engine;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.04.13
 * Time: 15:55
 */
public class NewDailyGameConfig {

	private int daysPerMove;
	private int userColor;
	private boolean rated;
	private int minRating;
	private int maxRating;
	private int gameType;
	private String opponentName;

	public static class Builder{
		private int daysPerMove;
		private int userColor;
		private boolean rated;
		private int minRating;
		private int maxRating;
		private int gameType;
		private String opponentName;
/*
		loadItem.addRequestParams(RestHelper.P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(RestHelper.P_USER_SIDE, color);
		loadItem.addRequestParams(RestHelper.P_IS_RATED, isRated);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);
		loadItem.addRequestParams(RestHelper.P_OPPONENT, opponentName);

fields____|___values__|required|______explanation__________________________________________________
opponent				false	See explanation above for possible values. Default is `null`.
daysPerMove		\d+		true	Days per move. 1,3,5,7,14
userPosition	0|1|2	true	User will play as - 0 = random, 1 = white, 2 = black. Default is `0`.
minRating		\d+		false	Minimum rating.
maxRating		\d+		false	Maximum rating.
isRated			0|1		true	Is game seek rated or not. Default is `1`.
gameType	chess(960)?	true	Game type code. Default is `1`.
gameSeekName	\w+		false	Name of new game/challenge. Default is `Let's Play!`.
			 */

		/**
		 * Create new Seek game with default values
		 */
		public Builder(){
			daysPerMove = 3;
			rated = true;
		}

		public Builder setDaysPerMove(int daysPerMove) {
			this.daysPerMove = daysPerMove;
			return this;
		}

		public Builder setUserColor(int userColor) {
			this.userColor = userColor;
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

		public NewDailyGameConfig build(){
			return new NewDailyGameConfig(this);
		}
	}

	private NewDailyGameConfig(Builder builder) {
		this.daysPerMove = builder.daysPerMove;
		this.userColor = builder.userColor;
		this.rated = builder.rated;
		this.gameType = builder.gameType;
		this.opponentName = builder.opponentName;
		this.minRating = builder.minRating;
		this.maxRating = builder.maxRating;
	}

	public int getDaysPerMove() {
		return daysPerMove;
	}

	public int getUserColor() {
		return userColor;
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

	public int getMinRating() {
		return minRating;
	}

	public int getMaxRating() {
		return maxRating;
	}
}
