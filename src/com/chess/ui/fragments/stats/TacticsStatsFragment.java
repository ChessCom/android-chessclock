package com.chess.ui.fragments.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.TacticsHistoryItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.02.13
 * Time: 17:59
 */
public class TacticsStatsFragment extends CommonLogicFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_tactics_stats_frame, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_TACTICS_STATS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<TacticsHistoryItem>(new StatsItemUpdateListener()).executeTask(loadItem);
	}

	private class StatsItemUpdateListener extends ChessUpdateListener<TacticsHistoryItem> {

		public StatsItemUpdateListener() {
			super(TacticsHistoryItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingProgress(show);
		}

		@Override
		public void updateData(TacticsHistoryItem returnedObj) {
			super.updateData(returnedObj);

		}
	}
}
