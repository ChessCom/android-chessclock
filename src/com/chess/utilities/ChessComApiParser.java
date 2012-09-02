package com.chess.utilities;

import android.content.Context;
import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChessComApiParser {

	//Challenge a friend
	public static String[] GetFriendsParse(String result) {
		if (result.trim().length() > 8)
			return result.substring(9).trim().split("[|]");
		else
			return new String[]{StaticData.SYMBOL_EMPTY};
	}

	//online new game
//	public static ArrayList<GameListChallengeItem> ViewOpenChallengeParse(String result) {
//		ArrayList<GameListChallengeItem> output = new ArrayList<GameListChallengeItem>();
//
//		String[] g = result.split("[|]");
//		int count = g.length - 1;
//
//		int i, a;
//		for (i = 1; i <= count; i++) {
//			String[] tmp = new String[11];
//			a = 0;
//			for (String s : g[i].split(RestHelper.SYMBOL_PARAMS_SPLIT)) {
//				tmp[a++] = s;
//			}
//			output.add(new GameListChallengeItem(tmp));
//		}
//		return output;
//	}

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
			int gamescount = Integer.valueOf(GamesArray[0].substring(8));
			int i, j, inc = 0;
			String[] tmp = GamesArray[1].split(RestHelper.SYMBOL_PARAMS_SPLIT);

			for (i = 0; i < gamescount; i++) {
				String[] tmp2 = new String[17];
				for (j = 0; j < 17; j++) {
					tmp2[j] = tmp[inc++];
				}
				output.add(new GameListCurrentItem(tmp2));
			}
		} catch (Exception e) {   // TODO
			return output;
		}
		return output;
	}

	public static ArrayList<GameListFinishedItem> getFinishedOnlineGames(String result, Context temporaryContext) {
		Log.d("TEST", "getFinishedOnlineGames input = " +result);
		ArrayList<GameListFinishedItem> output = new ArrayList<GameListFinishedItem>();
//		Success+<total_games_returned>:(
// <game_id>
// :<color>:<game_type>:<username_string_length>:<opponent_username>:<opponent_rating>:<time_remaining_amount>:<time_remaining_units>:<fen_string_length>:<fen>:<timestamp>:<last_move_from_square>:<last_move_to_square>:<is_opponent_online>:<game_result>:)0-n



		String[] gamesArray = result.split(RestHelper.SYMBOL_PARAMS_SPLIT, 2);
		int gamesCnt;
		try {
			gamesCnt = Integer.parseInt(gamesArray[0].substring(8));
		} catch (NumberFormatException e) {  // TODO
			Log.e("getFinishedOnlineGames", e.toString());
			return output;
		}

		int i, j, inc = 0;
		String[] tmp = gamesArray[1].split(RestHelper.SYMBOL_PARAMS_SPLIT);
		for (i = 0; i < gamesCnt; i++) {
			String[] tmp2 = new String[15];
			for (j = 0; j < 15; j++) {
				tmp2[j] = tmp[inc++];
			}
			try { // TODO temp stick! remove after investigation
				output.add(new GameListFinishedItem(tmp2));
			} catch (NumberFormatException ex) {

				Map<String, String> extraData = new HashMap<String, String>();
				extraData.put(AppConstants.RESPONSE, AppData.getUserName(temporaryContext) + " " + result);
				BugSenseHandler.log("DEBUG_FINISHED_GAMES_LIST", extraData, ex);

				return output;
			}
		}
		return output;
	}

	public static GameOnlineItem GetGameParseV3(String result) {
        Log.d("TEST", "moves from online server = " + result);
		return new GameOnlineItem(result.split(RestHelper.SYMBOL_PARAMS_SPLIT));
	}

	public static ArrayList<MessageItem> receiveMessages(String result) {
		ArrayList<MessageItem> output = new ArrayList<MessageItem>();
		String[] tmp = result.substring(8).split(RestHelper.SYMBOL_PARAMS_SPLIT);
		int i;
		for (i = 0; i < tmp.length; i++) {
			if (tmp[i].length() > 1)
				output.add(new MessageItem(tmp[i].substring(0, 1), tmp[i].substring(1)));
		}
		return output;
	}
}
