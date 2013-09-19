package com.chess.db.tasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.QueryParams;


public class DeleteByUriTask extends AbstractUpdateTask<Cursor, Long> {

    private ContentResolver contentResolver;
    private QueryParams params;

    public DeleteByUriTask(TaskUpdateInterface<Cursor> taskFace, QueryParams params) {
        super(taskFace);
        this.params = params;
        contentResolver = taskFace.getMeContext().getContentResolver();
    }

    @Override
    protected Integer doTheTask(Long... ids) {
        Uri uri = params.getUri();

		int deletedCnt;

        if(ids.length > 0){
            int cnt = ids.length;
            String[] arguments = new String[cnt];
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0; i<cnt; i++){
                stringBuilder.append(DbDataManager.SELECTION_ID);
                arguments[i] = String.valueOf(ids[i]);
            }
            deletedCnt = contentResolver.delete(uri, stringBuilder.toString(), arguments);
        }else{
			deletedCnt = contentResolver.delete(uri, null, null);
        }

        if (deletedCnt > 0) {
            result = StaticData.RESULT_OK;
        } else {
            result = StaticData.VALUE_NOT_EXIST;
        }
        return result;
    }

}
