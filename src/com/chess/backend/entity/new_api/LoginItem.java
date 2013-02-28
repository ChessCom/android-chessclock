package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger
 * Date: 21.12.12
 * Time: 6:30
 */
public class LoginItem extends BaseResponseItem<LoginItem.Data>{
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

		public int getPremiumStatus() {
			return premium_status;
		}

		public int getTacticsRating() {
			return tactics_rating;
		}

		public String getUsername() {
			return username;
		}
	}
}
