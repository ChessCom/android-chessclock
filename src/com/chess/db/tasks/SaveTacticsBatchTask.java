package com.chess.db.tasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.QueryParams;
import com.chess.model.TacticItem;

import java.util.List;


public class SaveTacticsBatchTask extends AbstractUpdateTask<TacticItem, Long> {

    private ContentResolver contentResolver;
    private QueryParams params;
	private List<TacticItem> tacticsBatch;

    public SaveTacticsBatchTask(TaskUpdateInterface<TacticItem> taskFace, List<TacticItem> tacticsBatch) {
        super(taskFace);
		this.tacticsBatch = tacticsBatch;
        params = new QueryParams();
		params.setUri(DBConstants.TACTICS_BATCH_CONTENT_URI);

		contentResolver = taskFace.getMeContext().getContentResolver();
    }

    @Override
    protected Integer doTheTask(Long... ids) {
		for (TacticItem tacticItem : tacticsBatch) {
			String[] arguments = new String[2];

			arguments[0] = String.valueOf(tacticItem.getId());
			arguments[1] = String.valueOf(tacticItem.getUser());

			Uri uri = params.getUri();
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_TACTIC_ITEM_ID_AND_USER,
					DBDataManager.SELECTION_ITEM_ID_AND_USER, arguments, params.getOrder());
			if (cursor.moveToFirst()) {
				contentResolver.update(Uri.parse(uri.toString() + "/" + DBDataManager.getId(cursor)),
						DBDataManager.putTacticItemToValues(tacticItem), null, null);
			} else {
				contentResolver.insert(uri, DBDataManager.putTacticItemToValues(tacticItem));
			}

			cursor.close();
		}

        result = StaticData.RESULT_OK;

        return result;
    }


}
