package com.chess.ui.adapters;

import android.content.Context;
import android.util.Log;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.VideoSingleItem;
import com.chess.backend.entity.api.VideosItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;

import java.util.List;

public class VideosPaginationAdapter extends PaginationCursorAdapter<VideoSingleItem.Data> {


	public VideosPaginationAdapter(Context context, ItemsCursorAdapter adapter,
								   TaskUpdateInterface<VideoSingleItem.Data> taskFace, LoadItem loadItem) {
		super(context, adapter, taskFace);
		this.loadItem = loadItem;
		setFirstPage(0);
	}

	@Override
	protected List<VideoSingleItem.Data> fetchMoreItems(int page) {
		if (loadItem != null) {
			loadItem.replaceRequestParams(RestHelper.P_PAGE, String.valueOf(page));
			VideosItem item = null;
			try {
				item = RestHelper.getInstance().requestData(loadItem, VideosItem.class, context);
			} catch (InternalErrorException e) {
				e.logMe();
			}

			if (item != null && item.getData().size() > 0) {
				result = StaticData.RESULT_OK;

				itemList = item.getData();
				Log.d("TEST", "fetchMoreItems itemsList count = " + itemList.size() + " page = " + page);

				return itemList;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
