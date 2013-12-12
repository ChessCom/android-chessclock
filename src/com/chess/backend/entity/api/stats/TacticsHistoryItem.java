package com.chess.backend.entity.api.stats;

import com.chess.backend.entity.api.BaseResponseItem;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.02.13
 * Time: 18:18
 */
public class TacticsHistoryItem extends BaseResponseItem<TacticsHistoryItem.Data> {
/*
"daily_stats": [
      {
        "timestamp": 1199347200,
        "day_open_rating": 1200,
        "day_high_rating": 1200,
        "day_low_rating": 1200,
        "day_close_rating": 1200
      },...

"recent_problems": [
      {
        "id": 758,
        "rating": 1335,
        "average_seconds": 9,
        "date": 1361317851,
        "my_rating": 1383,
        "moves": {
          "correct_move_count": 1,
          "move_count": 1
        },
        "user_seconds": 9,
        "outcome": {
          "status": "passed",
          "score": 40,
          "user_rating_change": -11
        }
      }, ...
    ],
    "summary": {
      "current": 1118,
      "highest": {
        "rating": 1474,
        "timestamp": 1338361200
      },
      "lowest": {
        "rating": 0,
        "timestamp": 1338361200
      },
      "attempt_count": 73,
      "passed_count": 24,
      "failed_count": 49,
      "total_seconds": 4304,
      "todays_attempts": 0,
      "todays_average_score": 0
    }
  }
*/

	public static class Data {
		private List<DailyStats> daily_stats;
		private List<RecentProblem> recent_problems;
		private UserTacticsStatsData summary;

		public List<DailyStats> getDailyStats() {
			return daily_stats;
		}

		public List<RecentProblem> getRecentProblems() {
			return recent_problems;
		}

		public UserTacticsStatsData getSummary() {
			return summary;
		}

		public static class DailyStats {
			private long timestamp;
			private int day_open_rating;
			private int day_high_rating;
			private int day_low_rating;
			private int day_close_rating;

			public long getTimestamp() {
				return timestamp;
			}

			public int getDayOpenRating() {
				return day_open_rating;
			}

			public int getDayHighRating() {
				return day_high_rating;
			}

			public int getDayLowRating() {
				return day_low_rating;
			}

			public int getDayCloseRating() {
				return day_close_rating;
			}

			public void setTimestamp(long timestamp) {
				this.timestamp = timestamp;
			}

			public void setDayOpenRating(int day_open_rating) {
				this.day_open_rating = day_open_rating;
			}

			public void setDayHighRating(int day_high_rating) {
				this.day_high_rating = day_high_rating;
			}

			public void setDayLowRating(int day_low_rating) {
				this.day_low_rating = day_low_rating;
			}

			public void setDayCloseRating(int day_close_rating) {
				this.day_close_rating = day_close_rating;
			}
		}

		public static class RecentProblem {
/*
      {
        "id": 758,
        "rating": 1335,
        "average_seconds": 9,
        "date": 1361317851,
        "my_rating": 1383,
        "moves": {
          "correct_move_count": 1,
          "move_count": 1
        },
        "user_seconds": 9,
        "outcome": {
          "status": "passed",
          "score": 40,
          "user_rating_change": -11
        }
      }, ...

*/
			private int id;
			private int rating;
			private int average_seconds;
			private long date;
			private int my_rating;
			private Moves moves;
			private int user_seconds;
			private Outcome outcome;

			public int getId() {
				return id;
			}

			public int getRating() {
				return rating;
			}

			public int getAverageSeconds() {
				return average_seconds;
			}

			public long getDate() {
				return date;
			}

			public int getMyRating() {
				return my_rating;
			}

			public Moves getMoves() {
				return moves;
			}

			public int getUserSeconds() {
				return user_seconds;
			}

			public Outcome getOutcome() {
				return outcome;
			}

			public static class Moves {
				private int correct_move_count;
				private int move_count;

				public int getCorrectMoveCount() {
					return correct_move_count;
				}

				public void setCorrectMoveCount(int correct_move_count) {
					this.correct_move_count = correct_move_count;
				}

				public int getMoveCount() {
					return move_count;
				}

				public void setMoveCount(int move_count) {
					this.move_count = move_count;
				}
			}

			public static class Outcome {
				private String status;
				private int score;
				private int user_rating_change;

				public String getStatus() {
					return status;
				}

				public void setStatus(String status) {
					this.status = status;
				}

				public int getScore() {
					return score;
				}

				public void setScore(int score) {
					this.score = score;
				}

				public int getUserRatingChange() {
					return user_rating_change;
				}

				public void setUserRatingChange(int user_rating_change) {
					this.user_rating_change = user_rating_change;
				}
			}
		}

	}
}
