package com.chess.backend.entity.api;

import com.chess.statics.Symbol;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 01.03.14
 * Time: 8:15
 */
public class BaseResponseBatchItem {
/*
{
  "status": "success",
  "count": 3,
  "data": [
    {
      "status": "success",
      "count": 4,
      "data": [
        {
          "id": 49730288,
          "opponent_username": "KERRYISRAEL",
          "opponent_rating": 1449,
          "opponent_win_count": 424,
          "opponent_loss_count": 270,
          "opponent_draw_count": 14,
          "opponent_avatar": null,
          "color": 2,
          "days_per_move": 1,
          "game_type_id": 1,
          "is_rated": true,
          "initial_setup_fen": null,
          "url": "view_game_seek.html?id=49730288",
          "is_opponent_online": false
        }
      ],
      "request_id": 0
    },
    {
      "status": "success",
      "data": [
        {
          "id": 49328400,
          "opponent_username": "zarkostankovic86",
          "opponent_rating": 1038,
          "opponent_win_count": 6,
          "opponent_loss_count": 15,
          "opponent_draw_count": 0,
          "opponent_avatar": null,
          "color": 3,
          "days_per_move": 3,
          "game_type_id": 1,
          "is_rated": true,
          "initial_setup_fen": null,
          "url": "view_game_seek.html?id=49328400",
          "is_opponent_online": true
        }
      ],
      "request_id": 1
    },
    {
      "status": "success",
      "count": 7,
      "data": {
        "live_standard": {
          "rating": 1218,
          "highest_rating": 1420,
          "avg_oponent_rating": 1122.86,
          "total_games": 307,
          "wins": 100,
          "losses": 114,
          "draws": 93,
          "best_win_rating": 1023,
          "best_win_username": "Bobm5453"
        }
      },
      "request_id": 2
    }
  ]
}
 */

/*
	{
	  "status": "error",
	  "message": "Invalid password or username.",
	  "code": 5,
	  "more_info": "http:\/\/www.chess-7.com\/index_api_test.php\/codes#5"
	}
    "debug": [
        "Access denied.",
        "found method: 'GET'",
        "found requestPath: '/v1/tactics/?isInstall=0&loginToken=de3d73999ffdfcc44c9b5240c170b823'",
        "found data: ''",
        "expected signature: 'c2d049b873acea19d600b28acd57ef08fe55b454'"
    ]
*/

	private String status;
	private String message;
	private int count;
	private int code;
	private List<? extends BaseResponseItem> data;
	private String more_info;

	public List<? extends BaseResponseItem> getData() {
		return data;
	}

	/**
	 * TODO remove, for test purpose only
	 */
	@Deprecated
	public void setData(List<? extends BaseResponseItem> data) {
		this.data = data;
	}

	public String getStatus() {
		return status;
	}

	public int getCount() {
		return count;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * @return error code received from server. See more_info field for details
	 */
	public int getCode() {
		return code;
	}

	public String getMore_info() {
		return more_info;
	}

	public static String getSafeValue(String value) {
		return value == null ? Symbol.EMPTY : value;
	}

	public static String getSafeValue(String value, String defaultValue) {
		return value == null ? defaultValue : value;
	}
}
