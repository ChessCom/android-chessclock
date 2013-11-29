package com.chess.db;

import com.chess.backend.RestHelper;
import com.chess.statics.Symbol;

public class DbHelper {

	public static QueryParams getAll(DbScheme.Tables table) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[table.ordinal()]);
		return queryParams;
	}

	public static QueryParams getTableForUser(String username, DbScheme.Tables uriName) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[uriName.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{username});
		return queryParams;
	}

	public static QueryParams getDailyCurrentListGames(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.DAILY_CURRENT_GAMES.ordinal()]);
		queryParams.setProjection(DbDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DbDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{username});
		// SELECT *  FROM daily_current_games ORDER BY is_my_turn DESC, time_remaining DESC // use for multiple
		queryParams.setOrder(DbScheme.V_TIME_REMAINING + DbDataManager.DESCEND + Symbol.COMMA
				+ Symbol.SPACE + DbScheme.V_IS_MY_TURN + DbDataManager.DESCEND);
		return queryParams;
	}

	public static QueryParams getDailyCurrentMyListGames(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.DAILY_CURRENT_GAMES.ordinal()]);
		queryParams.setProjection(DbDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DbDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{username, RestHelper.V_TRUE});
		return queryParams;
	}

	public static QueryParams getDailyCurrentTheirListGames(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.DAILY_CURRENT_GAMES.ordinal()]);
		queryParams.setProjection(DbDataManager.PROJECTION_CURRENT_GAMES);
		queryParams.setSelection(DbDataManager.SELECTION_USER_TURN);
		queryParams.setArguments(new String[]{username, RestHelper.V_FALSE});
		return queryParams;
	}

	public static QueryParams getDailyFinishedListGames(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.DAILY_FINISHED_GAMES.ordinal()]);
		queryParams.setProjection(DbDataManager.PROJECTION_FINISHED_GAMES);
		queryParams.setSelection(DbDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{username});
		return queryParams;
	}

	public static QueryParams getLiveArchiveListGames(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LIVE_ARCHIVE_GAMES.ordinal()]);
		queryParams.setProjection(DbDataManager.PROJECTION_LIVE_ARCHIVE_GAMES);
		queryParams.setSelection(DbDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{username});
		return queryParams;
	}

	public static QueryParams getLiveArchiveGame(long gameId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LIVE_ARCHIVE_GAMES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_USER_AND_ID);
		queryParams.setArguments(new String[]{username, String.valueOf(gameId)});
		return queryParams;
	}

