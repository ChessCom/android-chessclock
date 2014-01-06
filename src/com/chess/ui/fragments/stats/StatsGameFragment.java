package com.chess.ui.fragments.stats;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.chess.R;
import com.chess.model.SelectionItem;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.DarkSpinnerIconAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.drawables.IconDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.01.13
 * Time: 10:37
 */
public class StatsGameFragment extends CommonLogicFragment implements AdapterView.OnItemSelectedListener {

	public final static int DAILY_CHESS = 0;
	public final static int LIVE_STANDARD = 1;
	public final static int LIVE_BLITZ = 2;
	public final static int LIVE_LIGHTNING = 3;
	public final static int DAILY_CHESS960 = 4;
	public static final int TACTICS = 5;
	public static final int LESSONS = 6;

	protected static final String CATEGORY = "mode";
	protected static final String USERNAME = "username";

	protected Spinner statsSpinner;
	protected String username;
	private int categoryPosition;
	protected int previousPosition = -1;
	protected List<SelectionItem> ratingsList;

	public StatsGameFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY, LIVE_STANDARD);
		setArguments(bundle);
	}

	public static StatsGameFragment createInstance(int code, String username) {
		StatsGameFragment fragment = new StatsGameFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY, code);
		bundle.putString(USERNAME, username);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			username = getArguments().getString(USERNAME);
			categoryPosition = getArguments().getInt(CATEGORY);
		} else {
			username = savedInstanceState.getString(USERNAME);
			categoryPosition = savedInstanceState.getInt(CATEGORY);
		}

		if (TextUtils.isEmpty(username)) {
			username = getUsername();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_stats_game_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		updateTitle();

		statsSpinner = (Spinner) view.findViewById(R.id.statsSpinner);

		ratingsList = createSpinnerList(getActivity());
		statsSpinner.setAdapter(new DarkSpinnerIconAdapter(getActivity(), ratingsList));
		statsSpinner.setOnItemSelectedListener(this);
		statsSpinner.setSelection(categoryPosition);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(CATEGORY, categoryPosition);
		outState.putString(USERNAME, username);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (position == previousPosition) {
			return;
		}
		previousPosition = position;
		updateBySelection();
	}

	protected void updateBySelection() {
		if (previousPosition == TACTICS) {
			changeInternalFragment(StatsGameTacticsFragment.createInstance(username));
		} else if (previousPosition == LESSONS) {
			changeInternalFragment(StatsGameLessonsFragment.createInstance(username));
		} else {
			showSelectedStats();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	protected void showSelectedStats() {
		// get selected position of spinner
		int gameType = statsSpinner.getSelectedItemPosition(); // specify which data to load in details

		changeInternalFragment(StatsGameDetailsFragment.createInstance(gameType, username));
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.stats_content_frame, fragment).commitAllowingStateLoss();
	}

	private List<SelectionItem> createSpinnerList(Context context) {
		ArrayList<SelectionItem> selectionItems = new ArrayList<SelectionItem>();

		String[] categories = context.getResources().getStringArray(R.array.game_stats_categories);
		String[] codes = context.getResources().getStringArray(R.array.game_stats_types);
		for (int i = 0; i < categories.length; i++) {
			String category = categories[i];
			SelectionItem selectionItem = new SelectionItem(getIconByCategory(i), category);
			selectionItem.setCode(codes[i]);
			selectionItems.add(selectionItem);
		}
		return selectionItems;
	}

	/**
	 *  Fill list according :
	 *	Daily - Chess
	 *	Live - Standard
	 *	Live - Blitz
	 *	Live - Bullet
	 *	Daily - Chess960
	 *	Tactics
	 *	Coach Manager
	 * @return Drawable icon for index
	 */
	private Drawable getIconByCategory(int index) {
		Context context = getActivity();
		switch (index) {
			case DAILY_CHESS:
				return new IconDrawable(context, R.string.ic_daily_game);
			case LIVE_STANDARD:
				return new IconDrawable(context, R.string.ic_live_standard);
			case LIVE_BLITZ:
				return new IconDrawable(context, R.string.ic_live_blitz);
			case LIVE_LIGHTNING:
				return new IconDrawable(context, R.string.ic_live_bullet);
			case DAILY_CHESS960:
				return new IconDrawable(context, R.string.ic_daily960_game);
			default: // case LESSONS:
				return new IconDrawable(context, R.string.ic_help);
		}
	}

	public void updateUsername(String username) {
		this.username = username;
		need2update = true;

		updateTitle();
		updateBySelection();
	}

	private void updateTitle() {
		if (!username.equals(getUsername())) {
			setTitle(username + Symbol.SPACE + getString(R.string.stats));
		} else {
			setTitle(R.string.stats);
		}
	}
}
