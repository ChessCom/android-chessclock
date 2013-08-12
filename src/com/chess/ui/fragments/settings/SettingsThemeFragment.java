package com.chess.ui.fragments.settings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.ThemeItem;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.backend.tasks.SaveImageToSdTask;
import com.chess.model.PopupItem;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.utilities.AppUtils;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.05.13
 * Time: 11:23
 */
public class SettingsThemeFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, PopupListSelectionFace {

	private static final String THEME_LOAD_TAG = "theme load popup";

	static final int BACKGROUND = 0;
	static final int BOARD = 1;
	private static final int ID_INSTALL = 0;
	private static final int ID_CUSTOMIZE = 1;

	public static final int PREVIEW_IMG_SIZE = 180;
	private static final String GAME_THEME_NAME = "Game";
	private static final String OPTION_SELECTION_TAG = "options select popup";

	private ListView listView;
	private ThemesUpdateListener themesUpdateListener;
	private ImageUpdateListener backgroundUpdateListener;
	private ImageUpdateListener boardUpdateListener;
	private BackgroundImageSaveListener mainBackgroundImgSaveListener;
	private BackgroundImageSaveListener boardImgSaveListener;

	private ImageDownloaderToListener imageDownloader;
	private String backgroundUrl;
	private int screenWidth;
	private int height;
	private ThemeItem.Data selectedThemeItem;
	private String boardBackgroundUrl;
	private List<ThemeItem.Data> themesList;
	private String selectedThemeName;
	private TextView loadProgressTxt;
	private TextView taskTitleTxt;
	private PopupCustomViewFragment loadProgressPopupFragment;
	private SparseArray<String> optionsArray;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private boolean need2update = true;
	private ThemesAdapter themesAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		screenWidth = getResources().getDisplayMetrics().widthPixels;
		height = getResources().getDisplayMetrics().heightPixels;

		themesUpdateListener = new ThemesUpdateListener();
		backgroundUpdateListener = new ImageUpdateListener(BACKGROUND);
		boardUpdateListener = new ImageUpdateListener(BOARD);
		mainBackgroundImgSaveListener = new BackgroundImageSaveListener(BACKGROUND);
		boardImgSaveListener = new BackgroundImageSaveListener(BOARD);

		imageDownloader = new ImageDownloaderToListener(getContext());

		selectedThemeName = getAppData().getThemeName();

