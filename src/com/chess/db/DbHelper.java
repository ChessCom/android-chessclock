package com.chess.db;

import com.chess.backend.RestHelper;

public class DbHelper {

	public static QueryParams getAllByUri(int uriCode) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[uriCode]);
//		queryParams.setSelection(DBDataManager.SELECTION_USER);
//		queryParams.setArguments(new String[]{"erik"});
		return queryParams;
	}

	public static QueryParams getDailyCurrentListGamesParams(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_CURRENT_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{username});
		return queryParams;
	}

	public static QueryParams getDailyCurrentMyListGamesParams(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_CURRENT_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{username, RestHelper.V_TRUE});
		return queryParams;
	}

	public static QueryParams getDailyCurrentTheirListGamesParams(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_CURRENT_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{username, RestHelper.V_FALSE});
		return queryParams;
	}

	public static QueryParams getDailyFinishedListGamesParams(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_FINISHED_GAMES]);
		queryParams.setProjection(DBDataManager.PROJECTION_FINISHED_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{username});
		return queryParams;
	}

//	public static QueryParams getRecentDailyOpponentParams(Context context){
//		QueryParams queryParams = new QueryParams();
//		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_FINISHED_GAMES]);
//		queryParams.setProjection(DBDataManager.PROJECTION_DAILY_PLAYER_NAMES);
//		queryParams.setSelection(DBDataManager.SELECTION_USER);
//		queryParams.setArguments(new String[]{getAppData().getUserName(context)});
//		return queryParams;
//	}

	public static QueryParams getDailyGameParams(long gameId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_CURRENT_GAMES]);
		queryParams.setSelection(DBDataManager.SELECTION_USER_AND_ID);
		queryParams.setArguments(new String[]{username, String.valueOf(gameId)});
		return queryParams;
	}

	public static QueryParams getDailyFinishedGameParams(long gameId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_FINISHED_GAMES]);
		queryParams.setSelection(DBDataManager.SELECTION_USER_AND_ID);
		queryParams.setArguments(new String[]{username, String.valueOf(gameId)});
		return queryParams;
	}

	public static QueryParams getUserParams(String userName, int uriCode) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[uriCode]);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{userName});
		return queryParams;
	}

	public static QueryParams getArticlesListParams(int limitCnt) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.ARTICLES]);
		if (limitCnt > 0) {
			queryParams.setOrder(DBConstants.V_CATEGORY + DBDataManager.LIMIT_ + limitCnt);
		} else {
			queryParams.setOrder(DBConstants.V_CATEGORY);
		}

		return queryParams;
	}

	public static QueryParams getVideosByCategoryParams(int categoryId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.VIDEOS]);
		queryParams.setSelection(DBDataManager.SELECTION_CATEGORY_ID);
		queryParams.setArguments(new String[]{String.valueOf(categoryId)});
		return queryParams;
	}

	public static QueryParams getVideosListParams() {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.VIDEOS]);
		queryParams.setOrder(DBConstants.V_CATEGORY);
		return queryParams;
	}


	public static QueryParams getArticlesListByCategoryParams(String category) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.ARTICLES]);
		queryParams.setSelection(DBDataManager.SELECTION_CATEGORY);
		queryParams.setArguments(new String[]{category});
		queryParams.setOrder(DBConstants.V_CATEGORY);
		return queryParams;
	}

	public static QueryParams getForumTopicByCategoryParams(int categoryId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.FORUM_TOPICS]);
		queryParams.setSelection(DBDataManager.SELECTION_CATEGORY_ID);
		queryParams.setArguments(new String[]{String.valueOf(categoryId)});
		return queryParams;
	}

	public static QueryParams getForumCategoriesParams() {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.FORUM_CATEGORIES]);
		return queryParams;
	}

	public static QueryParams getForumPostsParams(int topicId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.FORUM_POSTS]);
		queryParams.setSelection(DBDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(topicId)});
		return queryParams;
	}


}
