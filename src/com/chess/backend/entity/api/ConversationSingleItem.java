package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.08.13
 * Time: 11:56
 */
public class ConversationSingleItem extends BaseResponseItem<ConversationSingleItem.Data> {
/*
	"data": {
        "id": 1,
        "other_user_id": 41,
        "other_user_username": "erik",
        "other_user_is_online": false,
        "other_user_avatar_url": "//s3.amazonaws.com/chess-7/images_users/avatars/erik_origin.5.png",
        "new_messages_count": 0,
        "last_message_id": 16,
        "last_message_sender_username": "rest",
        "last_message_created_at": 1375505680,
        "last_message_content": "crating"
    }
*/

	public class Data extends ConversationItem.Data {
	}
}
