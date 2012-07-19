package com.chess.utilities;

import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.StaticData;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.model.MessageItem;

import java.util.ArrayList;

public class ChessComApiParser {

	//Challenge a friend
	public static String[] GetFriendsParse(String result) {
		if (result.trim().length() > 8)
			return result.substring(9).trim().split("[|]");
		else
			return new String[]{StaticData.SYMBOL_EMPTY};
	}

	//online new game
	public static ArrayList<GameListItem> ViewOpenChallengeParse(String result) {
		ArrayList<GameListItem> output = new ArrayList<GameListItem>();

		String[] g = result.split("[|]");
		int count = g.length - 1;

		int i, a;
		for (i = 1; i <= count; i++) {
			String[] tmp = new String[11];
			a = 0;
			for (String s : g[i].split(RestHelper.SYMBOL_PARAMS_SPLIT)) {
				tmp[a++] = s;
			}
			GameListItem gle = new GameListItem(GameListItem.LIST_TYPE_CHALLENGES, tmp, false);
			output.add(gle);
		}
		return output;
	}

	//online game
	public static ArrayList<GameListItem> getChallengesGames(String result) {
		ArrayList<GameListItem> output = new ArrayList<GameListItem>();

		String[] g = result.split("[|]");
		int count = g.length - 1;

		int i, a;
		for (i = 1; i <= count; i++) {
			String[] tmp = new String[11];
			a = 0;
			for (String s : g[i].split(RestHelper.SYMBOL_PARAMS_SPLIT)) {
				tmp[a++] = s;
			}
			output.add(new GameListItem(GameListItem.LIST_TYPE_CHALLENGES, tmp, false));
		}
		return output;
	}

	public static ArrayList<GameListItem> getCurrentOnlineGames(String result) {
		ArrayList<GameListItem> output = new ArrayList<GameListItem>();

		if (result.contains("|")) {
			return output;
		}

		String[] GamesArray = result.split(RestHelper.SYMBOL_PARAMS_SPLIT, 2);
		try {
			int gamescount = new Integer(GamesArray[0].substring(8));
			int i, j, inc = 0;
			String[] tmp = GamesArray[1].split(RestHelper.SYMBOL_PARAMS_SPLIT);

			for (i = 0; i < gamescount; i++) {
				String[] tmp2 = new String[17];
				for (j = 0; j < 17; j++) {
					tmp2[j] = tmp[inc++];
				}
				output.add(new GameListItem(GameListItem.LIST_TYPE_CURRENT, tmp2, false));
			}
		} catch (Exception e) {
			return output;
		}
		return output;
	}

	public static ArrayList<GameListItem> getFinishedOnlineGames(String result) {
		ArrayList<GameListItem> output = new ArrayList<GameListItem>();

		String[] GamesArray = result.split(RestHelper.SYMBOL_PARAMS_SPLIT, 2);
		int gamescount;
		try {
			gamescount = Integer.parseInt(GamesArray[0].substring(8));
		} catch (NumberFormatException e) {
			return output;
		}

		int i, j, inc = 0;
		String[] tmp = GamesArray[1].split(RestHelper.SYMBOL_PARAMS_SPLIT);
		for (i = 0; i < gamescount; i++) {
			String[] tmp2 = new String[15];
			for (j = 0; j < 15; j++) {
				tmp2[j] = tmp[inc++];
			}
			output.add(new GameListItem(GameListItem.LIST_TYPE_FINISHED, tmp2, false));
		}
		return output;
	}

	//online game
	public static GameItem GetGameParseV3(String result) {
        Log.d("TEST", "moves from online server = " + result);
		return new GameItem(result.split(RestHelper.SYMBOL_PARAMS_SPLIT), false);
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
