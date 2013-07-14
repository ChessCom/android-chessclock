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
			  "title": "Death Match 15: GM Caruana vs Aveskulov - Part 1",
			  "video_id": 1655,
			  "description": "DEATH MATCH 15",
			  "view_count": 2,
			  "country_id": 2,
			  "avatar_url": "//s3.amazonaws.com/chess-7/images_users/avatars/ACEChess.1.gif",
			  "comment_count": 0,
			  "category_name": "Chess TV",
			  "category_id": 11,
			  "skill_level": "All",
			  "eco_name": null,
			  "username": "ACEChess",
			  "chess_title": null,
			  "first_name": "",
			  "last_name": "",
			  "minutes": 96,
			  "create_date": 1372834800,
			  "url": "http://s3.amazonaws.com/chess-7/videos/origin/Deathmatch-15-Part1.mp4",
			  "key_fen": ""
		*/
		private String title;
		private long video_id;
		private String description;
		private String category_name;
		private int category_id;
		private String skill_level;
		private String username;
		private String avatar_url;
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

		public String getTitle() {
			return getSafeValue(title);
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
			return create_date * 1000L;
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
			return getSafeValue(avatar_url);
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
