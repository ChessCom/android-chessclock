package com.chess.backend.entity.new_api;

import com.chess.backend.RestHelper;
import com.chess.backend.statics.StaticData;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.12.12
 * Time: 20:12
 */
public class DailyGameByIdItem extends BaseResponseItem<DailyGameByIdItem.Data> {

/*
    "id": 35001998,
    "game_type": 1,
    "time_remaining": 0,
    "timestamp": 1367965030,
    "name": "Let's Play!",
    "has_new_message": false,
    "game_score": 0,
    "white_username": "rest",
    "black_username": "erik",
    "user_to_move": 1,
    "white_rating": 1200,
    "black_rating": 1201,
    "is_rated": true,
    "days_per_move": 3,
    "draw_offered": 0,
    "encoded_move_string": "",
    "move_list": ""
 */


	public static class Data {
		private long id;
		private int game_type;
		private long timestamp;
		private String name;
		private String white_username;
		private String black_username;
		private int black_user_country;
		private int white_user_country;
		private String starting_fen_position;
		private String move_list;
		private int user_to_move;
		private int white_rating;
		private int black_rating;
		private String encoded_move_string;
		private boolean has_new_message;
		private long time_remaining;
		private int game_score;
		private int draw_offered;
		private boolean is_rated;
		private int days_per_move;

		public long getGameId() {
			return id;
		}

		public void setGameId(long game_id) {
			this.id = game_id;
		}

		public int getGameType() {
			return game_type;
		}

		public void setGameType(int game_type) {
			this.game_type = game_type;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		public String getGameName() {
			return name;
		}

		public void setGameName(String game_name) {
			this.name = game_name;
		}

		public String getWhiteUsername() {
			return white_username;
		}

		public void setWhiteUsername(String white_username) {
			this.white_username = white_username;
		}

		public String getBlackUsername() {
			return black_username;
		}

		public void setBlackUsername(String black_username) {
			this.black_username = black_username;
		}

		public String getFenStartPosition() {
			return starting_fen_position == null? StaticData.SYMBOL_EMPTY : starting_fen_position; // could be null from server
		}

		public void setFenStartPosition(String starting_fen_position) {
			this.starting_fen_position = starting_fen_position;
		}

		public String getMoveList() {
			return move_list;
		}

		public void setMoveList(String move_list) {
			this.move_list = move_list;
		}

		public int getUserToMove() {
			return user_to_move;
		}

		public void setUserToMove(int user_to_move) {
			this.user_to_move = user_to_move;
		}

		public boolean isWhiteMove() {
			return user_to_move == RestHelper.P_WHITE;
		}

		public void setWhiteUserMove(boolean whiteMove) {
			user_to_move = whiteMove? RestHelper.P_WHITE: RestHelper.P_BLACK;
		}

		public int getWhiteRating() {
			return white_rating;
		}

		public void setWhiteRating(int white_rating) {
			this.white_rating = white_rating;
		}

		public int getBlackRating() {
			return black_rating;
		}

		public void setBlackRating(int black_rating) {
			this.black_rating = black_rating;
		}

		public String getEncodedMoveString() {
			return encoded_move_string;
		}

		public void setEncodedMoveString(String encoded_move_string) {
			this.encoded_move_string = encoded_move_string;
		}

		public boolean hasNewMessage() {
			return has_new_message;
		}

		public void setHasNewMessage(boolean has_new_message) {
			this.has_new_message = has_new_message;
		}

		public long getSecondsRemain() {
			return time_remaining;
		}

		public void setSecondsRemain(long seconds_remaining) {
			this.time_remaining = seconds_remaining;
		}

		public int getGameScore() {
			return game_score;
		}

		public void setGameScore(int game_result) {
			this.game_score = game_result;
		}

		public int isDrawOffered() {
			return draw_offered;
		}

		public void setDrawOffered(int draw_offered) {
			this.draw_offered = draw_offered;
		}

		public boolean isRated() {
			return is_rated;
		}

		public void setRated(boolean rated) {
			this.is_rated = rated;
		}

		public int getDaysPerMove() {
			return days_per_move;
		}

		public void setDaysPerMove(int days_per_move) {
			this.days_per_move = days_per_move;
		}

		public int getBlackUserCountry() {
			return black_user_country;
		}

		public int getWhiteUserCountry() {
			return white_user_country;
		}
	}
}
