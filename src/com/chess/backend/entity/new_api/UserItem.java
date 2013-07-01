package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.12.12
 * Time: 12:42
 */
public class UserItem extends BaseResponseItem<UserItem.Data> {
/*
        "id": 11436,
        "country_id": 2,
        "avatar": "//s3.amazonaws.com/chess-7/images_users/avatars/rest_origin.12.jpeg",
        "username": "rest",
        "last_login_date": 1371214315,
        "points": 0,
        "chess_title": null,
        "status": "ciaaoooo",
        "first_name": "",
        "last_name": "",
        "location": "",
        "country_name": "United States"
	*/

	public static class Data {
		private long id;
		private String username;
		private String avatar;
		private int country_id;
		private int premium_status;
		private String country_name;
		private long last_login_date;
		private int points;
		private String chess_title;
		private String status;
		private String first_name;
		private String last_name;
		private String location;

		public long getId() {
			return id;
		}

		public String getUsername() {
			return username;
		}

		public String getAvatar() {
			return avatar;
		}

		public int getCountryId() {
			return country_id;
		}

		public int getPremiumStatus() {
			return premium_status;
		}

		public String getCountryName() {
			return country_name;
		}

		public long getLastLoginDate() {
			return last_login_date;
		}

		public int getPoints() {
			return points;
		}

		public String getChessTitle() {
			return chess_title;
		}

		public String getStatus() {
			return status;
		}

		public String getFirstName() {
			return first_name;
		}

		public String getLastName() {
			return last_name;
		}

		public String getLocation() {
			return location;
		}
	}

}
