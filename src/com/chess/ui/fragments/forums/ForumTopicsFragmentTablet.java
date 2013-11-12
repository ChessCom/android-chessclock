package com.chess.ui.fragments.forums;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.ui.interfaces.FragmentParentFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.11.13
 * Time: 15:43
 */
public class ForumTopicsFragmentTablet extends ForumTopicsFragment {

	private FragmentParentFace parentFace;

	public ForumTopicsFragmentTablet() { }

	public static ForumTopicsFragmentTablet createInstance(int categoryId, FragmentParentFace parentFace) {
		ForumTopicsFragmentTablet fragment = new ForumTopicsFragmentTablet();
		fragment.parentFace = parentFace;
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY_ID, categoryId);
		fragment.setArguments(bundle);
		return fragment;
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		boolean headerAdded = listView.getHeaderViewsCount() > 0;
//		int offset = headerAdded ? -1 : 0;

		if (parentFace == null) {
			getActivityFace().showPreviousFragment();
		}

		if (position != 0) { // if NOT listView header
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			int topicId = DbDataManager.getInt(cursor, DbScheme.V_ID);

			parentFace.changeFragment(ForumPostsFragment.createInstance(topicId));
		}
	}

}
