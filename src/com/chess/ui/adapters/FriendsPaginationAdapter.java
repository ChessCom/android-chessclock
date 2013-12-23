package com.chess.ui.adapters;

import android.content.Context;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.FriendsItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.12.13
 * Time: 16:59
 */
public class FriendsPaginationAdapter extends PaginationCursorAdapter<FriendsItem.Data> {

	public FriendsPaginationAdapter(Context context, ItemsCursorAdapter adapter,
									TaskUpdateInterface<FriendsItem.Data> taskFace, LoadItem loadItem) {
		super(context, adapter, taskFace);
		this.loadItem = loadItem;
		setFirstPage(0);
	}

	@Override
	protected List<FriendsItem.Data> fetchMoreItems(int page) {
		if (loadItem != null) {
			loadItem.replaceRequestParams(RestHelper.P_PAGE, String.valueOf(page));
			FriendsItem item = null;
			try {
				item = RestHelper.getInstance().requestData(loadItem, FriendsItem.class, context);
			} catch (InternalErrorException e) {
				e.logMe();
			}

			if (item != null && item.getData() != null && item.getData().size() > 0) {
				result = StaticData.RESULT_OK;

				itemList = item.getData();
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
