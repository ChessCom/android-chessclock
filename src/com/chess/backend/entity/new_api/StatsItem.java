package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 13:18
 */
public class StatsItem extends BaseResponseItem<StatsItem.Data> {
/*
{
  "status": "success",
  "data": {
    "live": {
      "lightning": {
        "rating": {
          "current": 1324,
          "highest": {
            "rating": 1324,
            "timestamp": 1315586067
          },
          "best_win": {
            "rating": 1573,
            "username": "Computer1-EASY"
          },
          "average_opponent": 1373
        },
        "games": {
          "total": 12,
          "wins": 7,
          "losses": 5,
          "draws": 0
        }
      },
      "blitz": {
        "rating": {
          "current": 1418,
          "highest": {
            "rating": 1479,
            "timestamp": 1298306903
          },
          "best_win": {
            "rating": 1499,
            "username": "deepgreene"
          },
          "average_opponent": 1283
        },
        "games": {
          "total": 26,
          "wins": 16,
          "losses": 9,
          "draws": 1
        }
      },
      "standard": {
        "rating": {
          "current": 1268,
          "highest": {
            "rating": 1268,
            "timestamp": 1344327593
          },
          "best_win": {
            "rating": 978,
            "username": "Computer1-EASY"
          },
          "average_opponent": 1254
        },
        "games": {
          "total": 5,
          "wins": 2,
          "losses": 3,
          "draws": 0
        }
      }
    },
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
    },
    "tactics": {
      "current": 1311,
      "highest": {
        "rating": 1474,
        "timestamp": 1338361200
      },
      "lowest": {
        "rating": 0,
        "timestamp": 1338361200
      },
      "attempt_count": 53,
      "passed_count": 20,
      "failed_count": 33,
      "total_seconds": 3201
    },
    "cm": {
      "rating": {
        "current": 1334,
        "highest": {
          "rating": 1733,
          "timestamp": 1194328001
        },
        "lowest": {
          "rating": 1528,
          "timestamp": 1194632224
        },
        "lessons_tried": 23,
        "total_lesson_count": 2491,
        "lesson_complete_percentage": 0.9,
        "total_training_seconds": 159
      }
    }
  }
}
 */

	public class Data {
		private LiveStatsData live;
		private DailyStatsData turn_based;
		private TacticsStatsData tactics;
		private ChessMentorData cm;

		public LiveStatsData getLive() {
			return live;
		}

		public void setLive(LiveStatsData live) {
			this.live = live;
		}

		public DailyStatsData getDaily() {
			return turn_based;
		}

		public void setTurn_based(DailyStatsData turn_based) {
			this.turn_based = turn_based;
		}

		public TacticsStatsData getTactics() {
			return tactics;
		}

		public void setTactics(TacticsStatsData tactics) {
			this.tactics = tactics;
		}

		public ChessMentorData getChessMentor() {
			return cm;
		}

		public void setCm(ChessMentorData cm) {
			this.cm = cm;
		}
	}
}
