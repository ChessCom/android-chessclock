package com.chess.ui.fragments.settings;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.GetAndSaveTheme;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.themes.BackgroundSingleItem;
import com.chess.backend.entity.api.themes.SoundSingleItem;
import com.chess.backend.entity.api.themes.SoundsItem;
import com.chess.backend.entity.api.themes.ThemeItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.FileReadyListener;
import com.chess.backend.tasks.GetAndSaveFileToSdTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.backend.tasks.SaveImageToSdTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.PopupItem;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.StringSpinnerAdapter;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.popup_fragments.PopupBackgroundsFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.PiecePreviewImg;
import com.chess.utilities.AppUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.07.13
 * Time: 21:07
 */
public class SettingsThemeCustomizeFragment extends CommonLogicFragment implements AdapterView.OnItemSelectedListener {

	public static final int PREVIEW_IMG_SIZE = 180;

	private static final String THEME_LOAD_TAG = "theme load popup";
	private static final String BACKGROUND_SELECTION = "background selection popup";
	protected static final String THEME_ITEM = "theme_item";

	private ImageUpdateListener backgroundUpdateListener;
	private ImageUpdateListener backgroundLandUpdateListener;
	private BackgroundImageSaveListener mainBackgroundImgSaveListener;
	private BackgroundImageSaveListener mainBackgroundLandImgSaveListener;
	private BackgroundItemUpdateListener backgroundItemUpdateListener;
	private BackgroundPopupListener backgroundPopupListener;
	private SoundsItemUpdateListener soundsItemUpdateListener;
	private SoundPackSaveListener soundPackSaveListener;

	private ImageDownloaderToListener imageDownloader;
	private EnhancedImageDownloader imageLoader;

	private ThemeItem.Data themeItem;
	private TextView backgroundNameTxt;
	private Spinner soundsSpinner;
	private int backgroundWidth;
	private int backgroundHeight;

	private String backgroundUrlPort;
	private String backgroundUrlLand;

	private ProgressImageView boardPreviewImg;
	private ProgressImageView piecePreviewImg;
	private ProgressImageView backgroundPreviewImg;
	private TextView rowSampleTitleTxt;
	private TextView loadProgressTxt;
	private TextView taskTitleTxt;

	private PopupBackgroundsFragment backgroundsFragment;
	private PopupCustomViewFragment loadProgressPopupFragment;

	private SparseArray<String> soundsUrlsMap;
	private String selectedSoundPackUrl;
	private View loadProgressBar;
	private View applyBackgroundBtn;
	private BackgroundSingleItem.Data selectedBackgroundItem;

	private int previewLineWidth;
	private ProgressImageView piecesLineImage;
	private ProgressImageView boardLineImage;
	private SparseArray<String> defaultPiecesNamesMap;
	private HashMap<String, String> defaultPiecesResourceNamesMap;
	private SparseArray<String> defaultBoardNamesMap;
	private SparseArray<String> defaultSquareBoardNamesMap;
	private PiecePreviewImg piecesSquarePreviewImg;
	private String themeFontColor;

	public SettingsThemeCustomizeFragment() {
		ThemeItem.Data customizeItem = new ThemeItem.Data();
		customizeItem.setThemeName(AppConstants.CUSTOM_THEME_NAME);
		customizeItem.setLocal(true);

		Bundle bundle = new Bundle();
		bundle.putParcelable(THEME_ITEM, customizeItem);
		setArguments(bundle);
	}

