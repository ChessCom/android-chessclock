package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 10:37
 */
public class DailyChallengeItem extends BaseResponseItem<List<DailyChallengeItem.Data>>{
/*
	"id": 368,
	"opponent_username": "zarko5",
	"opponent_rating": 1200,
	"opponent_win_count": 0,
	"opponent_loss_count": 0,
	"opponent_draw_count": 0,
	"opponent_avatar": "//s3.amazonaws.com/chess-7/images/noavatar_l.gif",
	"color": 3,
	"days_per_move": 3,
	"game_type_id": 1,
	"is_rated": true,
	"initial_setup_fen": null,
	"url": "view_game_seek.html?id=368"
*/


	public static class Data {
		private long id;
		private String opponent_username;
		private int opponent_rating;
		private int opponent_win_count;
		private int opponent_loss_count;
		private int opponent_draw_count;
		private String opponent_avatar;
		private int color;
		private int days_per_move;
		private int game_type_id;
		private boolean is_rated;
		private String initial_setup_fen;
		private String url;

		public long getGameId() {
			return id;
		}

		public String getOpponentUsername() {
			return opponent_username;
		}

		public int getOpponentRating() {
			return opponent_rating;
		}

		public int getOpponentWinCount() {
			return opponent_win_count;
		}

		public int getOpponentLossCount() {
			return opponent_loss_count;
		}

		public int getOpponentDrawCount() {
			return opponent_draw_count;
		}

		public int getColor() {
			return color;
		}

		public int getDaysPerMove() {
			return days_per_move;
		}

		public int getGameType() {
			return game_type_id;
		}

		public boolean isRated() {
			return is_rated;
		}

		public String getInitial_setup_fen() {
			return initial_setup_fen;
		}

		public String getUrl() {
			return url;
		}

		public String getOpponentAvatar() {
			return opponent_avatar;
		}
	}
}
