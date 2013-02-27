package com.chess.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.chess.LeftRightImageEditText;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.popup_fragments.PopupCountriesFragment;
import com.chess.ui.popup_fragments.PopupSkillsFragment;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.01.13
 * Time: 9:09
 */
public class CreateProfileFragment extends ProfileSetupsFragment implements View.OnClickListener, PopupListSelectionFace,
		View.OnTouchListener {

	public static final String SKILL_SELECTION = "SKILL_SELECTION";
	public static final String COUNTRY_SELECTION = "COUNTRY_SELECTION";

	private PopupSkillsFragment skillsFragment;
	private String[] skillNames;
	private LeftRightImageEditText skillEdt;
	private LeftRightImageEditText countryEdt;
	private CountrySelectedListener countrySelectedListener;
	private PopupCountriesFragment countriesFragment;
	private String[] countryNames;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		skillNames = getResources().getStringArray(R.array.skills_name);
		countryNames = getResources().getStringArray(R.array.new_countries);
		countrySelectedListener = new CountrySelectedListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_create_profile_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.createProfileBtn).setOnClickListener(this);
		view.findViewById(R.id.skipBtn).setOnClickListener(this);
		view.findViewById(R.id.skipLay).setOnClickListener(this);

		countryEdt = (LeftRightImageEditText) view.findViewById(R.id.countryEdt);
		countryEdt.setOnTouchListener(this);

		skillEdt = (LeftRightImageEditText) view.findViewById(R.id.skillEdt);
		skillEdt.setOnTouchListener(this);

		// TODO select country automatically based on location
	}

	@Override
	public void onResume() {
		super.onResume();

		skillEdt.setText(skillNames[AppData.getUserSkill(getActivity())]);

		countryEdt.setText(AppData.getUserCountry(getActivity()));
		countryEdt.updateRightIcon(AppUtils.getUserFlag(getActivity()));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.skipBtn) {
			getActivityFace().switchFragment(new HomeTabsFragment());
		} else if (v.getId() == R.id.skipLay) {
			getActivityFace().switchFragment(new HomeTabsFragment());
		} else if (v.getId() == R.id.createProfileBtn) {
			getActivityFace().openFragment(new InviteFragment());
		}
	}

	private class CountrySelectedListener implements PopupListSelectionFace {

		@Override
		public void valueSelected(int code) {
			countriesFragment.dismiss();
			countriesFragment = null;
			countryEdt.setText(countryNames[code]);

			AppData.setUserCountry(getActivity(), countryNames[code]);
			countryEdt.updateRightIcon(AppUtils.getCountryFlag(getActivity(), countryNames[code]));
		}

		@Override
		public void dialogCanceled() {
			countriesFragment = null;
		}
	}

	@Override
	public void valueSelected(int code) {
		skillsFragment.dismiss();
		skillsFragment = null;
		skillEdt.setText(skillNames[code]);

		AppData.setUserSkill(getActivity(), code);
	}

	@Override
	public void dialogCanceled() {
		skillsFragment = null;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.skillEdt) {
			showSkillsFragment();
		} else if (v.getId() == R.id.countryEdt)  {
			showCountriesFragment();
		}

		return true;
	}

	private void showSkillsFragment() {
		if (skillsFragment != null) {
			return;
		}
		skillsFragment = PopupSkillsFragment.newInstance(this);
		skillsFragment.show(getFragmentManager(), SKILL_SELECTION);
	}

	private void showCountriesFragment() {
		if (countriesFragment != null) {
			return;
		}
		countriesFragment = PopupCountriesFragment.newInstance(countrySelectedListener);
		countriesFragment.show(getFragmentManager(), COUNTRY_SELECTION);
	}

}
