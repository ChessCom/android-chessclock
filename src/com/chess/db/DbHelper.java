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

	public static QueryParams getDailyCurrentListGames(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.DAILY_CURRENT_GAMES.ordinal()]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{username});
		return queryParams;
	}

	public static QueryParams getDailyCurrentMyListGames(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.DAILY_CURRENT_GAMES.ordinal()]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{username, RestHelper.V_TRUE});
		return queryParams;
	}

	public static QueryParams getDailyCurrentTheirListGames(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.DAILY_CURRENT_GAMES.ordinal()]);
		queryParams.setProjection(DBDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{username, RestHelper.V_FALSE});
		return queryParams;
	}

	public static QueryParams getDailyFinishedListGames(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.DAILY_FINISHED_GAMES.ordinal()]);
		queryParams.setProjection(DBDataManager.PROJECTION_FINISHED_GAMES);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{username});
		return queryParams;
	}

//	public static QueryParams getRecentDailyOpponent(Context context){
//		QueryParams queryParams = new QueryParams();
//		queryParams.setUri(DBConstants.uriArray[DBConstants.DAILY_FINISHED_GAMES]);
//		queryParams.setProjection(DBDataManager.PROJECTION_DAILY_PLAYER_NAMES);
//		queryParams.setSelection(DBDataManager.SELECTION_USER);
//		queryParams.setArguments(new String[]{getAppData().getUsername(context)});
//		return queryParams;
//	}

	public static QueryParams getDailyGame(long gameId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.DAILY_CURRENT_GAMES.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_USER_AND_ID);
		queryParams.setArguments(new String[]{username, String.valueOf(gameId)});
		return queryParams;
	}

	public static QueryParams getDailyFinishedGame(long gameId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.DAILY_FINISHED_GAMES.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_USER_AND_ID);
		queryParams.setArguments(new String[]{username, String.valueOf(gameId)});
		return queryParams;
	}

	public static QueryParams getUser(String userName, int uriCode) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[uriCode]);
		queryParams.setSelection(DBDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{userName});
		return queryParams;
	}

	public static QueryParams getArticlesList(int limitCnt) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.ARTICLES.ordinal()]);
		if (limitCnt > 0) {
			queryParams.setOrder(DBConstants.V_CATEGORY + DBDataManager.LIMIT_ + limitCnt);
		} else {
			queryParams.setOrder(DBConstants.V_CATEGORY);
		}

		return queryParams;
	}

	public static QueryParams getVideosByCategory(int categoryId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.VIDEOS.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_CATEGORY_ID);
		queryParams.setArguments(new String[]{String.valueOf(categoryId)});
		return queryParams;
	}

	public static QueryParams getVideosList() {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.VIDEOS.ordinal()]);
		queryParams.setOrder(DBConstants.V_CATEGORY);
		return queryParams;
	}


	public static QueryParams getArticlesListByCategory(String category) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.ARTICLES.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_CATEGORY);
		queryParams.setArguments(new String[]{category});
		queryParams.setOrder(DBConstants.V_CATEGORY);
		return queryParams;
	}

	/* Forums */
	public static QueryParams getForumCategories() {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.FORUM_CATEGORIES.ordinal()]);
		return queryParams;
	}

	public static QueryParams getForumTopicByCategory(int categoryId, int currentPage) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.FORUM_TOPICS.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_CATEGORY_ID_AND_PAGE);
		queryParams.setArguments(new String[]{String.valueOf(categoryId), String.valueOf(currentPage)});
		return queryParams;
	}

	public static QueryParams getForumTopicById(int topicId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.FORUM_TOPICS.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(topicId)});
		return queryParams;
	}

	public static QueryParams getForumPostsById(int topicId, int currentPage) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.FORUM_POSTS.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_ITEM_ID_AND_PAGE);
		queryParams.setArguments(new String[]{String.valueOf(topicId), String.valueOf(currentPage)});
		return queryParams;
	}

	/* Lessons */
	public static QueryParams getLessonCourseById(int courseId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.LESSONS_COURSES.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(courseId)});
		return queryParams;
	}

	public static QueryParams getLessonsListByCourseId(int courseId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.LESSONS_LESSONS_LIST.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_CATEGORY_ID);
		queryParams.setArguments(new String[]{String.valueOf(courseId)});
		return queryParams;
	}

	public static QueryParams getMentorLessonById(long lessonId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.LESSONS_MENTOR_LESSONS.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(lessonId)});
		return queryParams;
	}

	public static QueryParams getUserLessonById(long lessonId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.LESSONS_USER_LESSONS.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_ITEM_ID_AND_USER);
		queryParams.setArguments(new String[]{String.valueOf(lessonId), username});
		return queryParams;
	}

	public static QueryParams getLessonPositionsById(long lessonId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.LESSONS_POSITIONS.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(lessonId)});
		return queryParams;
	}

	public static QueryParams getLessonPositionMovesById(long lessonId, int positionNumber) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DBConstants.uriArray[DBConstants.Tables.LESSONS_POSITION_MOVES.ordinal()]);
		queryParams.setSelection(DBDataManager.SELECTION_ITEM_ID_AND_CURRENT_POSITION);
		queryParams.setArguments(new String[]{String.valueOf(lessonId), String.valueOf(positionNumber)});
		return queryParams;
	}

}
