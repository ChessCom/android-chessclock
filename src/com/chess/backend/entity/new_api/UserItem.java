package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.12.12
 * Time: 12:42
 */
public class UserItem extends BaseResponseItem<UserItem.Data> {
/*
    "status": "success",
    "data": {
        "id": 23900,
        "country_id": 2,
        "avatar": "//www.c.com/images/noavatar_l.gif"
    }
*/

/*
	"id": 31,
	"username": "jay",
	"avatar": "//www.c.com/images/noavatar_l.gif",
	"country_id": 2,
	"last_login_date": "2013-03-08T05:52:08-0800",
	"points": 549,
	"chess_title": "GM",
	"status": "ciaooooooo"

	*/

	public static class Data {
		private long id;
		private String username;
		private String avatar;
		private long country_id;
		private String last_login_date;
		private int points;
		private String chess_title;
		private String status;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getAvatar() {
			return avatar;
		}

		public void setAvatar(String avatar) {
			this.avatar = avatar;
		}

		public long getCountry_id() {
			return country_id;
		}

		public void setCountry_id(long country_id) {
			this.country_id = country_id;
		}

		public String getLast_login_date() {
			return last_login_date;
		}

		public void setLast_login_date(String last_login_date) {
			this.last_login_date = last_login_date;
		}

		public int getPoints() {
			return points;
		}

		public void setPoints(int points) {
			this.points = points;
		}

		public String getChess_title() {
			return chess_title;
		}

		public void setChess_title(String chess_title) {
			this.chess_title = chess_title;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}

}
