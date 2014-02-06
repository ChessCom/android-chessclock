package com.chess.ui.fragments.lessons;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.LessonsCursorAdapter;
import com.chess.ui.adapters.LessonsCursorAdapterTablet;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.11.13
 * Time: 18:06
 */
public class LessonsCategoriesFragmentTablet extends LessonsCategoriesFragment {

	private LessonsCursorAdapterTablet lessonsAdapter;
	private GridView listView;

	public LessonsCategoriesFragmentTablet() {}

	public static LessonsCategoriesFragmentTablet createInstance(String sectionName) {
		LessonsCategoriesFragmentTablet fragment = new LessonsCategoriesFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		lessonsAdapter = new LessonsCursorAdapterTablet(getActivity(), null);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String lessons = getString(R.string.lessons);
		setTitle(sectionName + Symbol.SPACE + lessons);

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

	protected void setAdapter(LessonsCursorAdapterTablet adapter) {
		this.lessonsAdapter = adapter;
	}

	@Override
	protected LessonsCursorAdapter getAdapter() {
		return lessonsAdapter;
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
	}
}
