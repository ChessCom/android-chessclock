package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.stats.GameStatsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.StaticData;

import java.util.List;

import static com.chess.db.DbScheme.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.02.13
 * Time: 19:32
 */
public class SaveGameStatsTask extends AbstractUpdateTask<GameStatsItem.Data, Long> {

	public static final String STANDARD = "standard";
	public static final String BLITZ = "blitz";
	public static final String LIGHTNING = "lightning";
	public static final String CHESS = "chess";
	public static final String CHESS960 = "chess960";
	private static final int TIMESTAMP = 0;
	private static final int RATING = 1;
	private final String username;

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];
	protected static String[] sArguments3 = new String[3];
	private String gameType;


	public SaveGameStatsTask(TaskUpdateInterface<GameStatsItem.Data> taskFace, GameStatsItem.Data item,
							 ContentResolver contentResolver, String gameType, String username) {
		super(taskFace);
		this.gameType = gameType;
		this.item = item;
		this.contentResolver = contentResolver;
		this.username = username;
	}

	@Override
	protected Integer doTheTask(Long... params) {

		if (gameType.equals(STANDARD)) {
			saveStatsGameLive( DbScheme.Tables.GAME_STATS_LIVE_STANDARD.ordinal());
			saveGraphStats(STANDARD);
		} else if (gameType.equals(LIGHTNING)) {
			saveStatsGameLive(DbScheme.Tables.GAME_STATS_LIVE_LIGHTNING.ordinal());
			saveGraphStats(LIGHTNING);
		} else if (gameType.equals(BLITZ)) {
			saveStatsGameLive(DbScheme.Tables.GAME_STATS_LIVE_BLITZ.ordinal());
			saveGraphStats(BLITZ);
		} else if (gameType.equals(CHESS)) {
			saveDailyStats(DbScheme.Tables.GAME_STATS_DAILY_CHESS.ordinal());
			saveGraphStats(CHESS);
		} else if (gameType.equals(CHESS960)) {
			saveDailyStats(DbScheme.Tables.GAME_STATS_DAILY_CHESS960.ordinal());
			saveGraphStats(CHESS960);
		}
		return StaticData.RESULT_OK;
	}

	private void saveStatsGameLive(int uriCode) {
		final String[] userArgument = arguments;
		userArgument[0] = username;

		Uri uri = DbScheme.uriArray[uriCode];

		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

		ContentValues values = DbDataManager.putGameStatsLiveItemToValues(item, username);

		DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	private void saveDailyStats(int uriCode) {
		final String[] userArgument = arguments;
		userArgument[0] = username;

		Uri uri = DbScheme.uriArray[uriCode];

		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER, DbDataManager.SELECTION_USER, userArgument, null);

		ContentValues values = DbDataManager.putGameStatsDailyItemToValues(item, username);

		DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
	}

	private void saveGraphStats(String gameType) {
		int maxX = item.getGraphData().getMaxX();
		int minY = item.getGraphData().getMinY();

		List<long[]> series = item.getGraphData().getSeries();
		for (long[] graphPoints : series) {

			long timestamp = graphPoints[TIMESTAMP];
			final String[] arguments = sArguments3;
			arguments[0] = String.valueOf(timestamp);
			arguments[1] = gameType;
			arguments[2] = username;

			Uri uri = DbScheme.uriArray[DbScheme.Tables.GAME_STATS_GRAPH_DATA.ordinal()];

			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_GRAPH_RECORD,
					DbDataManager.SELECTION_GRAPH_RECORD, arguments, null);

			ContentValues values = new ContentValues();

			values.put(V_USER, username);
			values.put(V_TIMESTAMP, timestamp);
			values.put(V_MIN_Y, minY);
			values.put(V_MAX_X, maxX);
			values.put(V_RATING, graphPoints[RATING]);
			values.put(V_GAME_TYPE, gameType);

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}
	}

}
