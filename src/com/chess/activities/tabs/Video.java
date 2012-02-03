package com.chess.activities.tabs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.activities.VideoList;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.model.VideoItem;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;

public class Video extends CoreActivity implements OnClickListener {
	private VideoItem item;
	private LinearLayout recent;
	private TextView upgrade, title, desc;
	private Spinner skills, categories;

	private SkillsItemSelectedListener skillsItemSelectedListener;
	private CategoriesItemSelectedListener categoriesItemSelectedListener;

	private void init(){
		skillsItemSelectedListener = new SkillsItemSelectedListener();
		categoriesItemSelectedListener = new CategoriesItemSelectedListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);

		init();
		upgrade = (TextView) findViewById(R.id.Upgrade);
		boolean liveMembershipLevel = false;
		if(lccHolder.getUser() != null){
			liveMembershipLevel = mainApp.isLiveChess() && (lccHolder.getUser().getMembershipLevel() < 50);
		}
//		boolean liveMembershipLevel =
//				lccHolder.getUser() != null ? mainApp.isLiveChess() && (lccHolder.getUser().getMembershipLevel() < 50) : false;
		if (liveMembershipLevel
				|| (!mainApp.isLiveChess() && Integer.parseInt(mainApp.getSharedData().getString(AppConstants.USER_PREMIUM_STATUS, "0")) < 3)) {
			upgrade.setVisibility(View.VISIBLE);
			upgrade.setOnClickListener(this);
		} else {
			upgrade.setVisibility(View.GONE);
		}

		recent = (LinearLayout) findViewById(R.id.recent);
		title = (TextView) findViewById(R.id.title);
		desc = (TextView) findViewById(R.id.desc);

		skills = (Spinner) findViewById(R.id.skills);
		skills.post(new Runnable() {
			@Override
			public void run() {
				skills.setSelection(mainApp.getSharedData().getInt(AppConstants.VIDEO_SKILL_LEVEL, 0));
			}
		});
		
		skills.setOnItemSelectedListener(skillsItemSelectedListener );
		categories = (Spinner) findViewById(R.id.categories);
		categories.post(new Runnable() {
			@Override
			public void run() {
				categories.setSelection(mainApp.getSharedData().getInt(AppConstants.VIDEO_CATEGORY, 0));
			}
		});
		categories.setOnItemSelectedListener(categoriesItemSelectedListener);

		findViewById(R.id.start).setOnClickListener(this);
	}

	private class SkillsItemSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			mainApp.getSharedDataEditor().putInt(AppConstants.VIDEO_SKILL_LEVEL, pos);
			mainApp.getSharedDataEditor().commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
	}

	private class CategoriesItemSelectedListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			mainApp.getSharedDataEditor().putInt(AppConstants.VIDEO_CATEGORY, pos);
			mainApp.getSharedDataEditor().commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
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
		mainApp.getTabHost().setCurrentTab(0);
	}

	@Override
	public void Update(int code) {
		if (code == -1) {
			if (appService != null) {
				appService.RunSingleTask(0,
						"http://www." + LccHolder.HOST + "/api/get_videos?id="
								+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
								+ "&page-size=1",
						progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true))
				);
			}
		} else if (code == 0) {
			recent.setVisibility(View.VISIBLE);
			item = new VideoItem(response.split("[|]")[2].split("<->"));
			title.setText(item.values.get(AppConstants.TITLE));
			desc.setText(item.values.get("description"));

			findViewById(R.id.play).setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.Upgrade){
			FlurryAgent.onEvent("upgrade From Videos", null);
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST
					+ "/login.html?als=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
					+ "&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html?c=androidvideos"));
			startActivity(intent);
		}else if(view.getId() == R.id.play){
			FlurryAgent.onEvent("Video Played", null);
			
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setDataAndType(Uri.parse(item.values.get("view_url").trim()), "video/*");
			startActivity(i);
		}else if(view.getId() == R.id.start){
			int s = skills.getSelectedItemPosition();
			int c = categories.getSelectedItemPosition();

			Intent i = new Intent(Video.this, VideoList.class);
			i.putExtra(AppConstants.VIDEO_SKILL_LEVEL, "");
			i.putExtra(AppConstants.VIDEO_CATEGORY, "");

			if (s > 0) {
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

					default:
						break;
				}
				i.putExtra(AppConstants.VIDEO_SKILL_LEVEL, skill);
			}
			if (c > 0) {
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

					default:
						break;
				}
				i.putExtra(AppConstants.VIDEO_CATEGORY, category);
			}
			startActivity(i);
		}
	}
}
