package com.chess.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.AppData;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.lcc.android.LccHolder;
import com.chess.model.VideoItem;
import com.chess.ui.adapters.VideosAdapter;
import com.chess.ui.adapters.VideosAdapter2;
import com.chess.ui.adapters.VideosPaginationAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.utilities.MyProgressDialog;

import java.util.ArrayList;

public class VideoListActivity2 extends LiveBaseActivity implements OnItemClickListener,
        View.OnClickListener{
    private ArrayList<VideoItem> items = new ArrayList<VideoItem>();
    private VideosAdapter videosAdapter;
    private ListView listView;
    private TextView videoUpgrade;
    private int page = 1;
    private boolean update;
    private String skill;
    private String category;
    //	private VideosListUpdateListener videosListUpdateListener;
    private VideosListItemsUpdateListener videosListItemUpdateListener;
    private VideosPaginationAdapter paginationAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videolist);

        videoUpgrade = (TextView) findViewById(R.id.upgradeBtn);
        boolean liveMembershipLevel = lccHolder.getUser() != null && mainApp.isLiveChess()
                && (lccHolder.getUser().getMembershipLevel() < 50);
        if (liveMembershipLevel
                || (!mainApp.isLiveChess() && Integer.parseInt(preferences.getString(AppConstants.USER_PREMIUM_STATUS, "0")) < 3)) {
            videoUpgrade.setVisibility(View.VISIBLE);
            videoUpgrade.setOnClickListener(this);

        } else {
            videoUpgrade.setVisibility(View.GONE);
        }

        listView = (ListView) findViewById(R.id.videosLV);
        listView.setOnItemClickListener(this);
//		listView.setOnScrollListener(this);

        skill = extras.getString(AppConstants.VIDEO_SKILL_LEVEL);
        category = extras.getString(AppConstants.VIDEO_CATEGORY);

//		videosListUpdateListener = new VideosListUpdateListener();
        videosListItemUpdateListener = new VideosListItemsUpdateListener();

    }

    @Override
    public void update(int code) {
    }


    @Override
    protected void onResume() {
        super.onResume();

        updateList();
    }

    private void updateList() {
//		"http://www." + LccHolder.HOST + "/api/get_videos?id="
//				+ preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY)
//				+ "&page-size=20&page=" + page + skill + category,

        LoadItem loadItem = new LoadItem();
        loadItem.setLoadPath(RestHelper.GET_VIDEOS);
        loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(coreContext));
        loadItem.addRequestParams(RestHelper.P_PAGE_SIZE, RestHelper.V_VIDEO_LIST_CNT);
        loadItem.addRequestParams(RestHelper.P_PAGE, String.valueOf(page));
        loadItem.addRequestParams(RestHelper.P_SKILL_LEVEL, skill);
        loadItem.addRequestParams(RestHelper.P_CATEGORY, category);

//		new GetStringObjTask(videosListUpdateListener).execute(loadItem);

        //set Pagination adapter params
        ArrayList<VideoItem> itemsList = new ArrayList<VideoItem>();
        VideosAdapter2 videosAdapter2 = new VideosAdapter2(this, itemsList);

        paginationAdapter = new VideosPaginationAdapter(this, videosAdapter2, videosListItemUpdateListener, loadItem);

        listView.setAdapter(paginationAdapter);
    }

    private class VideosListItemsUpdateListener extends AbstractUpdateListener<VideoItem> {
        public VideosListItemsUpdateListener() {
            super(coreContext);
        }

        @Override
        public void showProgress(boolean show) {
            getActionBarHelper().setRefreshActionItemState(show);
        }
    }


/*	private class VideosListUpdateListener extends ChessUpdateListener {
		public VideosListUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			Log.d("TEST", "Video response = " + returnedObj);
			String[] responseArray = returnedObj.trim().split("[|]");
			if (responseArray.length == 3) {
				responseArray = responseArray[2].split("<--->");
			} else
				return;

			if (page == 1)
				items.clear();

			for (String responseItem : responseArray) {
				items.add(new VideoItem(responseItem.split("<->")));
			}
			if (videosAdapter == null) {
				videosAdapter = new VideosAdapter(VideoListActivity2.this, R.layout.videolistelement, items);
				listView.setAdapter(videosAdapter);
			} else
				videosAdapter.notifyDataSetChanged();
		}
	}*/

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


}
