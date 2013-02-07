package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 13:24
 */
public class LiveStatsData {
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
		private Games games;

		public Rating getRating() {
			return rating;
		}

		public void setRating(Rating rating) {
			this.rating = rating;
		}

		public Games getGames() {
			return games;
		}

		public void setGames(Games games) {
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
			private BaseRatingItem highest;
			private BestWin best_win;

			private int average_opponent;

			public int getCurrent() {
				return current;
			}

			public void setCurrent(int current) {
				this.current = current;
			}

			public BaseRatingItem getHighest() {
				return highest;
			}

			public void setHighest(BaseRatingItem highest) {
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

			public class BestWin {
				private int rating;
				private String username;

				public int getRating() {
					return rating;
				}

				public void setRating(int rating) {
					this.rating = rating;
				}

				public String getUsername() {
					return username;
				}

				public void setUsername(String username) {
					this.username = username;
				}
			}
		}

		public class Games {
/*
      "total": 12,
      "wins": 7,
      "losses": 5,
      "draws": 0
*/
			private int total;
			private int wins;
			private int losses;
			private int draws;

			public int getTotal() {
				return total;
			}

			public void setTotal(int total) {
				this.total = total;
			}

			public int getWins() {
				return wins;
			}

			public void setWins(int wins) {
				this.wins = wins;
			}

			public int getLosses() {
				return losses;
			}

			public void setLosses(int losses) {
				this.losses = losses;
			}

			public int getDraws() {
				return draws;
			}

			public void setDraws(int draws) {
				this.draws = draws;
			}
		}
	}

}
