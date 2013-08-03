package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 01.08.13
 * Time: 21:36
 */
public class MessagesItem extends BaseResponseItem<List<MessagesItem.Data>> {
/*
      "id": 5,
      "created_at": 1375361796,
      "content": "test from sandboxsdasd",
      "sender_id": 11438,
      "sender_username": "alien_roger",
      "sender_avatar_url": "//s3.amazonaws.com/chess-7/images_users/avatars/alien_roger_small.2.png",
      "sender_is_online": false
*/

	public class Data {
		private long id;
		private long created_at;
		private String content;
		private long sender_id;
		private String sender_username;
		private String sender_avatar_url;
		private boolean sender_is_online;
		/* Local addition */
		private String user;
		private long conversationId;

		public long getId() {
			return id;
		}

		public long getCreatedAt() {
			return created_at;
		}

		public String getContent() {
			return content;
		}

		public long getSenderId() {
			return sender_id;
		}

		public String getSenderUsername() {
			return sender_username;
		}

		public String getSenderAvatarUrl() {
			return sender_avatar_url;
		}

		public boolean isSenderIsOnline() {
			return sender_is_online;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public long getConversationId() {
			return conversationId;
		}

		public void setConversationId(long conversationId) {
			this.conversationId = conversationId;
		}
	}

}
