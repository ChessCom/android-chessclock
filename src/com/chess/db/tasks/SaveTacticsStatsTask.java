package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.stats.TacticsHistoryItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.09.13
 * Time: 16:34
 */
public class SaveTacticsStatsTask extends AbstractUpdateTask<TacticsHistoryItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] sArguments2 = new String[2];
	private String username;

	public SaveTacticsStatsTask(TaskUpdateInterface<TacticsHistoryItem.Data> taskFace, TacticsHistoryItem.Data currentItem,
								ContentResolver resolver, String username) {
		super(taskFace);
		this.username = username;
		this.item = currentItem;

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		// save daily ratings
		List<TacticsHistoryItem.Data.DailyStats> dailyStatsList = item.getDailyStats();
		if (dailyStatsList != null) {
			for (TacticsHistoryItem.Data.DailyStats dailyStats : dailyStatsList) {
				final String[] arguments = sArguments2;
				arguments[0] = String.valueOf(dailyStats.getTimestamp());
				arguments[1] = username;

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DbScheme.uriArray[DbScheme.Tables.TACTICS_DAILY_STATS.ordinal()];
				Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_TIMESTAMP_AND_USER,
						DbDataManager.SELECTION_TIMESTAMP_AND_USER, arguments, null);

				ContentValues values = DbDataManager.putTacticDailyStatsItemToValues(dailyStats, username);

				DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
			}
		}

		// save recent problems stats
		List<TacticsHistoryItem.Data.RecentProblem> recentProblems = item.getRecentProblems();
		if (recentProblems != null) {
			for (TacticsHistoryItem.Data.RecentProblem problem : recentProblems) {
				final String[] arguments = sArguments2;
				arguments[0] = String.valueOf(problem.getId());
				arguments[1] = username;

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DbScheme.uriArray[DbScheme.Tables.TACTICS_PROBLEM_STATS.ordinal()];
				Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_AND_USER,
						DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments, null);

				ContentValues values = DbDataManager.putTacticProblemStatsItemToValues(problem, username);

				DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
			}
		}

		return StaticData.RESULT_OK;
	}
}