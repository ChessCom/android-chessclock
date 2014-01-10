package com.chess.backend.entity.api.daily_games;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.12.12
 * Time: 6:53
 */
public class DailyGameAcceptItem extends BaseResponseItem<DailyGameAcceptItem.Data> {
	/*
   "status": "success",
    "data": {
        "game_id": 35000574
    }
	*/

	public class Data {
		private long game_id;

		public long getGame_id() {
			return game_id;
		}

		public void setGame_id(long game_id) {
			this.game_id = game_id;
		}
	}
}
