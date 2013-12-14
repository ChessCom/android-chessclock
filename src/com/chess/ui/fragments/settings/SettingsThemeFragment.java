package com.chess.ui.fragments.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.GetAndSaveTheme;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ThemeState;
import com.chess.backend.entity.api.themes.*;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.backend.interfaces.FileReadyListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;
import com.chess.widgets.LinLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.05.13
 * Time: 11:23
 */
public class SettingsThemeFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	public static final String _3D_PART = "3d";

	private ListView listView;
	private ThemesAdapter themesAdapter;
	private ThemesUpdateListener themesUpdateListener;

	private int screenWidth;
	private int screenHeight;
	private ThemeItem.Data selectedThemeItem;
	protected ThemeItem.Data currentThemeItem;
	private List<ThemeItem.Data> themesList;
	private String selectedThemeName;
	private LoadServiceConnectionListener loadServiceConnectionListener;
	private ProgressUpdateListener progressUpdateListener;
	private boolean serviceBounded;
	private GetAndSaveTheme.ServiceBinder serviceBinder;
	private boolean needToLoadThemeAfterConnected;
	private SparseArray<ThemeState> themeLoadStateMap;
	private boolean themeApplied;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		screenWidth = getResources().getDisplayMetrics().widthPixels;
		screenHeight = getResources().getDisplayMetrics().heightPixels;

		themesList = new ArrayList<ThemeItem.Data>();
		themesUpdateListener = new ThemesUpdateListener();

		loadServiceConnectionListener = new LoadServiceConnectionListener();
		progressUpdateListener = new ProgressUpdateListener();
		themeLoadStateMap = new SparseArray<ThemeState>();

		themeApplied = true;

		pullToRefresh(true);

		boolean needToLoadThemes = DbDataManager.haveSavedThemesToLoad(getActivity());
		if (needToLoadThemes) { // connect to service to get state updates
			getActivity().bindService(new Intent(getActivity(), GetAndSaveTheme.class), loadServiceConnectionListener,
					Activity.BIND_AUTO_CREATE);
		}
		// fill theme loading state map
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.THEMES_LOAD_STATE));
		if (cursor != null && cursor.moveToFirst()) {
			do {
				int id = DbDataManager.getInt(cursor, DbScheme.V_ID);
				String state = DbDataManager.getString(cursor, DbScheme.V_STATE);
				themeLoadStateMap.put(id, ThemeState.valueOf(state));
			} while (cursor.moveToNext());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.theme);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		selectedThemeName = getAppData().getThemeName();

		if (need2update) {
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.THEMES));

			if (cursor != null && cursor.moveToFirst()) {
				do {
					themesList.add(DbDataManager.getThemeItemFromCursor(cursor));
				} while (cursor.moveToNext());

				updateUiData();
			} else {
				updateData();
			}
		} else {
			listView.setAdapter(themesAdapter);
			for (ThemeItem.Data theme : themesList) {
				if (theme.getThemeName().equals(selectedThemeName)) {
					currentThemeItem = theme;
					theme.setSelected(true);
				} else {
					theme.setSelected(false);
				}
			}
		}

		getActivity().bindService(new Intent(getActivity(), GetAndSaveTheme.class), loadServiceConnectionListener,
				Activity.BIND_AUTO_CREATE);
	}

	private void updateData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_THEMES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<ThemeItem>(themesUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		if (AppUtils.isNetworkAvailable(getActivity())) {
			updateData();
		}
	}

	private class ThemesUpdateListener extends ChessLoadUpdateListener<ThemeItem> {

		private ThemesUpdateListener() {
			super(ThemeItem.class);
		}

		@Override
		public void updateData(ThemeItem returnedObj) {
			super.updateData(returnedObj);

			themesList = returnedObj.getData();

			for (ThemeItem.Data data : themesList) {
				DbDataManager.saveThemeItemToDb(getContentResolver(), data);
			}

			SettingsThemeFragment.this.updateUiData();
		}
	}

	private void updateUiData() {
		ThemeItem.Data customItem = new ThemeItem.Data();
		customItem.setThemeName(getString(R.string.custom));
		customItem.setLocal(true);
		themesList.add(0, customItem);

		for (ThemeItem.Data theme : themesList) {
			if (theme.getThemeName().equals(selectedThemeName)) {
				currentThemeItem = theme;
				theme.setSelected(true);
			} else {
				theme.setSelected(false);
			}
		}

		themesAdapter = new ThemesAdapter(getActivity(), themesList, getImageFetcher());
		listView.setAdapter(themesAdapter);

		need2update = false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectedThemeItem = (ThemeItem.Data) parent.getItemAtPosition(position);
		if (selectedThemeItem.isLocal()) {
			openCustomizeFragment();
		} else {
			currentThemeItem = selectedThemeItem;

			ThemeState themeState = themeLoadStateMap.get((int) themesAdapter.getItemId(position), ThemeState.DEFAULT);
			if (themeState.equals(ThemeState.LOADED)) { // apply immediately
				if (!themeApplied) {
					return;
				}

				applyLoadedTheme(id);

				themeApplied = true;
				return;
			} else if (themeState.equals(ThemeState.ENQUIRED) || themeState.equals(ThemeState.LOADING)) {
				return;
			}

			themesAdapter.updateThemeLoadingStatus(id, ThemeState.ENQUIRED);

			((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();

			installSelectedTheme();
		}
	}

	private void applyLoadedTheme(long id) {
		AppData appData = getAppData();
		boolean themeLoaded = true;
		appData.setThemeName(selectedThemeItem.getThemeName());

		{ // load background
			QueryParams queryParams = DbHelper.getTableRecordById(DbScheme.Tables.THEME_BACKGROUNDS,
					selectedThemeItem.getBackgroundId());
			Cursor cursor = DbDataManager.query(getContentResolver(), queryParams);

			if (cursor != null && cursor.moveToFirst()) {
				BackgroundSingleItem.Data backgroundData = DbDataManager.getThemeBackgroundItemFromCursor(cursor);

				appData.setThemeBackgroundName(backgroundData.getName());
				appData.setThemeBackgroundPreviewUrl(backgroundData.getBackgroundPreviewUrl());
				appData.setThemeBackPath(backgroundData.getLocalPath());
				appData.setThemeFontColor(backgroundData.getFontColor());
				getActivityFace().updateMainBackground();
			} else {
				themeLoaded = false;
			}
		}

		{// load board
			QueryParams queryParams = DbHelper.getTableRecordById(DbScheme.Tables.THEME_BOARDS,
					selectedThemeItem.getBoardId());
			Cursor cursor = DbDataManager.query(getContentResolver(), queryParams);

			if (cursor != null && cursor.moveToFirst()) {
				BoardSingleItem.Data boardData = DbDataManager.getThemeBoardItemFromCursor(cursor);

				appData.setUseThemeBoard(true);
				appData.setThemeBoardId(boardData.getThemeBoardId());
				appData.setThemeBoardName(boardData.getName());
				appData.setThemeBoardPreviewUrl(boardData.getLineBoardPreviewUrl());
				appData.setThemeBoardCoordinateLight(Color.parseColor(boardData.getCoordinateColorLight()));
				appData.setThemeBoardCoordinateDark(Color.parseColor(boardData.getCoordinateColorDark()));
				appData.setThemeBoardHighlight(Color.parseColor(boardData.getHighlightColor()));
				appData.setThemeBoardPath(boardData.getLocalPath());
			} else {
				themeLoaded = false;
			}
		}

		{// load pieces
			QueryParams queryParams = DbHelper.getTableRecordById(DbScheme.Tables.THEME_PIECES,
					selectedThemeItem.getPiecesId());
			Cursor cursor = DbDataManager.query(getContentResolver(), queryParams);

			if (cursor != null && cursor.moveToFirst()) {
				PieceSingleItem.Data piecesData = DbDataManager.getThemePieceItemFromCursor(cursor);

				appData.setThemePiecesId(piecesData.getThemePieceId());
				appData.setThemePiecesName(piecesData.getName());
				appData.setThemePiecesPreviewUrl(piecesData.getPreviewUrl());

				appData.setUseThemePieces(true);
				appData.setThemePiecesPath(piecesData.getLocalPath());

				if (piecesData.getLocalPath().contains(_3D_PART)) {
					appData.setThemePieces3d(true);
				} else {
					appData.setThemePieces3d(false);
				}
			} else {
				themeLoaded = false;
			}
		}

		{ // load sounds
			QueryParams queryParams = DbHelper.getTableRecordById(DbScheme.Tables.THEME_SOUNDS,
					selectedThemeItem.getSoundsId());
			Cursor cursor = DbDataManager.query(getContentResolver(), queryParams);

			if (cursor != null && cursor.moveToFirst()) {
				SoundSingleItem.Data soundData = DbDataManager.getThemeSoundItemFromCursor(cursor);

				appData.setThemeSoundsId(soundData.getThemeSoundId());
				appData.setThemeSoundsPath(soundData.getLocalPath());

				SoundPlayer.setUseThemePack(true);
				SoundPlayer.setThemePath(soundData.getLocalPath());
			}
		}

		if (!themeLoaded) {
			DbDataManager.updateThemeLoadingStatus(getContentResolver(), selectedThemeItem, ThemeState.ENQUIRED);
			themesAdapter.updateThemeLoadingStatus(id, ThemeState.ENQUIRED);
			installSelectedTheme();
		} else {
			// deselect all themes
			for (ThemeItem.Data data : themesList) {
				data.setSelected(false);
			}
			// mark selected
			selectedThemeItem.setSelected(true);
		}
		themesAdapter.notifyDataSetChanged();
	}

	protected void openCustomizeFragment() {
		getActivityFace().openFragment(SettingsThemeCustomizeFragment.createInstance(currentThemeItem));
	}

	private void installSelectedTheme() {
		// we can update default theme with better board and background
		if (selectedThemeItem.getThemeName().equals(AppConstants.DEFAULT_THEME_NAME) && !isNetworkAvailable()) {
			AppData appData = getAppData();
			// clear themed settings
			appData.resetThemeToDefault();
			getActivityFace().updateMainBackground();

			appData.setThemeName(AppConstants.DEFAULT_THEME_NAME);
			appData.setThemeBackgroundName(AppConstants.DEFAULT_THEME_NAME);
			getActivityFace().updateActionBarBackImage();
		} else { // start loading main background image

			if (serviceBounded) {
				serviceBinder.getService().loadTheme(selectedThemeItem, screenWidth, screenHeight);
			} else {
				needToLoadThemeAfterConnected = true;
				getActivity().bindService(new Intent(getActivity(), GetAndSaveTheme.class), loadServiceConnectionListener,
						Activity.BIND_AUTO_CREATE);
			}
		}
	}

	private class LoadServiceConnectionListener implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			serviceBounded = true;

			serviceBinder = (GetAndSaveTheme.ServiceBinder) iBinder;
//			if (serviceBinder.getService().isInstallingTheme()) {
//				isInstallingTheme = true;
//			}

			serviceBinder.getService().setProgressUpdateListener(progressUpdateListener);
			if (needToLoadThemeAfterConnected) {
				serviceBinder.getService().loadTheme(selectedThemeItem, screenWidth, screenHeight);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			serviceBounded = false;
		}
	}

	private class ProgressUpdateListener implements FileReadyListener {

		@Override
		public void changeTitle(final String title) {
			if (getActivity() == null) {
				return;
			}
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					themesAdapter.setTitleForLoadingItem(title);
				}
			});
		}

		@Override
		public void setProgress(final int progress) {
			if (getActivity() == null) {
				return;
			}

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ThemeItem.Data loadingTheme = serviceBinder.getService().getLoadingTheme();
					if (progress == GetAndSaveTheme.DONE) {
						needToLoadThemeAfterConnected = false;
						if (serviceBounded) {
							getActivity().unbindService(loadServiceConnectionListener);
						}
						serviceBounded = false;
						themeLoadStateMap.put(loadingTheme.getId(), ThemeState.LOADED);
						selectedThemeItem = loadingTheme;
						applyLoadedTheme(loadingTheme.getId());
					} else {
						themeLoadStateMap.put(loadingTheme.getId(), ThemeState.LOADING);
					}

					themesAdapter.setProgressForItem(progress, loadingTheme.getId());
				}
			});

		}
	}

	private class ThemesAdapter extends ItemsAdapter<ThemeItem.Data> {

		private final int customColor;
		private final int boardPreviewImageSize;
		private final RelativeLayout.LayoutParams backImageParams;
		private final int imageHeight;
		private final AbsListView.LayoutParams listItemParams;
		private final Bitmap placeHolderBitmap;
		private final RelativeLayout.LayoutParams imageParams;
		private final RelativeLayout.LayoutParams progressParams;
		private final EnhancedImageDownloader imageLoader;
		private final HashMap<String, SmartImageFetcher.Data> imageDataMap;
		private int progressForItem;
		private String titleForLoadingItem;
		private int loadingThemeId;

		public ThemesAdapter(Context context, List<ThemeItem.Data> menuItems, SmartImageFetcher imageFetcher) {
			super(context, menuItems, imageFetcher);
			customColor = resources.getColor(R.color.theme_customize_back);
			boardPreviewImageSize = resources.getDimensionPixelSize(R.dimen.theme_board_preview_size);

			int screenWidth;
			if (!isTablet) {
				screenWidth = SettingsThemeFragment.this.screenWidth;
				imageHeight = (int) (screenWidth / 2.9f);
			} else {
				screenWidth = SettingsThemeFragment.this.screenWidth
						- resources.getDimensionPixelSize(R.dimen.tablet_side_menu_width);
				float aspect = resources.getDimension(R.dimen.theme_back_preview_aspect) / density;
				imageHeight = (int) (screenWidth / aspect);
			}

			backImageParams = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
			listItemParams = new ListView.LayoutParams(screenWidth, imageHeight);

			int backIMgColor = getResources().getColor(R.color.upgrade_toggle_button_p);
			placeHolderBitmap = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);

			imageParams = new RelativeLayout.LayoutParams(screenWidth, imageHeight);

			progressParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			imageLoader = new EnhancedImageDownloader(context);
			imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_settings_theme_item, parent, false);
			ViewHolder holder = new ViewHolder();

			holder.check = (TextView) view.findViewById(R.id.iconTxt);
			holder.title = (TextView) view.findViewById(R.id.rowTitleTxt);
			holder.backImg = (ImageView) view.findViewById(R.id.backgroundPreviewImg);
			holder.rowOverlayView = (LinLayout) view.findViewById(R.id.rowOverlayView);
			holder.boardPreviewImg = (ImageView) view.findViewById(R.id.boardPreviewImg);
			holder.piecesPreviewImg = (ProgressImageView) view.findViewById(R.id.piecesPreviewImg);
			holder.themeLoadProgressBar = (ProgressBar) view.findViewById(R.id.themeLoadProgressBar);
			holder.progressTitleTxt = (TextView) view.findViewById(R.id.progressTitleTxt);
			view.setTag(holder);

			holder.backImg.setLayoutParams(backImageParams);

			// List item params
			view.setLayoutParams(listItemParams);

			// Change Placeholder
			imageFetcher.setLoadingImage(placeHolderBitmap);
