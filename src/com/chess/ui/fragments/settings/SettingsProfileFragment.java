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
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.UserItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.popup_fragments.PopupCountriesFragment;
import com.chess.ui.fragments.popup_fragments.PopupSelectPhotoFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.utilities.AppUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
	public static final String BLUE_COLOR_DIVIDER = "##";

	private static final int REQ_CODE_PICK_IMAGE = 33;
	private static final int REQ_CODE_TAKE_IMAGE = 55;
	private static final int FILE_SIZE_LIMIT = 2 * 1024 * 1024;
	private static final int IMG_SIZE_LIMIT_H = 600; // limit for maximum side size
	private static final int IMG_SIZE_LIMIT_W = 800; // limit for maximum side size



	private ImageView flagImg;
	private TextView cancelMembershipTxt;
	private EditText firstNameValueEdt;
	private EditText lastNameValueEdt;
	private EditText locationValueEdt;
	private String membershipType;
	private String membershipExpireDate;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		density = getResources().getDisplayMetrics().density;
		AVATAR_SIZE = (int) (getResources().getDimension(R.dimen.img_profile_size_big) / density);
		progressImageView = new ProgressImageView(getContext(), AVATAR_SIZE);

		dateFormatter = new SimpleDateFormat(getString(R.string.membership_expire_date_format));
		imageDownloader = new EnhancedImageDownloader(getActivity());
		photoSelectedListener = new PhotoSelectedListener();
		countryNames = getResources().getStringArray(R.array.new_countries);
		countryCodes = getResources().getIntArray(R.array.new_country_ids);
		countrySelectedListener = new CountrySelectedListener();

		// fields data init
		firstNameStr = AppData.getUserFirstName(getActivity());
		lastNameStr = AppData.getUserLastName(getActivity());
		locationStr = AppData.getUserLocation(getActivity());
		countryStr = AppData.getUserCountry(getActivity());
		countryId = AppData.getUserCountryId(getActivity());

		userUpdateListener = new GetUserUpdateListener();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_profile_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.profile);

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
			String termsText = getString(R.string.profile_account_message_1) + StaticData.SYMBOL_NEW_STR + StaticData.SYMBOL_NEW_STR
					+ getString(R.string.profile_account_message_2) + StaticData.SYMBOL_SPACE
					+ getString(R.string.profile_account_message_3) + StaticData.SYMBOL_SPACE
					+ getString(R.string.profile_account_message_4);
			termsLinkTxt.setText(Html.fromHtml(termsText));
			Linkify.addLinks(termsLinkTxt, Linkify.WEB_URLS);
			termsLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
			termsLinkTxt.setLinkTextColor(Color.WHITE);
		}

		view.findViewById(R.id.shareBtn).setOnClickListener(this);
		view.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		view.findViewById(R.id.userPhotoLay).setOnClickListener(this);
		view.findViewById(R.id.countryArrowIconTxt).setOnClickListener(this);
		view.findViewById(R.id.countryLay).setOnClickListener(this);

		// filling fields
		firstNameValueEdt.setText(firstNameStr);
		lastNameValueEdt.setText(lastNameStr);
		locationValueEdt.setText(locationStr);
		countryValueTxt.setText(countryStr);

		// hide close buttons
		firstNameClearBtn.setVisibility(/*TextUtils.isEmpty(firstNameStr)? View.VISIBLE :*/ View.GONE);
		lastNameClearBtn.setVisibility(/*TextUtils.isEmpty(lastNameStr)? View.VISIBLE :*/ View.GONE);
		locationClearBtn.setVisibility(/*TextUtils.isEmpty(locationStr)? View.VISIBLE :*/ View.GONE);

		// set country flag
		updateUserCountry(countryStr);
	}

	@Override
	public void onResume() {
		super.onResume();

		updateData();
	}

	private void updateData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_USERS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));

		new RequestJsonTask<UserItem>(new GetUserUpdateListener()).executeTask(loadItem);
	}

	private class GetUserUpdateListener extends ChessUpdateListener<UserItem> {

		public GetUserUpdateListener() {
			super(UserItem.class);
		}

		@Override
		public void updateData(UserItem returnedObj) {
			super.updateData(returnedObj);

			// fill parameters
			membershipType = AppData.getUserPremiumStatusStr(getActivity());
			if (!membershipType.equals(getString(R.string.basic))) {
				membershipExpireDate = dateFormatter.format(Calendar.getInstance().getTime()); // TODO set correct time
				cancelMembershipTxt.setText(getString(R.string.profile_membership_renew_cancel, membershipType, membershipExpireDate));
			} else {
				cancelMembershipTxt.setText("Basic user text");
			}

			{// load user avatar
				int imageSize = (int) (AVATAR_SIZE * density);
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageSize, imageSize);

				ViewGroup parent = (ViewGroup) progressImageView.getParent();
				if (parent != null) {
					parent.removeAllViews();
				}
				userPhotoImg.addView(progressImageView, params);
				AppData.setUserAvatar(getActivity(), returnedObj.getData().getAvatar());
				if (!photoChanged) {
					imageDownloader.download(returnedObj.getData().getAvatar(), progressImageView, AVATAR_SIZE);
				}
			}
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.firstNameClearBtn) {
			firstNameValueEdt.setText(StaticData.SYMBOL_EMPTY);
		} else if (id == R.id.lastNameClearBtn) {
			lastNameValueEdt.setText(StaticData.SYMBOL_EMPTY);
		} else if (id == R.id.locationClearBtn) {
			locationValueEdt.setText(StaticData.SYMBOL_EMPTY);
		} else if (id == R.id.countryLay || id == R.id.flagImg || id == R.id.countryArrowIconTxt) {
			showCountriesFragment();
			startActionMode();
		} else if (id == R.id.userPhotoImg || id == R.id.userPhotoLay) {
			showPhotoSelectFragment();
			startActionMode();
		} else if (id == R.id.shareBtn) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invitation Subject");
			shareIntent.putExtra(Intent.EXTRA_TEXT, "Invitation text");
			startActivity(Intent.createChooser(shareIntent, getString(R.string.invite_a_friend)));
		} else if (id == R.id.upgradeBtn) {
			getActivityFace().openFragment(new UpgradeFragment());
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


					Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
					cursor.moveToFirst();

					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					mCurrentPhotoPath = cursor.getString(columnIndex);
					cursor.close();

					int size = (int) (AVATAR_SIZE * density);
					Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

					int rawSize = AppUtils.sizeOfBitmap(bitmap);
					if (rawSize > FILE_SIZE_LIMIT) {
						saveImageForUpload();
					}

					bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
					if (bitmap == null) {
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

	private void createProfile() {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.setLoadPath(RestHelper.CMD_USER_PROFILE);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_FIRST_NAME, firstNameStr);
		loadItem.addRequestParams(RestHelper.P_LAST_NAME, lastNameStr);
		loadItem.addRequestParams(RestHelper.P_COUNTRY_ID, countryId);
		loadItem.setFileMark(RestHelper.P_AVATAR);
		loadItem.setFilePath(mCurrentPhotoPath);
		loadItem.setFileSize(photoFileSize);

		new RequestJsonTask<UserItem>(new CreateProfileUpdateListener()).executeTask(loadItem);
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
			imageDownloader.download(returnedObj.getData().getAvatar(), progressImageView, AVATAR_SIZE);
		}
	}

	private void setPic() {
		// Get the dimensions of the View
		int targetW = progressImageView.getWidth();
		int targetH = progressImageView.getHeight();

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
		if (bitmap == null) {
			return;
		}

		int rawSize = AppUtils.sizeOfBitmap(bitmap);
		if (rawSize > FILE_SIZE_LIMIT) {
			saveImageForUpload();
		}

		progressImageView.setImageBitmap(bitmap);
		photoChanged = true;
	}

	private void saveImageForUpload() {
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

		bitmap = Bitmap.createScaledBitmap(bitmap, IMG_SIZE_LIMIT_W, IMG_SIZE_LIMIT_H, false);
		if (bitmap == null) {
			return;
		}
		photoFileSize = AppUtils.sizeOfBitmap(bitmap);

		File cacheDir;
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), AppUtils.getApplicationCacheDir(getActivity().getPackageName()));
		else
			cacheDir = getActivity().getCacheDir();

		if (!cacheDir.exists())// TODO adjust saving to SD or local , but if not show warning to user
			cacheDir.mkdirs();

		// save scaled bitmap to sd for upload
		String filename = AppData.getUserName(getActivity()) + System.currentTimeMillis();
		File imgFile = new File(cacheDir, filename);

		// save stream to SD
		try {
			OutputStream os = new FileOutputStream(imgFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// update path for upload
		mCurrentPhotoPath = imgFile.getAbsolutePath();
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
		} else if (v.getId() == R.id.lastNameValueEdt) {
			lastNameValueEdt.setBackgroundResource(R.drawable.textfield_selector);
			lastNameClearBtn.setVisibility(View.GONE);
			firstNameClearBtn.setVisibility(View.GONE);
			locationClearBtn.setVisibility(View.GONE);

			firstNameValueEdt.setBackgroundResource(R.color.transparent);
			locationValueEdt.setBackgroundResource(R.color.transparent);
		} else if (v.getId() == R.id.locationValueEdt) {
			locationValueEdt.setBackgroundResource(R.drawable.textfield_selector);
			locationClearBtn.setVisibility(View.GONE);
			lastNameClearBtn.setVisibility(View.GONE);
			firstNameClearBtn.setVisibility(View.GONE);

			firstNameValueEdt.setBackgroundResource(R.color.transparent);
			lastNameValueEdt.setBackgroundResource(R.color.transparent);
		}
		return false;
	}

	private class DoneClickListener implements ActionModeHelper.EditFace {

		@Override
		public void onDoneClicked() {
			if (fieldsWasChanged()) {
				hideKeyBoard();
				// hide close buttons
				firstNameClearBtn.setVisibility(View.GONE);
				lastNameClearBtn.setVisibility(View.GONE);
				locationClearBtn.setVisibility(View.GONE);

				// remember fields
				String firstNameTmp = getTextFromField(firstNameValueEdt);
//				if (!TextUtils.isEmpty(firstNameTmp)) {
					firstNameStr = firstNameTmp;
//				}
				String lastNameTmp = getTextFromField(lastNameValueEdt);
//				if (!TextUtils.isEmpty(lastNameTmp)) {
					lastNameStr = lastNameTmp;
//				}
				String locationTmp = getTextFromField(locationValueEdt);
//				if (!TextUtils.isEmpty(locationTmp)) {
					locationStr = locationTmp;
//				}

				AppData.setUserFirstName(getActivity(), firstNameStr);
				AppData.setUserLastName(getActivity(), lastNameStr);
				AppData.setUserLocation(getActivity(), locationStr);
				AppData.setUserCountry(getActivity(), countryStr);
				AppData.setUserCountryId(getActivity(), countryId);

				firstNameValueEdt.setBackgroundResource(R.color.transparent);
				lastNameValueEdt.setBackgroundResource(R.color.transparent);
				locationValueEdt.setBackgroundResource(R.color.transparent);

				createProfile();
			}

			inEditMode = false;
		}
	}

	private void startActionMode() {
		if (inEditMode) {
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
				|| photoChanged || countryId != AppData.getUserCountryId(getActivity());
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
			Log.d(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
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
