package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.12.12
 * Time: 15:18
 */
public class VideoItem extends BaseResponseItem<List<VideoItem.Data>> {

	public static class Data {
		/*
			"title": "test video by zarko",
			"video_id": 1649,
			"description": "test... hola :D",
			"view_count": 8,
			"country_id": 225,
			"user_avatar": "https:\/\/s3.amazonaws.com\/chess-7\/images_users\/avatars\/erikwwww.1.gif",
			"comment_count": 0,
			"category_name": "Strategy",
			"category_id": 8,
			"skill_level": "Intermediate",
			"username": "erikwwww",
			"first_name": "",
			"last_name": "",
			"minutes": 5,
			"create_date": 1368428400,
			"url": "https:\/\/s3.amazonaws.com\/chess-7\/videos\/android\/test-video-by-zarko.mp4",
			"key_fen": ""
		*/
		private String name;
		private long video_id;
		private String description;
		private String category_name;
		private int category_id;
		private String skill_level;
		private String username;
		private String user_avatar;
		private String first_name;
		private String chess_title;
		private String last_name;
		private int minutes;
		private int view_count;
		private int country_id;
		private int comment_count;
		private long create_date;
		private String url;
		private String key_fen;

		public String getName() {
			return getSafeValue(name);
		}

		public String getDescription() {
			return getSafeValue(description);
		}

		public String getSkillLevel() {
			return getSafeValue(skill_level);
		}

		public String getUsername() {
			return getSafeValue(username);
		}

		public String getFirstName() {
			return getSafeValue(first_name);
		}

		public String getChessTitle() {
			return getSafeValue(chess_title);
		}

		public String getLastName() {
			return getSafeValue(last_name);
		}

		public int getMinutes() {
			return minutes;
		}

		public long getCreateDate() {
			return create_date;
		}

		public String getUrl() {
			return getSafeValue(url);
		}

		public String getKeyFen() {
			return getSafeValue(key_fen);
		}

		public long getVideoId() {
			return video_id;
		}

		public String getCategoryName() {
			return getSafeValue(category_name);
		}

		public int getCategoryId() {
			return category_id;
		}

		public String getUserAvatar() {
			return getSafeValue(user_avatar);
		}

		public int getViewCount() {
			return view_count;
		}

		public int getCountryId() {
			return country_id;
		}

		public int getCommentCount() {
			return comment_count;
		}
	}
}
