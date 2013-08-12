package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.01.13
 * Time: 10:24
 */
public class ArticleItem extends BaseResponseItem<List<ArticleItem.Data>> {

/*
	"id": 225,
	"title": "Blueness",
	"create_date": 1369863194,
	"user_id": 5543,
	"username": "deepgreene",
	"category_name": "Strategy",
	"category_id": 14,
	"chess_title": "NM",
	"first_name": "Вовк",
	"last_name": "Андрій",
	"country_id": 3,
	"avatar_url": "https://s3.amazonaws.com/chess-7/images_users/avatars/deepgreene.gif",
	"image_url": "https://s3.amazonaws.com/chess-7/images_users/articles/blueness_origin.1.png",
	"is_thumb_in_content": true
*/

	public class Data {
		private long id;
		private String title;
		private long create_date;
		private long user_id;
		private String username;
		private String category_name;
		private long category_id;
		private String chess_title;
		private String first_name;
		private String last_name;
		private int country_id;
		private String avatar_url;
		private String image_url;
		private boolean is_thumb_in_content;

		public long getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public long getCreate_date() {
			return create_date;
		}

		public String getCategoryName() {
			return category_name;
		}

		public long getCategoryId() {
			return category_id;
		}

		public long getUserId() {
			return user_id;
		}

		public String getUsername() {
			return username;
		}

		public String getFirstName() {
			return getSafeValue(first_name);
		}

		public String getLastName() {
			return getSafeValue(last_name);
		}

		public String getChessTitle() {
			return getSafeValue(chess_title);
		}

		public int getCountryId() {
			return country_id;
		}

		public String getAvatar() {
			return getSafeValue(avatar_url);
		}

		public String getImageUrl() {
			return getSafeValue(image_url);
		}

		public boolean isIsThumbInContent() {
			return is_thumb_in_content;
		}
	}
}
