package com.chess.utilities;

import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.StaticData;
import com.chess.model.*;

import java.util.ArrayList;

public class ChessComApiParser {

	private static String[] finishedGameArray = new String[15];
	private static String[] currentGameArray = new String[17];

	//Challenge a friend
	public static String[] getFriendsParse(String result) {
		if (result.trim().length() > RestHelper.R_SUCCESS_P.length())
			return result.substring(9).trim().split("[|]");
		else
			return new String[]{StaticData.SYMBOL_EMPTY};
	}

	public static ArrayList<GameListChallengeItem> getChallengesGames(String result) {
		ArrayList<GameListChallengeItem> output = new ArrayList<GameListChallengeItem>();

		String[] g = result.split("[|]");
		int count = g.length - 1;

		int i, a;
		for (i = 1; i <= count; i++) {
			String[] tmp = new String[11];
			a = 0;
			for (String s : g[i].split(RestHelper.SYMBOL_PARAMS_SPLIT)) {
				tmp[a++] = s;
			}
			output.add(new GameListChallengeItem(tmp));
		}
		return output;
	}

	public static ArrayList<GameListCurrentItem> getCurrentOnlineGames(String result) {
		ArrayList<GameListCurrentItem> output = new ArrayList<GameListCurrentItem>();

		if (result.contains("|")) {
			return output;
		}

		String[] GamesArray = result.split(RestHelper.SYMBOL_PARAMS_SPLIT, 2);
		try {
			int gamesCount = Integer.valueOf(GamesArray[0].substring(RestHelper.R_SUCCESS_P.length()));
			int i, j, inc = 0;
			String[] tmp = GamesArray[1].split(RestHelper.SYMBOL_PARAMS_SPLIT);

			for (i = 0; i < gamesCount; i++) {
				for (j = 0; j < 17; j++) {
					currentGameArray[j] = tmp[inc++];
				}
				output.add(new GameListCurrentItem(currentGameArray));
			}
		} catch (Exception e) {   // TODO
			return output;
		}
		return output;
	}

	public static ArrayList<GameListFinishedItem> getFinishedOnlineGames(String result) {
		ArrayList<GameListFinishedItem> output = new ArrayList<GameListFinishedItem>();
//		Success+<total_games_returned>:(
// <game_id>
// :<color>:<game_type>:<username_string_length>:<opponent_username>:<opponent_rating>:<time_remaining_amount>:<time_remaining_units>:<fen_string_length>:<fen>:<timestamp>:<last_move_from_square>:<last_move_to_square>:<is_opponent_online>:<game_result>:)0-n



		String[] responseArray = result.split(RestHelper.SYMBOL_PARAMS_SPLIT, 2);
		int gamesCnt;
		try {
			gamesCnt = Integer.parseInt(responseArray[0].substring(RestHelper.R_SUCCESS_P.length()));
		} catch (NumberFormatException e) {  // TODO
			Log.e("getFinishedOnlineGames", e.toString());
			return output;
		}

		int i, j, inc = 0;
		String[] gamesArray = responseArray[1].split(RestHelper.SYMBOL_PARAMS_SPLIT);
		for (i = 0; i < gamesCnt; i++) {
			for (j = 0; j < 15; j++) {
				finishedGameArray[j] = gamesArray[inc++];
			}
			output.add(new GameListFinishedItem(finishedGameArray));
		}
		return output;
	}

	public static GameOnlineItem getGameParseV3(String result) {
		return new GameOnlineItem(result.split(RestHelper.SYMBOL_PARAMS_SPLIT));
	}

	public static ArrayList<MessageItem> receiveMessages(String result) {
		ArrayList<MessageItem> output = new ArrayList<MessageItem>();
		String[] tmp = result.substring(RestHelper.R_SUCCESS_P.length()).split(RestHelper.SYMBOL_PARAMS_SPLIT);
		int i;
		for (i = 0; i < tmp.length; i++) {
			if (tmp[i].length() > 1)
				output.add(new MessageItem(tmp[i].substring(0, 1), tmp[i].substring(1)));
		}
		return output;
	}
}
