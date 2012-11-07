package com.chess.db.tasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBDataManager;
import com.chess.db.QueryParams;

/**
 * Used to get a much amout of data, because query is performs in UI thread
 */
public class QueryForCursorTask extends AbstractUpdateTask<Cursor, Long> {

    private ContentResolver contentResolver;
    private QueryParams params;

    public QueryForCursorTask(TaskUpdateInterface<Cursor> taskFace, QueryParams params) {
        super(taskFace);
        this.params = params;
        contentResolver = taskFace.getMeContext().getContentResolver();
    }

    @Override
    protected Integer doTheTask(Long... ids) {
        Uri uri = params.getUri();


        if(/*ids != null*/ ids.length > 0){
            int cnt = ids.length;
            String[] arguments = new String[cnt];
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0; i<cnt; i++){
                stringBuilder.append(DBDataManager.SELECTION_ID);
                arguments[i] = String.valueOf(ids[i]);
            }
            item = contentResolver.query(uri, params.getProjection(), stringBuilder.toString(), arguments, params.getOrder());

            item.moveToFirst();
        }else{
            item = contentResolver.query(uri, params.getProjection(), params.getSelection(), params.getArguments(), params.getOrder());
        }

        if (item.moveToFirst()) {
            result = StaticData.RESULT_OK;
        } else {
            result = StaticData.VALUE_NOT_EXIST;
        }
        return result;
    }

}
