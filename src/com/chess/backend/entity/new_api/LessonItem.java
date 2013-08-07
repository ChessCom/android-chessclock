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

	public static class Data {
		private long id;
		private MentorLesson cm_lesson;
		private List<MentorPosition> cm_positions;
		private String legal_position_check;
		private String legal_move_check;
		private UserLesson user_cm_lesson;
		private boolean lesson_completed;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public MentorLesson getLesson() {
			return cm_lesson;
		}

		public List<MentorPosition> getPositions() {
			return cm_positions;
		}

		public String getLegalPositionCheck() {
			return getSafeValue(legal_position_check);
		}

		public String getLegalMoveCheck() {
			return getSafeValue(legal_move_check);
		}

		public UserLesson getUserLesson() {
			return user_cm_lesson;
		}

		public boolean isLessonCompleted() {
			return lesson_completed;
		}

		public void setLesson(MentorLesson cm_lesson) {
			this.cm_lesson = cm_lesson;
		}

		public void setPositions(List<MentorPosition> cm_positions) {
			this.cm_positions = cm_positions;
		}

		public void setLegalPositionCheck(String legal_position_check) {
			this.legal_position_check = legal_position_check;
		}

		public void setLegalMoveCheck(String legal_move_check) {
			this.legal_move_check = legal_move_check;
		}

		public void setUserLesson(UserLesson user_cm_lesson) {
			this.user_cm_lesson = user_cm_lesson;
		}
	}

	public static class MentorPosition {
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
		/* Local addition */
		private long lessonId;

		public long getLessonId() {
			return lessonId;
		}

		public void setLessonId(long id) {
			this.lessonId = id;
		}

		public int getPositionNumber() {
			return position_number;
		}

		public String getFen() {
			return getSafeValue(fen);
		}

		public int getUserToMove() {
			return user_to_move;
		}

		public int getMoveDifficulty() {
			return move_difficulty;
		}

		public String getAdvice1() {
			return getSafeValue(advice1);
		}

		public String getAdvice2() {
			return getSafeValue(advice2);
		}

		public String getAdvice3() {
			return getSafeValue(advice3);
		}

		public String getStandardResponseMoveCommentary() {
			return getSafeValue(standard_response_move_commentary);
		}

		public String getStandardWrongMoveCommentary() {
			return getSafeValue(standard_wrong_move_commentary);
		}

		public int getFinalPosition() {
			return final_position;
		}

		public String getAbout() {
			return getSafeValue(about);
		}

		public List<PossibleMove> getPossibleMoves() {
			return cm_moves;
		}

		public void setPositionNumber(int position_number) {
			this.position_number = position_number;
		}

		public void setFen(String fen) {
			this.fen = fen;
		}

		public void setUserToMove(int user_to_move) {
			this.user_to_move = user_to_move;
		}

		public void setMoveDifficulty(int move_difficulty) {
			this.move_difficulty = move_difficulty;
		}

		public void setAdvice1(String advice1) {
			this.advice1 = advice1;
		}

		public void setAdvice2(String advice2) {
			this.advice2 = advice2;
		}

		public void setAdvice3(String advice3) {
			this.advice3 = advice3;
		}

		public void setStandardResponseMoveCommentary(String standard_response_move_commentary) {
			this.standard_response_move_commentary = standard_response_move_commentary;
		}

		public void setStandardWrongMoveCommentary(String standard_wrong_move_commentary) {
			this.standard_wrong_move_commentary = standard_wrong_move_commentary;
		}

		public void setFinalPosition(int final_position) {
			this.final_position = final_position;
		}

		public void setAbout(String about) {
			this.about = about;
		}

		public void setPossibleMoves(List<PossibleMove> cm_moves) {
			this.cm_moves = cm_moves;
		}

		public PossibleMove getCorrectMove() {
			for (PossibleMove cm_move : cm_moves) {
				if (cm_move.getMoveType().equals(MOVE_DEFAULT)) {
					return cm_move;
				}
			}
			return null;
		}

		public static class PossibleMove {
			private int move_number;
			private String move;
			private String move_commentary;
			private String short_response_move;
			private String response_move_commentary;
			private String move_type;
			/* Local addition */
			private long lessonId;
			private int positionNumber;

			public long getLessonId() {
				return lessonId;
			}

			public void setLessonId(long lessonId) {
				this.lessonId = lessonId;
			}

			public int getPositionNumber() {
				return positionNumber;
			}

			public void setPositionNumber(int positionNumber) {
				this.positionNumber = positionNumber;
			}

			public int getMoveNumber() {
				return move_number;
			}

			public String getMove() {
				return getSafeValue(move);
			}

			public String getMoveCommentary() {
				return getSafeValue(move_commentary);
			}

			public String getShortResponseMove() {
				return getSafeValue(short_response_move);
			}

			public String getResponseMoveCommentary() {
				return getSafeValue(response_move_commentary);
			}

			public String getMoveType() {
				return getSafeValue(move_type);
			}

			public void setMoveNumber(int move_number) {
				this.move_number = move_number;
			}

			public void setMove(String move) {
				this.move = move;
			}

			public void setMoveCommentary(String move_commentary) {
				this.move_commentary = move_commentary;
			}

			public void setShortResponseMove(String short_response_move) {
				this.short_response_move = short_response_move;
			}

			public void setResponseMoveCommentary(String response_move_commentary) {
				this.response_move_commentary = response_move_commentary;
			}

			public void setMoveType(String move_type) {
				this.move_type = move_type;
			}
		}

	}

	public static class MentorLesson {
		private int lesson_number;
		private String author;
		private String name;
		private String about;
		private int goal;
		private String goal_commentary;
		private String goal_code;
		private int difficulty;
		/* Local addition */
		private long lessonId;

		public long getLessonId() {
			return lessonId;
		}

		public void setLessonId(long id) {
			this.lessonId = id;
		}

		public int getLessonNumber() {
			return lesson_number;
		}

		public String getAuthor() {
			return getSafeValue(author);
		}

		public String getName() {
			return getSafeValue(name);
		}

		public String getAbout() {
			return getSafeValue(about);
		}

		public int getGoal() {
			return goal;
		}

		public String getGoalCommentary() {
			return getSafeValue(goal_commentary);
		}

		public String getGoalCode() {
			return getSafeValue(goal_code);
		}

		public int getDifficulty() {
			return difficulty;
		}

		public void setLessonNumber(int lesson_number) {
			this.lesson_number = lesson_number;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setAbout(String about) {
			this.about = about;
		}

		public void setGoal(int goal) {
			this.goal = goal;
		}

		public void setGoalCommentary(String goal_commentary) {
			this.goal_commentary = goal_commentary;
		}

		public void setGoalCode(String goal_code) {
			this.goal_code = goal_code;
		}

		public void setDifficulty(int difficulty) {
			this.difficulty = difficulty;
		}
	}

	public static class UserLesson {
		private String initial_score;
		private String last_score;
		private int current_position;
		private int current_points;
		private float current_position_points;
		/* Local addition */
		private long lessonId;
		private String username;
		private String legal_position_check;
		private String legal_move_check;
		private boolean lesson_completed;

		public long getLessonId() {
			return lessonId;
		}

		public void setLessonId(long id) {
			this.lessonId = id;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getInitialScore() {
			return getSafeValue(initial_score);
		}

		public String getLastScore() {
			return getSafeValue(last_score);
		}

		public int getCurrentPosition() {
			return current_position;
		}

		public int getCurrentPoints() {
			return current_points;
		}

		public float getCurrentPositionPoints() {
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

		public void setCurrentPositionPoints(float current_position_points) {
			this.current_position_points = current_position_points;
		}

		public String getLegalPositionCheck() {
			return legal_position_check;
		}

		public void setLegalPositionCheck(String legal_position_check) {
			this.legal_position_check = legal_position_check;
		}

		public String getLegalMoveCheck() {
			return legal_move_check;
		}

		public void setLegalMoveCheck(String legal_move_check) {
			this.legal_move_check = legal_move_check;
		}

		public boolean isLessonCompleted() {
			return lesson_completed;
		}

		public void setLessonCompleted(boolean lesson_completed) {
			this.lesson_completed = lesson_completed;
		}
	}
}
