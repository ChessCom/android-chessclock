package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 6:49
 */
public class DailyFinishedGameData extends DailyGameBaseData{
	/*
	"id": 35000320,
	"i_play_as": 2,
	"game_type": 1,
	"opponent_username": "privatepirate",
	"opponent_rating": null,
	"time_remaining": 0,
	"fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
	"timestamp": 1307403901,
	"last_move_from_square": null,
	"last_move_to_square": null,
	"is_opponent_online": false,
	"game_score": 1
	 */
	private int game_score;

	public int getGameScore() {
		return game_score;
	}

	public void setGame_score(int game_score) {
		this.game_score = game_score;
	}
}
