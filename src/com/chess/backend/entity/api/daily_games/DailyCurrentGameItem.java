package com.chess.backend.entity.api.daily_games;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.08.13
 * Time: 15:37
 */
public class DailyCurrentGameItem extends BaseResponseItem<DailyCurrentGameItem.Data> {
/*
  "data": {
    "id": 35002029,
    "game_type": 1,
    "time_remaining": 0,
    "timestamp": 1376021581,
    "name": "Let's Play!",
    "is_opponent_online": false,
    "has_new_message": false,
    "game_score": null,
    "white_username": "rest",
    "black_username": "erik",
    "user_to_move": 2,
    "white_rating": 1207,
    "black_rating": 1073,
    "is_rated": true,
    "days_per_move": 3,
    "draw_offered": 0,
    "encoded_moves_piotr_string": "mC",
    "starting_fen_position": null,
    "move_list": "1. e4 ",
    "result_message": null,
    "white_avatar": "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/rest_origin.29.png",
    "black_avatar": "//d1lalstwiwz2br.cloudfront.net/images_users/avatars/erik_origin.9.jpeg",
    "white_premium_status": null,
    "black_premium_status": null,
    "white_country_id": 2,
    "black_country_id": 2
  },
*/

	public class Data extends DailyCurrentGameData {

	}
}
