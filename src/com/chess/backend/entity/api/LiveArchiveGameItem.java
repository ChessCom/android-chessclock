package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.09.13
 * Time: 6:42
 */
public class LiveArchiveGameItem extends BaseResponseItem<LiveArchiveGameItem.Data> {
/*
"data": {
    "games": [
		  {
			"id": 658745129,
			"i_play_as": 1,
			"game_type": 1,
			"fen": "rnbqkbnr/pppp1ppp/4p3/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -",
			"timestamp": 1385736616,
			"name": "anotherRoger vs. alien_roger",
			"last_move_from_square": "e3",
			"last_move_to_square": "e4",
			"is_opponent_online": true,
			"game_score": 1,
			"white_username": "anotherRoger",
			"black_username": "alien_roger",
			"white_rating": 1193,
			"black_rating": 1247,
			"is_rated": true,
			"encoded_moves_piotr_string": "mu0SuC",
			"starting_fen_position": null,
			"move_list": "1. e3 e6 2. e4 ",
			"result_message": "anotherRoger won by resignation",
			"white_avatar": "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/anotherRoger_origin.1.png",
			"black_avatar": "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/alien_roger_tiny.5.jpeg",
			"white_premium_status": 3,
			"black_premium_status": 3,
			"white_country_id": 39,
			"black_country_id": 116,
			"game_time_class": "standard"
		  }
		  ],
		"games_total_count": 293
*/

	public class Data {
		private List<LiveArchiveGameData> games;
		private int games_total_count;

		public List<LiveArchiveGameData> getGames() {
			return games;
		}

		public int getGamesTotalCount() {
			return games_total_count;
		}
	}
}
