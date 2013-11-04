package com.chess.backend.entity.api;

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
  			"user_id": 23900,
			"login_token": "058dfd8f10a7961e10112de0e3eaf779",
			"country_id": 2,
        	"avatar_url": "http://www.capi.com/images_users/avatars/test.gif"
		}
	}
*/

	public static class Data {
		private long id;
		private String login_token;
		private int country_id;
		private String avatar_url;

		public String getLoginToken() {
			return login_token;
		}

		public long getUserId() {
			return id;
		}

		public int getCountryId() {
			return country_id;
		}

		public String getAvatarUrl() {
			return avatar_url;
		}
	}
}
