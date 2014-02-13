/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chess.backend.gcm;

/**
 * Helper class used to communicate with the demo server.
 */
public final class GcmHelper {

	/**
	 * Google API project id registered to use GCM.
	 */
	public static final String SENDER_ID = "27129061667";

	public static final int REQUEST_REGISTER = 11;
	public static final int REQUEST_UNREGISTER = 22;
	public static final String NOTIFICATION_YOUR_MOVE = "NOTIFICATION_YOUR_MOVE";
	public static final String NOTIFICATION_NEW_MESSAGE = "NOTIFICATION_NEW_MESSAGE";
	public static final String NOTIFICATION_NEW_CHAT_MESSAGE = "NOTIFICATION_NEW_CHAT_MESSAGE";
	public static final String NOTIFICATION_NEW_CHALLENGE = "NOTIFICATION_NEW_CHALLENGE";
	public static final String NOTIFICATION_NEW_FRIEND_REQUEST = "NOTIFICATION_NEW_FRIEND_REQUEST";
	public static final String NOTIFICATION_GAME_OVER = "NOTIFICATION_GAME_OVER";
	public static final String NOTIFICATION_MOVE_MADE = "NOTIFICATION_MOVE_MADE";

/*
	$payload = array(
        'type' => 'NOTIFICATION_NEW_CHAT_MESSAGE',
        'game_id' => $params['gameId'],
        'message' => $params['message'],
        'sender' => $params['username'],
        'created_at' => $params['createdAt'],
    );

    $payload = array(
        'type' => 'NOTIFICATION_NEW_CHALLENGE',
        'sender' => $params['fromUsername'],
        'challenge_id' => $params['challengeId'],
    );

    $payload = array(
        'type' => 'NOTIFICATION_NEW_FRIEND_REQUEST',
        'sender' => $params['sender'],
        'message' => $params['message'],
        'created_at' => $params['createdAt'],
    );

    $payload = array(
        'type' => 'NOTIFICATION_GAME_OVER',
        'game_id' => $params['gameId'],
        'message' => $params['message'],
*/
}
