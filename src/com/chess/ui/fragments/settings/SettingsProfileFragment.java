package com.chess.ui.fragments.settings;

import actionbarcompat.ActionModeHelper;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.MembershipItem;
import com.chess.backend.entity.api.UserItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.popup_fragments.PopupCountriesFragment;
import com.chess.ui.fragments.popup_fragments.PopupSelectPhotoFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragmentTablet;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.utilities.AppUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.04.13
 * Time: 11:09
 */
public class SettingsProfileFragment extends CommonLogicFragment implements TextView.OnEditorActionListener, View.OnTouchListener {

	public static final String PHOTO_SELECTION = "PHOTO_SELECTION";
	public static final String COUNTRY_SELECTION = "COUNTRY_SELECTION";
	private static final String EDIT_MODE = "edit_mode";
	public static final String BLUE_COLOR_DIVIDER = "##";

	public static final int REQ_CODE_PICK_IMAGE = 33;
	public static final int REQ_CODE_TAKE_IMAGE = 55;
	private static final int FILE_SIZE_LIMIT = 2 * 1024 * 1024;
	private static final int IMG_SIZE_LIMIT_H = 600; // limit for maximum side size
	private static final int IMG_SIZE_LIMIT_W = 800; // limit for maximum side size


	private ImageView flagImg;
	private TextView cancelMembershipTxt;
	private EditText firstNameValueEdt;
	private EditText lastNameValueEdt;
	private EditText locationValueEdt;
	private SimpleDateFormat dateFormatter;
	/* photo */
	private FrameLayout userPhotoImg;
	private GetUserUpdateListener userUpdateListener;
	private EnhancedImageDownloader imageDownloader;
	private ProgressImageView progressImageView;
	private PopupCountriesFragment countriesFragment;
	private PopupSelectPhotoFragment photoSelectFragment;
	private PhotoSelectedListener photoSelectedListener;
	private CountrySelectedListener countrySelectedListener;
	private String mCurrentPhotoPath;
	private boolean photoChanged;
	/* country */
	private int[] countryCodes;
	private String[] countryNames;
	private TextView countryValueTxt;

	private TextView firstNameClearBtn;
	private TextView lastNameClearBtn;
	private TextView locationClearBtn;
	private String firstNameStr;
	private String lastNameStr;
	private String locationStr;
	private int countryId;
	private boolean inEditMode;
	private int photoFileSize;
	private int AVATAR_SIZE;
	private float density;
	private String countryStr;
	private ActionModeHelper actionModeHelper;
	private boolean discarded;
	private MembershipItem.Data membershipData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		density = getResources().getDisplayMetrics().density;
		AVATAR_SIZE = (int) (getResources().getDimension(R.dimen.img_profile_size_big) / density);
		progressImageView = new ProgressImageView(getContext(), AVATAR_SIZE);

		dateFormatter = new SimpleDateFormat(getString(R.string.common_date_format));
		imageDownloader = new EnhancedImageDownloader(getActivity());
		photoSelectedListener = new PhotoSelectedListener();
		countryNames = getResources().getStringArray(R.array.new_countries);
		countryCodes = getResources().getIntArray(R.array.new_country_ids);
		countrySelectedListener = new CountrySelectedListener();

		// fields data init
		firstNameStr = getAppData().getUserFirstName();
		lastNameStr = getAppData().getUserLastName();
		locationStr = getAppData().getUserLocation();
		countryStr = getAppData().getUserCountry();
		countryId = getAppData().getUserCountryId();

		userUpdateListener = new GetUserUpdateListener();

