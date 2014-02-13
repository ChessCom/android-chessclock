package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger
 * Date: 21.12.12
 * Time: 6:30
 */
public class LoginItem extends BaseResponseItem<LoginItem.Data> {
	/*
	{
		"status": "success",
		"data": {
			"login_token": "6d69c2715c6c069fb8eef91d6e1b4c7c",
			"premium_status": 3,
			"user_id": 41,
			"country_id": 2,
			"tactics_rating": 1474,
			"username": "erik",
			"avatar_url": "http://www.capi.com/images_users/avatars/erik.gif"
		}
	}
	 */

	public static class Data extends RegisterItem.Data {
		private int premium_status;
		private int tactics_rating;
		private String username;
		private String session_id;
		private String location;

		public int getPremiumStatus() {
			return premium_status;
		}

		public int getTacticsRating() {
			return tactics_rating;
		}

		public String getUsername() {
			return username;
		}

		/**
		 * As it might expire after 60 minutes we should check when it was received and perform re-login before using it again
		 *
		 * @return key to connect to live chess chess server
		 */
		public String getSessionId() {
			return session_id;
		}

		public String getLocation() {
			return location;
		}
	}
}
