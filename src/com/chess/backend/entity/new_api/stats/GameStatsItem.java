package com.chess.backend.entity.new_api.stats;

import com.chess.backend.entity.new_api.BaseResponseItem;
import com.chess.backend.statics.StaticData;

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
		private GameGames games;
		/**
		 * Doesn't exist for live games
		 */
		private Tournaments tournaments;

		public GameRating getRating() {
			return rating;
		}

		public GameGames getGames() {
			return games;
		}

		public Tournaments getTournaments() {
			return tournaments;
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
			return todays_rank;
		}

		public float getPercentile() {
			return percentile;
		}

		public int getGlickoRd() {
			return glicko_rd;
		}

		public BaseRating getHighest() {
			return highest;
		}

		public BaseRating getLowest() {
			return lowest;
		}

		public float getAverageOpponent() {
			return average_opponent;
		}

		public BestWin getBestWin() {
			return best_win;
		}

		public AvgOpponentRating getAverageOpponentRating() {
			return average_opponent_rating_when_i;
		}
	}

	public class GameGames extends GamesInfo {
		private int unrated;
		private int in_progress;
		private float timeout_percent;
		private GamesInfo wins;
		private GamesInfo losses;
		private GamesInfo draws;
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

		public GamesInfo getWins() {
			return wins;
		}

		public GamesInfo getLosses() {
			return losses;
		}

		public GamesInfo getDraws() {
			return draws;
		}

		public int getWinningStreak() {
			return winning_streak;
		}

		public int getLosingStreak() {
			return losing_streak;
		}

		public MostFrequentOpponent getMostFrequentOpponent() {
			return most_frequent_opponent;
		}
	}

	public class MostFrequentOpponent {
		private String username;
		private int games_played;

		public String getUsername() {
			return username == null? StaticData.SYMBOL_EMPTY : username;
		}

		public int getGamesPlayed() {
			return games_played;
		}
	}
}
