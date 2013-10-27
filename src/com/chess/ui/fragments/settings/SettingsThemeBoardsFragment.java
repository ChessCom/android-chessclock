package com.chess.ui.fragments.settings;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.themes.BoardSingleItem;
import com.chess.backend.entity.api.themes.BoardsItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.backend.tasks.SaveImageToSdTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.PopupItem;
import com.chess.model.SelectionItem;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.10.13
 * Time: 15:01
 */
public class SettingsThemeBoardsFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	private static final int THEME_SECTION = 0;
	private static final int DEFAULT_SECTION = 1;
	private static final String THEME_LOAD_TAG = "theme load popup";

	public static final int BOARD_SIZE_STEP = 8;
	public static final int BOARD_START_NAME = 20;
	public static final int BOARD_START_SIZE = 160;
	public static final int BOARD_END_NAME = 180;
	public static final int BOARD_END_SIZE = 1440;

	private BoardsItemUpdateListener boardsItemUpdateListener;
	private BoardSingleItemUpdateListener boardSingleItemUpdateListener;
	private CustomSectionedAdapter sectionedAdapter;
	private ThemeBoardsAdapter themeBoardsAdapter;
	private List<SelectionItem> defaultBoardSelectionList;
	private String themeBoardName;
	private List<SelectionItem> themeBoardSelectionList;
	private int screenWidth;
	private TextView loadProgressTxt;
	private TextView taskTitleTxt;
	private PopupCustomViewFragment loadProgressPopupFragment;
	private List<BoardSingleItem.Data> themeBoardItemsList;
	private SelectionItem selectedThemeBoardItem;
	private boolean boardIsLoading;
	private ImageSaveListener boardImgSaveListener;
	private String boardUrl;
	private ImageDownloaderToListener imageDownloader;
	private ImageUpdateListener boardUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		screenWidth = getResources().getDisplayMetrics().widthPixels;

		themeBoardName = getAppData().getThemeBoardName();

		boardImgSaveListener = new ImageSaveListener();
		boardsItemUpdateListener = new BoardsItemUpdateListener();
		boardSingleItemUpdateListener = new BoardSingleItemUpdateListener();