		{// options list setup
			optionsArray = new SparseArray<String>();
			optionsArray.put(ID_INSTALL, getString(R.string.install));
			optionsArray.put(ID_CUSTOMIZE, getString(R.string.customize));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.select_theme);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_THEMES);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

			new RequestJsonTask<ThemeItem>(themesUpdateListener).executeTask(loadItem);
		} else {
			listView.setAdapter(themesAdapter);
		}
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_CUSTOMIZE) {
			getActivityFace().openFragment(SettingsThemeCustomizeFragment.createInstance(selectedThemeItem));
		} else if (code == ID_INSTALL) {
			installSelectedTheme();
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	private class ThemesUpdateListener extends ChessLoadUpdateListener<ThemeItem> {

		private ThemesUpdateListener() {
			super(ThemeItem.class);
		}

		@Override
		public void updateData(ThemeItem returnedObj) {
			super.updateData(returnedObj);

			themesList = returnedObj.getData();
			for (ThemeItem.Data theme : themesList) {
				if (theme.getThemeName().equals(selectedThemeName)) {
					theme.setSelected(true);
				}
			}

			themesAdapter = new ThemesAdapter(getActivity(), themesList);
			listView.setAdapter(themesAdapter);

			need2update = false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		for (ThemeItem.Data data : themesList) {
			data.setSelected(false);
		}

		selectedThemeItem = (ThemeItem.Data) listView.getItemAtPosition(position);
		selectedThemeItem.setSelected(true);

		((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();

		if (optionsSelectFragment != null) {
			optionsSelectFragment.dismiss();
			optionsSelectFragment = null;
			return;
		}

		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsArray);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
	}

	private void installSelectedTheme() {
		if (selectedThemeItem.getThemeName().equals(GAME_THEME_NAME)) {
			getAppData().setThemeBackPath(StaticData.SYMBOL_EMPTY); // clear downloaded theme value
			getActivityFace().setMainBackground(R.drawable.img_theme_green_felt);

			getAppData().setThemeName(getString(R.string.theme_game_room));
		} else { // start loading main background image
			backgroundUrl = selectedThemeItem.getBackgroundUrl();

			{  // show popup with percentage of loading theme
				View layout = LayoutInflater.from(getActivity()).inflate(R.layout.new_progress_load_popup, null, false);

				loadProgressTxt = (TextView) layout.findViewById(R.id.loadProgressTxt);
				taskTitleTxt = (TextView) layout.findViewById(R.id.taskTitleTxt);

				taskTitleTxt.setText(R.string.loading_background);

				PopupItem popupItem = new PopupItem();
				popupItem.setCustomView((LinearLayout) layout);

				loadProgressPopupFragment = PopupCustomViewFragment.createInstance(popupItem);
				loadProgressPopupFragment.show(getFragmentManager(), THEME_LOAD_TAG);
			}

			imageDownloader.download(backgroundUrl, backgroundUpdateListener, screenWidth, height);
			selectedThemeName = selectedThemeItem.getThemeName();
			getAppData().setThemeName(selectedThemeName);
		}
	}

	private class ImageUpdateListener implements ImageReadyListener {

		private int listenerCode;

		private ImageUpdateListener(int listenerCode) {
			this.listenerCode = listenerCode;
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

			if (listenerCode == BACKGROUND) {
				taskTitleTxt.setText(R.string.saving_background);
				loadProgressTxt.setText(String.valueOf(0));
				loadProgressTxt.setVisibility(View.GONE);

				String filename = String.valueOf(backgroundUrl.hashCode());
				new SaveImageToSdTask(mainBackgroundImgSaveListener, bitmap).executeTask(filename);
			} else if (listenerCode == BOARD) {
				taskTitleTxt.setText(R.string.saving_board);
				loadProgressTxt.setText(String.valueOf(0));
				loadProgressTxt.setVisibility(View.GONE);

				String filename = String.valueOf(boardBackgroundUrl.hashCode());
				new SaveImageToSdTask(boardImgSaveListener, bitmap).executeTask(filename);
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
					loadProgressTxt.setText(String.valueOf(progress) + StaticData.SYMBOL_PERCENT);
				}
			});
		}
	}

	private class BackgroundImageSaveListener extends AbstractUpdateListener<Bitmap> {

		private int listenerCode;

		public BackgroundImageSaveListener(int listenerCode) {
			super(getActivity(), SettingsThemeFragment.this);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(Bitmap returnedObj) {

			if (listenerCode == BACKGROUND) {

				// set main background image as theme
				String filename = String.valueOf(backgroundUrl.hashCode());
				File imgFile = AppUtils.openFileByName(getActivity(), filename);
				getActivityFace().setMainBackground(imgFile.getAbsolutePath());

				// Start loading board background
				int size = screenWidth;
				taskTitleTxt.setText(R.string.loading_board);
				loadProgressTxt.setVisibility(View.VISIBLE);

				boardBackgroundUrl = selectedThemeItem.getBoardBackgroundUrl();
				imageDownloader.download(boardBackgroundUrl, boardUpdateListener, size);

			} else {
				// set board background image as theme
				String filename = String.valueOf(boardBackgroundUrl.hashCode());
				File imgFile = AppUtils.openFileByName(getActivity(), filename);
				String drawablePath = imgFile.getAbsolutePath();

				getAppData().setThemeBoardPath(drawablePath);

				if (loadProgressPopupFragment != null) {
					loadProgressPopupFragment.dismiss();
				}
			}
		}
	}

	private class ThemesAdapter extends ItemsAdapter<ThemeItem.Data> {

		public ThemesAdapter(Context context, List<com.chess.backend.entity.api.ThemeItem.Data> menuItems) {
			super(context, menuItems);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_settings_theme_item, parent, false);
			ViewHolder holder = new ViewHolder();

			holder.check = (TextView) view.findViewById(R.id.iconTxt);
			holder.title = (TextView) view.findViewById(R.id.rowTitleTxt);
			holder.backImg = (ProgressImageView) view.findViewById(R.id.backImg);
			holder.boardPreviewImg = (ProgressImageView) view.findViewById(R.id.boardPreviewImg);
			view.setTag(holder);

			int imageHeight = (int) (screenWidth / 2.9f);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
			holder.backImg.setLayoutParams(params);

			// Change Placeholder
			int backIMgColor = getResources().getColor(R.color.upgrade_toggle_button_p);
			Bitmap bitmap = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);
			holder.backImg.placeholder = bitmap;

			// Change Image params
			FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(screenWidth, imageHeight);
			holder.backImg.getImageView().setLayoutParams(imageParams);
			holder.backImg.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

			// Change ProgressBar params
			FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			progressParams.gravity = Gravity.CENTER;
			holder.backImg.getProgressBar().setLayoutParams(progressParams);

			return view;
		}

		@Override
		protected void bindView(ThemeItem.Data item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			if (item.isSelected()) {
				holder.check.setText(R.string.ic_check);
			} else {
				holder.check.setText(StaticData.SYMBOL_EMPTY);
			}

			holder.title.setText(item.getThemeName());

			imageLoader.download(item.getBackgroundPreviewUrl(), holder.backImg, screenWidth, screenWidth);
			imageLoader.download(item.getBoardPreviewUrl(), holder.boardPreviewImg, PREVIEW_IMG_SIZE);
		}

		public Context getContext() {
			return context;
		}

		public class ViewHolder {
			ProgressImageView backImg;
			ProgressImageView boardPreviewImg;
			TextView check;
			TextView title;
		}
	}
}
