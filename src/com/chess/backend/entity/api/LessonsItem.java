package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.08.13
 * Time: 22:43
 */
public class LessonsItem extends BaseResponseItem<LessonsItem.Data> {
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
		private List<LessonSingleItem> lessons;
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

		public List<LessonSingleItem> getLessons() {
			return lessons;
		}

		public void setLessonsTotalCount(int lessons_total_count) {
			this.lessons_total_count = lessons_total_count;
		}

		public void setLessons(List<LessonSingleItem> lessons) {
			this.lessons = lessons;
		}
	}
}
