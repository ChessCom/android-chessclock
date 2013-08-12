package com.chess.backend.entity.api.stats;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 15:24
 */
public class UserTacticsStatsData {
/*
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
    }
*/
	private int current;
	private BaseRating highest;
	private BaseRating lowest;
	private int attempt_count;
	private int passed_count;
	private int failed_count;
	private long total_seconds;
	private int todays_attemps;
	private int todays_average_score;

	public int getCurrent() {
		return current;
	}

	public BaseRating getHighest() {
		return highest;
	}

	public BaseRating getLowest() {
		return lowest;
	}

	public int getAttemptCount() {
		return attempt_count;
	}

	public int getPassedCount() {
		return passed_count;
	}

	public int getFailedCount() {
		return failed_count;
	}

	public long getTotalSeconds() {
		return total_seconds;
	}

	public int getTodaysAttemps() {
		return todays_attemps;
	}

	public int getTodaysAvgScore() {
		return todays_average_score;
	}
}
