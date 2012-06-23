package com.chess.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener2;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.VideoItem;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;

/**
 * VideoScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:19
 */
public class VideoScreenActivityDone extends LiveBaseActivity2 implements View.OnClickListener {
	private VideoItem item;
	private View recent;
	private Button upgrade;
	private TextView title, desc;
	private Spinner skillsSpinner, categoriesSpinner;

	private SkillsItemSelectedListener skillsItemSelectedListener;
	private CategoriesItemSelectedListener categoriesItemSelectedListener;
	private VideosItemUpdateListener videosItemUpdateListener;
	private Button playBtn;

	private void init() {
		skillsItemSelectedListener = new SkillsItemSelectedListener();
		categoriesItemSelectedListener = new CategoriesItemSelectedListener();
		videosItemUpdateListener = new VideosItemUpdateListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_screen);

		init();
		upgrade = (Button) findViewById(R.id.upgradeBtn);

		if (AppUtils.isNeedToUpgrade(this)) {
			upgrade.setVisibility(View.VISIBLE);
			upgrade.setOnClickListener(this);
		} else {
			upgrade.setVisibility(View.GONE);
		}

		recent = findViewById(R.id.recent);
		title = (TextView) findViewById(R.id.title);
		desc = (TextView) findViewById(R.id.desc);

		skillsSpinner = (Spinner) findViewById(R.id.skills);
		skillsSpinner.setSelection(preferences.getInt(AppConstants.VIDEO_SKILL_LEVEL, 0));
		skillsSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.skill));
		skillsSpinner.setOnItemSelectedListener(skillsItemSelectedListener);

		categoriesSpinner = (Spinner) findViewById(R.id.categories);
		categoriesSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.category));
		categoriesSpinner.setSelection(preferences.getInt(AppConstants.VIDEO_CATEGORY, 0));
		categoriesSpinner.setOnItemSelectedListener(categoriesItemSelectedListener);

		playBtn = (Button) findViewById(R.id.play);
		playBtn.setOnClickListener(this);


		findViewById(R.id.start).setOnClickListener(this);
	}

	private class SkillsItemSelectedListener implements AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			preferencesEditor.putInt(AppConstants.VIDEO_SKILL_LEVEL, pos);
			preferencesEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
	}

	private class CategoriesItemSelectedListener implements AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			preferencesEditor.putInt(AppConstants.VIDEO_CATEGORY, pos);
			preferencesEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateVideoItem();
	}

	private void updateVideoItem() {
		playBtn.setEnabled(false);

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GET_VIDEOS);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_PAGE_SIZE, RestHelper.V_VIDEO_ITEM_ONE);

		new GetStringObjTask(videosItemUpdateListener).executeTask(loadItem);
	}

	private class VideosItemUpdateListener extends ChessUpdateListener2 {
		public VideosItemUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			recent.setVisibility(View.VISIBLE);
			item = new VideoItem(returnedObj.split("[|]")[2].split("<->"));
			title.setText(item.values.get(AppConstants.TITLE));
			desc.setText(item.values.get(AppConstants.DESCRIPTION));
			playBtn.setEnabled(true);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			FlurryAgent.onEvent("upgrade From Videos", null);
			startActivity(AppData.getMembershipVideoIntent(this));
		} else if (view.getId() == R.id.play) {
			FlurryAgent.onEvent("Video Played", null);

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setDataAndType(Uri.parse(item.values.get(AppConstants.VIEW_URL).trim()), "video/*");
			startActivity(i);
		} else if (view.getId() == R.id.start) {
			int skillId = skillsSpinner.getSelectedItemPosition();
			int categoryId = categoriesSpinner.getSelectedItemPosition();

			Intent intent = new Intent(this, VideoListActivity.class);
			intent.putExtra(AppConstants.VIDEO_SKILL_LEVEL, StaticData.SYMBOL_EMPTY);
			intent.putExtra(AppConstants.VIDEO_CATEGORY, StaticData.SYMBOL_EMPTY);

			if (skillId > 0) {
				String skill = StaticData.SYMBOL_EMPTY;
				switch (skillId) {
					case 1:
						skill = getString(R.string.beginner_category);
						break;
					case 2:
						skill = getString(R.string.intermediate_category);
						break;
					case 3:
						skill = getString(R.string.advanced_category);
						break;

					default:
						break;
				}
				intent.putExtra(AppConstants.VIDEO_SKILL_LEVEL, skill);
			}
			if (categoryId > 0) {
				String category = StaticData.SYMBOL_EMPTY;
				switch (categoryId) {
					case 1:
						category = getString(R.string.amazing_games_category);
						break;
					case 2:
						category = getString(R.string.end_games_category);
						break;
					case 3:
						category = getString(R.string.openings_category);
						break;
					case 4:
						category = getString(R.string.rules_basics_category);
						break;
					case 5:
						category = getString(R.string.strategy_category);
						break;
					case 6:
						category = getString(R.string.tactics_category);
						break;

					default:
						break;
				}
				intent.putExtra(AppConstants.VIDEO_CATEGORY, category);
			}
			startActivity(intent);
		}
	}
}