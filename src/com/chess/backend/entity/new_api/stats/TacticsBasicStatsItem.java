package com.chess.backend.entity.new_api.stats;

import com.chess.backend.entity.new_api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.06.13
 * Time: 8:14
 */
public class TacticsBasicStatsItem extends BaseResponseItem<TacticsBasicStatsItem.Data> {
/*
	"current": 1026,
	"todays_attempts": 0,
	"todays_average_score": 0
*/

	public class Data {
		private int current;
		private int todays_attempts;
		private int todays_average_score;

		public int getCurrent() {
			return current;
		}

		public int getTodaysAttempts() {
			return todays_attempts;
		}

		public int getTodaysAverageScore() {
			return todays_average_score;
		}
	}
}
