package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger
 * Date: 21.12.12
 * Time: 6:30
 */
public class RegisterItem extends BaseResponseItem<RegisterItem.Data>{
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

	public static class Data {
		/*
			"user_id": 23900,
			"login_token": "058dfd8f10a7961e10112de0e3eaf779"
		*/
		private String login_token;
		private long user_id;

		public String getLogin_token() {
			return login_token;
		}

		public void setLogin_token(String login_token) {
			this.login_token = login_token;
		}

		public long getUser_id() {
			return user_id;
		}

		public void setUser_id(long user_id) {
			this.user_id = user_id;
		}

	}
}
