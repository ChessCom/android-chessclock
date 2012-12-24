package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 6:47
 */
public class DailyCurrentGameData extends DailyGameBaseData{
	/*
	"game_id": 35000530,
	"i_play_as": 1,
	"game_type_code": "chess",
	"opponent_username": "erikwwww",
	"opponent_rating": "1200",
	"time_remaining": 0,
	"fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
	"timestamp": 1339849800,
	"last_move_from_square": null,
	"last_move_to_square": null,
	"is_draw_offer_pending": false,
	"is_opponent_online": false,
	"is_my_turn": false,
	"has_new_message": false
	 */
	private boolean is_my_turn;
	private boolean has_new_message;
	private boolean is_draw_offer_pending;

	public boolean isMyTurn() {
		return is_my_turn;
	}

	public void setMyTurn(boolean is_my_turn) {
		this.is_my_turn = is_my_turn;
	}

	public boolean hasNewMessage() {
		return has_new_message;
	}

	public void setHasNewMessage(boolean has_new_message) {
		this.has_new_message = has_new_message;
	}

	public boolean isDrawOfferPending() {
		return is_draw_offer_pending;
	}

	public void setDrawOfferPending(boolean is_draw_offer_pending) {
		this.is_draw_offer_pending = is_draw_offer_pending;
	}
}
