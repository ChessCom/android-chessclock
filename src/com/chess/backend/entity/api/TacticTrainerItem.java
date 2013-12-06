package com.chess.backend.entity.api;

import com.chess.statics.Symbol;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.12.12
 * Time: 6:51
 */
public class TacticTrainerItem extends BaseResponseItem<TacticTrainerItem.Data> {
	/*
    "status": "success",
    "data": {
        "rating_info": {
            "score": 50,
            "user_rating_change": 1,
            "user_rating": 1444,
            "problem_rating_change": -1,
            "problem_rating": 1199
        },
        "tactics_problem": {
            "id": 873,
            "initial_fen": "6K1/3r3r/5kn1/5p1N/5P2/8/8/4R1R1 b KQkq - 1 1",
            "clean_move_string": "1... Rxh5 2. Rxg6+ Kxg6 3. Re6# ",
            "attempt_count": 2,
            "passed_count": 0,
            "user_moves_first": false,
            "rating": 1424,
            "average_seconds": 20
        }
    }
 	*/
	public static class Data {
		private TacticRatingData rating_info;
		private TacticProblemItem.Data tactics_problem;

		public TacticRatingData getRatingInfo() {
			return rating_info;
		}

		public void setRatingInfo(TacticRatingData rating_info) {
			this.rating_info = rating_info;
		}

		public TacticProblemItem.Data getTacticsProblem() {
			return tactics_problem;
		}

		public void setTacticsProblem(TacticProblemItem.Data tactics_problem) {
			this.tactics_problem = tactics_problem;
		}

		public String getUser() {
			return tactics_problem.getUser();
		}

		public void setUser(String user) {
			tactics_problem.setUser(user);
		}

		public boolean isStop() {
			return tactics_problem.isStop();
		}

		public void setStop(boolean stop) {
			tactics_problem.setStop(stop);
		}

		public boolean isAnswerWasShowed() {
			return tactics_problem.isAnswerWasShowed();
		}

		public void setAnswerWasShowed(boolean wasShowed) {
			tactics_problem.setAnswerWasShowed(wasShowed);
		}

		public boolean isRetry() {
			return tactics_problem.isRetry();
		}

		public void setRetry(boolean retry) {
			tactics_problem.setRetry(retry);
		}

		public boolean isCompleted() {
			return tactics_problem.isCompleted();
		}

		public void setCompleted(boolean completed) {
			tactics_problem.setCompleted(completed);
		}

		public String getSecondsSpentStr() {
			return AppUtils.getSecondsTimeFromSecondsStr(tactics_problem.getSecondsSpent());
		}

		public void setSecondsSpent(long secondsSpent) {
			tactics_problem.setSecondsSpent(secondsSpent);
		}

		public void increaseSecondsPassed() {
			tactics_problem.increaseSecondsPassed();
		}

		public long getSecondsSpent() {
			return tactics_problem.getSecondsSpent();
		}

		public String getPositiveScore() {
			int userRatingChangeInt = rating_info.getUserRatingChange();
			String userRatingChange = String.valueOf(userRatingChangeInt);
			String plusSymbol = (userRatingChangeInt > 0) ? Symbol.PLUS : Symbol.EMPTY;
			return Symbol.wrapInPars(plusSymbol + userRatingChange);
		}

		public String getNegativeScore() {
			int userRatingChangeInt = rating_info.getUserRatingChange();
			String userRatingChange = String.valueOf(userRatingChangeInt);
			return Symbol.wrapInPars(userRatingChange);
		}

		public long getId() {
			return tactics_problem.getId();
		}

		public String getCleanMoveString() {
			return tactics_problem.getCleanMoveString();
		}

		public String getInitialFen() {
			return tactics_problem.getInitialFen();
		}

		public boolean isUserMoveFirst() {
			return tactics_problem.isUserMoveFirst();
		}

		public int getUserRating() {
			return rating_info.getUserRating();
		}

		public int getProblemRating() {
			return tactics_problem.getRating();
		}

		public int getAvgSeconds() {
			return tactics_problem.getAvgSeconds();
		}

		public boolean hintWasUsed() {
			return tactics_problem.hintWasUsed();
		}

		public void setHintWasUsed(boolean hintWasUsed) {
			tactics_problem.setHintWasUsed(hintWasUsed);
		}
	}
}
