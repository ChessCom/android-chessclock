package com.chess.db.tasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.model.TacticItem;

import java.util.ArrayList;
import java.util.List;


public class SaveTacticsBatchTask extends AbstractUpdateTask<TacticItem, Long> {

    private ContentResolver contentResolver;
	private final List<TacticItem> tacticsBatch;
	private static String[] arguments = new String[2];

	public SaveTacticsBatchTask(TaskUpdateInterface<TacticItem> taskFace, List<TacticItem> tacticsBatch) {
        super(taskFace);
		this.tacticsBatch = new ArrayList<TacticItem>();
		this.tacticsBatch.addAll(tacticsBatch);

		contentResolver = taskFace.getMeContext().getContentResolver();
    }

    @Override
    protected Integer doTheTask(Long... ids) {
		synchronized (tacticsBatch) {
			for (TacticItem tacticItem : tacticsBatch) {

				arguments[0] = String.valueOf(tacticItem.getId());
				arguments[1] = tacticItem.getUser();

				Uri uri = DBConstants.TACTICS_BATCH_CONTENT_URI;
				Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_TACTIC_ITEM_ID_AND_USER,
						DBDataManager.SELECTION_TACTIC_ID_AND_USER, arguments, null);
				if (cursor.moveToFirst()) {
					contentResolver.update(Uri.parse(uri.toString() + DBDataManager.SLASH_ + DBDataManager.getId(cursor)),
							DBDataManager.putTacticItemToValues(tacticItem), null, null);
				} else {
					contentResolver.insert(uri, DBDataManager.putTacticItemToValues(tacticItem));
				}

				cursor.close();
			}
		}

        result = StaticData.RESULT_OK;

        return result;
    }


}