		if (savedInstanceState != null) {
			boolean inEditMode = savedInstanceState.getBoolean(EDIT_MODE);
			if (inEditMode) {
				startActionMode();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_profile_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(getUsername());

		{// Edit texts and clear buttons
			firstNameValueEdt = (EditText) view.findViewById(R.id.firstNameValueEdt);
			lastNameValueEdt = (EditText) view.findViewById(R.id.lastNameValueEdt);
			locationValueEdt = (EditText) view.findViewById(R.id.locationValueEdt);

			firstNameValueEdt.setOnTouchListener(this);
			lastNameValueEdt.setOnTouchListener(this);
			locationValueEdt.setOnTouchListener(this);

			firstNameValueEdt.setOnEditorActionListener(this);
			lastNameValueEdt.setOnEditorActionListener(this);
			locationValueEdt.setOnEditorActionListener(this);

			firstNameClearBtn = (TextView) view.findViewById(R.id.firstNameClearBtn);
			lastNameClearBtn = (TextView) view.findViewById(R.id.lastNameClearBtn);
			locationClearBtn = (TextView) view.findViewById(R.id.locationClearBtn);

			firstNameClearBtn.setOnClickListener(this);
			lastNameClearBtn.setOnClickListener(this);
			locationClearBtn.setOnClickListener(this);
		}

		flagImg = (ImageView) view.findViewById(R.id.flagImg);
		flagImg.setImageDrawable(new IconDrawable(getActivity(), R.string.ic_country));
		flagImg.setOnClickListener(this);

		countryValueTxt = (TextView) view.findViewById(R.id.countryValueTxt);
		countryValueTxt.setOnClickListener(this);

		cancelMembershipTxt = (TextView) view.findViewById(R.id.cancelMembershipTxt);
		userPhotoImg = (FrameLayout) view.findViewById(R.id.userPhotoImg);
		userPhotoImg.setOnClickListener(this);


		{ // Terms link handle
			TextView termsLinkTxt = (TextView) view.findViewById(R.id.termsLinkTxt);
			termsLinkTxt.setClickable(true);
			String termsText = getString(R.string.profile_account_message_1) + Symbol.NEW_STR + Symbol.NEW_STR
					+ getString(R.string.profile_account_message_2) + Symbol.SPACE
					+ getString(R.string.profile_account_message_3) + Symbol.SPACE
					+ getString(R.string.profile_account_message_4);
			termsLinkTxt.setText(Html.fromHtml(termsText));
			Linkify.addLinks(termsLinkTxt, Linkify.WEB_URLS);
			termsLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
			termsLinkTxt.setLinkTextColor(Color.WHITE);
		}

		view.findViewById(R.id.shareBtn).setOnClickListener(this);
		view.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		view.findViewById(R.id.userPhotoLay).setOnClickListener(this);
		view.findViewById(R.id.countryLay).setOnClickListener(this);

		// filling fields
		firstNameValueEdt.setText(firstNameStr);
		lastNameValueEdt.setText(lastNameStr);
		locationValueEdt.setText(locationStr);
		countryValueTxt.setText(countryStr);

		// hide close buttons
		firstNameClearBtn.setVisibility(View.GONE);
		lastNameClearBtn.setVisibility(View.GONE);
		locationClearBtn.setVisibility(View.GONE);

		// set country flag
		updateUserCountry(countryStr);
	}

	@Override
	public void onResume() {
		super.onResume();

//		logTest("inEditMode = " + inEditMode);
		if (!inEditMode && need2update) {
			updateData();
		}
	}

	private void updateData() {
		LoadItem loadItem = LoadHelper.getUserInfo(getUserToken());

		new RequestJsonTask<UserItem>(userUpdateListener).executeTask(loadItem);
	}

	public void discardChanges() {
		discarded = true;
//		logTest("discard");
		inEditMode = false;
		if (actionModeHelper != null) {
			actionModeHelper.closeActionMode();
		}
	}

	private class GetUserUpdateListener extends ChessLoadUpdateListener<UserItem> {

		public GetUserUpdateListener() {
			super(UserItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (!isTablet) {
				super.showProgress(show);
			}
		}

		@Override
		public void updateData(UserItem returnedObj) {
			super.updateData(returnedObj);
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}
			UserItem.Data data = returnedObj.getData();

			{// load user avatar
				int imageSize = (int) (AVATAR_SIZE * density);
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageSize, imageSize);

				ViewGroup parent = (ViewGroup) progressImageView.getParent();
				if (parent != null) {
					parent.removeAllViews();
				}
				userPhotoImg.addView(progressImageView, params);
				getAppData().setUserAvatar(data.getAvatar());
				if (!photoChanged) {
					imageDownloader.download(data.getAvatar(), progressImageView, AVATAR_SIZE);
				}
			}

			firstNameStr = data.getFirstName();
			lastNameStr = data.getLastName();
			locationStr = data.getLocation();
			countryStr = data.getCountryName();
			countryId = data.getCountryId();

			getAppData().setUserFirstName(firstNameStr);
			getAppData().setUserLastName(lastNameStr);
			getAppData().setUserLocation(locationStr);
			getAppData().setUserCountry(countryStr);
			getAppData().setUserCountryId(countryId);

			firstNameValueEdt.setText(firstNameStr);
			lastNameValueEdt.setText(lastNameStr);
			locationValueEdt.setText(locationStr);

			updateUserCountry(countryStr);

			if (need2update) {
				LoadItem loadItem = LoadHelper.getMembershipDetails(getUserToken());
				new RequestJsonTask<MembershipItem>(new GetDetailsListener()).executeTask(loadItem); // TODO set proper item
			} else {
				fillMembershipData();
			}
		}
	}

