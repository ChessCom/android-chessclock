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
	"id": 224,
	"title": "Testing thing",
	"create_date": 1369863079,
	"body": "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irur...",
	"user_id": 5543,
	"username": "deepgreene",
	"category_name": "For Beginners",
	"category_id": 11,
	"chess_title": "NM",
	"first_name": "Вовк",
	"last_name": "Андрій",
	"country_id": 3,
	"avatar_url": "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/deepgreene.gif",
	"image_url": "//d1lalstwiwz2br.cloudfront.net/images_users/articles/testing-thing_origin.1.png",
	"is_thumb_in_content": true
*/

	public static class Data {
		private long id;
		private String title;
		private long create_date;
		private String body;
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
		private String url;
		private boolean is_thumb_in_content;

		public long getId() {
			return id;
		}

		public String getTitle() {
			return getSafeValue(title);
		}

		public long getCreateDate() {
			return create_date;
		}

		public String getBody() {
			return getSafeValue(body);
		}

		public String getCategoryName() {
			return getSafeValue(category_name);
		}

		public long getCategoryId() {
			return category_id;
		}

		public long getUserId() {
			return user_id;
		}

		public String getUsername() {
			return getSafeValue(username);
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

		public String getUrl() {
			return getSafeValue(url);
		}

		public boolean isIsThumbInContent() {
			return is_thumb_in_content;
		}
	}
}
