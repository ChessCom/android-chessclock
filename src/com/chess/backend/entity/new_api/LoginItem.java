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
			"tactics_rating": 1474,
			"username": "erik"
		}
	}
	 */

	public static class Data extends RegisterItem.Data {
		/*
			"login_token": "6d69c2715c6c069fb8eef91d6e1b4c7c",
			"premium_status": 3,
			"user_id": 41,
			"tactics_rating": 1474,
			"username": "erik"
		 */
//	private String login_token;
		private int premium_status;
		//	private long user_id;
		private int tactics_rating;
		private String username;

//	public String getLogin_token() {
//		return login_token;
//	}

		public int getPremium_status() {
			return premium_status;
		}

		public void setPremium_status(int premium_status) {
			this.premium_status = premium_status;
		}

//	public long getUser_id() {
//		return user_id;
//	}

		public int getTactics_rating() {
			return tactics_rating;
		}

		public String getUsername() {
			return username;
		}

	}
}
