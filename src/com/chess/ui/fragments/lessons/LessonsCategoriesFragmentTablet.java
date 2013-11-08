package com.chess.ui.fragments.lessons;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import com.chess.R;
import com.chess.statics.Symbol;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.11.13
 * Time: 18:06
 */
public class LessonsCategoriesFragmentTablet extends LessonsCategoriesFragment {

	public LessonsCategoriesFragmentTablet() {}

	public static LessonsCategoriesFragmentTablet createInstance(String sectionName) {
		LessonsCategoriesFragmentTablet fragment = new LessonsCategoriesFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		fragment.setArguments(bundle);
		return fragment;
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
//		switch (item.getItemId()) {
//			case R.id.menu_search_btn:
//				getActivityFace().openFragment(new LessonsSearchFragment());
//				break;
//		}
		return false;
	}
}
