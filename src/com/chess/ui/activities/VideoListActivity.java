package com.chess.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.model.VideoItem;
import com.chess.ui.adapters.VideosAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.MyProgressDialog;

import java.util.ArrayList;

public class VideoListActivity extends LiveBaseActivity implements OnItemClickListener, View.OnClickListener, OnScrollListener {
	private ArrayList<VideoItem> items = new ArrayList<VideoItem>();
	private VideosAdapter videosAdapter = null;
	private ListView videosListView;
	private TextView videoUpgrade;
	private int page = 1;
    private boolean update;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videolist);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		videoUpgrade = (TextView) findViewById(R.id.upgradeBtn);
		boolean liveMembershipLevel =
				lccHolder.getUser() != null ? mainApp.isLiveChess() && (lccHolder.getUser().getMembershipLevel() < 50) : false;
		if (liveMembershipLevel
				|| (!mainApp.isLiveChess() && Integer.parseInt(mainApp.getSharedData().getString(AppConstants.USER_PREMIUM_STATUS, "0")) < 3)) {
			videoUpgrade.setVisibility(View.VISIBLE);
			videoUpgrade.setOnClickListener(this);

		} else {
			videoUpgrade.setVisibility(View.GONE);
		}

		videosListView = (ListView) findViewById(R.id.videosLV);
		videosListView.setOnItemClickListener(this);
		videosListView.setOnScrollListener(this);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);
	}

	@Override
	public void update(int code) {
		if (code == INIT_ACTIVITY) {
			if (appService != null && videosAdapter == null) {
				String skill = "&skill_level=" + extras.getString(AppConstants.VIDEO_SKILL_LEVEL);
				String category = "&category=" + extras.getString(AppConstants.VIDEO_CATEGORY);
				appService.RunSingleTask(0,
						"http://www." + LccHolder.HOST + "/api/get_videos?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
                                + "&page-size=20&page=" + page + skill + category,
						progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true))
				);
			}
		} else if (code == 0) {
			String[] responseArray = response.trim().split("[|]");
			if (responseArray.length == 3) {
				responseArray = responseArray[2].split("<--->");
			} else return;
			if (page == 1)
				items.clear();
			for (String responseItem : responseArray) {
				items.add(new VideoItem(responseItem.split("<->")));
			}
			if (videosAdapter == null) {
				videosAdapter = new VideosAdapter(VideoListActivity.this, R.layout.videolistelement, items);
				videosListView.setAdapter(videosAdapter);
			} else
				videosAdapter.notifyDataSetChanged();

		}
	}

	@Override
	public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse(items.get(pos).values.get(AppConstants.VIEW_URL).trim()), "video/*");
		startActivity(i);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(mainApp.getMembershipVideoIntent());
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem == totalItemCount - visibleItemCount)
			update = true;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE) {  // TODO implement pagination adapter
			if (update) {
				page++;
				String skill = "&skill_level=" + extras.getString(AppConstants.VIDEO_SKILL_LEVEL);
				String category = "&category=" + extras.getString(AppConstants.VIDEO_CATEGORY);
				appService.RunSingleTask(0,
						"http://www." + LccHolder.HOST + "/api/get_videos?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
                                + "&page-size=20&page=" + page + skill + category,
						progressDialog = new MyProgressDialog(ProgressDialog.show(VideoListActivity.this, null, getString(R.string.loading), true))
				);
				update = false;
			}
		}
	}
}
