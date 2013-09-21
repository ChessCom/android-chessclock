package com.chess.backend.entity.api.stats;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 15:31
 */
public class UserLessonsStatsData {
/*
 "lessons": {
            "rating": {
                "current": 1222,
                "highest": {
                    "rating": 1473,
                    "timestamp": 1375933728
                },
                "lowest": {
                    "rating": 841,
                    "timestamp": 1376097981
                }
            },
			"stats": {
				"lessons_tried": 101,
				"total_lesson_count": 2499,
				"lesson_complete_percentage": 4,
				"total_training_seconds": 0,
				"score": {
					"p_90_100": 44,
					"p_80_89": 1,
					"p_70_79": 0,
					"p_60_69": 1,
					"p_50_59": 0,
					"p_50": 55
				}
            }
        },
*/
	private Ratings rating;
	private Stats stats;

	public Ratings getRatings() {
		return rating == null ? new Ratings() : rating;
	}

	public Stats getStats() {
		return stats;
	}

	public class Ratings {
		private int current;
		private BaseRating highest;
		private BaseRating lowest;

		public int getCurrent() {
			return current;
		}

		public BaseRating getHighest() {
			return highest == null ? new BaseRating() : highest;
		}

		public BaseRating getLowest() {
			return lowest == null ? new BaseRating() : highest;
		}
	}

	public class Stats {
		private int lessons_tried;
		private int total_lesson_count;
		private float lesson_complete_percentage;
		private long total_training_seconds;
		private Score score;

		public int getLessonsTried() {
			return lessons_tried;
		}

		public int getTotalLessonCount() {
			return total_lesson_count;
		}

		public float getLessonCompletePercentage() {
			return lesson_complete_percentage;
		}

		public long getTotalTrainingSeconds() {
			return total_training_seconds;
		}

		public Score getScore() {
			return score == null ? new Score() : score;
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
