package com.chess.ui.fragments.settings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ThemeItem;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.05.13
 * Time: 11:23
 */
public class SettingsThemeFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private static final int FILE_SIZE = 100;
	public static final int PREVIEW_IMG_SIZE = 180;
	private ListView listView;
	private ThemesUpdateListener themesUpdateListener;
	private ImageUpdateListener backgroundUpdateListener;
	private ImageUpdateListener boardUpdateListener;

	private ImageDownloaderToListener imageDownloader;
	private String backgroundUrl;
	private int screenWidth;
	private int height;
	private ThemeItem.Data selectedMenuItem;
	private String boardBackgroundUrl;
	private List<ThemeItem.Data> themesList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		screenWidth = getResources().getDisplayMetrics().widthPixels;
		height = getResources().getDisplayMetrics().heightPixels;

		themesUpdateListener = new ThemesUpdateListener();
		backgroundUpdateListener = new ImageUpdateListener(ImageUpdateListener.BACKGROUND);
		boardUpdateListener = new ImageUpdateListener(ImageUpdateListener.BOARD);
		imageDownloader = new ImageDownloaderToListener(getContext());
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
	public void onStart() {
		super.onStart();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_THEMES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<ThemeItem>(themesUpdateListener).executeTask(loadItem);
	}

	private class ThemesUpdateListener extends ChessLoadUpdateListener<ThemeItem> {

		private ThemesUpdateListener() {
			super(ThemeItem.class);
		}

		@Override
		public void updateData(ThemeItem returnedObj) {
			super.updateData(returnedObj);

			themesList = returnedObj.getData();

			// adding default theme, to allow user to select it from full list
			ThemeItem.Data defaultThemeItem = new ThemeItem.Data();
			defaultThemeItem.setLocal(true);
			themesList.add(0, defaultThemeItem);

			ThemesAdapter adapter = new ThemesAdapter(getActivity(), themesList);

			listView.setAdapter(adapter);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		for (ThemeItem.Data data : themesList) {
			data.setSelected(false);
		}

		selectedMenuItem = (ThemeItem.Data) listView.getItemAtPosition(position);
		selectedMenuItem.setSelected(true);

		((BaseAdapter)parent.getAdapter()).notifyDataSetChanged();

		if (selectedMenuItem.isLocal()) {
			getAppData().setThemeBackPath(StaticData.SYMBOL_EMPTY); // clear downloaded theme value
			getActivityFace().setMainBackground(R.drawable.img_theme_green_felt);

		} else { // start loading main background image
			backgroundUrl = selectedMenuItem.getBackgroundUrl();

			showLoadingProgress(true);
			imageDownloader.download(backgroundUrl, backgroundUpdateListener, screenWidth, height);
		}
	}

	private class ImageUpdateListener implements ImageReadyListener {

		static final int BACKGROUND = 0;
		static final int BOARD = 1;
		private int listenerCode;

		private ImageUpdateListener(int listenerCode) {
			this.listenerCode = listenerCode;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {
			showLoadingProgress(false);

			Activity activity = getActivity();
			if (activity == null) {
				return;
			}

			if (bitmap == null) {
				showToast("error loading image. Internal error");
				return;
			}

			if (listenerCode == BACKGROUND) {
				saveMainBackgroundAndSet(bitmap);

				boardBackgroundUrl = selectedMenuItem.getBoardBackgroundUrl();
				int size = screenWidth;
				showLoadingProgress(true);

				imageDownloader.download(boardBackgroundUrl, boardUpdateListener, size);

			} else if (listenerCode == BOARD) {

				saveBoardBackgroundAndSet(bitmap);
				showLoadingProgress(false);
			}
		}
	}

	private void saveMainBackgroundAndSet(Bitmap bitmap) { // TODO move to asynctask
		File cacheDir;
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), AppUtils.getApplicationCacheDir(getActivity().getPackageName()));
		else
			cacheDir = getActivity().getCacheDir();

		if (!cacheDir.exists())// TODO adjust saving to SD or local , but if not show warning to user
			cacheDir.mkdirs();

		String filename = String.valueOf(backgroundUrl.hashCode());
		File imgFile = new File(cacheDir, filename);

//		showLoadingProgress(true);
		// save stream to SD
		try {
			OutputStream os = new FileOutputStream(imgFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			logTest("saveMainBackgroundAndSet FileNotFoundException = " + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			logTest("saveMainBackgroundAndSet IOException = " + e.toString());
			e.printStackTrace();
		}
//		showLoadingProgress(false);

		getActivityFace().setMainBackground(imgFile.getAbsolutePath());
	}

	private void saveBoardBackgroundAndSet(Bitmap bitmap) { // TODO move to asynctask
		File cacheDir;
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), AppUtils.getApplicationCacheDir(getActivity().getPackageName()));
		else
			cacheDir = getActivity().getCacheDir();

		if (!cacheDir.exists())// TODO adjust saving to SD or local , but if not show warning to user
			cacheDir.mkdirs();

		String filename = String.valueOf(boardBackgroundUrl.hashCode());
		File imgFile = new File(cacheDir, filename);

//		showLoadingProgress(true);
		// save stream to SD
		try {
			OutputStream os = new FileOutputStream(imgFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			logTest("saveMainBackgroundAndSet FileNotFoundException = " + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			logTest("saveMainBackgroundAndSet IOException = " + e.toString());
			e.printStackTrace();
		}
//		showLoadingProgress(false);

		String drawablePath = imgFile.getAbsolutePath();

		getAppData().setThemeBoardPath(drawablePath);
	}

	private class ThemesAdapter extends ItemsAdapter<ThemeItem.Data> {

		public ThemesAdapter(Context context, List<com.chess.backend.entity.new_api.ThemeItem.Data> menuItems) {
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
			FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(screenWidth, imageHeight);
			holder.backImg.setLayoutParams(params);
			holder.backImg.getImageView().setLayoutParams(params1);
			holder.backImg.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

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

			if (item.isLocal()) {
				holder.title.setText(R.string.theme_game_room);

				holder.backImg.setImageDrawable(getResources().getDrawable(R.drawable.img_theme_green_felt));
				holder.boardPreviewImg.setImageDrawable(getResources().getDrawable(R.drawable.img_board_theme_sample));
			} else {
				holder.title.setText(item.getThemeName());

				imageLoader.download(item.getBackgroundPreviewUrl(), holder.backImg, screenWidth, screenWidth);
				imageLoader.download(item.getBoardPreviewUrl(), holder.boardPreviewImg, PREVIEW_IMG_SIZE);
			}
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
