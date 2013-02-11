package com.chess.backend.entity.new_api.stats;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 13:24
 */
public class UserLiveStatsData {
/*
		{
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
}
*/

	private Stats lightning;
	private Stats blitz;
	private Stats standard;

	public Stats getLightning() {
		return lightning;
	}

	public void setLightning(Stats lightning) {
		this.lightning = lightning;
	}

	public Stats getBlitz() {
		return blitz;
	}

	public void setBlitz(Stats blitz) {
		this.blitz = blitz;
	}

	public Stats getStandard() {
		return standard;
	}

	public void setStandard(Stats standard) {
		this.standard = standard;
	}

	public class Stats {
/*
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
*/
		private Rating rating;
		private GamesInfoByResult games;

		public Rating getRating() {
			return rating;
		}

		public void setRating(Rating rating) {
			this.rating = rating;
		}

		public GamesInfoByResult getGames() {
			return games;
		}

		public void setGames(GamesInfoByResult games) {
			this.games = games;
		}

		public class Rating {
/*
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
*/
			private int current;
			private BaseRating highest;
			private BestWin best_win;

			private int average_opponent;

			public int getCurrent() {
				return current;
			}

			public void setCurrent(int current) {
				this.current = current;
			}

			public BaseRating getHighest() {
				return highest;
			}

			public void setHighest(BaseRating highest) {
				this.highest = highest;
			}

			public BestWin getBestWin() {
				return best_win;
			}

			public void setBest_win(BestWin best_win) {
				this.best_win = best_win;
			}

			public int getAverageOpponent() {
				return average_opponent;
			}

			public void setAverage_opponent(int average_opponent) {
				this.average_opponent = average_opponent;
			}

		}


	}

}
