package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.12.12
 * Time: 12:42
 */
public class UserItem extends BaseResponseItem<UserItem.Data> {
/*
 		"id": 31,
        "country_id": 2,
        "avatar_url": "//www.c.com/images/noavatar_l.gif",
        "username": "jay",
        "last_login_date": 1370443587,
        "points": 549,
        "chess_title": "GM",
        "status": "ciaooooooo",
        "first_name": "Jay",
        "last_name": "S",
        "location": "Israel",
        "country_name": "United States",
        "premium_status": 3,
        "member_since": 1178526885,
        "date_of_birth": 241167600,
        "about": "I'm kinda like Tom fr"
	*/

	public static class Data {
		private long id;
		private String username;
		private String avatar_url;
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
		private long member_since;
		private long date_of_birth;
		private String about;
		private String session_id;

		public long getId() {
			return id;
		}

		public String getUsername() {
			return username;
		}

		public String getAvatar() {
			return avatar_url;
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

		public long getMemberSince() {
			return member_since;
		}

		public long getDateOfBirth() {
			return date_of_birth;
		}

		public String getAbout() {
			return about;
		}

		public String getSessionId() {
			return getSafeValue(session_id);
		}
	}

}
