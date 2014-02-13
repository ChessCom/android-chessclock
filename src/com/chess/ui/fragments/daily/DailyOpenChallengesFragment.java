package com.chess.ui.fragments.daily;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.daily_games.DailyChallengeItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.DailyOpenSeeksAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.01.14
 * Time: 20:43
 */
public class DailyOpenChallengesFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, ItemClickListenerFace {

	private OpenSeeksUpdateListener openSeeksUpdateListener;
	private DailyOpenSeeksAdapter challengesGamesAdapter;
	private DailyChallengeItem.Data selectedChallengeItem;
	private int successToastMsgId;
	private DailyUpdateListener challengeInviteUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		openSeeksUpdateListener = new OpenSeeksUpdateListener();
		challengesGamesAdapter = new DailyOpenSeeksAdapter(this, null, getImageFetcher());
		challengeInviteUpdateListener = new DailyUpdateListener();
		pullToRefresh(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setAdapter(challengesGamesAdapter);

	}

	@Override
	public void onResume() {
		super.onResume();

		if (isNetworkAvailable()) {
			updateData();
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		if (isNetworkAvailable()) {
			updateData();
		}
	}

	private void updateData() {
		// get Current games first
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_SEEKS);
		loadItem.addRequestParams(P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<DailyChallengeItem>(openSeeksUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.gameResultTxt) { // used as accept button
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			selectedChallengeItem = challengesGamesAdapter.getItem(position);
			acceptChallenge();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DailyChallengeItem.Data challengeItem = (DailyChallengeItem.Data) parent.getItemAtPosition(position);

		if (!isTablet) {
			getActivityFace().openFragment(DailyInviteFragment.createInstance(challengeItem));
		} else {
			getActivityFace().openFragment(DailyInviteFragmentTablet.createInstance(challengeItem));
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private void acceptChallenge() {
		LoadItem loadItem = LoadHelper.acceptChallenge(getUserToken(), selectedChallengeItem.getGameId());
		successToastMsgId = R.string.challenge_accepted;

		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
	}

	private class DailyUpdateListener extends ChessUpdateListener<BaseResponseItem> {


		public DailyUpdateListener() {
			super(BaseResponseItem.class);
		}

		@Override
		public void updateData(BaseResponseItem returnedObj) {
			showToast(successToastMsgId);
			getActivityFace().showPreviousFragment();

		}
	}

	private class OpenSeeksUpdateListener extends ChessLoadUpdateListener<DailyChallengeItem> {

		private OpenSeeksUpdateListener() {
			super(DailyChallengeItem.class);
		}

		@Override
		public void updateData(DailyChallengeItem returnedObj) {
			super.updateData(returnedObj);

			challengesGamesAdapter.setItemsList(returnedObj.getData());
		}
	}

}
