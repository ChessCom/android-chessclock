package com.chess.db.tasks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ArticleItem;
import com.chess.backend.entity.new_api.FriendsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.google.gson.Gson;

import java.util.List;


public class SaveArticlesListTask extends AbstractUpdateTask<ArticleItem.Data, Long> {
	private static final String TAG = "SaveFriendsListTask";

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveArticlesListTask(TaskUpdateInterface<ArticleItem.Data> taskFace, List<ArticleItem.Data> currentItems,
								ContentResolver resolver) {
        super(taskFace);
		this.itemList = currentItems;
		this.contentResolver = resolver;
	}


	@Override
    protected Integer doTheTask(Long... ids) {

		for (ArticleItem.Data currentItem : itemList) {
			final String[] arguments2 = arguments;
			arguments2[0] = String.valueOf(currentItem.getId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DBConstants.ARTICLES_CONTENT_URI;

			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_ARTICLE_ID,
					DBDataManager.SELECTION_ARTICLE_ID, arguments2, null);
			if (cursor.moveToFirst()) {
				contentResolver.update(Uri.parse(uri.toString() + DBDataManager.SLASH_ + DBDataManager.getId(cursor)),
						DBDataManager.putArticleItemToValues(currentItem), null, null);
			} else {
				contentResolver.insert(uri, DBDataManager.putArticleItemToValues(currentItem));
			}

			cursor.close();

		}

        result = StaticData.RESULT_OK;

        return result;
    }


}
