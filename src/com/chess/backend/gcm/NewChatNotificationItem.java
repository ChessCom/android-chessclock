package com.chess.backend.gcm;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.08.13
 * Time: 5:55
 */
public class NewChatNotificationItem extends FriendRequestItem{
/*

        'game_id' => $params['gameId'],
        'message' => $params['message'],
        'sender' => $params['username'],
        'created_at' => $params['createdAt'],
*/

	private long gameId;

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}
}
