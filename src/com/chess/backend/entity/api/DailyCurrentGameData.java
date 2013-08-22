package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 6:47
 */
public class DailyCurrentGameData extends DailyGameBaseData{
/*
	"id": 35000579,
	"game_type": 1,
	"time_remaining": 0,
	"timestamp": 1370386403,
	"name": "Let's Play!",
	"has_new_message": false,
	"game_score": 0,
	"white_username": "erik",
	"black_username": "zarko5",
	"user_to_move": 1,
	"white_rating": 1465,
	"black_rating": 1200,
	"is_rated": true,
	"days_per_move": 3,
	"draw_offered": 0,
	"encoded_move_string": "mC",
	"starting_fen_position": null,
	"move_list": "1. e4 ",
	"white_avatar": "//www.c.com/images_users/avatars/erik.1.gif",
	"black_avatar": "//www.c.com/images_users/avatars/zarko5.1.gif"
*/
	private boolean is_my_turn;
	/**
	 * draw_offered - 0 = no draw offered, 1 = white offered draw, 2 = black offered draw
	 */
	private int draw_offered;

	public boolean isMyTurn() {
		return is_my_turn;
	}

	public void setMyTurn(boolean is_my_turn) {
		this.is_my_turn = is_my_turn;
	}

	public int isDrawOffered() {
		return draw_offered;
	}

	public void setDrawOffered(int draw_offered) {
		this.draw_offered = draw_offered;
	}
}
