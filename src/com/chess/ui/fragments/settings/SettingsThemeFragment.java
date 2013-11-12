package com.chess.ui.fragments.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.GetAndSaveTheme;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.themes.ThemeItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.interfaces.FileReadyListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.05.13
 * Time: 11:23
 */
public class SettingsThemeFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	public static final String _3D_PART = "3d";

	private static final String GAME_THEME_NAME = "Game";
	public static final String THEME_ITEM = "theme_item";
	public static final String SCREEN_WIDTH = "screen_width";
	public static final String SCREEN_HEIGHT = "screen_height";

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
	private boolean isInstallingTheme;
	private boolean serviceBounded;
	private GetAndSaveTheme.ServiceBinder serviceBinder;
	private boolean needToLoadThemeAfterConnected;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		screenWidth = getResources().getDisplayMetrics().widthPixels;
		screenHeight = getResources().getDisplayMetrics().heightPixels;

		themesList = new ArrayList<ThemeItem.Data>();
		themesUpdateListener = new ThemesUpdateListener();

		loadServiceConnectionListener = new LoadServiceConnectionListener();
		progressUpdateListener = new ProgressUpdateListener();
		selectedThemeName = getAppData().getThemeName();

		pullToRefresh(true);
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
		ThemeItem.Data customizeItem = new ThemeItem.Data();
		customizeItem.setThemeName(getString(R.string.customize));
		customizeItem.setLocal(true);
		themesList.add(0, customizeItem);

		for (ThemeItem.Data theme : themesList) {
			if (theme.getThemeName().equals(selectedThemeName)) {
				currentThemeItem = theme;
				theme.setSelected(true);
			}
		}

		themesAdapter = new ThemesAdapter(getActivity(), themesList);
		listView.setAdapter(themesAdapter);

		need2update = false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectedThemeItem = (ThemeItem.Data) listView.getItemAtPosition(position);

		if (selectedThemeItem.isLocal()) {
			openCustomizeFragment();
		} else {

			if (isInstallingTheme) {
				return;
			}

			isInstallingTheme = true;

			// deselect all themes
			for (ThemeItem.Data data : themesList) {
				data.setSelected(false);
			}
			// mark selected
			selectedThemeItem.setSelected(true);

			currentThemeItem = selectedThemeItem;
			((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();

			installSelectedTheme();
		}
	}

	protected void openCustomizeFragment() {
		getActivityFace().openFragment(SettingsThemeCustomizeFragment.createInstance(currentThemeItem));
	}

	private void installSelectedTheme() {
		if (selectedThemeItem.getThemeName().equals(GAME_THEME_NAME)) {
			// clear themed settings
			getAppData().resetThemeToDefault();
			getActivityFace().setMainBackground(R.drawable.img_theme_green_felt);

			getAppData().setThemeName(getString(R.string.theme_game_room));
			getAppData().setThemeBackgroundName(getString(R.string.theme_game_room));
			getActivityFace().updateActionBarBackImage();

			isInstallingTheme = false;
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
			if (serviceBinder.getService().isInstallingTheme()) {
				isInstallingTheme = true;
			}

			serviceBinder.getService().setProgressUpdateListener(progressUpdateListener);
			if (needToLoadThemeAfterConnected) {
				serviceBinder.getService().loadTheme(selectedThemeItem, screenWidth, screenHeight);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			serviceBounded = false;
			isInstallingTheme = false;
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
					if (progress == GetAndSaveTheme.DONE) {
						isInstallingTheme = false;
						needToLoadThemeAfterConnected = false;
						if (serviceBounded) {
							getActivity().unbindService(loadServiceConnectionListener);
						}
						serviceBounded = false;
					}
					themesAdapter.setProgressForItem(progress);
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
		private int progressForItem;
		private String titleForLoadingItem;

		public ThemesAdapter(Context context, List<ThemeItem.Data> menuItems) {
			super(context, menuItems);
			customColor = resources.getColor(R.color.theme_customize_back);
			boardPreviewImageSize = resources.getDimensionPixelSize(R.dimen.theme_board_preview_size);

			int screenWidth;
			if (!isTablet) {
				screenWidth = SettingsThemeFragment.this.screenWidth;
				imageHeight = (int) (screenWidth / 2.9f);
			} else {
				screenWidth = SettingsThemeFragment.this.screenWidth
						- resources.getDimensionPixelSize(R.dimen.tablet_side_menu_width);
				imageHeight = (int) (screenWidth / 4.0f);
			}

			backImageParams = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
			listItemParams = new ListView.LayoutParams(screenWidth, imageHeight);

			int backIMgColor = getResources().getColor(R.color.upgrade_toggle_button_p);
			placeHolderBitmap = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);

			imageParams = new RelativeLayout.LayoutParams(screenWidth, imageHeight);

			progressParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			imageLoader = new EnhancedImageDownloader(context);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_settings_theme_item, parent, false);
			ViewHolder holder = new ViewHolder();

			holder.check = (TextView) view.findViewById(R.id.iconTxt);
			holder.title = (TextView) view.findViewById(R.id.rowTitleTxt);
			holder.backImg = (ProgressImageView) view.findViewById(R.id.backgroundPreviewImg);
			holder.boardPreviewImg = (ProgressImageView) view.findViewById(R.id.boardPreviewImg);
			holder.piecesPreviewImg = (ProgressImageView) view.findViewById(R.id.piecesPreviewImg);
			holder.themeLoadProgressBar = (ProgressBar) view.findViewById(R.id.themeLoadProgressBar);
			holder.progressTitleTxt = (TextView) view.findViewById(R.id.progressTitleTxt);
			view.setTag(holder);

			holder.backImg.setLayoutParams(backImageParams);

			// List item params
			view.setLayoutParams(listItemParams);

			// Change Placeholder
			holder.backImg.placeholder = placeHolderBitmap;

			// Change Image params
			holder.backImg.getImageView().setLayoutParams(imageParams);
			holder.backImg.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

			// Change ProgressBar params
			progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			holder.backImg.getProgressBar().setLayoutParams(progressParams);

			return view;
		}

		@Override
		protected void bindView(ThemeItem.Data item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			if (item.isSelected()) {
				holder.check.setText(R.string.ic_check);

				if (isInstallingTheme) {
					if (progressForItem != GetAndSaveTheme.INDETERMINATE) {
						holder.themeLoadProgressBar.setProgress(progressForItem);
						holder.themeLoadProgressBar.setIndeterminate(false);
					} else {
						holder.themeLoadProgressBar.setIndeterminate(true);
					}

					holder.themeLoadProgressBar.setVisibility(View.VISIBLE);

					holder.progressTitleTxt.setText(titleForLoadingItem);
					holder.progressTitleTxt.setVisibility(View.VISIBLE);
				} else {
					holder.themeLoadProgressBar.setVisibility(View.GONE);
					holder.progressTitleTxt.setVisibility(View.GONE);
				}
			} else {
				holder.themeLoadProgressBar.setVisibility(View.GONE);
				holder.progressTitleTxt.setVisibility(View.GONE);

				holder.check.setText(Symbol.EMPTY);
			}

			if (item.isLocal()) {
				holder.title.setText(R.string.customize);

				holder.backImg.setImageDrawable(new ColorDrawable(customColor));
				holder.boardPreviewImg.setImageDrawable(getResources().getDrawable(R.drawable.empty));
				holder.piecesPreviewImg.setImageDrawable(getResources().getDrawable(R.drawable.empty));
			} else {
				holder.title.setText(item.getThemeName());

				imageLoader.download(item.getBackgroundPreviewUrl(), holder.backImg, screenWidth, screenWidth);
				imageLoader.download(item.getBoardPreviewUrl(), holder.boardPreviewImg, boardPreviewImageSize);
				imageLoader.download(item.getPiecesPreviewUrl(), holder.piecesPreviewImg, boardPreviewImageSize);
			}
		}

		public Context getContext() {
			return context;
		}

		public void setProgressForItem(int progressForItem) {
			this.progressForItem = progressForItem;
			notifyDataSetChanged();
		}

		public void setTitleForLoadingItem(String titleForLoadingItem) {
			this.titleForLoadingItem = titleForLoadingItem;
			notifyDataSetChanged();
		}

		public class ViewHolder {
			ProgressImageView backImg;
			ProgressImageView boardPreviewImg;
			ProgressImageView piecesPreviewImg;
			ProgressBar themeLoadProgressBar;
			TextView check;
			TextView title;
			TextView progressTitleTxt;
		}
	}
}
