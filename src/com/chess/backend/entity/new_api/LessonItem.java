package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.07.13
 * Time: 8:35
 */
public class LessonItem extends BaseResponseItem<LessonItem.Data> {

	public static final String MOVE_DEFAULT = "default";
	public static final String MOVE_ALTERNATE = "alternate";
	public static final String MOVE_WRONG = "wrong";

/*
{
    "status": "success",
    "count": 6,
    "data": {
        "cm_lesson": {
            "lesson_number": 36,
            "author": "Jeremy Silman",
            "name": "Spassky-Petrosian, Moscow (World Championship) 1969",
            "about": "In the present position Spassky enjoys a passed d-pawn. Black hopes that his queenside majority and his threats against White's a-pawn will compensate him while White wants to prove that his unblocked passer is the major force on the board.",
            "goal": 7,
            "goal_commentary": "White wants to show that his passed d-pawn will throw the enemy army into a panic.",
            "goal_code": "positional_gain",
            "difficulty": 6
        },
        "cm_positions": [
            {
                "position_number": 1,
                "fen": "2r2rk1/p4ppp/1p6/n2P1Q2/8/5N2/P1q2PPP/3RR1K1 w - - 0 1",
                "user_to_move": 1,
                "move_difficulty": 9,
                "advice1": "The rules for ownership of a passed pawn are: trade off the minor pieces but retain the Queens and at least one Rook; control the square directly in front of your passed pawn (thereby preventing the opponent from blockading it). Look for a move that accomplishes both of these goals.",
                "advice2": "The White Queen is hanging. If White doesn't trade Queens he will lose his a2-pawn. If he does trade Queens he will break the rule saying that the side with the passed pawn should avoid the exchange of Queens. Who and what should you believe?",
                "advice3": "Fight for control of the square directly in front of the passed pawn. Don't worry about minor material loss.",
                "standard_response_move_commentary": "",
                "standard_wrong_move_commentary": "You have to move your Queen, you should avoid the exchange of Queens, and you should fight for control of the square directly in front of your passed pawn (d6). Your move doesn't accomplish these goals.",
                "final_position": 0,
                "about": "Rules for the player who owns a passed pawn: 1) Control the square directly in front of the pawn; 2) Trade all the minor pieces so that a Knight or Bishop can't block the pawn; 3) Keep the Queens on so the opponent's King won't feel comfortable rushing up to combat your pawn; 4) Keep at least one Rook on.",
                "cm_moves": [
                    {
                        "move_number": 0,
                        "move": "Qf4",
                        "move_commentary": "1.Qf4 retains the Queens (usually a good idea for the player who owns the passed pawn) and eyes the d6-square (the square directly in front of the pawn). White is quite willing to give up a pawn in order to get his own passer rolling.",
                        "short_response_move": "c2a2",
                        "response_move_commentary": "1...Qxa2 wins a pawn and gives Black two connected passed pawns on the queenside. White's passed d-pawn is worth more than Black's two queenside pawns, though, because it is further advanced than the Black pawns and will play a more aggressive role in the battle than the passive creatures on a7 and b6.",
                        "move_type": "default"
                    }
                ]
            }
        ],
        "legal_position_check": "full",
        "legal_move_check": "full",
        "user_cm_lesson": {
            "initial_score": null,
            "last_score": null,
            "current_position": 0,
            "current_points": 29,
            "current_position_points": 9
        },
        "lesson_completed": false
    }
}

*/

	public class Data {
		private MentorLesson cm_lesson;
		private List<MentorPosition> cm_positions;
		private String legal_position_check;
		private String legal_move_check;
		private UserLesson user_cm_lesson;
		private boolean lesson_completed;

		public MentorLesson getLesson() {
			return cm_lesson;
		}

		public List<MentorPosition> getPositions() {
			return cm_positions;
		}

		public String getLegalPositionCheck() {
			return legal_position_check;
		}

		public String getLegalMoveCheck() {
			return legal_move_check;
		}

		public UserLesson getUserLesson() {
			return user_cm_lesson;
		}

		public boolean isLessonCompleted() {
			return lesson_completed;
		}
	}

	public class MentorPosition {

/*
		"position_number": 1,
		"fen": "2r2rk1/p4ppp/1p6/n2P1Q2/8/5N2/P1q2PPP/3RR1K1 w - - 0 1",
		"user_to_move": 1,
		"move_difficulty": 9,
		"advice1": "The rules for ownership of a passed pawn are: trade off the minor pieces but retain the Queens and at least one Rook; control the square directly in front of your passed pawn (thereby preventing the opponent from blockading it). Look for a move that accomplishes both of these goals.",
		"advice2": "The White Queen is hanging. If White doesn't trade Queens he will lose his a2-pawn. If he does trade Queens he will break the rule saying that the side with the passed pawn should avoid the exchange of Queens. Who and what should you believe?",
		"advice3": "Fight for control of the square directly in front of the passed pawn. Don't worry about minor material loss.",
		"standard_response_move_commentary": "",
		"standard_wrong_move_commentary": "You have to move your Queen, you should avoid the exchange of Queens, and you should fight for control of the square directly in front of your passed pawn (d6). Your move doesn't accomplish these goals.",
		"final_position": 0,
		"about": "Rules for the player who owns a passed pawn: 1) Control the square directly in front of the pawn; 2) Trade all the minor pieces so that a Knight or Bishop can't block the pawn; 3) Keep the Queens on so the opponent's King won't feel comfortable rushing up to combat your pawn; 4) Keep at least one Rook on.",
		"cm_moves": [
			{
				"move_number": 0,
				"move": "Qf4",
				"move_commentary": "1.Qf4 retains the Queens (usually a good idea for the player who owns the passed pawn) and eyes the d6-square (the square directly in front of the pawn). White is quite willing to give up a pawn in order to get his own passer rolling.",
				"short_response_move": "c2a2",
				"response_move_commentary": "1...Qxa2 wins a pawn and gives Black two connected passed pawns on the queenside. White's passed d-pawn is worth more than Black's two queenside pawns, though, because it is further advanced than the Black pawns and will play a more aggressive role in the battle than the passive creatures on a7 and b6.",
				"move_type": "default"
			}
		]
*/
		private int position_number;
		private String fen;
		private int user_to_move;
		private int move_difficulty;
		private String advice1;
		private String advice2;
		private String advice3;
		private String standard_response_move_commentary;
		private String standard_wrong_move_commentary;
		private int final_position;
		private String about;
		private List<PossibleMove> cm_moves;