//	public static QueryParams getRecentDailyOpponent(Context context){
//		QueryParams queryParams = new QueryParams();
//		queryParams.setUri(DbScheme.uriArray[DbScheme.DAILY_FINISHED_GAMES]);
//		queryParams.setProjection(DbDataManager.PROJECTION_DAILY_PLAYER_NAMES);
//		queryParams.setSelection(DbDataManager.SELECTION_USER);
//		queryParams.setArguments(new String[]{getAppData().getUsername(context)});
//		return queryParams;
//	}

	public static QueryParams getDailyGame(long gameId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.DAILY_CURRENT_GAMES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_USER_AND_ID);
		queryParams.setArguments(new String[]{username, String.valueOf(gameId)});
		return queryParams;
	}

	public static QueryParams getDailyFinishedGame(long gameId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.DAILY_FINISHED_GAMES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_USER_AND_ID);
		queryParams.setArguments(new String[]{username, String.valueOf(gameId)});
		return queryParams;
	}

	/* Article */
	public static QueryParams getArticlesList(int limitCnt) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.ARTICLES.ordinal()]);
		if (limitCnt > 0) {
			queryParams.setOrder(DbScheme.V_CREATE_DATE + DbDataManager.DESCEND + DbDataManager.LIMIT_ + limitCnt);
		} else {
			queryParams.setOrder(DbScheme.V_CATEGORY);
		}

		return queryParams;
	}

	public static QueryParams getArticleById(long articleId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.ARTICLES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(articleId)});
		return queryParams;
	}


	public static QueryParams getArticlesCommentsById(long articleId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.ARTICLE_COMMENTS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_PARENT_ID);
		queryParams.setArguments(new String[]{String.valueOf(articleId)});
		queryParams.setOrder(DbScheme.V_CREATE_DATE + DbDataManager.DESCEND);
		return queryParams;
	}

	/* Videos */
	public static QueryParams getVideoById(long videoId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.VIDEOS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(videoId)});
		return queryParams;
	}

	public static QueryParams getVideoCommentsById(long articleId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.VIDEO_COMMENTS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_PARENT_ID);
		queryParams.setArguments(new String[]{String.valueOf(articleId)});
		queryParams.setOrder(DbScheme.V_CREATE_DATE + DbDataManager.DESCEND);
		return queryParams;
	}

	public static QueryParams getVideosByCategory(int categoryId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.VIDEOS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_CATEGORY_ID);
		queryParams.setArguments(new String[]{String.valueOf(categoryId)});
		return queryParams;
	}

	public static QueryParams getVideosList() {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.VIDEOS.ordinal()]);
		queryParams.setOrder(DbScheme.V_CATEGORY);
		return queryParams;
	}

	public static QueryParams getArticlesListByCategory(int categoryId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.ARTICLES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_CATEGORY_ID);
		queryParams.setArguments(new String[]{String.valueOf(categoryId)});
		queryParams.setOrder(DbScheme.V_TITLE);
		return queryParams;
	}

	/* Forums */
	public static QueryParams getForumCategories() {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.FORUM_CATEGORIES.ordinal()]);
		return queryParams;
	}

	public static QueryParams getForumTopicByCategory(int categoryId, int currentPage) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.FORUM_TOPICS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_CATEGORY_ID_AND_PAGE);
		queryParams.setArguments(new String[]{String.valueOf(categoryId), String.valueOf(currentPage)});
		return queryParams;
	}

	public static QueryParams getForumTopicById(int topicId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.FORUM_TOPICS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(topicId)});
		return queryParams;
	}

	public static QueryParams getForumPostsById(int topicId, int currentPage) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.FORUM_POSTS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_ITEM_ID_AND_PAGE);
		queryParams.setArguments(new String[]{String.valueOf(topicId), String.valueOf(currentPage)});
		return queryParams;
	}

	/* Lessons */
	public static QueryParams getLessonsCurriculumCategories() {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LESSONS_CATEGORIES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_IS_CURRICULUM);
		queryParams.setArguments(new String[]{String.valueOf(1)}); // 1 - true
		return queryParams;
	}

	public static QueryParams getLessonsLibraryCategories() {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LESSONS_CATEGORIES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_IS_CURRICULUM);
		queryParams.setArguments(new String[]{String.valueOf(0)}); // 0 - false
		return queryParams;
	}

	public static QueryParams getLessonCourseById(int courseId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LESSONS_COURSES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(courseId)});
		return queryParams;
	}

	public static QueryParams getLessonCoursesForUser(String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LESSONS_COURSE_LIST.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_USER);
		queryParams.setArguments(new String[]{username});
		return queryParams;
	}

	public static QueryParams getLessonsListByCourseId(int courseId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LESSONS_LESSONS_LIST.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_COURSE_ID_AND_USER);
		queryParams.setArguments(new String[]{String.valueOf(courseId), username});
		return queryParams;
	}

	public static QueryParams getMentorLessonById(long lessonId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LESSONS_MENTOR_LESSONS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(lessonId)});
		return queryParams;
	}

	public static QueryParams getUserLessonById(long lessonId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LESSONS_USER_LESSONS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_ITEM_ID_AND_USER);
		queryParams.setArguments(new String[]{String.valueOf(lessonId), username});
		return queryParams;
	}

	public static QueryParams getLessonPositionsById(long lessonId) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LESSONS_POSITIONS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_ITEM_ID);
		queryParams.setArguments(new String[]{String.valueOf(lessonId)});
		return queryParams;
	}

	public static QueryParams getLessonPositionMovesById(long lessonId, int positionNumber) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LESSONS_POSITION_MOVES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_ITEM_ID_AND_CURRENT_POSITION);
		queryParams.setArguments(new String[]{String.valueOf(lessonId), String.valueOf(positionNumber)});
		return queryParams;
	}


	public static QueryParams getLessonsByCategory(int categoryId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.LESSONS_LESSONS_LIST.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_CATEGORY_ID_AND_USER);
		queryParams.setArguments(new String[]{String.valueOf(categoryId), username});
		return queryParams;
	}

	/* Messages Conversation */
	public static QueryParams getConversationMessagesById(long conversationId, String username) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.CONVERSATIONS_MESSAGES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_USER_CONVERSATION_ID);
		queryParams.setOrder(DbScheme.V_CREATE_DATE + DbDataManager.ASCEND);
		queryParams.setArguments(new String[]{username, String.valueOf(conversationId)});
		return queryParams;
	}

	/* Graph Stats Data */
	public static QueryParams getGraphItemForUser(String username, String gameType) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.GAME_STATS_GRAPH_DATA.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_GRAPH_TABLE);
		queryParams.setArguments(new String[]{gameType, username});
		return queryParams;
	}

	/* Explorer */
	public static QueryParams getExplorerMovesForFen(String fen) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.EXPLORER_MOVES.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_FEN);
		queryParams.setArguments(new String[]{fen});
		return queryParams;
	}

	public static QueryParams getExplorerVariationNamesForFen(String fen) {
		QueryParams queryParams = new QueryParams();
		queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.EXPLORER_VARIATIONS.ordinal()]);
		queryParams.setSelection(DbDataManager.SELECTION_FEN);
		queryParams.setArguments(new String[]{fen});
		return queryParams;
	}

}
