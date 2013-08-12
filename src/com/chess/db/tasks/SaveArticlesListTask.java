package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.ArticleItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbScheme;
import com.chess.db.DbDataManager;

import java.util.ArrayList;
import java.util.List;


public class SaveArticlesListTask extends AbstractUpdateTask<ArticleItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveArticlesListTask(TaskUpdateInterface<ArticleItem.Data> taskFace, List<ArticleItem.Data> currentItems,
								ContentResolver resolver) {
        super(taskFace, new ArrayList<ArticleItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
    protected Integer doTheTask(Long... ids) {
		synchronized (itemList) {
			for (ArticleItem.Data currentItem : itemList) {
				final String[] arguments2 = arguments;
				arguments2[0] = String.valueOf(currentItem.getId());

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DbScheme.uriArray[DbScheme.Tables.ARTICLES.ordinal()];

				Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID,
						DbDataManager.SELECTION_ITEM_ID, arguments2, null);

				ContentValues values = DbDataManager.putArticleItemToValues(currentItem);

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
