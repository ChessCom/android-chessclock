package com.chess.backend.entity.api.stats;

import com.chess.backend.statics.StaticData;

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
		return rank == null? StaticData.SYMBOL_EMPTY: rank;
	}

	public int getTotalPlayerCount() {
		return total_player_count;
	}
}