	public static SettingsThemeCustomizeFragment createInstance(ThemeItem.Data themeItem) {
		SettingsThemeCustomizeFragment fragment = new SettingsThemeCustomizeFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(THEME_ITEM, themeItem);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			themeItem = getArguments().getParcelable(THEME_ITEM);
		} else {
			themeItem = savedInstanceState.getParcelable(THEME_ITEM);
		}

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_theme_customize_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.custom);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (DbDataManager.haveSavedSoundThemes(getContentResolver()) && getAppData().isThemeSoundsLoaded()) {
			loadSoundsFromDb();
		} else {
			getSounds();
		}

		// show selected pieces line preview
		if (getAppData().isUseThemePieces()) {

			// Load line preview image
			String piecesPreviewUrl = getAppData().getThemePiecesPreviewUrl();
			imageLoader.download(piecesPreviewUrl, piecesLineImage, previewLineWidth);

			// load square preview image
			String squarePreviewUrl = piecesPreviewUrl.replace("/line/", "/square/");
			imageLoader.download(squarePreviewUrl, piecePreviewImg, PREVIEW_IMG_SIZE);
		} else {
			String themePiecesName = getAppData().getThemePiecesName();
			String pieceDefaultName = AppConstants.DEFAULT_THEME_PIECES_NAME;
			if (themePiecesName.equals(Symbol.EMPTY)) {
				piecesLineImage.setImageDrawable(getResources().getDrawable(R.drawable.pieces_game));

				String packageName = getActivity().getPackageName();
				int bnResourceId = getResources().getIdentifier(pieceDefaultName + "_bn", "drawable", packageName);
				int bpResourceId = getResources().getIdentifier(pieceDefaultName + "_bp", "drawable", packageName);
				int wnResourceId = getResources().getIdentifier(pieceDefaultName + "_wn", "drawable", packageName);
				int wpResourceId = getResources().getIdentifier(pieceDefaultName + "_wp", "drawable", packageName);
				// show default pieces preview
				Bitmap[][] previewBitmaps = new Bitmap[2][2];
				previewBitmaps[0][0] = ((BitmapDrawable) getResources().getDrawable(bnResourceId)).getBitmap();
				previewBitmaps[0][1] = ((BitmapDrawable) getResources().getDrawable(wnResourceId)).getBitmap();
				previewBitmaps[1][0] = ((BitmapDrawable) getResources().getDrawable(bpResourceId)).getBitmap();
				previewBitmaps[1][1] = ((BitmapDrawable) getResources().getDrawable(wpResourceId)).getBitmap();

				piecesSquarePreviewImg.setPiecesBitmaps(previewBitmaps);
				piecesSquarePreviewImg.setVisibility(View.VISIBLE);
			} else {
				for (int i = 0; i < defaultPiecesNamesMap.size(); i++) {
					int key = defaultPiecesNamesMap.keyAt(i);
					String value = defaultPiecesNamesMap.valueAt(i);
					if (value.equals(themePiecesName)) {
						piecesLineImage.setImageDrawable(getResources().getDrawable(key));
					}
				}

				for (Map.Entry<String, String> entry : defaultPiecesResourceNamesMap.entrySet()) {
					if (themePiecesName.equals(entry.getValue())) {
						pieceDefaultName = entry.getKey();
						break;
					}
				}

				String packageName = getActivity().getPackageName();
				int bnResourceId = getResources().getIdentifier(pieceDefaultName + "_bn", "drawable", packageName);
				int bpResourceId = getResources().getIdentifier(pieceDefaultName + "_bp", "drawable", packageName);
				int wnResourceId = getResources().getIdentifier(pieceDefaultName + "_wn", "drawable", packageName);
				int wpResourceId = getResources().getIdentifier(pieceDefaultName + "_wp", "drawable", packageName);
				// show default pieces preview
				Bitmap[][] previewBitmaps = new Bitmap[2][2];
				previewBitmaps[0][0] = ((BitmapDrawable) getResources().getDrawable(bnResourceId)).getBitmap();
				previewBitmaps[0][1] = ((BitmapDrawable) getResources().getDrawable(wnResourceId)).getBitmap();
				previewBitmaps[1][0] = ((BitmapDrawable) getResources().getDrawable(bpResourceId)).getBitmap();
				previewBitmaps[1][1] = ((BitmapDrawable) getResources().getDrawable(wpResourceId)).getBitmap();

				piecesSquarePreviewImg.setPiecesBitmaps(previewBitmaps);
				piecesSquarePreviewImg.setVisibility(View.VISIBLE);
			}
		}

		// show selected board line preview
		if (getAppData().isUseThemeBoard()) {

			// Load line preview image
			String boardPreviewUrl = getAppData().getThemeBoardPreviewUrl();
			imageLoader.download(boardPreviewUrl, boardLineImage, previewLineWidth);

			// load square preview image
			String squarePreviewUrl = boardPreviewUrl.replace("/line/", "/square/");
			imageLoader.download(squarePreviewUrl, boardPreviewImg, PREVIEW_IMG_SIZE);
		} else {
			String themeBoardsName = getAppData().getThemeBoardName();
			if (themeBoardsName.equals(Symbol.EMPTY)) {
				boardLineImage.setImageDrawable(getResources().getDrawable(R.drawable.board_sample_wood_dark));

				// load square preview image
				boardPreviewImg.setImageDrawable(getResources().getDrawable(R.drawable.board_wood_dark));
			} else {
				for (int i = 0; i < defaultBoardNamesMap.size(); i++) {
					int key = defaultBoardNamesMap.keyAt(i);
					String value = defaultBoardNamesMap.valueAt(i);
					if (value.equals(themeBoardsName)) {
						boardLineImage.setImageDrawable(getResources().getDrawable(key));
					}
				}

				// load square preview image
				for (int i = 0; i < defaultSquareBoardNamesMap.size(); i++) {
					int key = defaultSquareBoardNamesMap.keyAt(i);
					String value = defaultSquareBoardNamesMap.valueAt(i);
					if (value.equals(themeBoardsName)) {
						boardPreviewImg.setImageDrawable(getResources().getDrawable(key));
					}
				}
			}
		}

		// set background preview
		String themeName = getAppData().getThemeBackgroundName();
		if (themeName.equals(AppConstants.DEFAULT_THEME_NAME)) {
			backgroundPreviewImg.setImageDrawable(getResources().getDrawable(R.drawable.img_theme_green_felt_sample));
		} else {
			imageLoader.download(getAppData().getThemeBackgroundPreviewUrl(), backgroundPreviewImg, screenWidth);
		}

		updateThemeName();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(THEME_ITEM, themeItem);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.piecesView) {
			openFragment(new SettingsThemePiecesFragment());
		} else if (id == R.id.boardView) {
			openFragment(new SettingsThemeBoardsFragment());
		} else if (id == R.id.backgroundView) {
			if (backgroundsFragment != null) {
				return;
			}
			backgroundsFragment = PopupBackgroundsFragment.createInstance(backgroundPopupListener, themeItem);
			backgroundsFragment.show(getFragmentManager(), BACKGROUND_SELECTION);
		} else if (id == R.id.soundsView) {
			soundsSpinner.performClick();
		} else if (id == R.id.applyBackgroundBtn) {
			LoadItem loadItem;
			if (!isTablet) {
				// Get exactly sized url for theme background
				loadItem = LoadHelper.getBackgroundById(getUserToken(), selectedBackgroundItem.getBackgroundId(),
						screenWidth, screenHeight, RestHelper.V_HANDSET);
			} else {
				if (screenWidth > screenHeight) {
					backgroundWidth = screenHeight;
					backgroundHeight = screenWidth;
				} else {
					backgroundWidth = screenWidth;
					backgroundHeight = screenHeight;
				}
				// Get exactly sized url for theme background
				loadItem = LoadHelper.getBackgroundById(getUserToken(), selectedBackgroundItem.getBackgroundId(),
						backgroundWidth, backgroundHeight, RestHelper.V_TABLET);
			}

			new RequestJsonTask<BackgroundSingleItem>(backgroundItemUpdateListener).executeTask(loadItem);
		}
	}

	protected void openFragment(BasePopupsFragment fragment) {
		getActivityFace().openFragment(fragment);
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
		if (adapterView.getId() == R.id.soundsSpinner) {
			// check if sounds is different from theme's sounds
			int soundsId = themeItem.getSoundsId();
			int themeSoundsId = getAppData().getThemeSoundsId();
			// default soundsId is 1, but theme items have -1(UNDEFINED)
			if (soundsId != AppData.UNDEFINED
					&& soundsId != themeSoundsId && themeSoundsId != AppData.UNDEFINED) {
				getAppData().setThemeName(AppConstants.CUSTOM_THEME_NAME);
			}

			selectedSoundPackUrl = soundsUrlsMap.valueAt(pos);
			getAppData().setSoundSetPosition(pos);

			String savedPath = DbDataManager.haveSavedSoundPackForUrl(getContentResolver(), selectedSoundPackUrl);
			if (TextUtils.isEmpty(savedPath)) {
				new GetAndSaveFileToSdTask(soundPackSaveListener, true, AppUtils.getLocalDirForSounds(getActivity()))
						.executeTask(selectedSoundPackUrl);
			} else {
				updateSelectedSoundScheme(savedPath);
			}
			getAppData().setThemeSoundsId(soundsUrlsMap.keyAt(pos));
		}

		((BaseAdapter) adapterView.getAdapter()).notifyDataSetChanged();
	}

	private class SoundPackSaveListener extends ChessLoadUpdateListener<String> implements FileReadyListener {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			if (show) { // show popup with percentage of loading theme

				View layout = LayoutInflater.from(getActivity()).inflate(R.layout.new_progress_load_popup, null, false);

				TextView loadTitleTxt = (TextView) layout.findViewById(R.id.loadTitleTxt);
				loadProgressBar = layout.findViewById(R.id.loadProgressBar);
				loadProgressTxt = (TextView) layout.findViewById(R.id.loadProgressTxt);
				taskTitleTxt = (TextView) layout.findViewById(R.id.taskTitleTxt);

				loadTitleTxt.setText(R.string.installing_sound_pack);
				taskTitleTxt.setText(R.string.loading_sounds);
				loadProgressTxt.setVisibility(View.GONE);
				loadProgressBar.setVisibility(View.VISIBLE);

				PopupItem popupItem = new PopupItem();
				popupItem.setCustomView(layout);

				loadProgressPopupFragment = PopupCustomViewFragment.createInstance(popupItem);
				loadProgressPopupFragment.show(getFragmentManager(), THEME_LOAD_TAG);
			} else {
				if (loadProgressPopupFragment != null) {
					loadProgressPopupFragment.dismiss();
				}
			}
		}

		@Override
		public void updateData(String returnedObj) {
			super.updateData(returnedObj);

			DbDataManager.saveSoundPathToDb(getContentResolver(), selectedSoundPackUrl, returnedObj);

			updateSelectedSoundScheme(returnedObj);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			showToast("error occurred code " + resultCode);
		}

		@Override
		public void changeTitle(final String title) {
			FragmentActivity activity = getActivity();
			if (activity == null) {
				return;
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					taskTitleTxt.setText(title);
				}
			});
		}

		@Override
		public void setProgress(final int progress) {
			FragmentActivity activity = getActivity();
			if (activity == null) {
				return;
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loadProgressBar.setVisibility(View.GONE);
					loadProgressTxt.setVisibility(View.VISIBLE);
					loadProgressTxt.setText(String.valueOf(progress) + Symbol.PERCENT);
				}
			});
		}
	}

	private void updateSelectedSoundScheme(String path) {
		getAppData().setThemeSoundsPath(path);

		// update sounds flag
		SoundPlayer.setUseThemePack(true);
		SoundPlayer.setThemePath(path);
	}


	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
	}

	private class ImageUpdateListener implements ImageReadyListener {

		private int code;

		private ImageUpdateListener() {
			code = GetAndSaveTheme.BACKGROUND_PORT;
		}

		private ImageUpdateListener(int code) {
			this.code = code;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}

			if (bitmap == null) {
				showToast("error loading image. Internal error");
				return;
			}

			taskTitleTxt.setText(R.string.saving_background);
			loadProgressTxt.setText(String.valueOf(0));
			loadProgressTxt.setVisibility(View.GONE);

			if (code == GetAndSaveTheme.BACKGROUND_PORT) {
				String filename = String.valueOf(backgroundUrlPort.hashCode());
				new SaveImageToSdTask(mainBackgroundImgSaveListener, bitmap).executeTask(filename);
			} else {
				String filename = String.valueOf(backgroundUrlLand.hashCode());
				new SaveImageToSdTask(mainBackgroundLandImgSaveListener, bitmap).executeTask(filename);
			}

		}

		@Override
		public void setProgress(final int progress) {
			FragmentActivity activity = getActivity();
			if (activity == null) {
				return;
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loadProgressTxt.setText(String.valueOf(progress) + Symbol.PERCENT);
				}
			});
		}
	}

	private class BackgroundImageSaveListener extends AbstractUpdateListener<Bitmap> {

		private int code;

		public BackgroundImageSaveListener() {
			super(getActivity(), SettingsThemeCustomizeFragment.this);
			this.code = GetAndSaveTheme.BACKGROUND_PORT;
		}

		public BackgroundImageSaveListener(int code) {
			super(getActivity(), SettingsThemeCustomizeFragment.this);
			this.code = code;
		}

		@Override
		public void updateData(Bitmap returnedObj) {
			if (code == GetAndSaveTheme.BACKGROUND_PORT) {
				// set main background image as theme
				String filename = String.valueOf(backgroundUrlPort.hashCode());
				try {
					File imgFile = AppUtils.openFileByName(getActivity(), filename);
					getAppData().setThemeBackPathPort(imgFile.getAbsolutePath());
					getActivityFace().updateMainBackground();
					getAppData().setThemeFontColor(themeFontColor);
					getAppData().setThemeBackgroundName(selectedBackgroundItem.getName());

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				// set main background image as theme
				String filename = String.valueOf(backgroundUrlLand.hashCode());
				try {
					File imgFile = AppUtils.openFileByName(getActivity(), filename);
					getAppData().setThemeBackPathLand(imgFile.getAbsolutePath());
					getActivityFace().updateMainBackground();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}


			// hide checkMark button
			applyBackgroundBtn.setVisibility(View.GONE);

			if (loadProgressPopupFragment != null) {
				loadProgressPopupFragment.dismiss();
			}
		}
	}

	private void getSounds() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_SOUND);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<SoundsItem>(soundsItemUpdateListener).executeTask(loadItem);
	}

	private class SoundsItemUpdateListener extends ChessLoadUpdateListener<SoundsItem> {

		private SoundsItemUpdateListener() {
			super(SoundsItem.class);
		}

		@Override
		public void updateData(SoundsItem returnedObj) {
			super.updateData(returnedObj);

			List<SoundSingleItem.Data> itemsList = returnedObj.getData();
			for (SoundSingleItem.Data currentItem : itemsList) {
				DbDataManager.saveThemeSoundsItemToDb(getContentResolver(), currentItem);
			}
			getAppData().setThemeSoundsLoaded(true);
			loadSoundsFromDb();
		}
	}

	private void loadSoundsFromDb() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.THEME_SOUNDS));

		List<String> soundsList = new ArrayList<String>();
		if (cursor != null && cursor.moveToFirst()) {
			do {
				int id = DbDataManager.getInt(cursor, DbScheme.V_ID);
				String url = DbDataManager.getString(cursor, DbScheme.V_URL);
				soundsUrlsMap.put(id, url);
				soundsList.add(DbDataManager.getString(cursor, DbScheme.V_NAME));
			} while (cursor.moveToNext());
		} else { // DB was cleared
			getSounds();
		}

		soundsSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), soundsList));
		soundsSpinner.setEnabled(true);
		soundsSpinner.setSelection(getAppData().getSoundSetPosition());
	}

	private void updateThemeName() {
		AppData appData = getAppData();
		// check if background is different from theme's background
		String themeName = appData.getThemeName();
		String themeBackgroundName = appData.getThemeBackgroundName();
		if (!themeName.equalsIgnoreCase(themeBackgroundName) && !TextUtils.isEmpty(themeBackgroundName)) {
			appData.setThemeName(AppConstants.CUSTOM_THEME_NAME);
			return;
		}

		// check if board is different from theme's board
		int boardId = themeItem.getBoardId();
		int themeBoardId = appData.getThemeBoardId();
		if (boardId != themeBoardId && themeBoardId != AppData.UNDEFINED) {
			appData.setThemeName(AppConstants.CUSTOM_THEME_NAME);
			return;
		}

		// check if pieces is different from theme's pieces
		int piecesId = themeItem.getPiecesId();
		int themePiecesId = appData.getThemePiecesId();
		if (piecesId != themePiecesId && themePiecesId != AppData.UNDEFINED) {
			appData.setThemeName(AppConstants.CUSTOM_THEME_NAME);
		}
	}

	private class BackgroundPopupListener implements PopupListSelectionFace {

		@Override
		public void onValueSelected(int code) {
			selectedBackgroundItem = backgroundsFragment.getItemByCode(code);

			imageLoader.download(selectedBackgroundItem.getBackgroundPreviewUrl(), backgroundPreviewImg, screenWidth);
			backgroundNameTxt.setText(selectedBackgroundItem.getName());

			getAppData().setThemeBackgroundPreviewUrl(selectedBackgroundItem.getBackgroundPreviewUrl());
			getAppData().setThemeBackgroundName(selectedBackgroundItem.getName());

			backgroundsFragment.dismiss();
			backgroundsFragment = null;

			// show button to apply changes
			applyBackgroundBtn.setVisibility(View.VISIBLE);
		}

		@Override
		public void onDialogCanceled() {
			backgroundsFragment = null;
		}
	}

	private class BackgroundItemUpdateListener extends ChessLoadUpdateListener<BackgroundSingleItem> {

		private int code;

		private BackgroundItemUpdateListener() {
			super(BackgroundSingleItem.class);
			code = GetAndSaveTheme.BACKGROUND_PORT;
		}

		private BackgroundItemUpdateListener(int code) {
			super(BackgroundSingleItem.class);
			this.code = code;
		}

		@Override
		public void updateData(BackgroundSingleItem returnedObj) {


			if (TextUtils.isEmpty(returnedObj.getData().getResizedImage())) {
				return;
			}

			{  // show popup with percentage of loading theme
				View layout = LayoutInflater.from(getActivity()).inflate(R.layout.new_progress_load_popup, null, false);

				loadProgressTxt = (TextView) layout.findViewById(R.id.loadProgressTxt);
				taskTitleTxt = (TextView) layout.findViewById(R.id.taskTitleTxt);

				taskTitleTxt.setText(R.string.loading_background);

				PopupItem popupItem = new PopupItem();
				popupItem.setCustomView(layout);

				loadProgressPopupFragment = PopupCustomViewFragment.createInstance(popupItem);
				loadProgressPopupFragment.show(getFragmentManager(), THEME_LOAD_TAG);
			}

			if (code == GetAndSaveTheme.BACKGROUND_PORT) {
				backgroundUrlPort = returnedObj.getData().getResizedImage();
				imageDownloader.download(backgroundUrlPort, backgroundUpdateListener, backgroundWidth, backgroundHeight);
			} else { // LAND
				backgroundWidth = screenWidth;
				backgroundHeight = screenHeight;

				// 800 < 1280
				if (screenWidth < screenHeight) {
					backgroundHeight = screenWidth;
					backgroundWidth = screenHeight;
				}

				backgroundUrlLand = returnedObj.getData().getResizedImage();
				imageDownloader.download(backgroundUrlLand, backgroundLandUpdateListener, backgroundWidth, backgroundHeight);
			}

			// load second image for tablet
			if (isTablet && code == GetAndSaveTheme.BACKGROUND_PORT) {
				backgroundWidth = screenWidth;
				backgroundHeight = screenHeight;

				// 800 < 1280   // for landscape
				if (screenWidth < screenHeight) {
					backgroundHeight = screenWidth;
					backgroundWidth = screenHeight;
				}

				LoadItem loadItem = LoadHelper.getBackgroundById(getUserToken(), selectedBackgroundItem.getBackgroundId(),
						backgroundWidth, backgroundHeight, RestHelper.V_TABLET);
				new RequestJsonTask<BackgroundSingleItem>(new BackgroundItemUpdateListener(GetAndSaveTheme.BACKGROUND_LAND)).executeTask(loadItem);

			}

			themeFontColor = selectedBackgroundItem.getFontColor();

			getActivityFace().updateActionBarBackImage();
			updateThemeName();
		}
	}

	private void init() {
		soundsUrlsMap = new SparseArray<String>();

		imageLoader = new EnhancedImageDownloader(getActivity());
		{// Piece bitmaps list init
			defaultPiecesNamesMap = new SparseArray<String>();
			defaultPiecesNamesMap.put(R.drawable.pieces_game, getString(R.string.pieces_game));
			defaultPiecesNamesMap.put(R.drawable.pieces_alpha, getString(R.string.pieces_alpha));
			defaultPiecesNamesMap.put(R.drawable.pieces_book, getString(R.string.pieces_book));
			defaultPiecesNamesMap.put(R.drawable.pieces_cases, getString(R.string.pieces_cases));
			defaultPiecesNamesMap.put(R.drawable.pieces_classic, getString(R.string.pieces_classic));
			defaultPiecesNamesMap.put(R.drawable.pieces_club, getString(R.string.pieces_club));
			defaultPiecesNamesMap.put(R.drawable.pieces_condal, getString(R.string.pieces_condal));
			defaultPiecesNamesMap.put(R.drawable.pieces_maya, getString(R.string.pieces_maya));
			defaultPiecesNamesMap.put(R.drawable.pieces_modern, getString(R.string.pieces_modern));
			defaultPiecesNamesMap.put(R.drawable.pieces_vintage, getString(R.string.pieces_vintage));
		}

		{// Piece default resource name list init
			defaultPiecesResourceNamesMap = new HashMap<String, String>();
			defaultPiecesResourceNamesMap.put("game", getString(R.string.pieces_game));
			defaultPiecesResourceNamesMap.put("alpha", getString(R.string.pieces_alpha));
			defaultPiecesResourceNamesMap.put("book", getString(R.string.pieces_book));
			defaultPiecesResourceNamesMap.put("cases", getString(R.string.pieces_cases));
			defaultPiecesResourceNamesMap.put("classic", getString(R.string.pieces_classic));
			defaultPiecesResourceNamesMap.put("club", getString(R.string.pieces_club));
			defaultPiecesResourceNamesMap.put("condal", getString(R.string.pieces_condal));
			defaultPiecesResourceNamesMap.put("maya", getString(R.string.pieces_maya));
			defaultPiecesResourceNamesMap.put("modern", getString(R.string.pieces_modern));
			defaultPiecesResourceNamesMap.put("vintage", getString(R.string.pieces_vintage));
		}

		{// Board line bitmaps list init
			defaultBoardNamesMap = new SparseArray<String>();
			defaultBoardNamesMap.put(R.drawable.board_sample_wood_dark, getString(R.string.board_wood_dark));
			defaultBoardNamesMap.put(R.drawable.board_sample_wood_light, getString(R.string.board_wood_light));
			defaultBoardNamesMap.put(R.drawable.board_sample_blue, getString(R.string.board_blue));
			defaultBoardNamesMap.put(R.drawable.board_sample_brown, getString(R.string.board_brown));
			defaultBoardNamesMap.put(R.drawable.board_sample_green, getString(R.string.board_green));
			defaultBoardNamesMap.put(R.drawable.board_sample_grey, getString(R.string.board_grey));
			defaultBoardNamesMap.put(R.drawable.board_sample_marble, getString(R.string.board_marble));
			defaultBoardNamesMap.put(R.drawable.board_sample_red, getString(R.string.board_red));
			defaultBoardNamesMap.put(R.drawable.board_sample_tan, getString(R.string.board_tan));
		}

		{// Board square bitmaps list init
			defaultSquareBoardNamesMap = new SparseArray<String>();
			defaultSquareBoardNamesMap.put(R.drawable.board_wood_dark, getString(R.string.board_wood_dark));
			defaultSquareBoardNamesMap.put(R.drawable.board_wood_light, getString(R.string.board_wood_light));
			defaultSquareBoardNamesMap.put(R.drawable.board_blue, getString(R.string.board_blue));
			defaultSquareBoardNamesMap.put(R.drawable.board_brown, getString(R.string.board_brown));
			defaultSquareBoardNamesMap.put(R.drawable.board_green, getString(R.string.board_green));
			defaultSquareBoardNamesMap.put(R.drawable.board_grey, getString(R.string.board_grey));
			defaultSquareBoardNamesMap.put(R.drawable.board_marble, getString(R.string.board_marble));
			defaultSquareBoardNamesMap.put(R.drawable.board_red, getString(R.string.board_red));
			defaultSquareBoardNamesMap.put(R.drawable.board_tan, getString(R.string.board_tan));
		}

		backgroundPopupListener = new BackgroundPopupListener();
		backgroundItemUpdateListener = new BackgroundItemUpdateListener();
		backgroundUpdateListener = new ImageUpdateListener();
		backgroundLandUpdateListener = new ImageUpdateListener(GetAndSaveTheme.BACKGROUND_LAND);

		mainBackgroundImgSaveListener = new BackgroundImageSaveListener();
		mainBackgroundLandImgSaveListener = new BackgroundImageSaveListener(GetAndSaveTheme.BACKGROUND_LAND);

		soundsItemUpdateListener = new SoundsItemUpdateListener();
		soundPackSaveListener = new SoundPackSaveListener();

		imageDownloader = new ImageDownloaderToListener(getContext());
	}

	private void widgetsInit(View view) {

		Resources resources = getResources();

		{ // background params
			int screenWidth;
			int imageHeight;
			if (!isTablet) {
				screenWidth = SettingsThemeCustomizeFragment.this.screenWidth;
				imageHeight = (int) (screenWidth / 2.9f);
			} else {
				screenWidth = SettingsThemeCustomizeFragment.this.screenWidth
						- resources.getDimensionPixelSize(R.dimen.tablet_side_menu_width);
				imageHeight = (int) (screenWidth / 4.0f);
			}

			backgroundPreviewImg = (ProgressImageView) view.findViewById(R.id.backgroundPreviewImg);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
			backgroundPreviewImg.setLayoutParams(params);

			// Change main preview container params
			RelativeLayout previewRelLay = (RelativeLayout) view.findViewById(R.id.previewRelLay);
			LinearLayout.LayoutParams previewRelLayParams = new LinearLayout.LayoutParams(screenWidth, imageHeight);
			previewRelLay.setLayoutParams(previewRelLayParams);

			// Change Placeholder
			int backIMgColor = resources.getColor(R.color.upgrade_toggle_button_p);
			backgroundPreviewImg.placeholder = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);

			// Change Image params
			RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
			backgroundPreviewImg.getImageView().setLayoutParams(imageParams);
			backgroundPreviewImg.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

			// Change ProgressBar params
			RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			backgroundPreviewImg.getProgressBar().setLayoutParams(progressParams);
		}

		boardPreviewImg = (ProgressImageView) view.findViewById(R.id.boardPreviewImg);
		piecePreviewImg = (ProgressImageView) view.findViewById(R.id.piecesPreviewImg);
		piecesSquarePreviewImg = (PiecePreviewImg) view.findViewById(R.id.piecesSquarePreviewImg);
		piecesLineImage = (ProgressImageView) view.findViewById(R.id.piecesLineImage);
		boardLineImage = (ProgressImageView) view.findViewById(R.id.boardLineImage);

		piecesSquarePreviewImg.setVisibility(View.INVISIBLE);

		Drawable piecesDrawableExample = resources.getDrawable(R.drawable.pieces_alpha);
		previewLineWidth = piecesDrawableExample.getIntrinsicWidth();
		int imageHeight = piecesDrawableExample.getIntrinsicHeight();

		// Change Image params
		RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(previewLineWidth, imageHeight);
		piecesLineImage.getImageView().setLayoutParams(imageParams);
		piecesLineImage.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

		boardLineImage.getImageView().setLayoutParams(imageParams);
		boardLineImage.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

		// Preview Sample
		rowSampleTitleTxt = (TextView) view.findViewById(R.id.rowSampleTitleTxt);
		int fontColor = Color.parseColor("#" + themeItem.getFontColor());
		rowSampleTitleTxt.setTextColor(fontColor);

		//spinners
		view.findViewById(R.id.backgroundView).setOnClickListener(this);
		view.findViewById(R.id.piecesView).setOnClickListener(this);
		view.findViewById(R.id.boardView).setOnClickListener(this);
		view.findViewById(R.id.soundsView).setOnClickListener(this);
//		view.findViewById(R.id.colorsView).setOnClickListener(this);

		applyBackgroundBtn = view.findViewById(R.id.applyBackgroundBtn);
		applyBackgroundBtn.setOnClickListener(this);

		// Backgrounds
		backgroundNameTxt = (TextView) view.findViewById(R.id.backgroundNameTxt);
		backgroundNameTxt.setText(themeItem.getThemeName());

		imageLoader.download(themeItem.getBackgroundPreviewUrl(), backgroundPreviewImg, screenWidth, screenWidth);

		// Sounds
		soundsSpinner = (Spinner) view.findViewById(R.id.soundsSpinner);
		soundsSpinner.setEnabled(false);
		soundsSpinner.setOnItemSelectedListener(this);
	}

}
