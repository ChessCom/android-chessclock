package com.chess.backend.entity.api.stats;

import com.chess.backend.statics.StaticData;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.02.13
 * Time: 10:50
 */
public class BestWin {

	private int rating;
	private long game_id;
	private String username;

	public int getRating() {
		return rating;
	}

	public String getUsername() {
		return username == null? StaticData.SYMBOL_EMPTY: username;
	}

	public long getGameId() {
		return game_id;
	}

}
