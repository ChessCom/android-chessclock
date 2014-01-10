package com.chess.backend.entity.api.daily_games;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 6:47
 */
public class DailyCurrentGameData extends DailyGameBaseData{
/*
	id: 35002080,
	i_play_as: 1,
	game_type_id: 1,
	time_remaining: 2386489,
	fen: "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 1 2",
	timestamp: 1385535830,
	name: "Let's Play!",
	last_move_from_square: "e7",
	last_move_to_square: "e5",
	is_draw_offer_pending: false,
	is_opponent_online: true,
	is_my_turn: true,
	has_new_message: false,
	white_username: "anotherroger",
	black_username: "alien_roger",
	white_rating: 1425,
	black_rating: 1247,
	is_rated: false,
	days_per_move: 1,
	draw_offered: 0,
	encoded_moves_piotr_string: "mC0K",
	starting_fen_position: null,
	move_list: "1. e4 e5 ",
	white_avatar: "//s3.amazonaws.com/chess-4/images_users/avatars/anotherroger_origin.2.jpeg",
	black_avatar: "//s3.amazonaws.com/chess-4/images_users/avatars/alien_roger_origin.6.jpeg",
	white_premium_status: 2,
	black_premium_status: 3,
	white_country_id: 2,
	black_country_id: 116,
	white_first_name: "",
	white_last_name: "",
	black_first_name: "Alien",
	black_last_name: "Roger",
	is_tournament_game: false
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
