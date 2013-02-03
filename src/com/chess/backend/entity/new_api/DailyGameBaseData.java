package com.chess.backend.entity.new_api;

import com.chess.backend.statics.StaticData;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 6:31
 */
public class DailyGameBaseData {
/*
	"game_id": 35000494,
	"i_play_as": 2,
	"game_type": 1,
	"opponent_username": "deepgreene",
	"opponent_rating": null,
	"time_remaining": 0,
	"fen": "rnbqkbnr/pppp1ppp/8/4p3/4PP2/8/PPPP2PP/RNBQKBNR b KQkq f3 1 2",
	"timestamp": 1339127284,
	"last_move_from_square": "f2",
	"last_move_to_square": "f4",
	"is_opponent_online": false,
*/

	private long game_id;
	private int i_play_as;
	private int game_type;
	private String opponent_username;
	private int opponent_rating;
	private long time_remaining;
	private String fen;
	private long timestamp;
	private String last_move_from_square;
	private String last_move_to_square;
	private boolean is_opponent_online;

	public long getGameId() {
		return game_id;
	}

	public void setGameId(long game_id) {
		this.game_id = game_id;
	}

	public int getMyColor() {
		return i_play_as;
	}

	public void setMyColor(int i_play_as) {
		this.i_play_as = i_play_as;
	}

	public int getGameType() {
		return game_type;
	}

	public void setGameType(int game_type) {
		this.game_type = game_type;
	}

	public String getOpponentUsername() {
		return opponent_username;
	}

	public void setOpponentUsername(String opponent_username) {
		this.opponent_username = opponent_username;
	}

	public int getOpponentRating() {
		return opponent_rating;
	}

	public void setOpponentRating(int opponent_rating) {
		this.opponent_rating = opponent_rating;
	}

	public long getTimeRemaining() {
		return time_remaining;
	}

	public void setTimeRemaining(long time_remaining) {
		this.time_remaining = time_remaining;
	}

	public String getFen() {
		return fen;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getLastMoveFromSquare() {
		return last_move_from_square == null ? StaticData.SYMBOL_EMPTY : last_move_from_square;
	}

	public void setLastMoveFromSquare(String last_move_from_square) {
		this.last_move_from_square = last_move_from_square;
	}

	public String getLastMoveToSquare() {
		return last_move_to_square == null ? StaticData.SYMBOL_EMPTY : last_move_to_square;
	}

	public void setLastMoveToSquare(String last_move_to_square) {
		this.last_move_to_square = last_move_to_square;
	}

	public boolean isOpponentOnline() {
		return is_opponent_online;
	}

	public void setOpponentOnline(boolean is_opponent_online) {
		this.is_opponent_online = is_opponent_online;
	}
}
