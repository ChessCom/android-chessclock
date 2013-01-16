package com.chess.db;

import android.content.Context;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.AppData;

public class DbHelper {

	public static QueryParams getDailyCurrentMyListGamesParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.ECHESS_CURRENT_LIST_GAMES_CONTENT_URI);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_LIST_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{AppData.getUserName(context), RestHelper.V_TRUE});
		return queryParams;
	}

	public static QueryParams getDailyCurrentTheirListGamesParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.ECHESS_CURRENT_LIST_GAMES_CONTENT_URI);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_LIST_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{AppData.getUserName(context), RestHelper.V_FALSE});
		return queryParams;
	}

	public static QueryParams getEchessFinishedListGamesParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.ECHESS_FINISHED_LIST_GAMES_CONTENT_URI);
		queryParams.setProjection(DBDataManager.PROJECTION_FINISHED_LIST_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{AppData.getUserName(context)});
		return queryParams;
	}

	public static QueryParams getEchessGameParams(Context context, long gameId){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.ECHESS_ONLINE_GAMES_CONTENT_URI);
		queryParams.setSelection(DBDataManager.SELECTION_GAME_ID);
		queryParams.setArguments(new String[]{AppData.getUserName(context), String.valueOf(gameId)});
		return queryParams;
	}

}
