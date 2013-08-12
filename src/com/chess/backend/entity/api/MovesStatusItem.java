package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.12.12
 * Time: 10:57
 */
public class MovesStatusItem extends BaseResponseItem<MovesStatusItem.Data> {
/*
   "status": "success",
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

		public boolean isIs_my_turn_to_move() {
			return is_my_turn_to_move;
		}

		public void setIs_my_turn_to_move(boolean is_my_turn_to_move) {
			this.is_my_turn_to_move = is_my_turn_to_move;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
	}
}
