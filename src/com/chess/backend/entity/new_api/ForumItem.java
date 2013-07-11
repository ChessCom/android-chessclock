package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.07.13
 * Time: 16:26
 */
public class ForumItem extends BaseResponseItem<List<ForumItem.Data>>{
/*
"data": [
{
	"category": {
		"forum_category_id": 10,
		"category": "General Chess Discussion",
		"url": "general",
		"last_post_username": "autotestsuser10"
	},
	"topics": [
		{
			"forum_topic_id": 481,
			"subject": "Isn't Chess the Best??",
			"url": "general/isnt-chess-the-best",
			"topic_username": "Lucky",
			"post_count": 34,
			"last_post_username": "dmitry123",
			"last_post_create_date": 1347099488
		 },
		 {
			 "forum_topic_id": 1976,
			 "subject": "asdasda",
			 "url": "general/asdasda",
			 "topic_username": "autotestsuser10",
			 "post_count": 2,
			 "last_post_username": "autotestsuser10",
			 "last_post_create_date": 1347276604
		 },
		 {
			 "forum_topic_id": 1763,
			 "subject": "test",
			 "url": "general/test5",
			 "topic_username": "dmitry_admin",
			 "post_count": 2,
			 "last_post_username": "dmitry_admin",
			 "last_post_create_date": 1347119985
		 }
	 ]
 }
]
*/

	public class Data {
		private Category category;
		private List<Topic> topics;

		public Category getCategory() {
			return category;
		}

		public List<Topic> getTopics() {
			return topics;
		}
	}

	public static class Category{
/*
	"forum_category_id": 10,
	"category": "General Chess Discussion",
	"url": "general",
	"last_post_username": "autotestsuser10"
*/
		private int forum_category_id;
		private String category;
		private String url;
		private String last_post_username;

		private int getCategoryId() {
			return forum_category_id;
		}

		private String getCategory() {
			return category;
		}

		private String getUrl() {
			return url;
		}

		private String getLastPostUsername() {
			return last_post_username;
		}
	}

	public static class Topic {
/*
	"forum_topic_id": 481,
	"subject": "Isn't Chess the Best??",
	"url": "general/isnt-chess-the-best",
	"topic_username": "Lucky",
	"post_count": 34,
	"last_post_username": "dmitry123",
	"last_post_create_date": 1347099488
*/
		private int forum_topic_id;
		private String subject;
		private int category_id;
		private String url;
		private String topic_username;
		private String last_post_username;
		private int post_count;
		private long last_post_create_date;

		public int getId() {
			return forum_topic_id;
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
	}
}
