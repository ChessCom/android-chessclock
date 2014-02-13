package com.chess.ui.fragments.videos;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.entity.api.CommonViewedItem;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.model.CurriculumItems;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.activities.VideoActivity;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.ui.interfaces.ItemClickListenerFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.11.13
 * Time: 10:34
 */
public class VideosCurriculumFragmentTablet extends CommonLogicFragment implements ItemClickListenerFace,
		ExpandableListView.OnGroupClickListener, AdapterView.OnItemClickListener {

	public static final String CLICK_TIME = "click time";
	public static final String CURRENT_PLAYING_ID = "current playing id";


	private static final int LIBRARY = 6;
	private static final int WATCH_VIDEO_REQUEST = 9898;
	public static final long WATCHED_TIME = 3 * 60 * 1000;

	private ExpandableListView expListView;

	private VideoGroupsListAdapter curriculumAdapter;


	private CurriculumItems curriculumItems;
	private long playButtonClickTime;
	private long currentPlayingId;
	private SparseBooleanArray curriculumViewedMap;
	private FragmentParentFace parentFace;

	public static VideosCurriculumFragmentTablet createInstance(FragmentParentFace parentFace) {
		VideosCurriculumFragmentTablet fragment = new VideosCurriculumFragmentTablet();
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();

		// restore state
		if (savedInstanceState != null) {
			playButtonClickTime = savedInstanceState.getLong(CLICK_TIME);
			currentPlayingId = savedInstanceState.getLong(CURRENT_PLAYING_ID);

			verifyAndSaveViewedState();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.videos_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.videos);

		{ // Curriculum mode
			expListView = (ExpandableListView) view.findViewById(R.id.expListView);
			expListView.setOnGroupClickListener(this);
			expListView.setGroupIndicator(null);
		}

		showLibrary();

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search_btn, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onResume() {
		super.onResume();

		verifyAndSaveViewedState();
	}

	private void showLibrary() {
		// get viewed marks here after we return to this from details fragment
		Cursor cursor = DbDataManager.getVideoViewedCursor(getActivity(), getUsername());
		if (cursor != null) {
			do {
				int videoId = DbDataManager.getInt(cursor, DbScheme.V_ID);
				boolean isViewed = DbDataManager.getInt(cursor, DbScheme.V_DATA_VIEWED) > 0;
				curriculumViewedMap.put(videoId, isViewed);
			} while (cursor.moveToNext());
			cursor.close();
		}

		expListView.setAdapter(curriculumAdapter);

		expandLastSection();
	}

	private void expandLastSection() {
		if (ICS_PLUS_API) {
			expListView.expandGroup(0, true); // TODO adjust properly last incomplete
		} else {
			expListView.expandGroup(0);
		}
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		if (groupPosition == LIBRARY) { // turn in to library mode
			getAppData().setUserChooseVideoLibrary(true);
			showLibrary();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_search_btn:
				getActivityFace().openFragment(new VideosSearchFragment());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.titleTxt) { // Clicked on title in Curriculum mode, open details
			View parent = (View) v.getParent();
			if (parent != null) {
				parent.performClick();
			}
			Integer childPosition = (Integer) v.getTag(R.id.list_item_id);
			Integer groupPosition = (Integer) v.getTag(R.id.list_item_id_group);
			if (childPosition == null || groupPosition == null) {
				return;
			}

			int id = curriculumItems.getIds()[groupPosition][childPosition];
			getActivityFace().openFragment(VideoDetailsFragment.createInstance(id));

		} else if (v.getId() == R.id.completedIconTxt) {
			Integer childPosition = (Integer) v.getTag(R.id.list_item_id);
			Integer groupPosition = (Integer) v.getTag(R.id.list_item_id_group);
			if (childPosition == null || groupPosition == null) {
				return;
			}

			String currentPlayingLink = curriculumItems.getUrls()[groupPosition][childPosition];
			currentPlayingId = curriculumItems.getIds()[groupPosition][childPosition];

			Intent intent = new Intent(getActivity(), VideoActivity.class);
			intent.putExtra(AppConstants.VIDEO_LINK, currentPlayingLink);
			startActivity(intent);

			// start record time to watch
			playButtonClickTime = System.currentTimeMillis();
		} else if (v.getId() == R.id.curriculumHeader) {
			getAppData().setUserChooseVideoLibrary(false);
			showLibrary();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Integer childPosition = (Integer) view.getTag(R.id.list_item_id);
		Integer groupPosition = (Integer) view.getTag(R.id.list_item_id_group);
		if (childPosition == null || groupPosition == null) {
			return;
		}
		int videoId = curriculumItems.getIds()[groupPosition][childPosition];
		if (inPortrait()) {
			getActivityFace().openFragment(VideoDetailsFragment.createInstance(videoId));
		} else {
			parentFace.changeFragment(VideoDetailsFragment.createInstance(videoId));
		}
	}

	private void verifyAndSaveViewedState() {
		long resumeFromVideoTime = System.currentTimeMillis();

		if (resumeFromVideoTime - playButtonClickTime > WATCHED_TIME) {
			CommonViewedItem item = new CommonViewedItem(currentPlayingId, getUsername());
			DbDataManager.saveVideoViewedState(getContentResolver(), item);

			// update current list
			curriculumViewedMap.put((int) currentPlayingId, true); // TODO test logic
			curriculumAdapter.notifyDataSetChanged();

			// save updates to DB
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(CLICK_TIME, playButtonClickTime);
		outState.putLong(CURRENT_PLAYING_ID, currentPlayingId);
	}


	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private void init() {
		curriculumItems = new CurriculumItems();

		curriculumItems.setCategories(getResources().getStringArray(R.array.videos_curriculum));
		{ // Titles
			String[] beginners = getResources().getStringArray(R.array.video_cur_beginners_titles);
			String[] openings = getResources().getStringArray(R.array.video_cur_openings_titles);
			String[] tactics = getResources().getStringArray(R.array.video_cur_tactics_titles);
			String[] strategy = getResources().getStringArray(R.array.video_cur_strategy_titles);
			String[] endgames = getResources().getStringArray(R.array.video_cur_endgames_titles);
			String[] amazingGames = getResources().getStringArray(R.array.video_cur_amazing_games_titles);

			curriculumItems.setTitles(new String[][]{beginners, openings, tactics, strategy, endgames, amazingGames});
		}
		{ // Links
			String[] beginners = getResources().getStringArray(R.array.video_cur_beginners);
			String[] openings = getResources().getStringArray(R.array.video_cur_openings);
			String[] tactics = getResources().getStringArray(R.array.video_cur_tactics);
			String[] strategy = getResources().getStringArray(R.array.video_cur_strategy);
			String[] endgames = getResources().getStringArray(R.array.video_cur_endgames);
			String[] amazingGames = getResources().getStringArray(R.array.video_cur_amazing_games);

			curriculumItems.setUrls(new String[][]{beginners, openings, tactics, strategy, endgames, amazingGames});
		}
		{ // Ids
			int[] beginners = getResources().getIntArray(R.array.video_cur_beginners_ids);
			int[] openings = getResources().getIntArray(R.array.video_cur_openings_ids);
			int[] tactics = getResources().getIntArray(R.array.video_cur_tactics_ids);
			int[] strategy = getResources().getIntArray(R.array.video_cur_strategy_ids);
			int[] endgames = getResources().getIntArray(R.array.video_cur_endgames_ids);
			int[] amazingGames = getResources().getIntArray(R.array.video_cur_amazing_games_ids);

			curriculumItems.setIds(new int[][]{beginners, openings, tactics, strategy, endgames, amazingGames});
		}

		curriculumViewedMap = new SparseBooleanArray();

		curriculumAdapter = new VideoGroupsListAdapter(curriculumItems);   // categories, titles
	}

	public class VideoGroupsListAdapter extends BaseExpandableListAdapter {
		private final LayoutInflater inflater;
		private final CurriculumItems items;
		private final int spacing;
		private final int columnHeight;

		public VideoGroupsListAdapter(CurriculumItems items) {
			this.items = items;
			inflater = LayoutInflater.from(getActivity());
			columnHeight = getResources().getDimensionPixelSize(R.dimen.video_thumb_size);
			spacing = getResources().getDimensionPixelSize(R.dimen.grid_view_spacing);
		}

		@Override
		public int getGroupCount() {
			return items.getCategories().length;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return items.getCategories()[groupPosition];
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return items.getTitles()[groupPosition];
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.common_titled_list_item, parent, false);
				holder = new ViewHolder();

				holder.text = (TextView) convertView.findViewById(R.id.headerTitleTxt);
				holder.icon = (TextView) convertView.findViewById(R.id.headerIconTxt);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(getGroup(groupPosition).toString());
			if (groupPosition == LIBRARY) {
				holder.icon.setText(Symbol.EMPTY);
			} else {
				if (isExpanded) {
					holder.icon.setText(R.string.ic_up);
				} else {
					holder.icon.setText(R.string.ic_down);
				}
			}

			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

			convertView = inflater.inflate(R.layout.new_common_grid_title_item, parent, false);

			GridView gridView = (GridView) convertView.findViewById(R.id.gridView);

			String[] child = (String[]) getChild(groupPosition, childPosition);

			gridView.setAdapter(new CurriculumTitlesAdapter(getActivity(), groupPosition)); // TODO improve
			gridView.setOnItemClickListener(VideosCurriculumFragmentTablet.this);

			// calculate the column and row counts based on your display
			final int rowCount;
			if (inLandscape()) {
				rowCount = (int) Math.ceil(child.length / (float) 2);
			} else {
				rowCount = (int) Math.ceil(child.length / (float) 1);
			}

			// calculate and set the height for the current gridView
			gridView.getLayoutParams().height = Math.round(rowCount * (columnHeight + spacing * 2));

			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		private class ViewHolder {
			TextView text;
			TextView icon;
		}
	}

	private class CurriculumTitlesAdapter extends BaseAdapter {

		private int groupPosition;
		private final LayoutInflater inflater;
		private final int incompleteIconColor;
		private final int completedTextColor;
		private final int incompleteTextColor;
		private final int completedIconColor;

		public CurriculumTitlesAdapter(Context context, int groupPosition) {
			this.groupPosition = groupPosition;
			inflater = LayoutInflater.from(context);

			completedTextColor = getResources().getColor(R.color.new_light_grey_3);
			incompleteTextColor = getResources().getColor(R.color.new_text_blue);
			completedIconColor = getResources().getColor(R.color.new_light_grey_2);
			incompleteIconColor = getResources().getColor(R.color.orange_button);
		}

		@Override
		public int getCount() {
			return curriculumItems.getTitles()[groupPosition].length;
		}

		@Override
		public Object getItem(int position) {
			return curriculumItems.getTitles()[groupPosition][position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.new_common_thumb_titles_list_item, parent, false);
				holder = new ViewHolder();

				holder.text = (TextView) convertView.findViewById(R.id.titleTxt);
				holder.statusTxt = (TextView) convertView.findViewById(R.id.completedIconTxt);

				holder.text.setOnClickListener(VideosCurriculumFragmentTablet.this);
				holder.statusTxt.setOnClickListener(VideosCurriculumFragmentTablet.this);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(getItem(position).toString());
			holder.text.setTag(R.id.list_item_id, position);
			holder.text.setTag(R.id.list_item_id_group, groupPosition);
			// set another pair of tags for view click handle
			convertView.setTag(R.id.list_item_id, position);
			convertView.setTag(R.id.list_item_id_group, groupPosition);

			holder.statusTxt.setTag(R.id.list_item_id, position);
			holder.statusTxt.setTag(R.id.list_item_id_group, groupPosition);

			if (isVideoWatched(groupPosition, position)) {
				holder.text.setTextColor(completedTextColor);
				holder.statusTxt.setTextColor(completedIconColor);
				holder.statusTxt.setText(R.string.ic_check);
				holder.statusTxt.setPadding(0, 0, 0, 0);
			} else {
				holder.text.setTextColor(incompleteTextColor);
				holder.statusTxt.setText(R.string.ic_play);
				holder.statusTxt.setTextColor(incompleteIconColor);
				holder.statusTxt.setPadding((int) (4 * density), 0, 0, 0);
			}
			return convertView;
		}

		private boolean isVideoWatched(int groupPosition, int childPosition) {
			return curriculumViewedMap.get(curriculumItems.getIds()[groupPosition][childPosition], false);
		}

		private class ViewHolder {
			TextView text;
			TextView statusTxt;
		}
	}
}