//		boardsPackSaveListener = new BoardsPackSaveListener();
		imageDownloader = new ImageDownloaderToListener(getActivity());
		boardUpdateListener = new ImageUpdateListener();

		Resources resources = getResources();
		themeBoardSelectionList = new ArrayList<SelectionItem>();
		themeBoardItemsList = new ArrayList<BoardSingleItem.Data>();

		// Boards bitmaps list init
		defaultBoardSelectionList = new ArrayList<SelectionItem>();
		defaultBoardSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_wood_dark), getString(R.string.board_wood_dark)));
		defaultBoardSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_wood_light), getString(R.string.board_wood_light)));
		defaultBoardSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_blue), getString(R.string.board_blue)));
		defaultBoardSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_brown), getString(R.string.board_brown)));
		defaultBoardSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_green), getString(R.string.board_green)));
		defaultBoardSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_grey), getString(R.string.board_grey)));
		defaultBoardSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_marble), getString(R.string.board_marble)));
		defaultBoardSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_red), getString(R.string.board_red)));
		defaultBoardSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_tan), getString(R.string.board_tan)));

		for (SelectionItem selectionItem : defaultBoardSelectionList) {
			if (selectionItem.getCode().equals(themeBoardName)) {
				selectionItem.setChecked(true);
				break;
			}
		}

		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_comp_archive_header,
				new int[]{THEME_SECTION, DEFAULT_SECTION});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.select_style);

		ListView listView = (ListView) view.findViewById(R.id.listView);

		themeBoardsAdapter = new ThemeBoardsAdapter(getContext(), null);
		DefaultBoardsAdapter defaultBoardsAdapter = new DefaultBoardsAdapter(getActivity(), defaultBoardSelectionList);

		sectionedAdapter.addSection("Theme Boards", themeBoardsAdapter);
		sectionedAdapter.addSection("Default Boards", defaultBoardsAdapter);
		listView.setAdapter(sectionedAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Don't load custom board if we are not logged in
		if (!TextUtils.isEmpty(getUserToken())) {
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.THEME_BOARDS));

			if (cursor != null && cursor.moveToFirst()) {
				do {
					themeBoardItemsList.add(DbDataManager.getThemeBoardItemFromCursor(cursor));
				} while (cursor.moveToNext());

				updateUiData();
			} else {
				getBoards();
			}
		}
	}

	private void getBoards() {
		// load board line previews
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_BOARDS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<BoardsItem>(boardsItemUpdateListener).executeTask(loadItem);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int section = sectionedAdapter.getCurrentSection(position);

		if (section == THEME_SECTION) {
			if (boardIsLoading) {
				return;
			}

			// don't allow to select while it's loading
			boardIsLoading = true;

			selectedThemeBoardItem = (SelectionItem) parent.getItemAtPosition(position);
			for (SelectionItem selectionItem : themeBoardSelectionList) {
				if (selectedThemeBoardItem.getCode().equals(selectionItem.getCode())) {
					selectionItem.setChecked(true);
				} else {
					selectionItem.setChecked(false);
				}
			}
			for (SelectionItem selectionItem : defaultBoardSelectionList) {
				selectionItem.setChecked(false);
			}

			sectionedAdapter.notifyDataSetChanged();

			int selectedBoardId = 1;
			for (BoardSingleItem.Data data : themeBoardItemsList) {
				if (data.getName().equals(selectedThemeBoardItem.getCode())) {
					selectedBoardId = data.getThemeBoardId();
					break;
				}
			}

			// start loading board
			LoadItem loadItem = LoadHelper.getBoardById(getUserToken(), selectedBoardId);
			new RequestJsonTask<BoardSingleItem>(boardSingleItemUpdateListener).executeTask(loadItem);
		} else {
			SelectionItem defaultBoardItem = (SelectionItem) parent.getItemAtPosition(position);
			for (SelectionItem selectionItem : defaultBoardSelectionList) {
				if (defaultBoardItem.getText().equals(selectionItem.getText())) {
					selectionItem.setChecked(true);
					// save board theme name to appData
					getAppData().setUseThemeBoard(false);
					getAppData().setThemeBoardName(selectionItem.getText());
				} else {
					selectionItem.setChecked(false);
				}
			}
			if (themeBoardSelectionList != null) { // for guest mode we don't have theme board
				for (SelectionItem selectionItem : themeBoardSelectionList) {
					selectionItem.setChecked(false);
				}
			}

			// go back
			getActivityFace().showPreviousFragment();
		}
	}

	private class BoardsItemUpdateListener extends ChessLoadUpdateListener<BoardsItem> {

		private BoardsItemUpdateListener() {
			super(BoardsItem.class);
		}

		@Override
		public void updateData(BoardsItem returnedObj) {
			super.updateData(returnedObj);

			themeBoardItemsList = returnedObj.getData();

			updateUiData();

			for (BoardSingleItem.Data currentItem : themeBoardItemsList) {
				DbDataManager.saveThemeBoardItemToDb(getContentResolver(), currentItem);
			}
		}
	}

	private void updateUiData() {
		for (BoardSingleItem.Data boardItem : themeBoardItemsList) {
			SelectionItem selectionItem = new SelectionItem(null, boardItem.getLineBoardPreviewUrl());
			selectionItem.setCode(boardItem.getName());
			themeBoardSelectionList.add(selectionItem);
		}

		for (SelectionItem selectionItem : themeBoardSelectionList) {
			if (selectionItem.getCode().equals(themeBoardName)) {
				selectionItem.setChecked(true);
				break;
			}
		}

		themeBoardsAdapter.setItemsList(themeBoardSelectionList);
		sectionedAdapter.notifyDataSetChanged();
	}


	private class ThemeBoardsAdapter extends ItemsAdapter<SelectionItem> {

		private final int previewWidth;
		private final Bitmap placeHolderBitmap;
		private final RelativeLayout.LayoutParams imageParams;
		private final LinearLayout.LayoutParams linearLayoutParams;
		private final RelativeLayout.LayoutParams progressParams;
		private final EnhancedImageDownloader imageLoader;

		public ThemeBoardsAdapter(Context context, List<SelectionItem> menuItems) {
			super(context, menuItems);

			imageLoader = new EnhancedImageDownloader(context);

			int backIMgColor = getResources().getColor(R.color.upgrade_toggle_button_p);
			placeHolderBitmap = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);

			Drawable boardDrawableExample = resources.getDrawable(R.drawable.board_sample_wood_dark);
			previewWidth = boardDrawableExample.getIntrinsicWidth();
			int imageHeight = boardDrawableExample.getIntrinsicHeight();

			imageParams = new RelativeLayout.LayoutParams(previewWidth, imageHeight);
			linearLayoutParams = new LinearLayout.LayoutParams(previewWidth, imageHeight);

			progressParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		}

		@Override
		protected View createView(ViewGroup parent) { // View to display in layout
			View view = inflater.inflate(R.layout.selection_progress_image_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.image = (ProgressImageView) view.findViewById(R.id.image);

			holder.image.setLayoutParams(linearLayoutParams);

			// Change Placeholder
			holder.image.placeholder = placeHolderBitmap;

			// Change Image params
			holder.image.getImageView().setLayoutParams(imageParams);
			holder.image.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

			// Change ProgressBar params
			holder.image.getProgressBar().setLayoutParams(progressParams);

			holder.text = (CheckedTextView) view.findViewById(R.id.text);

			view.setTag(holder);

			return view;
		}

		@Override
		protected void bindView(SelectionItem item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			imageLoader.download(item.getText(), holder.image, previewWidth, previewWidth);

			holder.text.setText(item.getCode());
			holder.text.setChecked(item.isChecked());
		}

		private class ViewHolder {
			public CheckedTextView text;
			public ProgressImageView image;
		}

		public Context getContext() {
			return context;
		}
	}

	private class DefaultBoardsAdapter extends ItemsAdapter<SelectionItem> {

		private final int previewWidth;
		private final Bitmap placeHolderBitmap;
		//			private final float aspectRatio;
		private final RelativeLayout.LayoutParams imageParams;
		private final LinearLayout.LayoutParams linearLayoutParams;
		private final RelativeLayout.LayoutParams progressParams;

		public DefaultBoardsAdapter(Context context, List<SelectionItem> menuItems) {
			super(context, menuItems);

			int backIMgColor = getResources().getColor(R.color.upgrade_toggle_button_p);
			placeHolderBitmap = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);

			Drawable boardDrawableExample = resources.getDrawable(R.drawable.board_sample_wood_dark);
			previewWidth = boardDrawableExample.getIntrinsicWidth();
			int imageHeight = boardDrawableExample.getIntrinsicHeight();


			imageParams = new RelativeLayout.LayoutParams(previewWidth, imageHeight);
			linearLayoutParams = new LinearLayout.LayoutParams(previewWidth, imageHeight);

			progressParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		}

		@Override
		protected View createView(ViewGroup parent) { // View to display in layout
			View view = inflater.inflate(R.layout.selection_progress_image_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.image = (ProgressImageView) view.findViewById(R.id.image);

			holder.image.setLayoutParams(linearLayoutParams);

			// Change Placeholder
			holder.image.placeholder = placeHolderBitmap;

			// Change Image params
			holder.image.getImageView().setLayoutParams(imageParams);
			holder.image.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

			// Change ProgressBar params
			holder.image.getProgressBar().setLayoutParams(progressParams);

			holder.text = (CheckedTextView) view.findViewById(R.id.text);

			view.setTag(holder);

			return view;
		}

		@Override
		protected void bindView(SelectionItem item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			holder.image.setImageDrawable(item.getImage());

			holder.text.setText(item.getText());
			holder.text.setChecked(item.isChecked());
		}

		private class ViewHolder {
			public CheckedTextView text;
			public ProgressImageView image;
		}

		public Context getContext() {
			return context;
		}
	}

	private class BoardSingleItemUpdateListener extends ChessLoadUpdateListener<BoardSingleItem> {

		private BoardSingleItemUpdateListener() {
			super(BoardSingleItem.class);
		}

		@Override
		public void updateData(BoardSingleItem returnedObj) {

			String coordinateColorLight = returnedObj.getData().getCoordinateColorLight();
			String coordinateColorDark = returnedObj.getData().getCoordinateColorDark();
			String highlightColor = returnedObj.getData().getHighlightColor();

			getAppData().setThemeBoardCoordinateLight(Color.parseColor(coordinateColorLight));
			getAppData().setThemeBoardCoordinateDark(Color.parseColor(coordinateColorDark));
			getAppData().setThemeBoardHighlight(Color.parseColor(highlightColor));

			// get boards dir in s3
			String boardDir = returnedObj.getData().getThemeDir();

			{  // show popup with percentage of loading theme
				View layout = LayoutInflater.from(getActivity()).inflate(R.layout.new_progress_load_popup, null, false);

				loadProgressTxt = (TextView) layout.findViewById(R.id.loadProgressTxt);
				taskTitleTxt = (TextView) layout.findViewById(R.id.taskTitleTxt);

				taskTitleTxt.setText(R.string.loading_board);

				PopupItem popupItem = new PopupItem();
				popupItem.setCustomView(layout);

				loadProgressPopupFragment = PopupCustomViewFragment.createInstance(popupItem);
				loadProgressPopupFragment.show(getFragmentManager(), THEME_LOAD_TAG);
			}

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

			boardUrl = BoardsItem.PATH + boardDir + "/" + name + ".png";
			logTest(" board url = " + boardUrl);

			taskTitleTxt.setText(R.string.loading_board);

			// Start loading board image
			imageDownloader.download(boardUrl, boardUpdateListener, screenWidth);
		}
	}

	private class ImageUpdateListener implements ImageReadyListener {

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

			taskTitleTxt.setText(R.string.saving_board);
			loadProgressTxt.setText(String.valueOf(0));
			loadProgressTxt.setVisibility(View.GONE);

			String filename = String.valueOf(boardUrl.hashCode()); // TODO rename to MD5
			new SaveImageToSdTask(boardImgSaveListener, bitmap).executeTask(filename);
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

	private class ImageSaveListener extends AbstractUpdateListener<Bitmap> {


		public ImageSaveListener() {
			super(getActivity(), SettingsThemeBoardsFragment.this);
		}

		@Override
		public void updateData(Bitmap returnedObj) {

			// set board image as theme
			String filename = String.valueOf(boardUrl.hashCode());

			try {
				File imgFile = AppUtils.openFileByName(getActivity(), filename);
				String drawablePath = imgFile.getAbsolutePath();

				// save board theme name to appData
				getAppData().setUseThemeBoard(true);
				getAppData().setThemeBoardPath(drawablePath);
				getAppData().setThemeBoardName(selectedThemeBoardItem.getCode());
				getAppData().setThemeBoardPreviewUrl(selectedThemeBoardItem.getText());

			} catch (IOException e) {
				e.printStackTrace();
			}

			boardIsLoading = false;


			if (loadProgressPopupFragment != null) {
				loadProgressPopupFragment.dismiss();
			}

			// go back
			getActivityFace().showPreviousFragment();
		}
	}

}
