package com.chess.backend.entity.new_api.stats;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.02.13
 * Time: 11:23
 */
public class Tournaments {
	/*
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
	private All all;
	private Games games;

	public All getAll() {
		return all;
	}

	public Games getGames() {
		return games;
	}

	public class All {
		private int leaderboard_points;
		private int events_entered;
		private int first_place_finishes;
		private int second_place_finishes;
		private int third_place_finishes;
		private int withdrawals;
		private int tournaments_hosted;
		private int total_count_players_hosted;

		public int getLeaderboardPoints() {
			return leaderboard_points;
		}

		public int getEventsEntered() {
			return events_entered;
		}

		public int getFirstPlaceFinishes() {
			return first_place_finishes;
		}

		public int getSecondPlaceFinishes() {
			return second_place_finishes;
		}

		public int getThirdPlaceFinishes() {
			return third_place_finishes;
		}

		public int getWithdrawals() {
			return withdrawals;
		}

		public int getTournamentsHosted() {
			return tournaments_hosted;
		}

		public int getTotalCountPlayersHosted() {
			return total_count_players_hosted;
		}
	}

	public class Games {
		private int total;
		private int won;
		private int lost;
		private int draw;
		private int in_progress;

		public int getTotal() {
			return total;
		}

		public int getWon() {
			return won;
		}

		public int getLost() {
			return lost;
		}

		public int getDraw() {
			return draw;
		}

		public int getInProgress() {
			return in_progress;
		}
	}
}
