package com.chess.backend.entity.api.stats;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.02.13
 * Time: 10:56
 */
public class AvgOpponentRating {
	/*
	  "average_opponent_rating_when_i": {
		"win": 0,
		"lose": 0,
		"draw": 0
	  }
	*/
	private int win;
	private int lose;
	private int draw;

	public int getWin() {
		return win;
	}

	public int getLose() {
		return lose;
	}

	public int getDraw() {
		return draw;
	}
}
