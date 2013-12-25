package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.12.13
 * Time: 10:53
 */
public class DailyFinishedGamesItem extends BaseResponseItem<DailyFinishedGamesItem.Data> {

/*
	"data": {
		"games": [
			{
				.....
			}
		],
		games_total_count: 123
}


*/

	public class Data {

		private List<DailyFinishedGameData> games;
		private int games_total_count;

		public List<DailyFinishedGameData> getGames() {
			return games;
		}

		public int getGamesTotalCount() {
			return games_total_count;
		}
	}
}
