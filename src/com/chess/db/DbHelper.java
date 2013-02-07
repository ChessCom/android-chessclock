package com.chess.db;

import android.content.Context;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.AppData;

public class DbHelper {

	public static QueryParams getDailyCurrentMyListGamesParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.ECHESS_CURRENT_LIST_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_LIST_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{AppData.getUserName(context), RestHelper.V_TRUE});
		return queryParams;
	}

	public static QueryParams getDailyCurrentTheirListGamesParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.ECHESS_CURRENT_LIST_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_LIST_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{AppData.getUserName(context), RestHelper.V_FALSE});
		return queryParams;
	}

	public static QueryParams getEchessFinishedListGamesParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.ECHESS_FINISHED_LIST_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_FINISHED_LIST_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{AppData.getUserName(context)});
		return queryParams;
	}

	public static QueryParams getEchessGameParams(Context context, long gameId){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.ECHESS_ONLINE_GAMES]);
		queryParams.setSelection(DBDataManager.SELECTION_GAME_ID);
		queryParams.setArguments(new String[]{AppData.getUserName(context), String.valueOf(gameId)});
		return queryParams;
	}

	public static QueryParams getFriendsListParams(Context context){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.FRIENDS]);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{AppData.getUserName(context)});
		return queryParams;
	}

	public static QueryParams getArticlesListParams(){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.ARTICLES]);
		queryParams.setOrder(DBConstants.V_CATEGORY);
		return queryParams;
	}

	public static QueryParams getVideosListByCategoryParams(String category){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.VIDEOS]);
		queryParams.setSelection(DBDataManager.SELECTION_CATEGORY);
		queryParams.setArguments(new String[]{category});
		queryParams.setOrder(DBConstants.V_CATEGORY);
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

	public static QueryParams getStatsLiveStandardParams(String user){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.STATS_LIVE_STANDARD]);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{user});
		return queryParams;
	}

	public static QueryParams getStatsLiveLightningParams(String user){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.STATS_LIVE_LIGHTNING]);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{user});
		return queryParams;
	}

	public static QueryParams getStatsLiveBlitzParams(String user){
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.STATS_LIVE_BLITZ]);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{user});
		return queryParams;
	}

}
