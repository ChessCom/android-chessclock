package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger
 * Date: 21.12.12
 * Time: 6:30
 */
public class LoginItem extends BaseResponseItem{
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
	private LoginData data;

	public LoginData getData() {
		return data;
	}

	public void setData(LoginData data) {
		this.data = data;
	}
}
