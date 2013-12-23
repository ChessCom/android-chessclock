package com.chess.ui.adapters;

import android.content.Context;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.backend.entity.api.LessonsItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.10.13
 * Time: 11:52
 */
public class LessonsPaginationAdapter extends PaginationCursorAdapter<LessonSingleItem> {

	public LessonsPaginationAdapter(Context context, ItemsCursorAdapter adapter,
								   TaskUpdateInterface<LessonSingleItem> taskFace, LoadItem loadItem) {
		super(context, adapter, taskFace);
		this.loadItem = loadItem;
		setFirstPage(0);
	}

	@Override
	protected List<LessonSingleItem> fetchMoreItems(int page) {
		if (loadItem != null) {
			loadItem.replaceRequestParams(RestHelper.P_PAGE, String.valueOf(page));
			LessonsItem item = null;
			try {
				item = RestHelper.getInstance().requestData(loadItem, LessonsItem.class, context);
			} catch (InternalErrorException e) {
				e.logMe();
			}

			if (item != null && item.getData().getLessons() != null && item.getData().getLessons().size() > 0) {
				result = StaticData.RESULT_OK;

				itemList = item.getData().getLessons();
				return itemList;
			} else {
				result = StaticData.EMPTY_DATA;

				return null;
			}
		} else {
			result = StaticData.EMPTY_DATA;

			return null;
		}
	}

}