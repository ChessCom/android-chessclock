package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.08.13
 * Time: 7:42
 */
public class ArticleDetailsItem extends BaseResponseItem<ArticleDetailsItem.Data> {
/*
    "id": 224,
    "title": "Testing thing",
    "create_date": 1369863079,
    "body": "<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. yeah!</p>",
    "url": "testing-thing",
    "user_id": 5543,
    "username": "deepgreene",
    "category_name": "For Beginners",
    "category_id": 11,
    "chess_title": "NM",
    "first_name": "Вовк",
    "last_name": "Андрій",
    "country_id": 3,
    "avatar_url": "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/deepgreene.gif",
    "view_count": 14,
    "comment_count": 3,
    "image_url": "//d1lalstwiwz2br.cloudfront.net/images_users/articles/testing-thing_origin.1.png",
    "is_thumb_in_content": true
*/

	public class Data extends ArticleItem.Data{
		private int view_count;
		private int comment_count;

		public int getViewCount() {
			return view_count;
		}

		public int getCommentCount() {
			return comment_count;
		}
	}
}
