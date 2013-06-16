package com.chess.ui.fragments.welcome;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.LeftImageEditText;
import com.chess.LeftRightImageEditText;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.UserItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.home.HomeTabsFragment;
import com.chess.ui.fragments.popup_fragments.PopupCountriesFragment;
import com.chess.ui.fragments.popup_fragments.PopupSelectPhotoFragment;
import com.chess.ui.fragments.popup_fragments.PopupSkillsFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.utilities.AppUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.01.13
 * Time: 9:09
 */
public class CreateProfileFragment extends CommonLogicFragment implements View.OnClickListener, PopupListSelectionFace {

	public static final String SKILL_SELECTION = "SKILL_SELECTION";
	public static final String COUNTRY_SELECTION = "COUNTRY_SELECTION";
	public static final String PHOTO_SELECTION = "PHOTO_SELECTION";

	private static final String DEFAULT_COUNTRY = "United States";
	private static final int AVATAR_SIZE = 80;
	private static final int REQ_CODE_PICK_IMAGE = 33;
	private static final int REQ_CODE_TAKE_IMAGE = 55;
	private static final int NON_INIT = -1;


	private String[] skillNames;
	private String[] countryNames;
	private String[] countryCodes;
	private int[] countryIds;
	private int userCountryId = NON_INIT;
	private LeftRightImageEditText skillEdt;
	private LeftRightImageEditText countryEdt;
	private LeftRightImageEditText profilePhotoEdt;
	private PopupSkillsFragment skillsFragment;
	private CreateProfileUpdateListener createProfileUpdateListener;
	private LeftImageEditText firstNameEdt;
	private LeftImageEditText lastNameEdt;
	/* photo */
	private EnhancedImageDownloader imageDownloader;
	private PopupCountriesFragment countriesFragment;
	private PopupSelectPhotoFragment photoSelectFragment;
	private PhotoSelectedListener photoSelectedListener;
	private CountrySelectedListener countrySelectedListener;
	private String mCurrentPhotoPath;
	private boolean photoChanged;
	private int photoFileSize;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		skillNames = getResources().getStringArray(R.array.skills_name);
		countryNames = getResources().getStringArray(R.array.new_countries);
		countryCodes = getResources().getStringArray(R.array.new_countries_codes);
		countryIds = getResources().getIntArray(R.array.new_country_ids);
		imageDownloader = new EnhancedImageDownloader(getActivity());
		countrySelectedListener = new CountrySelectedListener();
		photoSelectedListener = new PhotoSelectedListener();

		createProfileUpdateListener = new CreateProfileUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_create_profile_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(false);

		view.findViewById(R.id.createProfileBtn).setOnClickListener(this);
		view.findViewById(R.id.skipBtn).setOnClickListener(this);
		view.findViewById(R.id.skipLay).setOnClickListener(this);

		firstNameEdt = (LeftImageEditText) view.findViewById(R.id.firstNameEdt);
		lastNameEdt = (LeftImageEditText) view.findViewById(R.id.lastNameEdt);
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
					userCountryId = countryIds[i];
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
			createProfile();
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
//					String filePath = cursor.getString(columnIndex);
					mCurrentPhotoPath = cursor.getString(columnIndex);
					cursor.close();


