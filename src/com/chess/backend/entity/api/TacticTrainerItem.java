package com.chess.backend.entity.api;

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
            "rating": 1424,
            "average_seconds": 20
        }
    }
 	*/
	public static class Data {
		private TacticRatingData rating_info;
		private TacticItem.Data tactics_problem;

		public TacticRatingData getRatingInfo() {
			return rating_info;
		}

		public void setRatingInfo(TacticRatingData rating_info) {
			this.rating_info = rating_info;
		}

		public TacticItem.Data getTacticsProblem() {
			return tactics_problem;
		}

		public void setTacticsProblem(TacticItem.Data tactics_problem) {
			this.tactics_problem = tactics_problem;
		}
	}
}