	private class GetDetailsListener extends ChessUpdateListener<MembershipItem> {

		private GetDetailsListener() {
			super(MembershipItem.class);
		}

		@Override
		public void updateData(MembershipItem returnedObj) {
			super.updateData(returnedObj);

			Activity activity = getActivity(); // TODO probably redundant
			if (activity == null) {
				return;
			}

			// update selected modes
			membershipData = returnedObj.getData();
			fillMembershipData();

			need2update = false;
		}
	}

	private void fillMembershipData() {
		if (membershipData.getIs_premium() > 0) {
			getAppData().setUserPremiumStatus(membershipData.getLevel());

			String membershipExpireDate;
			if (membershipData.getDate().getExpires() != 0) {
				Date time = new Date(membershipData.getDate().getExpires()* 1000L);
				membershipExpireDate = dateFormatter.format(time);
			} else {
				membershipExpireDate = "--/--/--";
			}

			String text = getString(R.string.profile_membership_renew_cancel, membershipData.getType(), membershipExpireDate)
					+ Symbol.NEW_STR + getString(R.string.profile_membership_renew_cancel_1);

			cancelMembershipTxt.setClickable(true);
			cancelMembershipTxt.setText(Html.fromHtml(text));
			Linkify.addLinks(cancelMembershipTxt, Linkify.WEB_URLS);
			cancelMembershipTxt.setMovementMethod(LinkMovementMethod.getInstance());
			cancelMembershipTxt.setLinkTextColor(Color.WHITE);
		} else {
			cancelMembershipTxt.setText(R.string.upgrade_to_unlimited_access);
		}
		cancelMembershipTxt.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.firstNameClearBtn) {
			firstNameValueEdt.setText(Symbol.EMPTY);
		} else if (id == R.id.lastNameClearBtn) {
			lastNameValueEdt.setText(Symbol.EMPTY);
		} else if (id == R.id.locationClearBtn) {
			locationValueEdt.setText(Symbol.EMPTY);
		} else if (id == R.id.countryLay || id == R.id.flagImg || id == R.id.countryValueTxt) {
			showCountriesFragment();
			startActionMode();
		} else if (id == R.id.userPhotoImg || id == R.id.userPhotoLay) {
			showPhotoSelectFragment();
			startActionMode();
		} else if (id == R.id.shareBtn) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this awesome Chess app");
			shareIntent.putExtra(Intent.EXTRA_TEXT, "Chess.com v3 Beta available now only via \n\nBeta community "
					+ "\n\nhttps://plus.google.com/u/0/communities/103811010308225325535"
					+ "\n\n Chess.com app in Google Play \n\nhttps://play.google.com/store/apps/details?id=com.chess");
			startActivity(Intent.createChooser(shareIntent, getString(R.string.invite_a_friend)));
		} else if (id == R.id.upgradeBtn) {
			if (!isTablet) {
				getActivityFace().openFragment(new UpgradeFragment());
			} else {
				getActivityFace().openFragment(new UpgradeFragmentTablet());
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
			case REQ_CODE_PICK_IMAGE:
				if (resultCode == Activity.RESULT_OK) {
					mCurrentPhotoPath = Symbol.EMPTY; // drop previous file path

					Uri selectedImage = imageReturnedIntent.getData();
					if (selectedImage == null) {
						return;
					}
					String[] filePathColumn = {MediaStore.Images.Media.DATA};

					Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
					if (cursor != null && cursor.moveToFirst()) {
						int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
						mCurrentPhotoPath = cursor.getString(columnIndex);
						cursor.close();
					}

					if (TextUtils.isEmpty(mCurrentPhotoPath)) {
						mCurrentPhotoPath = selectedImage.getPath();
					}
					Bitmap bitmap = null;
					if (imageReturnedIntent.getDataString() != null && imageReturnedIntent.getDataString().contains("docs.file")) {
						try {
							InputStream inputStream = getContentResolver().openInputStream(selectedImage);
							bitmap = BitmapFactory.decodeStream(inputStream);
							saveImageForUpload(bitmap); // save to get appropriate filePath

						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}

					int size = (int) (AVATAR_SIZE * density);
					if (bitmap == null) {
						bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
					}
					logTest("PATH: " + mCurrentPhotoPath);
					if (bitmap == null) {
						logTest("WRONG PATH: " + mCurrentPhotoPath);
						showToast(R.string.unable_to_select_this_photo);
						return;
					}

					int rawSize = AppUtils.sizeOfBitmap(bitmap);
					if (rawSize > FILE_SIZE_LIMIT) {         // TODO
						showToast(R.string.optimizing_image);
						saveImageForUpload(bitmap);
					}

					bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
					if (bitmap == null) {
						showToast(R.string.unable_to_select_this_photo);
						return;
					}
					Drawable drawable = new BitmapDrawable(getResources(), bitmap);
					drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());

					progressImageView.setImageDrawable(drawable);
					photoChanged = true;
				}
				break;
			case REQ_CODE_TAKE_IMAGE:
				setPic();
				break;
		}
	}

	private void updateProfile() {
		LoadItem loadItem = LoadHelper.postUserProfile(getUserToken(), firstNameStr, lastNameStr,
				countryId, getAppData().getUserSkill());
		loadItem.addRequestParams(RestHelper.P_LOCATION, locationStr);
		loadItem.setFileMark(RestHelper.P_AVATAR);
		loadItem.setFilePath(mCurrentPhotoPath);
		loadItem.setFileSize(photoFileSize);

		new RequestJsonTask<UserItem>(new CreateProfileUpdateListener()).executeTask(loadItem);
	}

	private class CreateProfileUpdateListener extends ChessLoadUpdateListener<UserItem> {

		public CreateProfileUpdateListener() {
			super(UserItem.class);
		}

		@Override
		public void updateData(UserItem returnedObj) {
			getAppData().setUserAvatar(returnedObj.getData().getAvatar());
			imageDownloader.download(returnedObj.getData().getAvatar(), progressImageView, AVATAR_SIZE);
		}
	}

	private void setPic() {
		// Get the dimensions of the View
		int targetW = Math.max(progressImageView.getWidth(), AVATAR_SIZE);
		int targetH = Math.max(progressImageView.getHeight(), AVATAR_SIZE);

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
		if (bitmap == null) { // TODO improve logic here
			return;
		}

		int rawSize = AppUtils.sizeOfBitmap(bitmap);
		if (rawSize > FILE_SIZE_LIMIT) {
			saveImageForUpload(bitmap);
		}

		progressImageView.setImageBitmap(bitmap);
		photoChanged = true;
	}

	private void saveImageForUpload(Bitmap bitmap) {
//		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

		bitmap = Bitmap.createScaledBitmap(bitmap, IMG_SIZE_LIMIT_W, IMG_SIZE_LIMIT_H, false);
		logTest("saveImageForUpload bitmap = " + bitmap);
		if (bitmap == null) { // TODO improve logic here
			return;
		}
		photoFileSize = AppUtils.sizeOfBitmap(bitmap);
		logTest("saveImageForUpload photoFileSize = " + photoFileSize);

		File cacheDir;
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), AppUtils.getApplicationCacheDirPath(getActivity().getPackageName()));
		else
			cacheDir = getActivity().getCacheDir();

		if (!cacheDir.exists())// TODO adjust saving to SD or local , but if not show warning to user
			cacheDir.mkdirs();

		// save scaled bitmap to sd for upload
		String filename = getAppData().getUsername() + System.currentTimeMillis();
		logTest("saveImageForUpload filename = " + filename);

		File imgFile = new File(cacheDir, filename);

		// save stream to SD
		try {
			OutputStream os = new FileOutputStream(imgFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			logTest("saveImageForUpload FileNotFoundException = " + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			logTest("saveImageForUpload IOException = " + e.toString());
			e.printStackTrace();
		}

		// update path for upload
		mCurrentPhotoPath = imgFile.getAbsolutePath();
		logTest("saveImageForUpload mCurrentPhotoPath = " + mCurrentPhotoPath);
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(EDIT_MODE, inEditMode);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
		if (actionId == EditorInfo.IME_ACTION_DONE ||
				((keyEvent != null) && (keyEvent.getAction() == KeyEvent.FLAG_EDITOR_ACTION
						|| keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER))
				) {
			if (v.getId() == R.id.firstNameValueEdt) {
				firstNameClearBtn.setVisibility(View.GONE);
				firstNameValueEdt.setBackgroundResource(R.color.transparent);
			} else if (v.getId() == R.id.lastNameValueEdt) {
				lastNameClearBtn.setVisibility(View.GONE);
				lastNameValueEdt.setBackgroundResource(R.color.transparent);
			} else if (v.getId() == R.id.locationValueEdt) {
				locationClearBtn.setVisibility(View.GONE);
				locationValueEdt.setBackgroundResource(R.color.transparent);
			}
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		startActionMode();

		if (v.getId() == R.id.firstNameValueEdt) {
			firstNameValueEdt.setBackgroundResource(R.drawable.textfield_selector);
			firstNameClearBtn.setVisibility(View.GONE); // TODO restore if need
			lastNameClearBtn.setVisibility(View.GONE);
			locationClearBtn.setVisibility(View.GONE);

			lastNameValueEdt.setBackgroundResource(R.color.transparent);
			locationValueEdt.setBackgroundResource(R.color.transparent);

			showKeyBoardImplicit(firstNameValueEdt);
		} else if (v.getId() == R.id.lastNameValueEdt) {
			lastNameValueEdt.setBackgroundResource(R.drawable.textfield_selector);
			lastNameClearBtn.setVisibility(View.GONE);
			firstNameClearBtn.setVisibility(View.GONE);
			locationClearBtn.setVisibility(View.GONE);

			firstNameValueEdt.setBackgroundResource(R.color.transparent);
			locationValueEdt.setBackgroundResource(R.color.transparent);

			showKeyBoardImplicit(lastNameValueEdt);
		} else if (v.getId() == R.id.locationValueEdt) {
			locationValueEdt.setBackgroundResource(R.drawable.textfield_selector);
			locationClearBtn.setVisibility(View.GONE);
			lastNameClearBtn.setVisibility(View.GONE);
			firstNameClearBtn.setVisibility(View.GONE);

			firstNameValueEdt.setBackgroundResource(R.color.transparent);
			lastNameValueEdt.setBackgroundResource(R.color.transparent);

			showKeyBoardImplicit(locationValueEdt);
		}

		return false;
	}

	private class DoneClickListener implements ActionModeHelper.EditFace {

		@Override
		public void onDoneClicked() {
			logTest("done clicked");
			if (!discarded && fieldsWasChanged()) {
				hideKeyBoard();
				// hide close buttons
				firstNameClearBtn.setVisibility(View.GONE);
				lastNameClearBtn.setVisibility(View.GONE);
				locationClearBtn.setVisibility(View.GONE);

				// remember fields
				firstNameStr = getTextFromField(firstNameValueEdt);
				lastNameStr = getTextFromField(lastNameValueEdt);
				locationStr = getTextFromField(locationValueEdt);

				getAppData().setUserFirstName(firstNameStr);
				getAppData().setUserLastName(lastNameStr);
				getAppData().setUserLocation(locationStr);
				getAppData().setUserCountry(countryStr);
				getAppData().setUserCountryId(countryId);

				firstNameValueEdt.setBackgroundResource(R.color.transparent);
				lastNameValueEdt.setBackgroundResource(R.color.transparent);
				locationValueEdt.setBackgroundResource(R.color.transparent);

				updateProfile();
			}

			inEditMode = false;
		}
	}

	private void startActionMode() {
		if (inEditMode) {
			logTest("already in edit mode");
			return;
		}
		inEditMode = true;
		if (actionModeHelper == null) {
			actionModeHelper = ActionModeHelper.createInstance(getActivityFace().getActionBarActivity());
		}
		actionModeHelper.setEditFace(new DoneClickListener());
		actionModeHelper.startActionMode();
	}

	private boolean fieldsWasChanged() {
		return !getTextFromField(firstNameValueEdt).equals(firstNameStr)
				|| !getTextFromField(lastNameValueEdt).equals(lastNameStr) || !getTextFromField(locationValueEdt).equals(locationStr)
				|| photoChanged || countryId != getAppData().getUserCountryId();
	}

	private class CountrySelectedListener implements PopupListSelectionFace {

		@Override
		public void onValueSelected(int code) {
			countriesFragment.dismiss();
			countriesFragment = null;
			countryStr = countryNames[code];
			countryId = countryCodes[code];

			updateUserCountry(countryStr);
		}

		@Override
		public void onDialogCanceled() {
			countriesFragment = null;
		}
	}

	private void updateUserCountry(String country) {
		countryValueTxt.setText(country);
		flagImg.setImageDrawable(AppUtils.getCountryFlagScaled(getActivity(), country));
	}

	private class PhotoSelectedListener implements PopupListSelectionFace {

		private static final String JPEG_FILE_PREFIX = "IMG_";
		private static final String JPEG_FILE_SUFFIX = ".jpg";

		@Override
		public void onValueSelected(int code) {
			photoSelectFragment.dismiss();
			photoSelectFragment = null;
			if (code == 0) { // TODO
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
			return File.createTempFile(
					imageFileName,
					JPEG_FILE_SUFFIX,
					getAlbumDir()
			);
		}

		@Override
		public void onDialogCanceled() {
			photoSelectFragment = null;
			inEditMode = false;
			if (actionModeHelper != null)
				actionModeHelper.closeActionMode();
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
			Log.d("TEST", "External storage is not mounted READ/WRITE.");
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

	//	private void stripUnderlines(TextView textView) {
//		SpannedString s = (SpannedString) textView.getText();
//		URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
//		for (URLSpan span: spans) {
//			int start = s.getSpanStart(span);
//			int end = s.getSpanEnd(span);
//			s. removeSpan(span);
//			span = new URLSpanNoUnderline(span.getURL());
//			s.setSpan(span, start, end, 0);
//		}
//		textView.setText(s);
//	}

//	private class URLSpanNoUnderline extends URLSpan {
//		public URLSpanNoUnderline(String url) {
//			super(url);
//		}
//		@Override
//		public void updateDrawState(TextPaint ds) {
//			super.updateDrawState(ds);
//			ds.setUnderlineText(false);
//		}
//	}
}
