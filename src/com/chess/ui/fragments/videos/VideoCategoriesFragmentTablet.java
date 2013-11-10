package com.chess.ui.fragments.videos;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.DarkSpinnerAdapter;
import com.chess.ui.adapters.VideosCursorAdapter;
import com.chess.ui.adapters.VideosCursorAdapterTablet;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.11.13
 * Time: 10:47
 */
public class VideoCategoriesFragmentTablet extends VideoCategoriesFragment{


	private VideosCursorAdapterTablet videosAdapter;
	private GridView listView;

	public VideoCategoriesFragmentTablet() {}

	public static VideoCategoriesFragmentTablet createInstance(String sectionName) {
		VideoCategoriesFragmentTablet fragment = new VideoCategoriesFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		videosAdapter = new VideosCursorAdapterTablet(this, null);
		super.onCreate(savedInstanceState);

		getAdapter().addViewedMap(viewedVideosMap);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String videos = getString(R.string.videos);
		setTitle(sectionName + Symbol.SPACE + videos);

		// hide spinner
		categorySpinner.setVisibility(View.GONE);
	}

	@Override
	public void onResume() {
		super.onResume();

		updateByCategory();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	protected void setAdapter(VideosCursorAdapterTablet adapter) {
		this.videosAdapter = adapter;
	}

	@Override
	protected VideosCursorAdapter getAdapter() {
		return videosAdapter;
	}

	@Override
	protected void showEmptyView(boolean show) {
		if (show) {
			// don't hide loadingView if it's loading
			if (loadingView.getVisibility() != View.VISIBLE) {
				loadingView.setVisibility(View.GONE);
			}

			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void widgetsInit(View view) {
		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);

		listView = (GridView) view.findViewById(R.id.listView);
		listView.setAdapter(paginationAdapter);
		listView.setOnItemClickListener(this);

		// get viewed marks
		Cursor cursor = DbDataManager.getVideoViewedCursor(getActivity(), getUsername());
		if (cursor != null) {
			do {
				int videoId = DbDataManager.getInt(cursor, DbScheme.V_ID);
				boolean isViewed = DbDataManager.getInt(cursor, DbScheme.V_DATA_VIEWED) > 0;
				viewedVideosMap.put(videoId, isViewed);
			} while (cursor.moveToNext());
			cursor.close();
		}

		boolean loaded = categoriesNames.size() != 0 || fillCategories();

		if (loaded) {
			int position;
			for (position = 0; position < categoriesNames.size(); position++) {
				String category = categoriesNames.get(position);
				if (category.equals(sectionName)) {
					selectedCategoryId = categoriesIds.get(position);
					break;
				}
			}

			categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);
			categorySpinner.setAdapter(new DarkSpinnerAdapter(getActivity(), categoriesNames));
			categorySpinner.setOnItemSelectedListener(this);
			categorySpinner.setSelection(position);  // TODO remember last selection.
		}
	}
}
