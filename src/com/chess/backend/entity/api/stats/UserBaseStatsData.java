package com.chess.backend.entity.api.stats;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 13:58
 */
public class UserBaseStatsData {
/*
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

		public int getAverageOpponent() {
			return average_opponent;
		}
	}
}
