package com.chess.ui.fragments.stats;

import android.os.Bundle;
import android.view.View;
import com.chess.R;
import com.chess.model.SelectionItem;
import com.chess.statics.Symbol;
import com.chess.ui.interfaces.FragmentParentFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.11.13
 * Time: 16:11
 */
public class StatsGameFragmentTablet extends StatsGameFragment {

	private FragmentParentFace parentFace;

	public StatsGameFragmentTablet() {
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY, LIVE_STANDARD);
		setArguments(bundle);
	}

	public static StatsGameFragmentTablet createInstance(FragmentParentFace parentFace, int code, String username) {
		StatsGameFragmentTablet fragment = new StatsGameFragmentTablet();
		fragment.parentFace = parentFace;
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY, code);
		bundle.putString(USERNAME, username);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String rating = getString(R.string.rating_);
		SelectionItem title = (SelectionItem) statsSpinner.getSelectedItem();
		setTitle(rating + Symbol.SPACE + title.getText());

		statsSpinner.setVisibility(View.GONE);
	}

	@Override
	public void onResume() {
		super.onResume();

		updateBySelection();
	}

	@Override
	protected void updateBySelection() {
		if (previousPosition == TACTICS) {
			parentFace.changeFragment(new StatsGameTacticsFragment());
		} else if (previousPosition == LESSONS) {
			parentFace.changeFragment(new StatsGameLessonsFragment());
		} else {
			showSelectedStats();
		}
	}

	@Override
	protected void showSelectedStats() {
		// get selected position of spinner that match game type
		int gameType = statsSpinner.getSelectedItemPosition(); // specify which data to load in details

		parentFace.changeFragment(StatsGameDetailsFragment.createInstance(gameType, username));
	}
}
