package com.chess.ui.adapters;

import android.content.Context;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ArticleItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.09.13
 * Time: 11:06
 */
public class ArticlesPaginationAdapter extends PaginationCursorAdapter<ArticleItem.Data> {

	public ArticlesPaginationAdapter(Context context, ItemsCursorAdapter adapter,
								   TaskUpdateInterface<ArticleItem.Data> taskFace, LoadItem loadItem) {
		super(context, adapter, taskFace);
		this.loadItem = loadItem;
		setFirstPage(0);
	}

	@Override
	protected List<ArticleItem.Data> fetchMoreItems(int page) {
		if (loadItem != null) {
			loadItem.replaceRequestParams(RestHelper.P_PAGE, String.valueOf(page));
			ArticleItem item = null;
			try {
				item = RestHelper.getInstance().requestData(loadItem, ArticleItem.class, context);
			} catch (InternalErrorException e) {
				e.logMe();
			}

			if (item != null && item.getData().size() > 0) {
				result = StaticData.RESULT_OK;

				itemList = item.getData();
				return itemList;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}
