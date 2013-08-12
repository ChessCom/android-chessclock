package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.08.13
 * Time: 22:43
 */
public class LessonSearchItem extends BaseResponseItem<LessonSearchItem.Data> {
/*
        "lessons_total_count": 2,
        "lessons": [
            {
                "completed": true,
                "id": 679,
                "name": "Double Attack"
            },
            {
                "completed": true,
                "id": 679,
                "name": "Double Attack"
            }
        ]
*/

	public static class Data {
		private int lessons_total_count;
		private List<LessonListItem> lessons;
		/* Local additions */
		private long id;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public int getLessonsTotalCount() {
			return lessons_total_count;
		}

		public List<LessonListItem> getLessons() {
			return lessons;
		}

		public void setLessonsTotalCount(int lessons_total_count) {
			this.lessons_total_count = lessons_total_count;
		}

		public void setLessons(List<LessonListItem> lessons) {
			this.lessons = lessons;
		}
	}
}
