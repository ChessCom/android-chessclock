package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 13:46
 */
public class DailyStatsData extends BaseStatsData{
/*
{
  "turn_based": {
    "chess": {
      "rating": {
        "current": 1471,
        "highest": {
          "rating": 1471,
          "timestamp": 1311359975
        },
        "average_opponent": 0,
        "best_win": {
          "rating": 1929,
          "game_id": 35000360,
          "username": "deepgreene"
        },
        "todays_rank": {
          "rank": null,
          "total_player_count": null
        }
      },
      "timeouts": 0,
      "time_per_move": 119762.71,
      "games": {
        "total": 16,
        "wins": 7,
        "losses": 7,
        "draws": 2
      }
    },
    "chess960": {
      "rating": {
        "current": 997,
        "highest": {
          "rating": 997,
          "timestamp": 1257443104
        },
        "average_opponent": 0,
        "best_win": {
          "rating": null,
          "game_id": null
        },
        "todays_rank": {
          "rank": null,
          "total_player_count": null
        }
      },
      "games": {
        "total": 1,
        "wins": 0,
        "losses": 1,
        "draws": 0
      }
    }
  }
}

	"rating": {                               "rating": {
        "current": 997,                         "current": 1268,
        "highest": {                            "highest": {
          "rating": 997,                          "rating": 1268,
          "timestamp": 1257443104                 "timestamp": 1344327593
        },                                      },
        "average_opponent": 0,                  "average_opponent": 1254
        "best_win": {                           "best_win": {
          "rating": null,                         "rating": 978,
          "game_id": null                         "username": "Computer1-EASY"
        },                                      },
        "todays_rank": {                      },
          "rank": null,                       "games": {
          "total_player_count": null            "total": 5,
        }                                       "wins": 2,
      },                                        "losses": 3,
      "games": {                                "draws": 0
        "total": 1,
        "wins": 0,
        "losses": 1,
        "draws": 0
      }
*/

	private ChessStatsData chess;
	private ChessStatsData chess960;

	public ChessStatsData getChess() {
		return chess;
	}

	public void setChess(ChessStatsData chess) {
		this.chess = chess;
	}

	public ChessStatsData getChess960() {
		return chess960;
	}

	public void setChess960(ChessStatsData chess960) {
		this.chess960 = chess960;
	}

	public class ChessStatsData {
		private DailyRating rating;

		public DailyRating getRating() {
			return rating;
		}

		public void setRating(DailyRating rating) {
			this.rating = rating;
		}

		public class DailyRating extends BaseStatsData.Rating{
			/*
				"todays_rank": {
					"rank": null,
					"total_player_count": null
				}
			*/
			private TodaysRank todays_rank;

			public class TodaysRank {
				private String rank;
				private int total_player_count;
			}
		}
	}
}
