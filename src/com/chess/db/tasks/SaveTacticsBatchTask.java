package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.TacticItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbConstants;
import com.chess.db.DbDataManager;

import java.util.ArrayList;
import java.util.List;


//public class SaveTacticsBatchTask extends AbstractUpdateTask<TacticItemOld, Long> {
public class SaveTacticsBatchTask extends AbstractUpdateTask<TacticItem.Data, Long> {

	private final String userName;
	private ContentResolver contentResolver;
	private final List<TacticItem.Data> tacticsBatch;
	private static String[] arguments = new String[2];

	public SaveTacticsBatchTask(TaskUpdateInterface<TacticItem.Data> taskFace, List<TacticItem.Data> tacticsBatch,
								ContentResolver resolver) {
        super(taskFace);
		this.tacticsBatch = new ArrayList<TacticItem.Data>();
		this.tacticsBatch.addAll(tacticsBatch);
		this.contentResolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		userName = appData.getUsername();

	}

    @Override
    protected Integer doTheTask(Long... ids) {
		synchronized (tacticsBatch) {
			for (TacticItem.Data tacticItem : tacticsBatch) {
				tacticItem.setUser(userName);
				arguments[0] = String.valueOf(tacticItem.getId());
				arguments[1] = userName;

				Uri uri = DbConstants.uriArray[DbConstants.Tables.TACTICS_BATCH.ordinal()];
				Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_AND_USER,
						DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments, null);

				ContentValues values = DbDataManager.putTacticItemToValues(tacticItem);

				if (cursor.moveToFirst()) {
					contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
				} else {
					contentResolver.insert(uri, values);
				}

				cursor.close();
			}
		}

        result = StaticData.RESULT_OK;

        return result;
    }


}
