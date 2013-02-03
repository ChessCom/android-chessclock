package com.chess.backend.entity.new_api;

import com.chess.backend.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.12.12
 * Time: 12:34
 */
public class FriendsItem extends BaseResponseItem<List<FriendsItem.Data>> {
/*
    "status": "success",
    "count": 4,
    "data": [
        {
          	"user_id": 31,
            "username": "jay",
            "is_online": true,
            "country_id": 2,
            "location": "Israel",
            "avatar_url": "http://www.capi.com/images_users/avatars/jay.gif"
        }...
    ]
*/

	public class Data {
		private long user_id;
		private String username;
		private boolean is_online;
		private long country_id;
		private String location;
		private String avatar_url;

		public long getUserId() {
			return user_id;
		}

		public void setUser_id(long user_id) {
			this.user_id = user_id;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public boolean isOnline() {
			return is_online;
		}

		public void setOnline(boolean is_online) {
			this.is_online = is_online;
		}

		public long getCountryId() {
			return country_id;
		}

		public String getLocation() {
			return location == null? StaticData.SYMBOL_EMPTY: location;
		}

		public String getAvatarUrl() {
			return avatar_url;
		}
	}
}
