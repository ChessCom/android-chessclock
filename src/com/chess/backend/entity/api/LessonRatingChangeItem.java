package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.08.13
 * Time: 5:22
 */
public class LessonRatingChangeItem extends BaseResponseItem<LessonRatingChangeItem.Data> {
/*
    "data": {
        "change": 8,
        "new": 1032
    }
*/

	public class Data {
		private int change;
		private int new_rating = 1234; // TODO remove hardcode and replace with other name that comes from server

		public int getChange() {
			return change;
		}

		public int getNewRating() {
			return new_rating;
		}
	}
}
