package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 6:49
 */
public class DailyFinishedGameData extends DailyGameBaseData{
	/*
	"id": 35002001,
	"i_play_as": 2,
	"game_type": 1,
	"fen": "rnbqkbnr/2pppppp/1p6/p7/8/5PPP/PPPPP3/RNBQKBNR w KQkq - 1 4",
	"timestamp": 1371204995,
	"name": "Let's Play!",
	"last_move_from_square": "a6",
	"last_move_to_square": "a5",
	"is_opponent_online": false,
	"has_new_message": true,
	"game_score": 1,
	"white_username": "erik",
	"black_username": "rest",
	"white_rating": 1073,
	"black_rating": 1073,
	"starting_fen_position": null,
	"move_list": "nvXPpxWOowOG",
	"result_message": "rest won on time",
	"opponent_avatar": "//s3.amazonaws.com/chess-7/images_users/avatars/erik_small.1.png"
	 */
	private int game_score;
	private String result_message;

	public int getGameScore() {
		return game_score;
	}

	public void setGameScore(int game_score) {
		this.game_score = game_score;
	}

	public String getResultMessage() {
		return result_message;
	}

	public void setResultMessage(String result_message) {
		this.result_message = result_message;
	}
}
