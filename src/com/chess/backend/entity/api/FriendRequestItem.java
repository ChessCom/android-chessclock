package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 31.01.14
 * Time: 6:35
 */
public class FriendRequestItem extends BaseResponseItem<List<FriendRequestItem.Data>> {

/*
	"data": [
        {
            "request_id": 1184,
            "user_id": 10071,
            "username": "marc",
            "message": null,
            "is_online": false,
            "avatar_url": "//s3.amazonaws.com/chess-dev/images_users/avatars/marc_small.1.png"
        }
    ]
*/

	public class Data {
		private long request_id;
		private long user_id;
		private String username;
		private String message;
		private boolean is_online;
		private String avatar_url;

		public long getRequestId() {
			return request_id;
		}

		public long getUserId() {
			return user_id;
		}

		public String getUsername() {
			return getSafeValue(username);
		}

		public String getMessage() {
			return getSafeValue(message);
		}

		public boolean isIsonline() {
			return is_online;
		}

		public String getAvatarUrl() {
			return getSafeValue(avatar_url);
		}
	}
}
