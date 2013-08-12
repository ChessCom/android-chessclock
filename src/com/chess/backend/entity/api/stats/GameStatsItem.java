package com.chess.backend.entity.api.stats;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.02.13
 * Time: 9:33
 */
public class GameStatsItem extends BaseResponseItem<GameStatsItem.Data> {
/*

"rating": {
  "current": 1324,
  "todays_rank": {
	"rank": null,
	"total_player_count": null
  },
  "percentile": null,
  "glicko_rd": 159,
  "highest": {
	"rating": 1324,
	"timestamp": 1315586067
  },
  "lowest": {
	"rating": 1324,
	"timestamp": 1178574672
  },
  "average_opponent": 1372.75,
  "best_win": {
	"rating": null,
	"game_id": 309211,
	"username": null
  },
  "average_opponent_rating_when_i": {
	"win": 0,
	"lose": 0,
	"draw": 0
  }
},
"games": {
  "total": 12,
  "white": 0,
  "black": 0,
  "unrated": 0,
  "in_progress": 0,
  "timeout_percent": 0,
  "wins": {
	"total": 7,
	"white": 0,
	"black": 0
  },
  "losses": {
	"total": 5,
	"white": 0,
	"black": 0
  },
  "draws": {
	"total": 0,
	"white": 0,
	"black": 0
  },
  "winning_streak": 5,
  "losing_streak": 4
  "most_frequent_opponent": {
	"username": "erik",
	"games_played": 5
  }

"tournaments": {
  "all": {
	"leaderboard_points": 0,
	"events_entered": 2,
	"first_place_finishes": 0,
	"second_place_finishes": 0,
	"third_place_finishes": 0,
	"withdrawals": 0,
	"tournaments_hosted": 0,
	"total_count_players_hosted": 0
  },
  "games": {
	"total": 11,
	"won": 6,
	"lost": 5,
	"drawn": 0,
	"in_progress": 2
  }
}

*/

	public class Data {
		private GameRating rating;
		private Games games;
		private Tournaments tournaments;  // Doesn't exist for live games
		private GraphData graph_data;

		public GameRating getRating() {
			return rating == null? new GameRating() : rating;
		}

		public Games getGames() {
			return games == null? new Games() : games;
		}

		public Tournaments getTournaments() {
			return tournaments == null? new Tournaments() : tournaments;
		}
	}

	public class GameRating {
		private int current;
		private StatsTodaysRank todays_rank;
		private float percentile;
		private int glicko_rd;
		private BaseRating highest;
		private BaseRating lowest;
		private float average_opponent;
		private BestWin best_win;
		private AvgOpponentRating average_opponent_rating_when_i;

		public int getCurrent() {
			return current;
		}

		public StatsTodaysRank getTodaysRank() {
			return todays_rank == null? new StatsTodaysRank() : todays_rank;
		}

		public float getPercentile() {
			return percentile;
		}

		public int getGlickoRd() {
			return glicko_rd;
		}

		public BaseRating getHighest() {
			return highest == null? new BaseRating() : highest;
		}

		public BaseRating getLowest() {
			return lowest == null? new BaseRating() : lowest;
		}

		public float getAverageOpponent() {
			return average_opponent;
		}

		public BestWin getBestWin() {
			return best_win == null? new BestWin() : best_win;
		}

		public AvgOpponentRating getAverageOpponentRating() {
			return average_opponent_rating_when_i == null? new AvgOpponentRating() : average_opponent_rating_when_i;
		}
	}

	public class Games extends GamesInfoByColor {
		private int unrated;
		private int in_progress;
		private float timeout_percent;
		private GamesInfoByColor wins;
		private GamesInfoByColor losses;
		private GamesInfoByColor draws;
		private int winning_streak;
		private int losing_streak;

		private MostFrequentOpponent most_frequent_opponent;

		public int getUnrated() {
			return unrated;
		}

		public int getInProgress() {
			return in_progress;
		}

		public float getTimeoutPercent() {
			return timeout_percent;
		}

		public GamesInfoByColor getWins() {
			return wins == null? new GamesInfoByColor() : wins;
		}

		public GamesInfoByColor getLosses() {
			return losses == null? new GamesInfoByColor() : losses;
		}

		public GamesInfoByColor getDraws() {
			return draws == null? new GamesInfoByColor() : draws;
		}

		public int getWinningStreak() {
			return winning_streak;
		}

		public int getLosingStreak() {
			return losing_streak;
		}

		public MostFrequentOpponent getMostFrequentOpponent() {
			return most_frequent_opponent == null? new MostFrequentOpponent() : most_frequent_opponent;
		}
	}

	public class MostFrequentOpponent {
		private String username;
		private int games_played;

		public String getUsername() {
			return getSafeValue(username);
		}

		public int getGamesPlayed() {
			return games_played;
		}
	}
}
