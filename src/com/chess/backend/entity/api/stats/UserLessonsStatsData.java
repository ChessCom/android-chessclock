package com.chess.backend.entity.api.stats;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 15:31
 */
public class UserLessonsStatsData {
/*
"tactics": {
		"rating": {
			"current": 1219,
			"highest": {
				"rating": 1473,
				"timestamp": 1375933728
			},
			"lowest": {
				"rating": 841,
				"timestamp": 1376097981
			}
		},
		"lessons": {
			"stats": {
				"lessons_tried": 99,
				"total_lesson_count": 2499,
				"lesson_complete_percentage": 4,
				"total_training_seconds": 0,
				"score": {
					"90 - 100%": 43,
					"80 - 89%": 1,
					"70 - 79%": 0,
					"60 - 69%": 1,
					"50 - 59%": 0,
					"< 50%": 54
				}
			}
		}
	}
*/
	private Ratings rating;
	private Lessons lessons;

	public Ratings getRatings() {
		return rating;
	}

	public Lessons getLessons() {
		return lessons;
	}

	public class Ratings {
		private int current;
		private BaseRating highest;
		private BaseRating lowest;

		public int getCurrent() {
			return current;
		}

		public BaseRating getHighest() {
			return highest;
		}

		public BaseRating getLowest() {
			return lowest;
		}
	}

	public class Lessons {
		private Stats stats;

		public Stats getStats() {
			return stats;
		}
	}

	public class Stats {
		private int lessons_tried;
		private int total_lesson_count;
		private int lesson_complete_percentage;
		private long total_training_seconds;
		private Score score;

		public int getLessonsTried() {
			return lessons_tried;
		}

		public int getTotalLessonCount() {
			return total_lesson_count;
		}

		public int getLessonCompletePercentage() {
			return lesson_complete_percentage;
		}

		public long getTotalTrainingSeconds() {
			return total_training_seconds;
		}

		public Score getScore() {
			return score;
		}
	}

	public class Score {
		private int p_90_100;
		private int p_80_89;
		private int p_70_79;
		private int p_60_69;
		private int p_50_59;
		private int p_50;

		public int getP_90_100() {
			return p_90_100;
		}

		public int getP_80_89() {
			return p_80_89;
		}

		public int getP_70_79() {
			return p_70_79;
		}

		public int getP_60_69() {
			return p_60_69;
		}

		public int getP_50_59() {
			return p_50_59;
		}

		public int getP_50() {
			return p_50;
		}
	}
}