//			holder.backImg.placeholder = placeHolderBitmap;

			// Change Image params
			holder.backImg.setLayoutParams(imageParams);
			holder.backImg.setScaleType(ImageView.ScaleType.FIT_XY);

			// Change ProgressBar params
			progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//			holder.backImg.getProgressBar().setLayoutParams(progressParams);

			return view;
		}

		@Override
		protected void bindView(ThemeItem.Data item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			ThemeState status = themeLoadStateMap.get(item.getId(), ThemeState.DEFAULT);
			if (status.equals(ThemeState.LOADING) && item.getId() == loadingThemeId) {
				if (progressForItem != GetAndSaveTheme.INDETERMINATE) {
					holder.themeLoadProgressBar.setProgress(progressForItem);
					holder.themeLoadProgressBar.setIndeterminate(false);
				} else {
					holder.themeLoadProgressBar.setIndeterminate(true);
				}

				holder.themeLoadProgressBar.setVisibility(View.VISIBLE);

				holder.progressTitleTxt.setText(titleForLoadingItem);
				holder.progressTitleTxt.setVisibility(View.VISIBLE);
				holder.rowOverlayView.setDrawableStyle(R.style.ListItem);
			} else if (status.equals(ThemeState.LOADED)) {
				holder.themeLoadProgressBar.setVisibility(View.GONE);
				holder.progressTitleTxt.setVisibility(View.GONE);
				holder.rowOverlayView.setDrawableStyle(R.style.ListItem);

				if (item.isSelected()) {
					holder.check.setText(R.string.ic_check);
				} else {
					holder.check.setText(Symbol.EMPTY);
				}
			} else if (status.equals(ThemeState.DEFAULT)) {
				holder.themeLoadProgressBar.setVisibility(View.GONE);
				holder.progressTitleTxt.setVisibility(View.GONE);
				holder.rowOverlayView.setBackgroundColor(getResources().getColor(R.color.semitransparent_white));

				if (item.isSelected()) {
					holder.check.setText(R.string.ic_check);
				} else {
					holder.check.setText(Symbol.EMPTY);
				}
			} else if (status.equals(ThemeState.ENQUIRED)) {
				holder.themeLoadProgressBar.setVisibility(View.GONE);
				holder.progressTitleTxt.setVisibility(View.VISIBLE);
				holder.progressTitleTxt.setText(R.string.waiting_to_load);
				holder.rowOverlayView.setDrawableStyle(R.style.ListItem);

				holder.check.setText(Symbol.EMPTY);
			}

			if (item.getThemeName().equals(AppConstants.DEFAULT_THEME_NAME)) {
				holder.rowOverlayView.setDrawableStyle(R.style.ListItem);
			}

			if (item.isLocal()) {
				holder.title.setText(R.string.custom);

				holder.backImg.setImageDrawable(new ColorDrawable(customColor));
				holder.boardPreviewImg.setImageDrawable(getResources().getDrawable(R.drawable.empty));
				holder.piecesPreviewImg.setImageDrawable(getResources().getDrawable(R.drawable.empty));
				holder.piecesPreviewImg.setBackgroundResource(R.drawable.empty);
				holder.boardPreviewImg.setBackgroundResource(R.drawable.empty);
				holder.rowOverlayView.setDrawableStyle(R.style.ListItem);

			} else {
				holder.title.setText(item.getThemeName());

				String backgroundPreviewUrl = item.getBackgroundPreviewUrl();
				if (!imageDataMap.containsKey(backgroundPreviewUrl)) {
					imageDataMap.put(backgroundPreviewUrl, new SmartImageFetcher.Data(backgroundPreviewUrl, screenWidth));
				}
				imageFetcher.loadImage(imageDataMap.get(backgroundPreviewUrl), holder.backImg);

				String boardPreviewUrl = item.getBoardPreviewUrl();
				if (!imageDataMap.containsKey(boardPreviewUrl)) {
					imageDataMap.put(boardPreviewUrl, new SmartImageFetcher.Data(boardPreviewUrl, boardPreviewImageSize));
				}
				imageFetcher.loadImage(imageDataMap.get(boardPreviewUrl), holder.boardPreviewImg);

//				String piecesPreviewUrl = item.getPiecesPreviewUrl(); // TODO investigate why it makes black background here...
//				if (!imageDataMap.containsKey(piecesPreviewUrl)) {
//					imageDataMap.put(piecesPreviewUrl, new SmartImageFetcher.Data(piecesPreviewUrl, boardPreviewImageSize));
//				}
//				imageFetcher.loadImage(imageDataMap.get(piecesPreviewUrl), holder.piecesPreviewImg);

//				imageLoader.download(imageUrl, holder.backImg, screenWidth, screenWidth);
//				imageLoader.download(item.getBoardPreviewUrl(), holder.boardPreviewImg, boardPreviewImageSize);
				imageLoader.download(item.getPiecesPreviewUrl(), holder.piecesPreviewImg, boardPreviewImageSize);
			}
		}

		public Context getContext() {
			return context;
		}

		public void setProgressForItem(int progressForItem, int themeId) {
			this.progressForItem = progressForItem;
			this.loadingThemeId = themeId;
			notifyDataSetChanged();
		}

		public void setTitleForLoadingItem(String titleForLoadingItem) {
			this.titleForLoadingItem = titleForLoadingItem;
			notifyDataSetChanged();
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).getId();
		}

		public void updateThemeLoadingStatus(long id, ThemeState status) {
			themeLoadStateMap.put((int) id, status);
			notifyDataSetChanged();
		}

		public class ViewHolder {
			LinLayout rowOverlayView;
			ImageView backImg;
			ImageView boardPreviewImg;
			ProgressImageView piecesPreviewImg;
			ProgressBar themeLoadProgressBar;
			TextView check;
			TextView title;
			TextView progressTitleTxt;
		}
	}


}
