package com.chess.backend.entity.new_api.stats;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.02.13
 * Time: 11:10
 */
public class GamesInfo {
/*
  "draws": {
	"total": 0,
	"white": 0,
	"black": 0
  },
*/
	private int total;
	private int white;
	private int black;

	public int getTotal() {
		return total;
	}

	public int getWhite() {
		return white;
	}

	public int getBlack() {
		return black;
	}
}
