package com.chess.backend.entity.api.daily_games;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 6:49
 */
public class DailyFinishedGameData extends DailyGameBaseData {
	/*
	"id": 35000778,
	"i_play_as": 1,
	"game_type_id": 1,
	"fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
	"timestamp": 1357166897,
	"name": "marcos",
	"last_move_from_square": null,
	"last_move_to_square": null,
	"is_opponent_online": false,
	"has_new_message": false,
	"game_score": 0,
	"white_username": "erik",
	"black_username": "deepgreene",
	"white_rating": 1471,
	"black_rating": 1749,
	"is_rated": true,
	"encoded_moves_piotr_string": "",
	"starting_fen_position": null,
	"move_list": "",
	"result_message": "deepgreene won on time",
	"white_avatar": "//s3.amazonaws.com/chess-7/images_users/avatars/erik_origin.5.png",
	"black_avatar": "//s3.amazonaws.com/chess-7/images_users/avatars/deepgreene.gif",
	"white_premium_status": 3,
	"black_premium_status": 3,
	"white_country_id": 2,
	"black_country_id": 3
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
