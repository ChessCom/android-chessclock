package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.01.13
 * Time: 10:24
 */
public class ArticleItem extends BaseResponseItem<List<ArticleItem.Data>> {

/*
      "id": 213,
      "title": "YT embed 2012-11-08 1",
      "create_date": 1352407220,
      "article_category": "For Beginners",
      "article_category_id": 11,
      "user_id": 5543,
      "username": "deepgreene"
*/

	public class Data {
		private long id;
		private String title;
		private long create_date;
		private String article_category;
		private long article_category_id;
		private long user_id;
		private String username;
		private String first_name = "Georgy";
		private String chess_title = "GM";
		private String last_name = "Kaidanov";

		public long getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public long getCreate_date() {
			return create_date;
		}

		public String getArticle_category() {
			return article_category;
		}

		public long getArticle_category_id() {
			return article_category_id;
		}

		public long getUser_id() {
			return user_id;
		}

		public String getUsername() {
			return username;
		}

		public String getFirst_name() {
			return first_name;
		}

		public String getChess_title() {
			return chess_title;
		}

		public String getLast_name() {
			return last_name;
		}

	}
}
