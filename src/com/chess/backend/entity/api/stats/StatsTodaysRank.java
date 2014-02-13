package com.chess.backend.entity.api.stats;

import com.chess.statics.Symbol;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.02.13
 * Time: 10:43
 */
public class StatsTodaysRank {
	private String rank;
	private int total_player_count;

	public String getRank() {
		return rank == null ? Symbol.EMPTY : rank;
	}

	public int getTotalPlayerCount() {
		return total_player_count;
	}
}
