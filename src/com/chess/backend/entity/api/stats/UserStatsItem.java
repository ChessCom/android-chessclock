package com.chess.backend.entity.api.stats;

import com.chess.backend.entity.api.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 13:18
 */
public class UserStatsItem extends BaseResponseItem<UserStatsItem.Data> {
/*
{
    "live_standard": {
            "rating": "Unrated",
            "highest_rating": 1200,
            "avg_oponent_rating": 0,
            "total_games": 6,
            "wins": 1,
            "losses": 5,
            "draws": 0,
            "best_win_rating": null,
            "best_win_username": null
        },
        "live_blitz": {
            "rating": 1155,
            "highest_rating": 1332,
            "avg_oponent_rating": 1171,
            "total_games": 91,
            "wins": 40,
            "losses": 39,
            "draws": 12,
            "best_win_rating": null,
            "best_win_username": null
        },
        "live_bullet": {
            "rating": 1038,
            "highest_rating": 1200,
            "avg_oponent_rating": 1362,
            "total_games": 1,
            "wins": 0,
            "losses": 1,
            "draws": 0,
            "best_win_rating": null,
            "best_win_username": null
        },
        "daily_chess": {
            "rating": 1349,
            "highest_rating": 1349,
            "avg_oponent_rating": 0,
            "total_games": 3,
            "wins": 3,
            "losses": 0,
            "draws": 0,
            "best_win_rating": 1073,
            "best_win_username": "erik"
        },
        "chess_960": {
            "rating": null,
            "highest_rating": null,
            "avg_oponent_rating": null,
            "total_games": null,
            "wins": null,
            "losses": null,
            "draws": null,
            "best_win_rating": null,
            "best_win_username": null
        },
        "lessons": {
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

	public class Data {
		private UserStatsData live_standard;
		private UserStatsData live_blitz;
		private UserStatsData live_bullet;
		private UserStatsData daily_chess;
		private UserStatsData chess_960;
		private UserLessonsStatsData lessons;
//		private TacticsHistoryItem tactics;

		public UserStatsData getLiveStandard() {
			return live_standard;
		}

		public UserStatsData getLiveBlitz() {
			return live_blitz;
		}

		public UserStatsData getLiveBullet() {
			return live_bullet;
		}

		public UserStatsData getDailyChess() {
			return daily_chess;
		}

		public UserStatsData getChess960() {
			return chess_960;
		}

//		public TacticsHistoryItem getTactics() {
//			return tactics;
//		}
//
//		public void setTactics(TacticsHistoryItem tactics) {
//			this.tactics = tactics;
//		}

		public UserLessonsStatsData getLessons() {
			return lessons;
		}

		public void setChessMentorData(UserLessonsStatsData chess_mentor) {
			this.lessons = chess_mentor;
		}
	}
}
