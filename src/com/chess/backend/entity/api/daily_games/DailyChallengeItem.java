package com.chess.backend.entity.api.daily_games;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.backend.entity.api.BaseResponseItem;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 10:37
 */
public class DailyChallengeItem extends BaseResponseItem<List<DailyChallengeItem.Data>> {
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
	"is_opponent_online": true,

	id: 46875376,
	opponent_username: "imaduck29384756",
	opponent_rating: 1053,
	opponent_win_count: 40,
	opponent_loss_count: 78,
	opponent_draw_count: 1,
	opponent_avatar: null,
	color: 1,
	days_per_move: 2,
	game_type_id: 1,
	is_rated: true,
	initial_setup_fen: null,
	url: "view_game_seek.html?id=46875376",
	is_opponent_online: false
*/


	public static class Data implements Parcelable {
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
		private boolean is_opponent_online;
		private String initial_setup_fen;

		public long getGameId() {
			return id;
		}

		public String getOpponentUsername() {
			return getSafeValue(opponent_username);
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

		public int getGameTypeId() {
			return game_type_id;
		}

		public boolean isRated() {
			return is_rated;
		}

		public String getInitialSetupFen() {
			return getSafeValue(initial_setup_fen);
		}

		public String getOpponentAvatar() {
			return getSafeValue(opponent_avatar);
		}

		public boolean isOpponentOnline() {
			return is_opponent_online;
		}

		protected Data(Parcel in) {
			id = in.readLong();
			opponent_username = in.readString();
			opponent_rating = in.readInt();
			opponent_win_count = in.readInt();
			opponent_loss_count = in.readInt();
			opponent_draw_count = in.readInt();
			opponent_avatar = in.readString();
			color = in.readInt();
			days_per_move = in.readInt();
			game_type_id = in.readInt();
			is_rated = in.readByte() != 0x00;
			is_opponent_online = in.readByte() != 0x00;
			initial_setup_fen = in.readString();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(id);
			dest.writeString(opponent_username);
			dest.writeInt(opponent_rating);
			dest.writeInt(opponent_win_count);
			dest.writeInt(opponent_loss_count);
			dest.writeInt(opponent_draw_count);
			dest.writeString(opponent_avatar);
			dest.writeInt(color);
			dest.writeInt(days_per_move);
			dest.writeInt(game_type_id);
			dest.writeByte((byte) (is_rated ? 0x01 : 0x00));
			dest.writeByte((byte) (is_opponent_online ? 0x01 : 0x00));
			dest.writeString(initial_setup_fen);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<Data> CREATOR = new Parcelable.Creator<Data>() {
			@Override
			public Data createFromParcel(Parcel in) {
				return new Data(in);
			}

			@Override
			public Data[] newArray(int size) {
				return new Data[size];
			}
		};
	}
}
