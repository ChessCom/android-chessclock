package com.chess.backend.entity.api.daily_games;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.01.14
 * Time: 12:20
 */
public class MyMoveItem extends BaseResponseItem<MyMoveItem.Data> {

/*
	"data": {
        "is_my_turn_to_move": true,
        "message": "Your Move!",
        "timestamp": 1355687586
    }
*/

	public class Data {
		private boolean is_my_turn_to_move;
		private String message;
		private long timestamp;

		public boolean isIsMyTurn() {
			return is_my_turn_to_move;
		}

		public String getMessage() {
			return message;
		}

		public long getTimestamp() {
			return timestamp;
		}
	}
}