					float density = getResources().getDisplayMetrics().density;
					int size = (int) (58.5f * density); // TODO remove hardcode
					Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
					bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
					photoFileSize = AppUtils.sizeOfBitmap(bitmap);
					Drawable drawable = new BitmapDrawable(getResources(), bitmap);
					drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
					profilePhotoEdt.setRightIcon(drawable);
				}
				break;
			case REQ_CODE_TAKE_IMAGE:
				setPic();
				break;
		}
	}

	private void setPic() {
		// Get the dimensions of the View

		int targetW = profilePhotoEdt.getRightImageWidth();
		int targetH = profilePhotoEdt.getRightImageHeight();

		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		float density = getResources().getDisplayMetrics().density;
		int size = (int) (58.5f * density); // TODO remove hardcode
		bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
		photoFileSize = AppUtils.sizeOfBitmap(bitmap);
		Drawable drawable = new BitmapDrawable(getResources(), bitmap);
		drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());

		profilePhotoEdt.setRightIcon(drawable);
		photoChanged = true;
	}

	private void createProfile() {
		// check needed fields

		if (userCountryId == NON_INIT) {
			countryEdt.setError(getString(R.string.required));
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.setLoadPath(RestHelper.CMD_USER_PROFILE);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_FIRST_NAME, getTextFromField(firstNameEdt));
		loadItem.addRequestParams(RestHelper.P_LAST_NAME, getTextFromField(lastNameEdt));
		loadItem.addRequestParams(RestHelper.P_COUNTRY_ID, userCountryId);
		loadItem.addRequestParams(RestHelper.P_SKILL_LEVEL, AppData.getUserSkill(getActivity()));
		loadItem.setFileMark(RestHelper.P_AVATAR);
		loadItem.setFilePath(mCurrentPhotoPath);
		loadItem.setFileSize(photoFileSize);

		new RequestJsonTask<UserItem>(createProfileUpdateListener).executeTask(loadItem);
	}

	private class CreateProfileUpdateListener extends ChessUpdateListener<UserItem> {

		public CreateProfileUpdateListener() {
			super(UserItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (show) {
				showPopupHardProgressDialog(R.string.processing_);
			} else {
				if (isPaused)
					return;

				dismissProgressDialog();
			}
		}

		@Override
		public void updateData(UserItem returnedObj) {
			AppData.setUserAvatar(getActivity(), returnedObj.getData().getAvatar());

			getActivityFace().openFragment(new InviteFragment());
		}
	}

	private class CountrySelectedListener implements PopupListSelectionFace {

		@Override
		public void onValueSelected(int code) {
			countriesFragment.dismiss();
			countriesFragment = null;
			String country = countryNames[code];
			userCountryId = countryIds[code];

			AppData.setUserCountry(getActivity(), country);
			updateUserCountry(country);
		}

		@Override
		public void onDialogCanceled() {
			countriesFragment = null;
		}
	}

	private class PhotoSelectedListener implements PopupListSelectionFace {

		private static final String JPEG_FILE_PREFIX = "IMG_";
		private static final String JPEG_FILE_SUFFIX = ".jpg";

		@Override
		public void onValueSelected(int code) {
			photoSelectFragment.dismiss();
			photoSelectFragment = null;
			if (code == 0) {
				Intent photoPickIntent = new Intent(Intent.ACTION_GET_CONTENT);
				photoPickIntent.setType("image/*");
				startActivityForResult(Intent.createChooser(photoPickIntent, getString(R.string.pick_photo)), REQ_CODE_PICK_IMAGE);
			} else {
				if (AppUtils.isIntentAvailable(getActivity(), MediaStore.ACTION_IMAGE_CAPTURE)) {
					Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File file;

					try {
						file = setUpPhotoFile();
						mCurrentPhotoPath = file.getAbsolutePath();
						takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
					} catch (IOException e) {
						e.printStackTrace();
						mCurrentPhotoPath = null;
					}
					startActivityForResult(takePictureIntent, REQ_CODE_TAKE_IMAGE);
				}
			}
		}

		private File setUpPhotoFile() throws IOException {

			File f = createImageFile();
			mCurrentPhotoPath = f.getAbsolutePath();

			return f;
		}

		private File createImageFile() throws IOException {
			// Create an image file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
			File image = File.createTempFile(
					imageFileName,
					JPEG_FILE_SUFFIX,
					getAlbumDir()
			);
			mCurrentPhotoPath = image.getAbsolutePath();
			return image;
		}

		@Override
		public void onDialogCanceled() {
			photoSelectFragment = null;
		}
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

			storageDir = getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	private String getAlbumName() {
		return "Chess.com";
	}

	public File getAlbumStorageDir(String albumName) {
		return new File(
				Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES
				),
				albumName
		);
	}

	@Override
	public void onValueSelected(int code) {
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
	public void onDialogCanceled() {
		skillsFragment = null;
	}

	private void showSkillsFragment() {
		if (skillsFragment != null) {
			return;
		}
		skillsFragment = PopupSkillsFragment.createInstance(this);
		skillsFragment.show(getFragmentManager(), SKILL_SELECTION);
	}

	private void showCountriesFragment() {
		if (countriesFragment != null) {
			return;
		}
		countriesFragment = PopupCountriesFragment.createInstance(countrySelectedListener);
		countriesFragment.show(getFragmentManager(), COUNTRY_SELECTION);
	}

	private void showPhotoSelectFragment() {
		if (photoSelectFragment != null) {
			return;
		}
		photoSelectFragment = PopupSelectPhotoFragment.createInstance(photoSelectedListener);
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
