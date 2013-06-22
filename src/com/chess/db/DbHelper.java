package com.chess.db;

import android.content.Context;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.AppData;

public class DbHelper {

	public static QueryParams getAllByUri(int uriCode){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[uriCode]);
//		queryParams.setSelection(DBDataManager.SELECTION_USER);
//		queryParams.setArguments(new String[]{"erik"});
		return queryParams;
	}

	public static QueryParams getDailyCurrentListGamesParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_CURRENT_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{AppData.getUserName(context)});
		return queryParams;
	}

	public static QueryParams getDailyCurrentMyListGamesParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_CURRENT_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{AppData.getUserName(context), RestHelper.V_TRUE});
		return queryParams;
	}

	public static QueryParams getDailyCurrentTheirListGamesParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_CURRENT_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{AppData.getUserName(context), RestHelper.V_FALSE});
		return queryParams;
	}

	public static QueryParams getDailyFinishedListGamesParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_FINISHED_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_FINISHED_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{AppData.getUserName(context)});
		return queryParams;
	}

	public static QueryParams getRecentDailyOpponentParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_FINISHED_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_DAILY_PLAYER_NAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{AppData.getUserName(context)});
		return queryParams;
	}

	public static QueryParams getDailyGameParams(Context context, long gameId){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_CURRENT_GAMES]);
		queryParams.setSelection(DBDataManager.SELECTION_GAME_ID);
		queryParams.setArguments(new String[]{AppData.getUserName(context), String.valueOf(gameId)});
		return queryParams;
	}

	public static QueryParams getDailyFinishedGameParams(Context context, long gameId){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_FINISHED_GAMES]);
		queryParams.setSelection(DBDataManager.SELECTION_GAME_ID);
		queryParams.setArguments(new String[]{AppData.getUserName(context), String.valueOf(gameId)});
		return queryParams;
	}

	public static QueryParams getUserParams(String userName, int uriCode){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[uriCode]);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{userName});
		return queryParams;
	}

	public static QueryParams getArticlesListParams(int limitCnt){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.ARTICLES]);
		if (limitCnt > 0) {
			queryParams.setOrder(DBConstants.V_CATEGORY + DBDataManager.LIMIT_ + limitCnt);
		} else {
			queryParams.setOrder(DBConstants.V_CATEGORY);
		}

		return queryParams;
	}

	public static QueryParams getVideosListByCategoryParams(String category/*, String sortOrder*/){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.VIDEOS]);
		queryParams.setSelection(DBDataManager.SELECTION_CATEGORY);
		queryParams.setArguments(new String[]{category});
//		queryParams.setOrder(sortOrder);
		return queryParams;
	}

	public static QueryParams getVideosListParams(){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.VIDEOS]);
		queryParams.setOrder(DBConstants.V_CATEGORY);
		return queryParams;
	}


	public static QueryParams getArticlesListByCategoryParams(String category){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.ARTICLES]);
		queryParams.setSelection(DBDataManager.SELECTION_CATEGORY);
		queryParams.setArguments(new String[]{category});
		queryParams.setOrder(DBConstants.V_CATEGORY);
		return queryParams;
	}
}
