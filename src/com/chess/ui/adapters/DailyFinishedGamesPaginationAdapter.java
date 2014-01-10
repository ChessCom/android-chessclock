package com.chess.ui.adapters;

import android.content.Context;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.daily_games.DailyFinishedGameData;
import com.chess.backend.entity.api.daily_games.DailyFinishedGamesItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.01.14
 * Time: 16:57
 */
public class DailyFinishedGamesPaginationAdapter extends PaginationCursorAdapter<DailyFinishedGameData> {

	public DailyFinishedGamesPaginationAdapter(Context context, ItemsCursorAdapter adapter,
											   TaskUpdateInterface<DailyFinishedGameData> taskFace, LoadItem loadItem) {
		super(context, adapter, taskFace);
		this.loadItem = loadItem;
		setFirstPage(0);
	}

	@Override
	protected List<DailyFinishedGameData> fetchMoreItems(int page) {
		if (loadItem != null) {
			loadItem.replaceRequestParams(RestHelper.P_PAGE, String.valueOf(page));
			DailyFinishedGamesItem item = null;
			try {
				item = RestHelper.getInstance().requestData(loadItem, DailyFinishedGamesItem.class, context);
			} catch (InternalErrorException e) {
				e.logMe();
			}

			if (item != null && item.getData() != null && item.getData().getGames() != null
					&& item.getData().getGames().size() > 0) {
				result = StaticData.RESULT_OK;

				itemList = item.getData().getGames();
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