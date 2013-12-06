package com.chess.backend.entity.api;


import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.12.12
 * Time: 11:23
 */
public class TacticProblemItem extends BaseResponseItem<List<TacticProblemItem.Data>> {
/*
	"status": "success",
	"count": 3,
	"data": [
		{
		  "id": 746,
		  "initial_fen": "r3k2r/ppp1b2p/2n1pNp1/1Q1q4/2pp4/8/PPP2PPP/R1B1R1K1 b kq - 1 1",
		  "clean_move_string": "1... Bxf6 2. Qxd5 ",
		  "attempt_count": 0,
		  "passed_count": 0,
		  "rating": 940,
		  "average_seconds": 14
		},
*/

	public static class Data {
		private long id;
		private String initial_fen;
		private String clean_move_string;
		private int attempt_count;
		private int passed_count;
		private boolean user_moves_first;
		private int rating;
		private int average_seconds;
		/*Local addition */
		private String user;
		private long secondsSpent;
		private boolean stop;
		private boolean answerWasShowed;
		private boolean retry;
		private boolean completed;
		private boolean hintWasUsed;

		public void setId(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

		public String getInitialFen() {
			return initial_fen;
		}

		public void setMoveList(String moveList) {
			clean_move_string = moveList;
		}

		public String getCleanMoveString() {
			return clean_move_string;
		}

		public int getAttemptCnt() {
			return attempt_count;
		}

		public int getPassedCnt() {
			return passed_count;
		}

		public boolean isUserMoveFirst() {
			return user_moves_first;
		}

		public void setIsUserMoveFirst(boolean is_user_move) {
			this.user_moves_first = is_user_move;
		}

		public int getRating() {
			return rating;
		}

		public int getAvgSeconds() {
			return average_seconds;
		}

		public void setFen(String fen) {
			initial_fen = fen;
		}

		public void setAttemptCnt(int attemptCnt) {
			this.attempt_count = attemptCnt;
		}

		public void setPassedCnt(int passedCnt) {
			passed_count = passedCnt;
		}

		public void setRating(int rating) {
			this.rating = rating;
		}

		public void setAvgSeconds(int avgSeconds) {
			average_seconds = avgSeconds;
		}

		public boolean isCompleted() {
			return completed;
		}

		public void setCompleted(boolean completed) {
			this.completed = completed;
		}

		public boolean isRetry() {
			return retry;
		}

		public void setRetry(boolean retry) {
			this.retry = retry;
		}

		public boolean isAnswerWasShowed() {
			return answerWasShowed;
		}

		public void setAnswerWasShowed(boolean answerWasShowed) {
			this.answerWasShowed = answerWasShowed;
		}

		public boolean isStop() {
			return stop;
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}

		public long getSecondsSpent() {
			return secondsSpent;
		}

		public void setSecondsSpent(long secondsSpent) {
			this.secondsSpent = secondsSpent;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public void increaseSecondsPassed() {
			secondsSpent++;
		}

		public void setHintWasUsed(boolean hintWasUsed) {
			this.hintWasUsed = hintWasUsed;
		}
		public boolean hintWasUsed() {
			return hintWasUsed;
		}
	}

}
