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
            "current": 1073,
            "todays_rank": {
                "rank": null,
                "total_player_count": null
            },
            "percentile": null,
            "glicko_rd": 272,
            "highest": {
                "rating": 1506,
                "timestamp": 1359661966
            },
            "lowest": {
                "rating": 948,
                "timestamp": 1260233861
            },
            "average_opponent": 0,
            "best_win": {
                "rating": 1929,
                "game_id": 35000360,
                "username": "deepgreene"
            },
            "average_opponent_rating_when_i": {
                "win": 0,
                "lose": 0,
                "draw": 0
            }
        },
        "games": {
            "total": 25,
            "white": 14,
            "black": 13,
            "unrated": 3,
            "in_progress": 13,
            "timeout_percent": 50,
            "wins": {
                "total": 9,
                "white": 3,
                "black": 6
            },
            "losses": {
                "total": 14,
                "white": 9,
                "black": 6
            },
            "draws": {
                "total": 2,
                "white": 2,
                "black": 1
            },
            "winning_streak": 4,
            "losing_streak": 10,
            "most_frequent_opponent": {
                "username": "deepgreene",
                "games_played": 8
            }
        },
        "tournaments": {
            "all": {
                "leaderboard_points": 0,
                "events_entered": 2,
                "first_place_finishes": 0,
                "second_place_finishes": 0,
                "third_place_finishes": 0,
                "withdrawals": 1,
                "tournaments_hosted": 0,
                "total_count_players_hosted": 0
            },
            "games": {
                "total": 14,
                "wins": 6,
                "losses": 8,
                "draws": 0,
                "in_progress": 0
            }
        },
        "graph_data": {
            "min_y": 945,
            "max_x": 1606,
            "series": [
                [
                    1260259200000,
                    1045
                ],
                [
                    1302246000000,
                    1218
                ],
                [
                    1305270000000,
                    1307
                ],
                [
                    1306220400000,
                    1318
                ],
                [
                    1311318000000,
                    1471
                ],
                [
                    1359619200000,
                    1506
                ],
                [
                    1367478000000,
                    1319
                ],
                [
                    1369983600000,
                    1201
                ],
                [
                    1370674800000,
                    1073
                ]
            ]
        }

*/

	public class Data {
		private GameRating rating;
		private Games games;
		private Tournaments tournaments;  // Doesn't exist for live games
		private GraphData graph_data;

		public GameRating getRating() {
			return rating == null ? new GameRating() : rating;
		}

		public Games getGames() {
			return games == null ? new Games() : games;
		}

		public Tournaments getTournaments() {
			return tournaments == null ? new Tournaments() : tournaments;
		}

		public GraphData getGraphData() {
			return graph_data;
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
			return todays_rank == null ? new StatsTodaysRank() : todays_rank;
		}

		public float getPercentile() {
			return percentile;
		}

		public int getGlickoRd() {
			return glicko_rd;
		}

		public BaseRating getHighest() {
			return highest == null ? new BaseRating() : highest;
		}

		public BaseRating getLowest() {
			return lowest == null ? new BaseRating() : lowest;
		}

		public int getAverageOpponent() {
			return (int) average_opponent;
		}

		public BestWin getBestWin() {
			return best_win == null ? new BestWin() : best_win;
		}

		public AvgOpponentRating getAverageOpponentRating() {
			return average_opponent_rating_when_i == null ? new AvgOpponentRating() : average_opponent_rating_when_i;
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
			return wins == null ? new GamesInfoByColor() : wins;
		}

		public GamesInfoByColor getLosses() {
			return losses == null ? new GamesInfoByColor() : losses;
		}

		public GamesInfoByColor getDraws() {
			return draws == null ? new GamesInfoByColor() : draws;
		}

		public int getWinningStreak() {
			return winning_streak;
		}

		public int getLosingStreak() {
			return losing_streak;
		}

		public MostFrequentOpponent getMostFrequentOpponent() {
			return most_frequent_opponent == null ? new MostFrequentOpponent() : most_frequent_opponent;
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
