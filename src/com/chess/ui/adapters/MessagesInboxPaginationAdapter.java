package com.chess.ui.adapters;

import android.content.Context;
import android.util.Log;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ConversationItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.12.13
 * Time: 7:02
 */
public class MessagesInboxPaginationAdapter extends PaginationCursorAdapter<ConversationItem.Data> {

	public MessagesInboxPaginationAdapter(Context context, ItemsCursorAdapter adapter,
									TaskUpdateInterface<ConversationItem.Data> taskFace, LoadItem loadItem) {
		super(context, adapter, taskFace);
		this.loadItem = loadItem;
		setFirstPage(0);
	}

	@Override
	protected List<ConversationItem.Data> fetchMoreItems(int page) {
		if (loadItem != null) {
			loadItem.replaceRequestParams(RestHelper.P_PAGE, String.valueOf(page));
			ConversationItem item = null;
			try {
				item = RestHelper.getInstance().requestData(loadItem, ConversationItem.class, context);
			} catch (InternalErrorException e) {
				e.logMe();
			}
			Log.d("TEST", " item = " + item);

			if (item != null && item.getData() != null && item.getData().size() > 0) {
				result = StaticData.RESULT_OK;

				itemList = item.getData();
				Log.d("TEST", "fetchMoreItems itemsList count = " + itemList.size() + " page = " + page);

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