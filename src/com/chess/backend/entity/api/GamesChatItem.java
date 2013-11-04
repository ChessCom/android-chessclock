package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.11.13
 * Time: 18:10
 */
public class GamesChatItem extends BaseResponseItem<List<GamesChatItem.Data>> {

/*
   "data": [
        {
            "create_date": 1381448060,
            "user_id": 6382103,
            "message": "test"
        },
*/

	public class Data {
		private long create_date;
		private long user_id;
		private String message;

		public long getCreateDate() {
			return create_date;
		}

		public long getUserId() {
			return user_id;
		}

		public String getMessage() {
			return message;
		}
	}
}
