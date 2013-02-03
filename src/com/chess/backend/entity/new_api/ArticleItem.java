package com.chess.backend.entity.new_api;

import com.chess.backend.statics.StaticData;

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
		private String first_name;
		private String chess_title;
		private String last_name;

		public long getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public long getCreate_date() {
			return create_date;
		}

		public String getArticleCategory() {
			return article_category;
		}

		public long getArticleCategoryId() {
			return article_category_id;
		}

		public long getUserId() {
			return user_id;
		}

		public String getUsername() {
			return username;
		}

		public String getFirstName() {
			return first_name == null? StaticData.SYMBOL_EMPTY : first_name;
		}

		public String getChessTitle() {
			return chess_title == null? StaticData.SYMBOL_EMPTY : chess_title;
		}

		public String getLastName() {
			return last_name == null? StaticData.SYMBOL_EMPTY : last_name;
		}

	}
}
