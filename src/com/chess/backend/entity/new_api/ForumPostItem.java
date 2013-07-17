package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 17:29
 */
public class ForumPostItem extends BaseResponseItem<ForumPostItem.Data> {
/*
"data": {
    "comments_total_count": 35,
    "comments": [
      {
		"avatar_url": "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/erik.gif",
		"body": "comment 1",
		"comment_number": 1,
		"country_id": 225,
		"create_date": 1309917367,
		"premium_status": true,
		"username": "erik"
	  },
    ]
  }
*/

	public class Data {
		private int comments_total_count;
		private List<Post> comments;

		public int getCommentsCount() {
			return comments_total_count;
		}

		public List<Post> getPosts() {
			return comments;
		}
	}

	public static class Post {
/*
	"username": "deepgreene",
	"create_date": 1324703604,
	"body": "<p>Ok this is a normal sentence.&nbsp;</p>\r\n<ol>\r\n<li>List item</li>\r\n<li>List item</li>\r\n<li>Etc</li>\r\n</ol>\r\n<p><strong>Bold text can be&nbsp;<em>really</em>&nbsp;<span style=\"color: #ff0000;\">RED<span style=\"color: #000000;\">.</span></span></strong><span style=\"color: #ff0000;\"><span style=\"color: #000000;\">&nbsp;This is no longer bold.&nbsp;</span></span></p>"
*/

		private String username;
		private long create_date;
		private String body;
		private String avatar_url;
		private int premium_status;
		private int comment_number;
		private int country_id;
		private long topicId;
		private int page;

		public String getUsername() {
			return username;
		}

		public long getCreateDate() {
			return create_date;
		}

		public String getBody() {
			return body;
		}

		public long getTopicId() {
			return topicId;
		}

		public void setTopicId(long topicId) {
			this.topicId = topicId;
		}

		public String getAvatarUrl() {
			return avatar_url;
		}

		public int isPremiumStatus() {
			return premium_status;
		}

		public int getCountryId() {
			return country_id;
		}

		public int getCommentNumber() {
			return comment_number;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}
	}


}
