package com.chess.backend.entity.new_api;


import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.12.12
 * Time: 11:23
 */
public class TacticItem extends BaseResponseItem<List<TacticItem.TacticsData>> {
	/*
		"status": "success",
		"count": 3,
		"data": [
			{
				"tactics_problem_id": 823,
				"initial_fen": "5r1k/1b4pp/8/p1R5/Pp2p2q/1Q2B2P/1P3PrK/4R3 w - - 1 2",
				"clean_move_string": "1. Kxg2 Rxf2+ 2. Bxf2 e3+ 3. Rd5 Qxf2+ 4. Kh1 Qxe1+ 5. Kh2 Qf2+ 6. Kh1 e2 ",
				"attempt_count": 1,
				"passed_count": 0,
				"rating": 1283,
				"average_seconds": 30
			}...
		]
	*/

	public static class TacticsData {

		private long tactics_problem_id;
		private String initial_fen;
		private String clean_move_string;
		private int attempt_count;
		private int passed_count;
		private int rating;
		private int average_seconds;
		private String user;
		private long secondsSpent;
		private TacticResultItem resultItem;
		private boolean stop;
		private boolean wasShowed;
		private boolean retry;

		public long getId() {
			return tactics_problem_id;
		}

		public String getInitialFen() {
			return initial_fen;
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

		public int getRating() {
			return rating;
		}

		public int getAvgSeconds() {
			return average_seconds;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}



		public boolean isStop() {
			return stop;
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}

		public boolean isWasShowed() {
			return wasShowed;
		}

		public void setWasShowed(boolean wasShowed) {
			this.wasShowed = wasShowed;
		}

		public boolean isRetry() {
			return retry;
		}

		public void setRetry(boolean retry) {
			this.retry = retry;
		}


		public String getSecondsSpentStr() {
			return AppUtils.getSecondsTimeFromSecondsStr(secondsSpent);
		}

		public void setSecondsSpent(long secondsSpent) {
			this.secondsSpent = secondsSpent;
		}

		public void increaseSecondsPassed() {
			secondsSpent++;
		}

		public TacticResultItem getResultItem() {
			return resultItem;
		}

		public void setResultItem(String[] values) {
			this.resultItem = new TacticResultItem(values);
		}

		public void setResultItem(TacticResultItem resultItem) {
			this.resultItem = resultItem;
		}

		public long getSecondsSpent() {
			return secondsSpent;
		}


		public void setId(long id) {
			tactics_problem_id = id;
		}

		public void setFen(String fen) {
			initial_fen = fen;
		}

		public void setMoveList(String moveList) {
			clean_move_string = moveList;
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
	}

}
