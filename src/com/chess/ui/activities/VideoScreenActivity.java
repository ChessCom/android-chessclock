package com.chess.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.model.VideoItem;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;

/**
 * VideoScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:19
 */
public class VideoScreenActivity extends LiveBaseActivity implements View.OnClickListener {
	private VideoItem item;
	private View recent;
	private Button upgrade;
	private TextView title, desc;
	private Spinner skills, categories;

	private SkillsItemSelectedListener skillsItemSelectedListener;
	private CategoriesItemSelectedListener categoriesItemSelectedListener;

	private void init() {
		skillsItemSelectedListener = new SkillsItemSelectedListener();
		categoriesItemSelectedListener = new CategoriesItemSelectedListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		init();
		upgrade = (Button) findViewById(R.id.upgradeBtn);
		boolean liveMembershipLevel = false;
		if (lccHolder.getUser() != null) {
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

		recent = findViewById(R.id.recent);
		title = (TextView) findViewById(R.id.title);
		desc = (TextView) findViewById(R.id.desc);

		skills = (Spinner) findViewById(R.id.skills);
//		skills.post(new Runnable() {
//			@Override
//			public void run() {
//				skills.setSelection(mainApp.getSharedData().getInt(AppConstants.VIDEO_SKILL_LEVEL, 0));
//			}
//		});
		skills.setSelection(mainApp.getSharedData().getInt(AppConstants.VIDEO_SKILL_LEVEL, 0));
		skills.setAdapter(new ChessSpinnerAdapter(this, R.array.skill));
		skills.setOnItemSelectedListener(skillsItemSelectedListener);
		categories = (Spinner) findViewById(R.id.categories);
		categories.setAdapter(new ChessSpinnerAdapter(this, R.array.category));
		categories.setSelection(mainApp.getSharedData().getInt(AppConstants.VIDEO_CATEGORY, 0));
//		categories.post(new Runnable() {
//			@Override
//			public void run() {
//				categories.setSelection(mainApp.getSharedData().getInt(AppConstants.VIDEO_CATEGORY, 0));
//			}
//		});
		categories.setOnItemSelectedListener(categoriesItemSelectedListener);

		findViewById(R.id.start).setOnClickListener(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);
	}

	private class SkillsItemSelectedListener implements AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			mainApp.getSharedDataEditor().putInt(AppConstants.VIDEO_SKILL_LEVEL, pos);
			mainApp.getSharedDataEditor().commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
	}

	private class CategoriesItemSelectedListener implements AdapterView.OnItemSelectedListener {
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
		update(INIT_ACTIVITY);
		super.onResume();
	}

//	@Override
//	public void LoadPrev(int code) {
//		//finish();
//		mainApp.getTabHost().setCurrentTab(0);
//	}

	@Override
	public void update(int code) {
		if (code == INIT_ACTIVITY) {
			if (appService != null) {
				appService.RunSingleTask(0,
						"http://www." + LccHolder.HOST + "/api/get_videos?id="
								+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
								+ "&page-size=1",
						progressDialog = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true))
				);
			}
		} else if (code == 0) {
			recent.setVisibility(View.VISIBLE);
			item = new VideoItem(response.split("[|]")[2].split("<->"));
			title.setText(item.values.get(AppConstants.TITLE));
			desc.setText(item.values.get(AppConstants.DESCRIPTION));

			findViewById(R.id.play).setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			FlurryAgent.onEvent("upgrade From Videos", null);
			startActivity(mainApp.getMembershipVideoIntent());
		} else if (view.getId() == R.id.play) {
			FlurryAgent.onEvent("Video Played", null);

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setDataAndType(Uri.parse(item.values.get(AppConstants.VIEW_URL).trim()), "video/*");
			startActivity(i);
		} else if (view.getId() == R.id.start) {
			int s = skills.getSelectedItemPosition();
			int c = categories.getSelectedItemPosition();

//			Intent i = new Intent(coreContext, VideoList.class);
			Intent i = new Intent(coreContext, VideoListActivity.class);
			i.putExtra(AppConstants.VIDEO_SKILL_LEVEL, AppConstants.SYMBOL_EMPTY);
			i.putExtra(AppConstants.VIDEO_CATEGORY, AppConstants.SYMBOL_EMPTY);

			if (s > 0) {
				String skill = AppConstants.SYMBOL_EMPTY;
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
				String category = AppConstants.SYMBOL_EMPTY;
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