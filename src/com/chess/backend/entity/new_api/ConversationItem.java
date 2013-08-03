package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.07.13
 * Time: 20:43
 */
public class ConversationItem extends BaseResponseItem<List<ConversationItem.Data>> {
/*
      "id": 2,
      "other_user_id": 11436,
      "other_user_username": "rest",
      "other_user_is_online": false,
      "other_user_avatar_url": "//s3.amazonaws.com/chess-7/images_users/avatars/rest_origin.17.png",
      "new_messages_count": 1,
      "last_message_id": 1,
      "last_message_sender_username": "rest",
      "last_message_created_at": 1375191708,
      "last_message_content": "test from sandbox"
*/

	public static class Data {
		private long id;
		private long other_user_id;
		private String other_user_username;
		private boolean other_user_is_online;
		private String other_user_avatar_url;
		private int new_messages_count;
		private long last_message_id;
		private String last_message_sender_username;
		private long last_message_created_at;
		private String last_message_content;
		/* local addition*/
		private String user;

		public long getId() {
			return id;
		}

		public long getOtherUserId() {
			return other_user_id;
		}

		public String getOtherUserUsername() {
			return other_user_username;
		}

		public boolean isOtherUserIsOnline() {
			return other_user_is_online;
		}

		public String getOtherUserAvatarUrl() {
			return other_user_avatar_url;
		}

		public int getNewMessagesCount() {
			return new_messages_count;
		}

		public long getLastMessageId() {
			return last_message_id;
		}

		public String getLastMessageSenderUsername() {
			return last_message_sender_username;
		}

		public long getLastMessageCreatedAt() {
			return last_message_created_at;
		}

		public String getLastMessageContent() {
			return last_message_content;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

	}
}
