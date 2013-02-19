package com.chess.backend.entity.new_api;

import com.chess.backend.entity.new_api.stats.UserTacticsStatsData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.02.13
 * Time: 18:18
 */
public class TacticsHistoryItem extends BaseResponseItem<List<TacticsHistoryItem.Data>> {
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
        "date": 1361230775,
        "tactics_id": 785,
        "rating": 1508,
        "my_rating": 1422,
        "moves": {
          "correct_move_count": 1,
          "move_count": 5
        },
        "average_seconds": 75,
        "user_seconds": 92,
        "outcome": {
          "status": "failed",
          "score": 10,
          "user_rating_change": -21
        }
      }, ...
    ],
    "summary": {
      "current": 1422,
      "highest": {
        "rating": 1474,
        "timestamp": 1338361200
      },
      "lowest": {
        "rating": 0,
        "timestamp": 1338361200
      },
      "attempt_count": 48,
      "passed_count": 17,
      "failed_count": 31,
      "total_seconds": 3325
    }
  }
*/

	public class Data {
		private DailyStats daily_stats;
		private RecentProblems recent_problems;
		private UserTacticsStatsData summary;

		public DailyStats getDaily_stats() {
			return daily_stats;
		}

		public void setDaily_stats(DailyStats daily_stats) {
			this.daily_stats = daily_stats;
		}

		private class DailyStats {
			private long timestamp;
			private int day_open_rating;
			private int day_high_rating;
			private int day_low_rating;
			private int day_close_rating;

			private long getTimestamp() {
				return timestamp;
			}

			private void setTimestamp(long timestamp) {
				this.timestamp = timestamp;
			}

			private int getDay_open_rating() {
				return day_open_rating;
			}

			private void setDay_open_rating(int day_open_rating) {
				this.day_open_rating = day_open_rating;
			}

			private int getDay_high_rating() {
				return day_high_rating;
			}

			private void setDay_high_rating(int day_high_rating) {
				this.day_high_rating = day_high_rating;
			}

			private int getDay_low_rating() {
				return day_low_rating;
			}

			private void setDay_low_rating(int day_low_rating) {
				this.day_low_rating = day_low_rating;
			}

			private int getDay_close_rating() {
				return day_close_rating;
			}

			private void setDay_close_rating(int day_close_rating) {
				this.day_close_rating = day_close_rating;
			}
		}

		private class RecentProblems {
/*
      {
        "date": 1361230775,
        "tactics_id": 785,
        "rating": 1508,
        "my_rating": 1422,
        "moves": {
          "correct_move_count": 1,
          "move_count": 5
        },
        "average_seconds": 75,
        "user_seconds": 92,
        "outcome": {
          "status": "failed",
          "score": 10,
          "user_rating_change": -21
        }
      }, ...

*/
			private long date;
			private int tactics_id;
			private int rating;
			private int my_rating;
			private Moves moves;
			private int average_seconds;
			private int user_seconds;
			private Outcome outcome;

			private class Moves {
				private int correct_move_count;
				private int move_count;

				private int getCorrectMoveCount() {
					return correct_move_count;
				}

				private void setCorrectMoveCount(int correct_move_count) {
					this.correct_move_count = correct_move_count;
				}

				private int getMoveCount() {
					return move_count;
				}

				private void setMoveCount(int move_count) {
					this.move_count = move_count;
				}
			}

			private class Outcome {
				private String status;
				private int score;
				private int user_rating_change;

				private String getStatus() {
					return status;
				}

				private void setStatus(String status) {
					this.status = status;
				}

				private int getScore() {
					return score;
				}

				private void setScore(int score) {
					this.score = score;
				}

				private int getUserRatingChange() {
					return user_rating_change;
				}

				private void setUserRatingChange(int user_rating_change) {
					this.user_rating_change = user_rating_change;
				}
			}
		}

	}
}
