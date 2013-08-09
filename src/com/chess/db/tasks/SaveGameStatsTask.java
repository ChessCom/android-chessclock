package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.stats.GameStatsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbConstants;
import com.chess.db.DbDataManager;

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
	private final String userName;

	private ContentResolver resolver;
	protected static String[] arguments = new String[1];
	private String gameType;


	public SaveGameStatsTask(TaskUpdateInterface<GameStatsItem.Data> taskFace, GameStatsItem.Data item,
							 ContentResolver resolver, String gameType) {
		super(taskFace);
		this.gameType = gameType;
		this.item = item;
		this.resolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		userName = appData.getUsername();

	}

	@Override
	protected Integer doTheTask(Long... params) {

		if (gameType.equals(STANDARD)) {
			saveStatsGameLive(userName, DbConstants.Tables.GAME_STATS_LIVE_STANDARD.ordinal());
		} else if (gameType.equals(LIGHTNING)) {
			saveStatsGameLive(userName, DbConstants.Tables.GAME_STATS_LIVE_LIGHTNING.ordinal());
		} else if (gameType.equals(BLITZ)) {
			saveStatsGameLive(userName, DbConstants.Tables.GAME_STATS_LIVE_BLITZ.ordinal());
		} else if (gameType.equals(CHESS)) {
			saveDailyStats(userName, DbConstants.Tables.GAME_STATS_DAILY_CHESS.ordinal());
		} else if (gameType.equals(CHESS960)) {
			saveDailyStats(userName, DbConstants.Tables.GAME_STATS_DAILY_CHESS960.ordinal());
		}
		return StaticData.RESULT_OK;
	}

	private void saveStatsGameLive(String userName, int uriCode) {
		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);

		Uri uri = DbConstants.uriArray[uriCode];

		Cursor cursor = resolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

		ContentValues values = DbDataManager.putGameStatsLiveItemToValues(item, userName);

		if (cursor.moveToFirst()) {
			resolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
		} else {
			resolver.insert(uri, values);
		}
	}

	private void saveDailyStats(String userName, int uriCode) {
		final String[] userArgument = arguments;
		userArgument[0] = String.valueOf(userName);

		Uri uri = DbConstants.uriArray[uriCode];

		Cursor cursor = resolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

		ContentValues values = DbDataManager.putGameStatsDailyItemToValues(item, userName);

		if (cursor.moveToFirst()) {
			resolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
		} else {
			resolver.insert(uri, values);
		}
	}

}
