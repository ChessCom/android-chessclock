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
        "username": "Lucky",
        "create_date": 1309917367,
        "body": "<p>It totally is.&nbsp;</p>\r\n<p>Editing to test a thread can be edited by staff whilst its locked</p>"
      },

      {
        "username": "deepgreene",
        "create_date": 1324663848,
        "body": "<p><span style=\"color: #800000;\"></span>Blah blah blah&nbsp;<strong>Bold</strong>&nbsp;<em>italics&nbsp;</em></p>\r\n<p><span style=\"color: #ffff00;\">Yellow!!!!!</span></p>\r\n<p><span style=\"color: #ffff00;\"></span></p>\r\n<hr />\r\n<p><span style=\"color: #ffff00;\"><br /></span></p>"
      },
      {
        "username": "kohai",
        "create_date": 1324664523,
        "body": "deepgreene wrote:\r\n          Blah blah blah Bold italics \r\nYellow!!!!!\r\n\r\n\r\n\r\n        test"
      },
      {
        "username": "deepgreene",
        "create_date": 1324703604,
        "body": "<p>Ok this is a normal sentence.&nbsp;</p>\r\n<ol>\r\n<li>List item</li>\r\n<li>List item</li>\r\n<li>Etc</li>\r\n</ol>\r\n<p><strong>Bold text can be&nbsp;<em>really</em>&nbsp;<span style=\"color: #ff0000;\">RED<span style=\"color: #000000;\">.</span></span></strong><span style=\"color: #ff0000;\"><span style=\"color: #000000;\">&nbsp;This is no longer bold.&nbsp;</span></span></p>"
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
		private long topicId;

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
	}


}
