package com.chess.ui.adapters;

import android.content.Context;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.VideoItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.utilities.AppUtils;

import java.util.List;

// TODO adjust for VideoCategoriesFragment
public class VideosPaginationAdapter extends PaginationCursorAdapter<VideoItem.Data> {

	private static final String TAG = "VideosPaginationAdapter";

	protected LoadItem loadItem;

	public VideosPaginationAdapter(Context context, ItemsCursorAdapter adapter,
								   TaskUpdateInterface<VideoItem.Data> taskFace, LoadItem loadItem) {
		super(context, adapter, taskFace);
        this.loadItem = loadItem;
		setFirstPage(0);
	}

	@Override
	protected List<VideoItem.Data> fetchMoreItems(int page) {
		loadItem.replaceRequestParams(RestHelper.P_PAGE, String.valueOf(page));
		VideoItem item = null;
		try {
			 item = RestHelper.getInstance().requestData(loadItem, VideoItem.class, AppUtils.getAppId(context));
		} catch (InternalErrorException e) {
			e.logMe();
		}

		if (item != null) {
			result = StaticData.RESULT_OK;
			itemList = item.getData();
		}

        return itemList;
	}


}
