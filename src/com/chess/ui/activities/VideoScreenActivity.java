package com.chess.ui.activities;

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
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
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
public class VideoScreenActivity extends LiveBaseActivity implements View.OnClickListener {
	private VideoItem item;
	private View recent;
	private TextView title, desc;
	private Spinner skillsSpinner;
	private Spinner categoriesSpinner;

	private SkillsItemSelectedListener skillsItemSelectedListener;
	private CategoriesItemSelectedListener categoriesItemSelectedListener;
	private VideosItemUpdateListener videosItemUpdateListener;
	private Button playBtn;

	private void init() {
		skillsItemSelectedListener = new SkillsItemSelectedListener();
		categoriesItemSelectedListener = new CategoriesItemSelectedListener();
		videosItemUpdateListener = new VideosItemUpdateListener();

		showSearch = true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_screen);

		init();

		Button upgrade = (Button) findViewById(R.id.upgradeBtn);
		upgrade.setOnClickListener(this);

		if (AppUtils.isNeedToUpgrade(this)) {
			upgrade.setVisibility(View.VISIBLE);
		} else {
			upgrade.setVisibility(View.GONE);
		}

		recent = findViewById(R.id.recent);
		title = (TextView) findViewById(R.id.title);
		desc = (TextView) findViewById(R.id.desc);

		skillsSpinner = (Spinner) findViewById(R.id.skillsSpinner);
		skillsSpinner.setSelection(preferences.getInt(AppConstants.VIDEO_SKILL_LEVEL, 0));
		skillsSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.skill));
		skillsSpinner.setOnItemSelectedListener(skillsItemSelectedListener);

		categoriesSpinner = (Spinner) findViewById(R.id.categoriesSpinner);
		categoriesSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.category));
		categoriesSpinner.setSelection(preferences.getInt(AppConstants.VIDEO_CATEGORY, 0));
		categoriesSpinner.setOnItemSelectedListener(categoriesItemSelectedListener);

		playBtn = (Button) findViewById(R.id.play);
		playBtn.setOnClickListener(this);

		findViewById(R.id.start).setOnClickListener(this);

		handleIntent(getIntent());
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

	private class VideosItemUpdateListener extends ChessUpdateListener {
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
		showToast(query);

	}

	//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the options menu from XML
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.options_menu, menu);
//
//		// Get the SearchView and set the searchable configuration
//		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//		searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
//
//		return super.onCreateOptionsMenu(menu);
//	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			FlurryAgent.onEvent(FlurryData.UPGRADE_FROM_VIDEOS, null);
			startActivity(AppData.getMembershipVideoIntent(this));
		} else if (view.getId() == R.id.play) {
			FlurryAgent.onEvent(FlurryData.VIDEO_PLAYED, null);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(item.values.get(AppConstants.VIEW_URL).trim()), "video/*");
			startActivity(intent);
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