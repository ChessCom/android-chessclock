package com.chess.backend.entity.api.daily_games;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.12.12
 * Time: 12:10
 */
public class DailySeekItem extends BaseResponseItem<DailySeekItem.Data> {
/*
    "status": "success",
    "data": {
        "type": "game_seek",
        "game_seek": {
            "game_seek_id": 95,
            "game_seek_url": "view_game_seek.html?id=95",
            "is_opponent_our_member": false
        }
    }
*/

	public static class Data {
		private String type;
		private GameSeek game_seek;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public GameSeek getGame_seek() {
			return game_seek;
		}

		public void setGame_seek(GameSeek game_seek) {
			this.game_seek = game_seek;
		}

		private class GameSeek {
			private long game_seek_id;
			private String game_seek_url;
			private boolean is_opponent_our_member;

			public long getGame_seek_id() {
				return game_seek_id;
			}

			public void setGame_seek_id(long game_seek_id) {
				this.game_seek_id = game_seek_id;
			}

			public String getGame_seek_url() {
				return game_seek_url;
			}

			public void setGame_seek_url(String game_seek_url) {
				this.game_seek_url = game_seek_url;
			}

			public boolean isIs_opponent_our_member() {
				return is_opponent_our_member;
			}

			public void setIs_opponent_our_member(boolean is_opponent_our_member) {
				this.is_opponent_our_member = is_opponent_our_member;
			}
		}
	}
}
