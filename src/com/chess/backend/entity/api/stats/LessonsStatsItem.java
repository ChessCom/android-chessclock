package com.chess.backend.entity.api.stats;

import com.chess.backend.entity.api.BaseResponseItem;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 6:44
 */
public class LessonsStatsItem extends BaseResponseItem<LessonsStatsItem.Data> {
/*
 	"data": {
        "rating": {
            "current": 1334,
            "highest": {
                "rating": 1733,
                "timestamp": 1194328001
            },
            "lowest": {
                "rating": 1528,
                "timestamp": 1194632224
            }
        },
        "lessons": {
            "stats": {
                "lessons_tried": 23,
                "total_lesson_count": 2491,
                "lesson_complete_percentage": 0.9,
                "total_training_seconds": 159,
                "score": {
                    "90 - 100%": 1,
                    "80 - 89%": 0,
                    "70 - 79%": 0,
                    "60 - 69%": 1,
                    "50 - 59%": 4,
                    "< 50%": 17
                }
            },
            "recent": [
                {
                    "code": "SVB010",
                    "lesson_id": 113,
                    "name": "How to Capture Using the King",
                    "category": "Rules and Basics",
                    "rating": 712,
                    "my_score": 0
                },
                 ...
            ]
        },
        "graph_data": {
               "series": [
                 [
                     1194632224,
                     1528
                 ],
                 ..
             ]
       }
	 */

	public class Data {
		private UserLessonsStatsData.Ratings rating;
		private LessonsDataStats lessons;
		private GraphData graph_data;

		public UserLessonsStatsData.Ratings getRating() {
			return rating;
		}

		public LessonsDataStats getLessons() {
			return lessons;
		}

		public GraphData getGraph_data() {
			return graph_data;
		}
	}

	public static class LessonsDataStats {
		private UserLessonsStatsData.Stats stats;
		private List<RecentLessonData> recent;

		public UserLessonsStatsData.Stats getStats() {
			return stats;
		}

		public List<RecentLessonData> getRecent() {
			return recent;
		}
	}

	public static class RecentLessonData {
		private String code;
		private long lesson_id;
		private String name;
		private String category;
		private int rating;
		private int my_score;

		public String getCode() {
			return code;
		}

		public long getLessonId() {
			return lesson_id;
		}

		public String getName() {
			return name;
		}

		public String getCategory() {
			return category;
		}

		public int getRating() {
			return rating;
		}

		public int getMyScore() {
			return my_score;
		}
	}

}
