package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.12.12
 * Time: 6:51
 */
public class LoginData {
/*
	"login_token": "6d69c2715c6c069fb8eef91d6e1b4c7c",
	"premium_status": 3,
	"user_id": 41,
	"tactics_rating": 1474,
	"username": "erik"
 */
	private String login_token;
	private int premium_status;
	private long user_id;
	private int tactics_rating;
	private String username;

	public String getLogin_token() {
		return login_token;
	}

	public void setLogin_token(String login_token) {
		this.login_token = login_token;
	}

	public int getPremium_status() {
		return premium_status;
	}

	public void setPremium_status(int premium_status) {
		this.premium_status = premium_status;
	}

	public long getUser_id() {
		return user_id;
	}

	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	public int getTactics_rating() {
		return tactics_rating;
	}

	public void setTactics_rating(int tactics_rating) {
		this.tactics_rating = tactics_rating;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
