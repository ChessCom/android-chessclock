package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 6:29
 */
public class DailyGamesAllItem extends BaseResponseItem<DailyGamesAllItem.DailyGamesAllData>{
	/*
{
    "status": "success",
    "data": {
        "challenges": [
			{
				"game_seek_id": 94,
				"opponent_username": "erik",
				"opponent_rating": 1471,
				"opponent_win_count": 7,
				"opponent_loss_count": 7,
				"opponent_draw_count": 2,
				"color": 0,
				"color_descriptive": "Random",
				"days_per_move": 3,
				"game_type": 1,
				"is_rated": false,
				"initial_setup_fen": null
			}...
		],
        "current": [
            {
                "game_id": 35000530,
                "i_play_as": 1,
                "game_type_code": "chess",
                "opponent_username": "erikwwww",
                "opponent_rating": "1200",
                "time_remaining": 0,
                "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "timestamp": 1339849800,
                "last_move_from_square": null,
                "last_move_to_square": null,
                "is_draw_offer_pending": false,
                "is_opponent_online": false,
                "is_my_turn": false,
                "has_new_message": false
            }....
        ],
        "finished": [
        	{
				"game_id": 35000494,
				"i_play_as": 2,
				"game_type_code": "chess",
				"opponent_username": "deepgreene",
				"opponent_rating": null,
				"time_remaining": 0,
				"fen": "rnbqkbnr/pppp1ppp/8/4p3/4PP2/8/PPPP2PP/RNBQKBNR b KQkq f3 1 2",
				"timestamp": 1339127284,
				"last_move_from_square": "f2",
				"last_move_to_square": "f4",
				"is_opponent_online": false,
				"game_score": 0
        	}...
		]
    }
}
	 */

	public static class DailyGamesAllData{
		private List<DailyChallengeData> challenges;
		private List<DailyCurrentGameData> current;
		private List<DailyFinishedGameData> finished;

		public List<DailyChallengeData> getChallenges() {
			return challenges;
		}

		public void setChallenges(List<DailyChallengeData> challenges) {
			this.challenges = challenges;
		}

		public List<DailyCurrentGameData> getCurrent() {
			return current;
		}

		public void setCurrent(List<DailyCurrentGameData> current) {
			this.current = current;
		}

		public List<DailyFinishedGameData> getFinished() {
			return finished;
		}

		public void setFinished(List<DailyFinishedGameData> finished) {
			this.finished = finished;
		}
	}
}
