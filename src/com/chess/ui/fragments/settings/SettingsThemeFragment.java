package com.chess.ui.fragments.settings;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.themes.*;
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
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.utilities.AppUtils;

import java.io.File;
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
	private static final String THEME_LOAD_TAG = "theme load popup";

	static final int BACKGROUND = 0;
	static final int BOARD = 1;

	public static final int BOARD_SIZE_STEP = 8;
	public static final int BOARD_START_NAME = 20;
	public static final int BOARD_START_SIZE = 160;
	public static final int BOARD_END_NAME = 180;
	public static final int BOARD_END_SIZE = 1440;

//	private static final int ID_INSTALL = 0;
//	private static final int ID_CUSTOMIZE = 1;

	//	public static final int PREVIEW_IMG_SIZE = 180;
	private static final String GAME_THEME_NAME = "Game";

	private ListView listView;
	private ThemesAdapter themesAdapter;
	private ThemesUpdateListener themesUpdateListener;
	private ImageUpdateListener backgroundUpdateListener;
	private ImageUpdateListener boardUpdateListener;
	private BackgroundImageSaveListener mainBackgroundImgSaveListener;
	private BackgroundImageSaveListener boardImgSaveListener;
	private PopupCustomViewFragment loadProgressPopupFragment;

	private ImageDownloaderToListener imageDownloader;
	private String backgroundUrl;
	private int screenWidth;
	private int screenHeight;
	private ThemeItem.Data selectedThemeItem;
	private ThemeItem.Data currentThemeItem;
	private String boardUrl;
	private List<ThemeItem.Data> themesList;
	private String selectedThemeName;
	private TextView loadProgressTxt;
	private TextView taskTitleTxt;
	private BackgroundItemUpdateListener backgroundItemUpdateListener;
	private BoardItemUpdateListener boardItemUpdateListener;
	private PiecesItemUpdateListener piecesItemUpdateListener;
	private SoundsItemUpdateListener soundsItemUpdateListener;
	private SoundPackSaveListener soundPackSaveListener;
	private PiecesPackSaveListener piecesPackSaveListener;
	private String selectedSoundPackUrl;
	private String selectedPieceDir;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		screenWidth = getResources().getDisplayMetrics().widthPixels;
		screenHeight = getResources().getDisplayMetrics().heightPixels;

		themesList = new ArrayList<ThemeItem.Data>();
		themesUpdateListener = new ThemesUpdateListener();
		backgroundUpdateListener = new ImageUpdateListener(BACKGROUND);
		boardUpdateListener = new ImageUpdateListener(BOARD);
		mainBackgroundImgSaveListener = new BackgroundImageSaveListener(BACKGROUND);
		boardImgSaveListener = new BackgroundImageSaveListener(BOARD);
		backgroundItemUpdateListener = new BackgroundItemUpdateListener();
		boardItemUpdateListener = new BoardItemUpdateListener();
		piecesItemUpdateListener = new PiecesItemUpdateListener();
		soundsItemUpdateListener = new SoundsItemUpdateListener();
		soundPackSaveListener = new SoundPackSaveListener();
		piecesPackSaveListener = new PiecesPackSaveListener();

		imageDownloader = new ImageDownloaderToListener(getContext());

		selectedThemeName = getAppData().getThemeName();
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
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.THEMES));

			if (cursor != null && cursor.moveToFirst()) {
				do {
					themesList.add(DbDataManager.getThemeItemFromCursor(cursor));
				} while (cursor.moveToNext());

				updateUiData();
			} else {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.getInstance().CMD_THEMES);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

				new RequestJsonTask<ThemeItem>(themesUpdateListener).executeTask(loadItem);
			}

		} else {
			listView.setAdapter(themesAdapter);
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

			updateUiData();
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
			getActivityFace().openFragment(SettingsThemeCustomizeFragment.createInstance(currentThemeItem));
		} else {
			for (ThemeItem.Data data : themesList) {
				data.setSelected(false);
			}

			selectedThemeItem.setSelected(true);
			currentThemeItem = selectedThemeItem;
			((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();

			installSelectedTheme();
		}
	}

	private void installSelectedTheme() {
		if (selectedThemeItem.getThemeName().equals(GAME_THEME_NAME)) {
			getAppData().setThemeBoardPath(Symbol.EMPTY); // clear downloaded theme value
			getAppData().setThemeBackPath(Symbol.EMPTY); // clear downloaded theme value
			getAppData().setThemePiecesPath(Symbol.EMPTY); // clear downloaded theme value
			getAppData().setThemeSoundPath(Symbol.EMPTY); // clear downloaded theme value
			getActivityFace().setMainBackground(R.drawable.img_theme_green_felt);

			getAppData().setThemeName(getString(R.string.theme_game_room));
		} else { // start loading main background image
			// Get exactly sized url for theme background
			LoadItem loadItem = LoadHelper.getBackgroundById(getUserToken(), selectedThemeItem.getBackgroundId(),
					screenWidth, screenHeight, RestHelper.V_HANDSET);

			new RequestJsonTask<BackgroundItem>(backgroundItemUpdateListener).executeTask(loadItem);

			selectedThemeName = selectedThemeItem.getThemeName();
			getAppData().setThemeName(selectedThemeName);
		}
	}

	private class BackgroundItemUpdateListener extends ChessLoadUpdateListener<BackgroundItem> {

		private BackgroundItemUpdateListener() {
			super(BackgroundItem.class);
		}

		@Override
		public void updateData(BackgroundItem returnedObj) {

			backgroundUrl = returnedObj.getData().getResizedImage();

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

			// Start loading background image
			imageDownloader.download(backgroundUrl, backgroundUpdateListener, screenWidth, screenHeight);
		}
	}

	private class BoardItemUpdateListener extends ChessLoadUpdateListener<BoardItem> {

		private BoardItemUpdateListener() {
			super(BoardItem.class);
		}

		@Override
		public void updateData(BoardItem returnedObj) {

			// get boards dir in s3
			String boardDir = returnedObj.getData().getThemeDir();

			// we start to count pixels until we reach needed size for board
			int boardSize = BOARD_START_SIZE;
			int name;
			for (name = BOARD_START_NAME; name < BOARD_END_NAME; name++) {
				if (boardSize == screenWidth) { // 480 == 480

					break;
				}

//				// if we step over the range and missed needed size, than take the closest one
//				if (screenWidth > boardSize) {
//
//				}
				boardSize += BOARD_SIZE_STEP;
			}

			boardUrl = BoardItem.PATH + boardDir + "/" + name+ ".png";
			logTest(" board url = " + boardUrl);

			taskTitleTxt.setText(R.string.loading_board);

			// Start loading board image
			imageDownloader.download(boardUrl, boardUpdateListener, screenWidth);
		}
	}

	private class PiecesItemUpdateListener extends ChessLoadUpdateListener<PiecesItem> {

		private PiecesItemUpdateListener() {
			super(PiecesItem.class);
		}

		@Override
		public void updateData(PiecesItem returnedObj) {

			// get pieces dir in s3
			selectedPieceDir = returnedObj.getData().getThemeDir();
			int pieceWidth = screenWidth / 8;
			int pieceHeight = screenWidth / 8;

			String[] imagesToLoad = new String[12]; // 6 pieces for each side
			String[] whitePieceImageCodes = ChessBoard.whitePieceImageCodes;
			for (int i = 0; i < whitePieceImageCodes.length; i++) {
				String imageCode = whitePieceImageCodes[i];
				imagesToLoad[i] = PiecesItem.PATH + selectedPieceDir + "/" + pieceWidth + "/" + imageCode + ".png";
			}

			String[] blackPieceImageCodes = ChessBoard.blackPieceImageCodes;

			for (int i = 0; i < blackPieceImageCodes.length; i++) {
				String imageCode = blackPieceImageCodes[i];
				imagesToLoad[6 + i] = PiecesItem.PATH + selectedPieceDir + "/" + pieceWidth + "/" + imageCode + ".png";
			}

			taskTitleTxt.setText(R.string.loading_pieces);

			// Start loading pieces image
			new GetAndSaveFileToSdTask(piecesPackSaveListener, 	AppUtils.getLocalDirForPieces(getActivity(), selectedPieceDir))
					.executeTask(imagesToLoad);
		}
	}

	private class PiecesPackSaveListener extends ChessUpdateListener<String> implements FileReadyListener {

		private PiecesPackSaveListener() {
			useList = true;
		}

		@Override
		public void updateListData(List<String> itemsList) {
			super.updateListData(itemsList);

			getAppData().setThemePiecesPath(selectedPieceDir);

			if (selectedPieceDir.contains(_3D_PART)) {
				getAppData().setThemePieces3d(true);
			} else {
				getAppData().setThemePieces3d(false);
			}

			// Get sounds zip url if id is valid
			if (selectedThemeItem.getSoundsId() != -1) {

				LoadItem loadItem = LoadHelper.getSoundsById(getUserToken(), selectedThemeItem.getSoundsId());

				new RequestJsonTask<SoundSingleItem>(soundsItemUpdateListener).executeTask(loadItem);
			} else {
				if (loadProgressPopupFragment != null) {
					loadProgressPopupFragment.dismiss();
				}

				// clear sounds theme
				SoundPlayer.setUseThemePack(false);
				SoundPlayer.setThemePath(Symbol.EMPTY);
			}
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
//					loadProgressBar.setVisibility(View.GONE);
					loadProgressTxt.setVisibility(View.VISIBLE);
					loadProgressTxt.setText(String.valueOf(progress) + Symbol.PERCENT);
				}
			});
		}
	}


	private class SoundsItemUpdateListener extends ChessLoadUpdateListener<SoundSingleItem> {

		private SoundsItemUpdateListener() {
			super(SoundSingleItem.class);
		}

		@Override
		public void updateData(SoundSingleItem returnedObj) {

			// get sounds dir in s3
			selectedSoundPackUrl = returnedObj.getData().getSoundPackZipUrl();

			new GetAndSaveFileToSdTask(soundPackSaveListener, true, AppUtils.getLocalDirForSounds(getActivity()))
					.executeTask(selectedSoundPackUrl);
		}
	}

	private class SoundPackSaveListener extends ChessLoadUpdateListener<String> implements FileReadyListener {

		@Override
		public void updateData(String returnedObj) {
			super.updateData(returnedObj);

			DbDataManager.saveSoundPathToDb(getContentResolver(), selectedSoundPackUrl, returnedObj);

			// save sounds path to settings
			getAppData().setThemeSoundPath(returnedObj);

			// update sounds flag
			SoundPlayer.setUseThemePack(true);
			SoundPlayer.setThemePath(returnedObj);

			// hide progress
			if (loadProgressPopupFragment != null) {
				loadProgressPopupFragment.dismiss();
			}
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
//					loadProgressBar.setVisibility(View.GONE);
					loadProgressTxt.setVisibility(View.VISIBLE);
					loadProgressTxt.setText(String.valueOf(progress) + Symbol.PERCENT);
				}
			});
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

				String filename = String.valueOf(backgroundUrl.hashCode()); // TODO rename to MD5
				new SaveImageToSdTask(mainBackgroundImgSaveListener, bitmap).executeTask(filename);
			} else if (listenerCode == BOARD) {
				taskTitleTxt.setText(R.string.saving_board);
				loadProgressTxt.setText(String.valueOf(0));
				loadProgressTxt.setVisibility(View.GONE);

				String filename = String.valueOf(boardUrl.hashCode()); // TODO rename to MD5
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
					loadProgressTxt.setText(String.valueOf(progress) + Symbol.PERCENT);
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

				// Get board main path
				LoadItem loadItem = LoadHelper.getBoardById(getUserToken(), selectedThemeItem.getBoardId());

				new RequestJsonTask<BoardItem>(boardItemUpdateListener).executeTask(loadItem);

				int size = screenWidth;
				taskTitleTxt.setText(R.string.loading_board);
				loadProgressTxt.setVisibility(View.VISIBLE);



			} else if (listenerCode == BOARD) {
				// set board image as theme
				String filename = String.valueOf(boardUrl.hashCode());
				File imgFile = AppUtils.openFileByName(getActivity(), filename);
				String drawablePath = imgFile.getAbsolutePath();

				getAppData().setThemeBoardPath(drawablePath);

				// Get pieces main path on s3
				LoadItem loadItem = LoadHelper.getPiecesById(getUserToken(), selectedThemeItem.getPiecesId());
				logTest(" start loading pieces");
				new RequestJsonTask<PiecesItem>(piecesItemUpdateListener).executeTask(loadItem);
			}
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

		public ThemesAdapter(Context context, List<ThemeItem.Data> menuItems) {
			super(context, menuItems);
			customColor = resources.getColor(R.color.theme_customize_back);
			boardPreviewImageSize = (int) (resources.getDimensionPixelSize(R.dimen.theme_board_preview_size));

			imageHeight = (int) (screenWidth / 2.9f);
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
			holder.backImg = (ProgressImageView) view.findViewById(R.id.backImg);
			holder.boardPreviewImg = (ProgressImageView) view.findViewById(R.id.boardPreviewImg);
			holder.piecesPreviewImg = (ProgressImageView) view.findViewById(R.id.piecesPreviewImg);
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
			} else {
				holder.check.setText(Symbol.EMPTY);
			}

			if (item.isLocal()) {
				holder.title.setText(R.string.customize);

				holder.backImg.setImageDrawable(new ColorDrawable(customColor));
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

		public class ViewHolder {
			ProgressImageView backImg;
			ProgressImageView boardPreviewImg;
			ProgressImageView piecesPreviewImg;
			TextView check;
			TextView title;
		}
	}
}
