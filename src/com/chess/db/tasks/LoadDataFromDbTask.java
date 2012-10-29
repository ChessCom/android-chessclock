package com.chess.db.tasks;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataProvider;
import com.chess.db.QueryParams;


public class LoadDataFromDbTask extends AbstractUpdateTask<Cursor, Long> {

	private ContentResolver contentResolver;
	private QueryParams params;

	public LoadDataFromDbTask(TaskUpdateInterface<Cursor> taskFace, QueryParams params) {
		super(taskFace);
		this.params = params;
		contentResolver = taskFace.getMeContext().getContentResolver();
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		int result = StaticData.EMPTY_DATA;

		if (ids != null && ids.length > 0) {
			item = contentResolver.query(Uri.parse(params.getUri().toString() + "/" + ids[0]), params.getProjection(), params.getSelection(), params.getArguments(), params.getOrder());
		} else {
			if (params.isUseRawQuery()) {
				ContentProviderClient client = contentResolver.acquireContentProviderClient(DBConstants.PROVIDER_NAME);
				SQLiteDatabase dbHandle = ((DBDataProvider) client.getLocalContentProvider()).getDbHandle();
				StringBuilder projection = new StringBuilder();
				for (String projections : params.getProjection()) {
					projection.append(projections).append(StaticData.SYMBOL_COMMA);
				}
				// TODO hide to down level
				item = dbHandle.rawQuery("SELECT " + projection.toString().substring(0, projection.length() - 1)
						+ " FROM " + params.getDbName() + " " + params.getCommands(), null);
				client.release();

			} else {
				item = contentResolver.query(params.getUri(), params.getProjection(), params.getSelection(), params.getArguments(), params.getOrder());
			}
		}

		if (item.moveToFirst()) {
			result = StaticData.RESULT_OK;
		}

		return result;
	}

}
