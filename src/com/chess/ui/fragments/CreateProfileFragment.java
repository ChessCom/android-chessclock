package com.chess.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.LeftRightImageEditText;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.popup_fragments.PopupCountriesFragment;
import com.chess.ui.popup_fragments.PopupSelectPhotoFragment;
import com.chess.ui.popup_fragments.PopupSkillsFragment;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.01.13
 * Time: 9:09
 */
public class CreateProfileFragment extends ProfileSetupsFragment implements View.OnClickListener, PopupListSelectionFace {

	public static final String SKILL_SELECTION = "SKILL_SELECTION";
	public static final String COUNTRY_SELECTION = "COUNTRY_SELECTION";
	public static final String PHOTO_SELECTION = "PHOTO_SELECTION";

	private static final String DEFAULT_COUNTRY = "United States";
	private static final int REQ_CODE_PICK_IMAGE = 33;
	private static final int REQ_CODE_TAKE_IMAGE = 55;


	private String[] skillNames;
	private String[] countryNames;
	private String[] countryCodes;
	private LeftRightImageEditText skillEdt;
	private LeftRightImageEditText countryEdt;
	private LeftRightImageEditText profilePhotoEdt;
	private CountrySelectedListener countrySelectedListener;
	private PhotoSelectedListener photoSelectedListener;
	private PopupSkillsFragment skillsFragment;
	private PopupCountriesFragment countriesFragment;
	private PopupSelectPhotoFragment photoSelectFragment;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		skillNames = getResources().getStringArray(R.array.skills_name);
		countryNames = getResources().getStringArray(R.array.new_countries);
		countryCodes = getResources().getStringArray(R.array.new_countries_codes);
		countrySelectedListener = new CountrySelectedListener();
		photoSelectedListener = new PhotoSelectedListener();
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
		countryEdt.setOnClickListener(this);

		skillEdt = (LeftRightImageEditText) view.findViewById(R.id.skillEdt);
		skillEdt.setOnClickListener(this);

		profilePhotoEdt = (LeftRightImageEditText) view.findViewById(R.id.profilePhotoEdt);
		profilePhotoEdt.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (AppData.isUserSkillSet(getActivity())) {
			updateSkillLevel(AppData.getUserSkill(getActivity()));
		}

		String userCountry = AppData.getUserCountry(getActivity());
		if (userCountry == null) {
			String locale = getResources().getConfiguration().locale.getCountry();

			if (locale != null) {
				int i;
				boolean found = false;
				for (i = 0; i < countryCodes.length; i++) {
					String countryCode = countryCodes[i];
					if (locale.equals(countryCode)) {
						found = true;
						break;
					}
				}
				if (found) {
					userCountry = countryNames[i];
				} else {
					userCountry = DEFAULT_COUNTRY;
				}
			}
		}

		updateUserCountry(userCountry);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.skipBtn) {
			getActivityFace().switchFragment(new HomeTabsFragment());
		} else if (v.getId() == R.id.skipLay) {
			getActivityFace().switchFragment(new HomeTabsFragment());
		} else if (v.getId() == R.id.countryEdt) {
			showCountriesFragment();
		} else if (v.getId() == R.id.skillEdt) {
			showSkillsFragment();
		} else if (v.getId() == R.id.profilePhotoEdt) {
			showPhotoSelectFragment();
		} else if (v.getId() == R.id.createProfileBtn) {
			getActivityFace().openFragment(new InviteFragment());
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
			case REQ_CODE_PICK_IMAGE:
				if (resultCode == Activity.RESULT_OK) {
					Uri selectedImage = imageReturnedIntent.getData();
					String[] filePathColumn = {MediaStore.Images.Media.DATA};

					Cursor cursor = getContentResolver().query(
							selectedImage, filePathColumn, null, null, null);
					cursor.moveToFirst();

					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					String filePath = cursor.getString(columnIndex);
					cursor.close();


					float density = getResources().getDisplayMetrics().density;
					int size = (int) (58.5f * density);
					Bitmap bitmap = BitmapFactory.decodeFile(filePath);
					bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
					Drawable drawable = new BitmapDrawable(getResources(), bitmap);
					drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
					profilePhotoEdt.setRightIcon(drawable);
				}
				break;
		}
	}

	private class CountrySelectedListener implements PopupListSelectionFace {

		@Override
		public void valueSelected(int code) {
			countriesFragment.dismiss();
			countriesFragment = null;
			String country = countryNames[code];

			AppData.setUserCountry(getActivity(), country);
			updateUserCountry(country);
		}

		@Override
		public void dialogCanceled() {
			countriesFragment = null;
		}
	}

	private class PhotoSelectedListener implements PopupListSelectionFace {

		@Override
		public void valueSelected(int code) {
			photoSelectFragment.dismiss();
			photoSelectFragment = null;
			if (code == 0) {
				Intent photoPickIntent = new Intent(Intent.ACTION_GET_CONTENT);
				photoPickIntent.setType("image/*");
				startActivityForResult(Intent.createChooser(photoPickIntent, getString(R.string.pick_photo)), REQ_CODE_PICK_IMAGE);
			} else {
				Intent photoPickIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(Intent.createChooser(photoPickIntent, getString(R.string.pick_photo)), REQ_CODE_TAKE_IMAGE);
			}
		}

		@Override
		public void dialogCanceled() {
			photoSelectFragment = null;
		}
	}

	@Override
	public void valueSelected(int code) {
		skillsFragment.dismiss();
		skillsFragment = null;
		updateSkillLevel(code);

		AppData.setUserSkill(getActivity(), code);
	}

	private void updateSkillLevel(int level) {
		skillEdt.setText(skillNames[level]);
		skillEdt.setRightIcon(skillIcons[level]);
	}

	private void updateUserCountry(String country) {
		countryEdt.setText(country);
		countryEdt.setRightIcon(AppUtils.getCountryFlagScaled(getActivity(), country));
	}

	@Override
	public void dialogCanceled() {
		skillsFragment = null;
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

	private void showPhotoSelectFragment() {
		if (photoSelectFragment != null) {
			return;
		}
		photoSelectFragment = PopupSelectPhotoFragment.newInstance(photoSelectedListener);
		photoSelectFragment.show(getFragmentManager(), PHOTO_SELECTION);
	}

	private int[] skillIcons = new int[]{
			R.drawable.ic_skill_new,
			R.drawable.ic_skill_improving,
			R.drawable.ic_skill_intermediate,
			R.drawable.ic_skill_advanced,
			R.drawable.ic_skill_expert
	};


}
