package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.07.13
 * Time: 16:26
 */
public class ForumTopicItem extends BaseResponseItem<List<ForumTopicItem.Data>>{
/*
"data": [
        {
            "id": "481",
            "category_id": 10,
            "subject": "Isn't Chess the Best??",
            "url": "general/isnt-chess-the-best",
            "topic_username": "Lucky",
            "post_count": "35",
            "last_post_username": "deepgreene",
            "last_post_create_date": 1355272001
        },
]
*/

	public class Data {
		private int id;
		private int category_id;
		private String category_name;
		private String subject;
		private String url;
		private String topic_username;
		private String last_post_username;
		private int post_count;
		private long last_post_create_date;

		public int getId() {
			return id;
		}

		public String getSubject() {
			return subject;
		}

		public String getUrl() {
			return url;
		}

		public String getTopicUsername() {
			return topic_username;
		}

		public String getLastPostUsername() {
			return last_post_username;
		}

		public int getPostCount() {
			return post_count;
		}

		public long getLastPostDate() {
			return last_post_create_date;
		}

		public int getCategoryId() {
			return category_id;
		}

		public String getCategoryName() {
			return category_name;
		}

		public void setCategoryName(String category_name) {
			this.category_name = category_name;
		}
	}

}
