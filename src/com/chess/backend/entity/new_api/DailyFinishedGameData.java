package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 6:49
 */
public class DailyFinishedGameData extends DailyGameBaseData{
	/*
	"game_id": 35000494,
	"i_play_as": 2,
	"game_type_code": "chess",
	"opponent_username": "deepgreene",
	"opponent_rating": null,
	"time_remaining": 0,
	"fen": "rnbqkbnr/pppp1ppp/8/4p3/4PP2/8/PPPP2PP/RNBQKBNR b KQkq f3 1 2",
	"timestamp": 1339127284,
	"last_move_from_square": "f2",
	"last_move_to_square": "f4",
	"is_opponent_online": false,
	"game_score": 0
	 */
	private int game_score;

	public int getGameScore() {
		return game_score;
	}

	public void setGame_score(int game_score) {
		this.game_score = game_score;
	}
}