		public int getPositionNumber() {
			return position_number;
		}

		public String getFen() {
			return fen;
		}

		public int getUserToMove() {
			return user_to_move;
		}

		public int getMoveDifficulty() {
			return move_difficulty;
		}

		public String getAdvice1() {
			return advice1;
		}

		public String getAdvice2() {
			return advice2;
		}

		public String getAdvice3() {
			return advice3;
		}

		public String getStandardResponseMoveCommentary() {
			return standard_response_move_commentary;
		}

		public String getStandardWrongMoveCommentary() {
			return standard_wrong_move_commentary;
		}

		public int getFinalPosition() {
			return final_position;
		}

		public String getAbout() {
			return about;
		}

		public List<PossibleMove> getLessonMoves() {
			return cm_moves;
		}

		public class PossibleMove {
/*
				"move_number": 0,
				"move": "Qf4",
				"move_commentary": "1.Qf4 retains the Queens (usually a good idea for the player who owns the passed pawn) and eyes the d6-square (the square directly in front of the pawn). White is quite willing to give up a pawn in order to get his own passer rolling.",
				"short_response_move": "c2a2",
				"response_move_commentary": "1...Qxa2 wins a pawn and gives Black two connected passed pawns on the queenside. White's passed d-pawn is worth more than Black's two queenside pawns, though, because it is further advanced than the Black pawns and will play a more aggressive role in the battle than the passive creatures on a7 and b6.",
				"move_type": "default"
 */
			private int move_number;
			private String move;
			private String move_commentary;
			private String short_response_move;
			private String response_move_commentary;
			private String move_type;

			public int getMoveNumber() {
				return move_number;
			}

			public String getMove() {
				return move;
			}

			public String getMoveCommentary() {
				return move_commentary;
			}

			public String getShortResponseMove() {
				return short_response_move;
			}

			public String getResponseMoveCommentary() {
				return response_move_commentary;
			}

			public String getMoveType() {
				return move_type;
			}
		}

	}

/*
        "cm_lesson": {
            "lesson_number": 36,
            "author": "Jeremy Silman",
            "name": "Spassky-Petrosian, Moscow (World Championship) 1969",
            "about": "In the present position Spassky enjoys a passed d-pawn. Black hopes that his queenside majority and his threats against White's a-pawn will compensate him while White wants to prove that his unblocked passer is the major force on the board.",
            "goal": 7,
            "goal_commentary": "White wants to show that his passed d-pawn will throw the enemy army into a panic.",
            "goal_code": "positional_gain",
            "difficulty": 6
        },
*/

	public class MentorLesson {
		private int lesson_number;
		private String author;
		private String name;
		private String about;
		private int goal;
		private String goal_commentary;
		private String goal_code;
		private int difficulty;

		public int getLessonNumber() {
			return lesson_number;
		}

		public String getAuthor() {
			return author;
		}

		public String getName() {
			return name;
		}

		public String getAbout() {
			return about;
		}

		public int getGoal() {
			return goal;
		}

		public String getGoalCommentary() {
			return goal_commentary;
		}

		public String getGoalCode() {
			return goal_code;
		}

		public int getDifficulty() {
			return difficulty;
		}
	}

	public class UserLesson {
/*
		"initial_score": null,
		"last_score": null,
		"current_position": 0,
		"current_points": 29,
		"current_position_points": 9
*/
		private String initial_score;
		private String last_score;
		private int current_position;
		private int current_points;
		private int current_position_points;

		public String getInitialScore() {
			return initial_score;
		}

		public String getLastScore() {
			return last_score;
		}

		public int getCurrentPosition() {
			return current_position;
		}

		public int getCurrentPoints() {
			return current_points;
		}

		public int getCurrentPositionPoints() {
			return current_position_points;
		}

		public void setInitialScore(String initial_score) {
			this.initial_score = initial_score;
		}

		public void setLastScore(String last_score) {
			this.last_score = last_score;
		}

		public void setCurrentPosition(int current_position) {
			this.current_position = current_position;
		}

		public void setCurrentPoints(int current_points) {
			this.current_points = current_points;
		}

		public void setCurrentPositionPoints(int current_position_points) {
			this.current_position_points = current_position_points;
		}
	}
}
