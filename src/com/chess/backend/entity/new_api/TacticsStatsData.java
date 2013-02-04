package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 15:24
 */
public class TacticsStatsData {
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
	private BaseRatingItem highest;
	private BaseRatingItem lowest;
	private int attempt_count;
	private int passed_count;
	private int failed_count;
	private long total_seconds;

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

	public BaseRatingItem getLowest() {
		return lowest;
	}

	public void setLowest(BaseRatingItem lowest) {
		this.lowest = lowest;
	}

	public int getAttempt_count() {
		return attempt_count;
	}

	public void setAttempt_count(int attempt_count) {
		this.attempt_count = attempt_count;
	}

	public int getPassed_count() {
		return passed_count;
	}

	public void setPassed_count(int passed_count) {
		this.passed_count = passed_count;
	}

	public int getFailed_count() {
		return failed_count;
	}

	public void setFailed_count(int failed_count) {
		this.failed_count = failed_count;
	}

	public long getTotal_seconds() {
		return total_seconds;
	}

	public void setTotal_seconds(long total_seconds) {
		this.total_seconds = total_seconds;
	}

}
