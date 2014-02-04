package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.09.13
 * Time: 7:45
 */
public class VideoSingleItem extends BaseResponseItem<VideoSingleItem.Data> {

/*
	"video_id": 979,
	"title": "Everything You Need to Know 1: Start Playing Chess",
	"description": "The first video in the \"Everything You Need to Know\" video series is designed for complete newcomers to the game of chess! Learn how the pieces move, the values of each chessman, the goal of the game, and even enjoy a brief history lesson in this video lecture -- all in roughly 20 minutes! Your journey towards mastering the Game of Kings starts right here!",
	"category_name": "Rules &amp; Basics",
	"category_id": 1,
	"view_count": 63835,
	"country_id": 2,
	"avatar_url": "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/ACEChess_small.2.jpeg",
	"comment_count": 48,
	"skill_level": "Beginner",
	"eco_name": null,
	"username": "DanielRensch",
	"chess_title": "IM",
	"first_name": "Daniel",
	"last_name": "Rensch",
	"minutes": 22,
	"create_date": 1334265564,
	"url": "http://d1qwhygwfa4r9i.cloudfront.net/videos/origin/everything-you-need-to-know-start-playing-chess.mp4",
	"key_fen": "8/4k3/8/2R5/6P1/3K4/8/6N1 w - - 0 1"
    "web_url": "/video/player/amazing-too"
*/

	public static class Data {
		private long video_id;
		private String title;
		private String description;
		private String category_name;
		private int category_id;
		private long view_count;
		private int country_id;
		private String avatar_url;
		private int comment_count;
		private String skill_level;
		private String eco_name;
		private String username;
		private String chess_title;
		private String first_name;
		private String last_name;
		private int minutes;
		private long create_date;
		private String url;
		private String key_fen;
		private String web_url;

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

		public String getEcoName() {
			return eco_name;
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

		public String getWebUrl() {
			return getSafeValue(web_url);
		}

		public void setWebUrl(String web_url) {
			this.web_url = web_url;
		}

		public long getViewCount() {
			return view_count;
		}

		public int getCountryId() {
			return country_id;
		}

		public int getCommentCount() {
			return comment_count;
		}

		public void setVideoId(long video_id) {
			this.video_id = video_id;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setCategoryName(String category_name) {
			this.category_name = category_name;
		}

		public void setCategoryId(int category_id) {
			this.category_id = category_id;
		}

		public void setViewCount(long view_count) {
			this.view_count = view_count;
		}

		public void setCountryId(int country_id) {
			this.country_id = country_id;
		}

		public void setAvatarUrl(String avatar_url) {
			this.avatar_url = avatar_url;
		}

		public void setCommentCount(int comment_count) {
			this.comment_count = comment_count;
		}

		public void setSkillLevel(String skill_level) {
			this.skill_level = skill_level;
		}

		public void setEcoName(String eco_name) {
			this.eco_name = eco_name;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public void setChessTitle(String chess_title) {
			this.chess_title = chess_title;
		}

		public void setFirstName(String first_name) {
			this.first_name = first_name;
		}

		public void setLastName(String last_name) {
			this.last_name = last_name;
		}

		public void setMinutes(int minutes) {
			this.minutes = minutes;
		}

		public void setCreateDate(long create_date) {
			this.create_date = create_date;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void setKeyFen(String key_fen) {
			this.key_fen = key_fen;
		}
	}
}
