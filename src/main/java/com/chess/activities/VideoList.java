package com.chess.activities;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.chess.R;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.model.VideoItem;
import com.chess.utilities.MyProgressDialog;
import com.chess.views.VideosAdapter;

public class VideoList extends CoreActivity {
	private ArrayList<VideoItem> Items = new ArrayList<VideoItem>();
	private VideosAdapter VA = null;
	private ListView videosLV;
	private int page = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videolist);

		videosLV = (ListView)findViewById(R.id.videosLV);
		videosLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
				Intent i = new Intent(Intent.ACTION_VIEW);
	            i.setDataAndType(Uri.parse(Items.get(pos).values.get("view_url").trim()), "video/*");
	            startActivity(i);
			}
		});
		videosLV.setOnScrollListener(new OnScrollListener() {
			private boolean update = false;
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == SCROLL_STATE_IDLE){
					if(update){
						page++;
						String skill = "&skill_level="+extras.getString("skill");
						String category = "&category="+extras.getString("category");
						appService.RunSingleTask(0,
							"http://www." + LccHolder.HOST + "/api/get_videos?id="+App.sharedData.getString("user_token", "")+"&page-size=20&page="+page+skill+category,
							PD = new MyProgressDialog(ProgressDialog.show(VideoList.this, null, getString(R.string.loading), true))
						);
						update = false;
					}
				}
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if(firstVisibleItem == totalItemCount-visibleItemCount)
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
		if(code == -1){
			if(appService != null && VA == null){
				String skill = "&skill_level="+extras.getString("skill");
				String category = "&category="+extras.getString("category");
				appService.RunSingleTask(0,
					"http://www." + LccHolder.HOST + "/api/get_videos?id="+App.sharedData.getString("user_token", "")+"&page-size=20&page="+page+skill+category,
					PD = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true))
				);
			}
		} else if(code == 0){
			String[] tmp = response.trim().split("[|]");
			if(tmp.length == 3){
				tmp = tmp[2].split("<--->");
			} else return;
			if(page == 1)
				Items.clear();
			for(String v: tmp){
				Items.add(new VideoItem(v.split("<->")));
			}
			if(VA == null){
				VA = new VideosAdapter(VideoList.this, R.layout.videolistelement, Items);
				videosLV.setAdapter(VA);
			} else
				VA.notifyDataSetChanged();

		}
	}
}
