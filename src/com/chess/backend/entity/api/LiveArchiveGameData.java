package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.09.13
 * Time: 6:11
 */
public class LiveArchiveGameData extends DailyGameBaseData {
/*
 	"id": 35000570,
	"i_play_as": 1,
	"game_type": 1,
	"fen": "rnbqkbnr/pppppppp/8/8/8/1P6/P1PPPPPP/RNBQKBNR b KQkq - 1 1",
	"timestamp": 1362157370,
	"name": "Let's Play!",
	"last_move_from_square": "b2",
	"last_move_to_square": "b3",
	"is_opponent_online": false,
	"has_new_message": false,
	"game_score": 2,
	"white_username": "erik",
	"black_username": "user0704170548328",
	"white_rating": 1498,
	"black_rating": 1498,
	"is_rated": true,
	"encoded_moves_piotr_string": "gvZRkA!Tbs2UmC92lB8!fmYQeg5OdkOYfe6Ecu98ad76jzEvmv0KBJTEJQXQdREueuYSRdSBky6YAIWOvm85mf3VdcUMueY6iq6XfAXYsm!9mBKBcd56dtYKtv6YyG4WvLK8ow1TCK8UAtUNKT2?GPN1P5",
	"starting_fen_position": null,
	"move_list": "jr",
	"result_message": "Game drawn by agreement",
	"white_avatar": "//www.c.com/images/noavatar_l.gif",
	"black_avatar": "//www.c.com/images/noavatar_l.gif",
	"white_premium_status": 3,
	"black_premium_status": 1,
	"white_country_id": 2,
	"black_country_id": 2
*/

	private int game_score;
	private String result_message;
	private String game_time_class;

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

	public String getGameTimeClass() {
		return game_time_class;
	}

	public void setGameTimeClass(String game_time_class) {
		this.game_time_class = game_time_class;
	}
}
