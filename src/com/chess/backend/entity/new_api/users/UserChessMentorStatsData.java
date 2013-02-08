package com.chess.backend.entity.new_api.users;

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
	private int current;
	private UserBaseRatingItem highest;
	private UserBaseRatingItem lowest;
	private int lessons_tried;
	private int total_lesson_count;
	private int lesson_complete_percentage;
	private long total_training_seconds;

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public UserBaseRatingItem getHighest() {
		return highest;
	}

	public void setHighest(UserBaseRatingItem highest) {
		this.highest = highest;
	}

	public UserBaseRatingItem getLowest() {
		return lowest;
	}

	public void setLowest(UserBaseRatingItem lowest) {
		this.lowest = lowest;
	}

	public int getLessonsTried() {
		return lessons_tried;
	}

	public void setLessonsTried(int lessons_tried) {
		this.lessons_tried = lessons_tried;
	}

	public int getTotalLessonCount() {
		return total_lesson_count;
	}

	public void setTotalLessonCount(int total_lesson_count) {
		this.total_lesson_count = total_lesson_count;
	}

	public int getLessonCompletePercentage() {
		return lesson_complete_percentage;
	}

	public void setLessonCompletePercentage(int lesson_complete_percentage) {
		this.lesson_complete_percentage = lesson_complete_percentage;
	}

	public long getTotalTrainingSeconds() {
		return total_training_seconds;
	}

	public void setTotalTrainingSeconds(long total_training_seconds) {
		this.total_training_seconds = total_training_seconds;
	}

}
