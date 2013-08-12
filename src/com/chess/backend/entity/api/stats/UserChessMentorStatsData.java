package com.chess.backend.entity.api.stats;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 15:31
 */
public class UserChessMentorStatsData {
/*
      "rating": {
        "current": 1334,
        "highest": {
          "rating": 1733,
          "timestamp": 1194328001
        },
        "lowest": {
          "rating": 1528,
          "timestamp": 1194632224
        },
        "lessons_tried": 23,
        "total_lesson_count": 2491,
        "lesson_complete_percentage": 0.9,
        "total_training_seconds": 159
      }
*/
	public Rating rating;

	public Rating getRating() {
		return rating;
	}

	public class Rating {
		private int current;
		private BaseRating highest;
		private BaseRating lowest;
		private int lessons_tried;
		private int total_lesson_count;
		private float lesson_complete_percentage;
		private long total_training_seconds;

		public int getCurrent() {
			return current;
		}

		public BaseRating getHighest() {
			return highest;
		}

		public BaseRating getLowest() {
			return lowest;
		}

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
	}
}
