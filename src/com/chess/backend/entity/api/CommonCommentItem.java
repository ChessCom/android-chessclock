package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.08.13
 * Time: 17:40
 */
public class CommonCommentItem extends BaseResponseItem<List<CommonCommentItem.Data>> {
/*
 "data": [
        {
            "id": 1396,
            "body": "<p>sdfd</p>",
            "create_date": 1346413641,
            "user_id": 16597,
            "username": "autotestsuser12",
            "user_first_name": "",
            "user_last_name": "",
            "avatar_url": "//www.c.com/images_users/avatars/autotestsuser12_s.6.jpg",
            "country_id": 2
        }
    ]
*/

	public class Data {
		private long id;
		private String body;
		private long create_date;
		private long user_id;
		private String username;
		private String user_first_name;
		private String user_last_name;
		private String avatar_url;
		private int country_id;
		/* Local additions */
		private long parentId;

		public long getId() {
			return id;
		}

		public String getBody() {
			return getSafeValue(body);
		}

		public long getCreateDate() {
			return create_date;
		}

		public long getUserId() {
			return user_id;
		}

		public String getUsername() {
			return getSafeValue(username);
		}

		public String getFirstName() {
			return getSafeValue(user_first_name);
		}

		public String getLastName() {
			return getSafeValue(user_last_name);
		}

		public String getAvatar() {
			return getSafeValue(avatar_url);
		}

		public int getCountryId() {
			return country_id;
		}

		public long getParentId() {
			return parentId;
		}

		public void setParentId(long parentId) {
			this.parentId = parentId;
		}
	}
}
