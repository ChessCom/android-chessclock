package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.stats.GameStatsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.02.13
 * Time: 19:32
 */
public class SaveGameStatsTask extends AbstractUpdateTask<GameStatsItem.Data, Long> {

	private static final String STANDARD = "standard";
	private static final String BLITZ = "blitz";
	private static final String LIGHTNING = "lightning";
	private static final String CHESS = "chess";
	private static final String CHESS960 = "chess960";

	private ContentResolver resolver;
	protected static String[] arguments = new String[1];
	private String gameType;


	public SaveGameStatsTask(TaskUpdateInterface<GameStatsItem.Data> taskFace, GameStatsItem.Data item,
							 ContentResolver resolver, String gameType) {
		super(taskFace);
		this.gameType = gameType;
		this.item = item;
		this.resolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... params) {
		Context context = getTaskFace().getMeContext();
		if (context == null) {
			return StaticData.INTERNAL_ERROR;
		}
		String userName = AppData.getUserName(context);

		if (gameType.equals(STANDARD)) {
			saveStatsGameLive(userName, DBConstants.GAME_STATS_LIVE_STANDARD);
		} else if (gameType.equals(LIGHTNING)) {
			saveStatsGameLive(userName, DBConstants.GAME_STATS_LIVE_LIGHTNING);
		} else if (gameType.equals(BLITZ)) {
			saveStatsGameLive(userName, DBConstants.GAME_STATS_LIVE_BLITZ);
		} else if (gameType.equals(CHESS)) {
			saveDailyStats(userName, DBConstants.GAME_STATS_DAILY_CHESS);
		} else if (gameType.equals(CHESS960)) {
			saveDailyStats(userName, DBConstants.GAME_STATS_DAILY_CHESS960);
		}
		return StaticData.RESULT_OK;
	}

	private void saveStatsGameLive(String userName, int uriCode) {
		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);

		Uri uri = DBConstants.uriArray[uriCode];

		Cursor cursor = resolver.query(uri, DBDataManager.PROJECTION_USER, DBDataManager.SELECTION_USER, userArgument, null);

		ContentValues values = DBDataManager.putGameStatsLiveItemToValues(item, userName);

		if (cursor.moveToFirst()) {
			resolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
		} else {
			resolver.insert(uri, values);
		}
	}

	private void saveDailyStats(String userName, int uriCode) {
		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);

		Uri uri = DBConstants.uriArray[uriCode];

		Cursor cursor = resolver.query(uri, DBDataManager.PROJECTION_USER, DBDataManager.SELECTION_USER, userArgument, null);

		ContentValues values = DBDataManager.putGameStatsDailyItemToValues(item, userName);

		if (cursor.moveToFirst()) {
			resolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
		} else {
			resolver.insert(uri, values);
		}
	}

}
