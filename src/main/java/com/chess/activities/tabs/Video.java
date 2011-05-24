package com.chess.activities.tabs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.chess.R;
import com.chess.activities.VideoList;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.model.VideoItem;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;

public class Video extends CoreActivity {
	private VideoItem item;
	private LinearLayout recent;
	private TextView Upgrade, title, desc;
	private Spinner skills, categories;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);
		Upgrade = (TextView)findViewById(R.id.Upgrade);
    int statusCode = App.isLiveChess() ? 50 : 2;
		if( Integer.parseInt( App.sharedData.getString("premium_status", "0") ) < statusCode ){
			Upgrade.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					FlurryAgent.onEvent("Upgrade From Videos", null);
          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/login.html?als="+App.sharedData.getString("user_token", "")+"&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html")));
				}
			});
		} else{
			Upgrade.setVisibility(View.GONE);
		}

		recent = (LinearLayout)findViewById(R.id.recent);
		title = (TextView)findViewById(R.id.title);
		desc = (TextView)findViewById(R.id.desc);

		skills = (Spinner)findViewById(R.id.skills);
		skills.post(new Runnable() {
			@Override
			public void run() {
				skills.setSelection(App.sharedData.getInt("skills", 0));
			}
		});
		skills.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				App.SDeditor.putInt("skills", pos);
				App.SDeditor.commit();
			}
			@Override
			public void onNothingSelected(AdapterView<?> a) {}
		});
		categories = (Spinner)findViewById(R.id.categories);
		categories.post(new Runnable() {
			@Override
			public void run() {
				categories.setSelection(App.sharedData.getInt("categories", 0));
			}
		});
		categories.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				App.SDeditor.putInt("categories", pos);
				App.SDeditor.commit();
			}
			@Override
			public void onNothingSelected(AdapterView<?> a) {}
		});

		findViewById(R.id.start).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int s = skills.getSelectedItemPosition();
				int c = categories.getSelectedItemPosition();
				Intent i = new Intent(Video.this, VideoList.class);
				i.putExtra("skill", "");
				i.putExtra("category", "");
				if(s>0){
					String skill = "";
					switch (s) {
						case 1:
							skill = "beginner";
							break;
						case 2:
							skill = "intermediate";
							break;
						case 3:
							skill = "advanced";
							break;

						default: break;
					}
					i.putExtra("skill", skill);
				}
				if(c>0){
					String category = "";
					switch (c) {
						case 1:
							category = "amazing-games";
							break;
						case 2:
							category = "endgames";
							break;
						case 3:
							category = "openings";
							break;
						case 4:
							category = "rules-basics";
							break;
						case 5:
							category = "strategy";
							break;
						case 6:
							category = "tactics";
							break;

						default: break;
					}
					i.putExtra("category", category);
				}
				startActivity(i);
			}
		});
	}
	@Override
	protected void onResume() {
		Update(-1);
		super.onResume();
	}

	@Override
	public void LoadNext(int code) {
	}
	@Override
	public void LoadPrev(int code) {
		//finish();
		App.mTabHost.setCurrentTab(0);
	}
	@Override
	public void Update(int code) {
		if(code == -1){
			if(appService != null){
				appService.RunSingleTask(0,
					"http://www." + LccHolder.HOST + "/api/get_videos?id="+App.sharedData.getString("user_token", "")+"&page-size=1",
					PD = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true))
				);
			}
		} else if(code == 0){
			recent.setVisibility(View.VISIBLE);
			item = new VideoItem(response.split("[|]")[2].split("<->"));
			title.setText(item.values.get("title"));
			desc.setText(item.values.get("description"));
			findViewById(R.id.play).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
          FlurryAgent.onEvent("Video Played", null);
					Intent i = new Intent(Intent.ACTION_VIEW);
		            i.setDataAndType(Uri.parse(item.values.get("view_url").trim()), "video/*");
		            startActivity(i);
				}
			});
		}
	}
}
