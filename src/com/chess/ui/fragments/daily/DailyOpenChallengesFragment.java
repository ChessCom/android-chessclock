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
import com.chess.backend.tasks.RequestBatchJsonTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.DailyOpenSeeksAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.01.14
 * Time: 20:43
 */
public class DailyOpenChallengesFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, ItemClickListenerFace {

	private DailyOpenSeeksAdapter openChallengesAdapter;
	private DailyChallengeItem.Data selectedChallengeItem;
	private int successToastMsgId;
	private DailyUpdateListener challengeInviteUpdateListener;
	private CustomSectionedAdapter sectionedAdapter;
	private DailyOpenSeeksAdapter myChallengesAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();


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
		listView.setAdapter(sectionedAdapter);
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
		LoadItem loadItem1 = new LoadItem();
		loadItem1.setLoadPath(RestHelper.getInstance().CMD_SEEKS);
		loadItem1.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		LoadItem loadItem2 = new LoadItem();
		loadItem2.setLoadPath(RestHelper.getInstance().CMD_SEEKS);
		loadItem2.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem2.addRequestParams(RestHelper.P_SHOW_ONLY_MINE, RestHelper.V_TRUE);

		Class[] classes = {DailyChallengeItem.class, DailyChallengeItem.class};
		new RequestBatchJsonTask(new BatchUpdateListener(), classes).executeTask(loadItem1, loadItem2);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.gameResultTxt) { // used as accept button
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			selectedChallengeItem = openChallengesAdapter.getItem(position);
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

	private class BatchUpdateListener extends ChessLoadUpdateListener<List> {

		private BatchUpdateListener() {
			super(List.class);
		}

		@Override
		public void updateData(List returnedObj) {
			super.updateData(returnedObj);

			List<? extends BaseResponseItem> responseItems = returnedObj;

			// get Open challenges
			if (responseItems.get(0).getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
				DailyChallengeItem dailyChallengeItem = (DailyChallengeItem) responseItems.get(0);
				if (dailyChallengeItem != null) {
					openChallengesAdapter.setItemsList(dailyChallengeItem.getData());
				}
			}

			// get My Own Challenges
			if (responseItems.get(1).getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
				DailyChallengeItem dailyMyChallengeItem = (DailyChallengeItem) responseItems.get(1);
				if (dailyMyChallengeItem != null) {
					List<DailyChallengeItem.Data> myChallenges = dailyMyChallengeItem.getData();
					for (DailyChallengeItem.Data myChallenge : myChallenges) {
						myChallenge.setMyChallenge(true);
					}

					myChallengesAdapter.setItemsList(myChallenges);
				}
			}

			sectionedAdapter.notifyDataSetChanged();
		}
	}

	private void init() {
		// init adapters
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.text_section_header_dark);

		openChallengesAdapter = new DailyOpenSeeksAdapter(this, null, getImageFetcher());
		myChallengesAdapter = new DailyOpenSeeksAdapter(this, null, getImageFetcher());

		sectionedAdapter.addSection(getString(R.string.open_challenges), openChallengesAdapter);
		sectionedAdapter.addSection(getString(R.string.my_challenges), myChallengesAdapter);

		challengeInviteUpdateListener = new DailyUpdateListener();
	}

}
