package com.chess.ui.activities.old;

import android.app.SearchManager;
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
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.activities.LiveBaseActivity;
import com.chess.ui.adapters.WhiteSpinnerAdapter;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;


/**
 * VideoScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:19
 */
public class VideoScreenActivity extends LiveBaseActivity {
//	private VideoItemOld item;
	private VideoItem.Data item;
	private View recent;
	private TextView title, desc;
	private Spinner skillsSpinner;
	private Spinner categoriesSpinner;

	private SkillsItemSelectedListener skillsItemSelectedListener;
	private CategoriesItemSelectedListener categoriesItemSelectedListener;
	private VideosItemUpdateListener videosItemUpdateListener;
	private Button playBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_screen);

		init();

		Button upgrade = (Button) findViewById(R.id.upgradeBtn);
		upgrade.setOnClickListener(this);

		if (AppUtils.isNeedToUpgradePremium(this)) {
			upgrade.setVisibility(View.VISIBLE);
		} else {
			upgrade.setVisibility(View.GONE);
		}

		recent = findViewById(R.id.recent);
		title = (TextView) findViewById(R.id.title);
		desc = (TextView) findViewById(R.id.desc);

		skillsSpinner = (Spinner) findViewById(R.id.skillsSpinner);
		skillsSpinner.setAdapter(new WhiteSpinnerAdapter(this, getItemsFromEntries(R.array.skill)));
		skillsSpinner.setOnItemSelectedListener(skillsItemSelectedListener);
		skillsSpinner.setSelection(preferences.getInt(AppConstants.PREF_VIDEO_SKILL_LEVEL, 0));

		categoriesSpinner = (Spinner) findViewById(R.id.categoriesSpinner);
		categoriesSpinner.setAdapter(new WhiteSpinnerAdapter(this, getItemsFromEntries(R.array.category)));
		categoriesSpinner.setOnItemSelectedListener(categoriesItemSelectedListener);
		categoriesSpinner.setSelection(preferences.getInt(AppConstants.PREF_VIDEO_CATEGORY, 0));

		playBtn = (Button) findViewById(R.id.playVideoBtn);
		playBtn.setOnClickListener(this);

		findViewById(R.id.start).setOnClickListener(this);

		handleIntent(getIntent());
	}

	private void init() {
		skillsItemSelectedListener = new SkillsItemSelectedListener();
		categoriesItemSelectedListener = new CategoriesItemSelectedListener();
		videosItemUpdateListener = new VideosItemUpdateListener();

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateVideoItem();
	}

	private void updateVideoItem() {
		if (isRestarted) {
			return;
		}

		playBtn.setEnabled(false);

		LoadItem loadItem = new LoadItem();
//		loadItem.setLoadPath(RestHelper.GET_VIDEOS);
		loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_PAGE_SIZE, RestHelper.V_VIDEO_ITEM_ONE);
		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_ITEM_ONE);

//		new GetStringObjTask(videosItemUpdateListener).executeTask(loadItem);
		new RequestJsonTask<VideoItem>(videosItemUpdateListener).executeTask(loadItem);
	}

//	private class VideosItemUpdateListener extends ChessUpdateListener {
	private class VideosItemUpdateListener extends ActionBarUpdateListener<VideoItem> {

		public VideosItemUpdateListener() {
			super(getInstance(), VideoItem.class);
		}

		@Override
		public void updateData(VideoItem returnedObj) {
			recent.setVisibility(View.VISIBLE);
			int cnt = returnedObj.getCount();
			if (cnt > 0){
				item = returnedObj.getData().get(0); // new VideoItemOld(returnedObj.split(RestHelper.SYMBOL_ITEM_SPLIT)[2].split("<->"));
				title.setText(item.getName());
				desc.setText(item.getDescription());

				playBtn.setEnabled(true);
			}
		}
	}

//	private class VideosItemUpdateListener extends ChessUpdateListener {
//
//		@Override
//		public void updateData(String returnedObj) {
//			recent.setVisibility(View.VISIBLE);
//			item = new VideoItemOld(returnedObj.split(RestHelper.SYMBOL_ITEM_SPLIT)[2].split("<->"));
//			title.setText(item.getTitle());
//			desc.setText(item.getDescription());
//
//			playBtn.setEnabled(true);
//		}
//	}
//

	private class SkillsItemSelectedListener implements AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			preferencesEditor.putInt(AppConstants.PREF_VIDEO_SKILL_LEVEL, pos);
			preferencesEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
	}

	private class CategoriesItemSelectedListener implements AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			preferencesEditor.putInt(AppConstants.PREF_VIDEO_CATEGORY, pos);
			preferencesEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Because this activity has set launchMode="singleTop", the system calls this method
		// to deliver the intent if this actvity is currently the foreground activity when
		// invoked again (when the user executes a search from this activity, we don't create
		// a new instance of this activity, so the system delivers the search intent here)
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			onSearchQuery(query);
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			onSearchQuery(query);
		}
	}

	@Override
	protected void onSearchAutoCompleteQuery(String query) {

	}

	@Override
	protected void onSearchQuery(String query) {
		Intent intent = new Intent(this, VideoListActivity.class);
		intent.putExtra(RestHelper.P_KEYWORD, query);
		startActivity(intent);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			FlurryAgent.logEvent(FlurryData.UPGRADE_FROM_VIDEOS, null);
			startActivity(AppData.getMembershipVideoIntent(this));
		} else if (view.getId() == R.id.playVideoBtn) {
			FlurryAgent.logEvent(FlurryData.VIDEO_PLAYED, null);

			Intent intent = new Intent(Intent.ACTION_VIEW);
//			intent.setDataAndType(Uri.parse(item.getViewUrl().trim()), "video/*");
			intent.setDataAndType(Uri.parse(item.getUrl().trim()), "video/*");
//			intent.setDataAndType(Uri.parse("http://s3.amazonaws.com/chesscom-videos/videos/ios/kaidanovs-comprehensive-repertoire-two-knights-defense---part-2.m3u8"), "video/*");
			startActivity(intent);
		} else if (view.getId() == R.id.start) {
			int skillId = skillsSpinner.getSelectedItemPosition();
			int categoryId = categoriesSpinner.getSelectedItemPosition();

			String skill = StaticData.SYMBOL_EMPTY;
			String category = StaticData.SYMBOL_EMPTY;

			switch (skillId) {
				case 0:
					skill = getString(R.string.beginner_category);
					break;
				case 1:
					skill = getString(R.string.intermediate_category);
					break;
				case 2:
					skill = getString(R.string.advanced_category);
					break;
			}
			switch (categoryId) {
				case 0:
					category = getString(R.string.amazing_games_category);
					break;
				case 1:
					category = getString(R.string.end_games_category);
					break;
				case 2:
					category = getString(R.string.openings_category);
					break;
				case 3:
					category = getString(R.string.rules_basics_category);
					break;
				case 4:
					category = getString(R.string.strategy_category);
					break;
				case 5:
					category = getString(R.string.tactics_category);
					break;
			}

			Intent intent = new Intent(this, VideoListActivity.class);
			intent.putExtra(RestHelper.P_SKILL_LEVEL, skill);
			intent.putExtra(RestHelper.P_CATEGORY, category);
			startActivity(intent);
		}
	}
}