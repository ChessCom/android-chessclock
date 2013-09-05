package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.09.13
 * Time: 6:55
 */
public class FriendRequestResultItem extends BaseResponseItem<FriendRequestResultItem.Data> {

/*
	"user_id": 41,
	"username": "erik",
	"message": "Friend request from erik accepted."
*/

	public class Data {
		private long user_id;
		private String username;
		private String message;

		public long getUserId() {
			return user_id;
		}

		public String getUsername() {
			return username;
		}

		public String getMessage() {
			return message;
		}
	}
}
