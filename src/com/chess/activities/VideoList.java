package com.chess.activities;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.chess.core.AppConstants;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.model.VideoItem;
import com.chess.utilities.MyProgressDialog;
import com.chess.views.VideosAdapter;

import java.util.ArrayList;

public class VideoList extends CoreActivity {
	private ArrayList<VideoItem> items = new ArrayList<VideoItem>();
	private VideosAdapter videosAdapter = null;
	private ListView videosListView;
	private TextView videoUpgrade;
	private int page = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videolist);

		videoUpgrade = (TextView) findViewById(R.id.videoUpgrade);
		boolean liveMembershipLevel =
				lccHolder.getUser() != null ? mainApp.isLiveChess() && (lccHolder.getUser().getMembershipLevel() < 50) : false;
		if (liveMembershipLevel
				|| (!mainApp.isLiveChess() && Integer.parseInt(mainApp.getSharedData().getString(AppConstants.USER_PREMIUM_STATUS, "0")) < 3)) {
			videoUpgrade.setVisibility(View.VISIBLE);
			videoUpgrade.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
							"http://www." + LccHolder.HOST + "/login.html?als=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") +
									"&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html?c=androidvideos")));
				}
			});
		} else {
			videoUpgrade.setVisibility(View.GONE);
		}

		videosListView = (ListView) findViewById(R.id.videosLV);
		videosListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.parse(items.get(pos).values.get("view_url").trim()), "video/*");
				startActivity(i);
			}
		});
		videosListView.setOnScrollListener(new OnScrollListener() {
			private boolean update = false;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE) {
					if (update) {
						page++;
						String skill = "&skill_level=" + extras.getString(AppConstants.VIDEO_SKILL_LEVEL);
						String category = "&category=" + extras.getString(AppConstants.VIDEO_CATEGORY);
						appService.RunSingleTask(0,
								"http://www." + LccHolder.HOST + "/api/get_videos?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&page-size=20&page=" + page + skill + category,
								progressDialog = new MyProgressDialog(ProgressDialog.show(VideoList.this, null, getString(R.string.loading), true))
						);
						update = false;
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem == totalItemCount - visibleItemCount)
					update = true;
			}
		});
	}

	@Override
	public void LoadNext(int code) {
	}

	@Override
	public void LoadPrev(int code) {
		finish();
	}

	@Override
	public void Update(int code) {
		if (code == -1) {
			if (appService != null && videosAdapter == null) {
				String skill = "&skill_level=" + extras.getString(AppConstants.VIDEO_SKILL_LEVEL);
				String category = "&category=" + extras.getString(AppConstants.VIDEO_CATEGORY);
				appService.RunSingleTask(0,
						"http://www." + LccHolder.HOST + "/api/get_videos?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&page-size=20&page=" + page + skill + category,
						progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true))
				);
			}
		} else if (code == 0) {
			String[] tmp = response.trim().split("[|]");
			if (tmp.length == 3) {
				tmp = tmp[2].split("<--->");
			} else return;
			if (page == 1)
				items.clear();
			for (String v : tmp) {
				items.add(new VideoItem(v.split("<->")));
			}
			if (videosAdapter == null) {
				videosAdapter = new VideosAdapter(VideoList.this, R.layout.videolistelement, items);
				videosListView.setAdapter(videosAdapter);
			} else
				videosAdapter.notifyDataSetChanged();

		}
	}
}
