package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.stats.GraphData;
import com.chess.backend.entity.api.stats.LessonsStatsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.StaticData;
import com.chess.ui.views.ChartView;

import java.util.List;

import static com.chess.db.DbScheme.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 7:58
 */
public class SaveLessonsStatsTask extends AbstractUpdateTask<LessonsStatsItem.Data, Long> {

	private static final int TIMESTAMP = 0;
	private static final int RATING = 1;

	private ContentResolver contentResolver;
	protected static String[] sArguments2 = new String[2];
	private String username;

	public SaveLessonsStatsTask(TaskUpdateInterface<LessonsStatsItem.Data> taskFace, LessonsStatsItem.Data currentItem,
								ContentResolver resolver, String username) {
		super(taskFace);
		this.username = username;
		this.item = currentItem;
		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		List<LessonsStatsItem.RecentLessonData> recentLessonDataList = item.getLessons().getRecent();
		if (recentLessonDataList != null) {
			for (LessonsStatsItem.RecentLessonData lessonData : recentLessonDataList) {
				final String[] arguments = sArguments2;
				arguments[0] = String.valueOf(lessonData.getLessonId());
				arguments[1] = username;

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DbScheme.uriArray[Tables.LESSONS_RECENT_STATS.ordinal()];
				Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_AND_USER,
						DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments, null);

				ContentValues values = new ContentValues();

				values.put(V_USER, username);
				values.put(V_ID, lessonData.getLessonId());
				values.put(V_RATING, lessonData.getRating());
				values.put(V_SCORE, lessonData.getMyScore());
				values.put(V_CODE, lessonData.getCode());
				values.put(V_NAME, lessonData.getName());
				values.put(V_CATEGORY, lessonData.getCategory());

				DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
			}
		}

		// save graph data
		GraphData graphData = item.getGraph_data();
		if (graphData != null) {
			List<long[]> series = graphData.getSeries();
			if (series.size() > 0) {
				series.add(new long[]{System.currentTimeMillis() / 1000, series.get(series.size() - 1)[ChartView.VALUE]});
			}

			for (long[] graphPoints : series) {

				long timestamp = graphPoints[TIMESTAMP];
				final String[] arguments = sArguments2;
				arguments[0] = String.valueOf(timestamp);
				arguments[1] = username;

				Uri uri = DbScheme.uriArray[Tables.LESSONS_GRAPH_STATS.ordinal()];

				Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_GRAPH_LESSONS_RECORD,
						DbDataManager.SELECTION_TIMESTAMP_AND_USER, arguments, null);

				ContentValues values = new ContentValues();

				values.put(V_USER, username);
				values.put(V_TIMESTAMP, timestamp);
				values.put(V_RATING, graphPoints[RATING]);
				DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
			}
		}

		return StaticData.RESULT_OK;
	}
}