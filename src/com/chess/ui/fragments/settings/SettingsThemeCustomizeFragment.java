package com.chess.ui.fragments.settings;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.widget.*;
import com.chess.R;
import com.chess.backend.entity.api.ThemeItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.Symbol;
import com.chess.backend.tasks.SaveImageToSdTask;
import com.chess.model.PopupItem;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.SelectionAdapter;
import com.chess.ui.adapters.StringSpinnerAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.popup_fragments.PopupBackgroundsFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.PiecePreviewImg;
import com.chess.utilities.AppUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.07.13
 * Time: 21:07
 */
public class SettingsThemeCustomizeFragment extends CommonLogicFragment implements AdapterView.OnItemSelectedListener, PopupListSelectionFace {

	static final int BACKGROUND = 0;
	static final int BOARD = 1;

	public static final int PREVIEW_IMG_SIZE = 180;
	private static final String THEME_LOAD_TAG = "theme load popup";
	private static final String BACKGROUND_SELECTION = "background selection popup";
	private static final String THEME_ITEM = "theme_item";

	private ImageUpdateListener backgroundUpdateListener;
	private ImageUpdateListener boardUpdateListener;
	private BackgroundImageSaveListener mainBackgroundImgSaveListener;
	private BackgroundImageSaveListener boardImgSaveListener;

	private ImageDownloaderToListener imageDownloader;
	private EnhancedImageDownloader imageLoader;

	private List<SelectionItem> piecesList;
	private List<SelectionItem> boardsList;
	private List<String> colorsList;
	private List<String> soundsList;
	private Spinner boardsSpinner;
	private Spinner piecesSpinner;

	private ThemeItem.Data themeItem;
	private TextView backgroundNameTxt;
	private Spinner soundsSpinner;
	private Spinner colorsSpinner;
	private Spinner coordinatesSpinner;
	private int screenWidth;
	private int height;

	private ThemeItem.Data selectedThemeItem;
	private String backgroundUrl;
	private String boardBackgroundUrl;

	private ProgressImageView boardPreviewImg;
	private PiecePreviewImg piecePreviewImg;
	private ProgressImageView backgroundPreviewImg;
	private TextView rowSampleTitleTxt;
	private TextView loadProgressTxt;
	private TextView taskTitleTxt;

	private int lightColor;
	private int darkColor;
	private PopupBackgroundsFragment backgroundsFragment;
	private PopupCustomViewFragment loadProgressPopupFragment;


	public SettingsThemeCustomizeFragment() {}

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

		setTitle(R.string.customize);

		widgetsInit(view);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_cancel, true);
		getActivityFace().showActionMenu(R.id.menu_accept, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
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
			piecesSpinner.performClick();
		} else if (id == R.id.boardView) {
			boardsSpinner.performClick();
		} else if (id == R.id.backgroundView) {
			if (backgroundsFragment != null) {
				return;
			}
			backgroundsFragment = PopupBackgroundsFragment.createInstance(this, themeItem);
			backgroundsFragment.show(getFragmentManager(), BACKGROUND_SELECTION);
		} else if (id == R.id.soundsView) {
			soundsSpinner.performClick();
		} else if (id == R.id.colorsView) {
			colorsSpinner.performClick();
		} else if (id == R.id.coordinatesView) {
			coordinatesSpinner.performClick();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
		if (adapterView.getId() == R.id.piecesSpinner) {
			for (SelectionItem item : piecesList) {
				item.setChecked(false);
			}

			getAppData().setPiecesId(pos);

			SelectionItem selectionItem = (SelectionItem) adapterView.getItemAtPosition(pos);
			selectionItem.setChecked(true);

		} else if (adapterView.getId() == R.id.coordinatesSpinner){

		} else if (adapterView.getId() == R.id.soundsSpinner){
		} else if (adapterView.getId() == R.id.colorsSpinner){
			if (pos == 0) {
				rowSampleTitleTxt.setTextColor(lightColor);
			} else {
				rowSampleTitleTxt.setTextColor(darkColor);
			}
		} else if (adapterView.getId() == R.id.boardsSpinner){
			for (SelectionItem item : boardsList) {
				item.setChecked(false);
			}

			getAppData().setChessBoardId(pos);

			SelectionItem selectionItem = (SelectionItem) adapterView.getItemAtPosition(pos);
			selectionItem.setChecked(true);
			String boardPreviewUrl;
			if (pos %2 == 0) {
				boardPreviewUrl = "https://dl.dropboxusercontent.com/s/uka6vt1mem1z6ex/space_preview.png?token_hash=AAGMIRkW9U0_rNBHiXVQH2dB1DR1EgFAfqf4zgP1HWSLbQ&dl=1";
			} else {
				boardPreviewUrl = "https://dl.dropboxusercontent.com/s/lfdljkbxorm13t6/ocean_preview.png?token_hash=AAGHRM1q4l1E0DHTQ5cYIqvxpHWVo4c3viIvAXUfiT-2iw&dl=1";
			}

			selectedThemeItem.setBoardBackgroundUrl("https://dl.dropboxusercontent.com/s/ktoi0ixf2qemlij/graffiti.png?token_hash=AAGxS0fXGPCrbBZw5P1OFV25mFj-RTSJxn2nYZTmuTzDbQ&dl=1");
			imageLoader.download(boardPreviewUrl, boardPreviewImg, PREVIEW_IMG_SIZE);
		}

		((BaseAdapter) adapterView.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_cancel:
				// revert changes
				showToast("cancel");
				return true;
			case R.id.menu_accept:
				// apply changes

				// set main background
				installSelectedTheme();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void installSelectedTheme() {
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
		String selectedThemeName = selectedThemeItem.getThemeName();
		getAppData().setThemeName(selectedThemeName);
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
					loadProgressTxt.setText(String.valueOf(progress) + Symbol.PERCENT);
				}
			});
		}
	}

	private class BackgroundImageSaveListener extends AbstractUpdateListener<Bitmap> {

		private int listenerCode;

		public BackgroundImageSaveListener(int listenerCode) {
			super(getActivity(), SettingsThemeCustomizeFragment.this);
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
	private void init() {
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		height = getResources().getDisplayMetrics().heightPixels;

		selectedThemeItem = themeItem;
		Resources resources = getResources();

		imageLoader = new EnhancedImageDownloader(getActivity());
		// Piece and board bitmaps list init
		piecesList = new ArrayList<SelectionItem>(9);
		piecesList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_alpha), getString(R.string.piece_alpha)));
		piecesList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_book), getString(R.string.piece_book)));
		piecesList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_cases), getString(R.string.piece_cases)));
		piecesList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_classic), getString(R.string.piece_classic)));
		piecesList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_club), getString(R.string.piece_club)));
		piecesList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_condal), getString(R.string.piece_condal)));
		piecesList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_maya), getString(R.string.piece_maya)));
		piecesList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_modern), getString(R.string.piece_modern)));
		piecesList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_vintage), getString(R.string.piece_vintage)));

		boardsList = new ArrayList<SelectionItem>(9);
		boardsList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_wood_dark), getString(R.string.board_wooddark)));
		boardsList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_wood_light), getString(R.string.board_woodlight)));
		boardsList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_blue), getString(R.string.board_blue)));
		boardsList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_brown), getString(R.string.board_brown)));
		boardsList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_green), getString(R.string.board_green)));
		boardsList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_grey), getString(R.string.board_grey)));
		boardsList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_marble), getString(R.string.board_marble)));
		boardsList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_red), getString(R.string.board_red)));
		boardsList.add(new SelectionItem(resources.getDrawable(R.drawable.board_sample_tan), getString(R.string.board_tan)));


		soundsList = new ArrayList<String>();
		soundsList.add("Scheme 1");
		soundsList.add("Scheme 2");
		soundsList.add("Scheme 3");
		soundsList.add("Scheme 4");
		soundsList.add("Scheme 5");
		soundsList.add("Scheme 6");

		colorsList = new ArrayList<String>();
		colorsList.add(getString(R.string.light));
		colorsList.add(getString(R.string.dark));

		lightColor = resources.getColor(R.color.white);
		darkColor = resources.getColor(R.color.new_subtitle_dark_grey);

		backgroundUpdateListener = new ImageUpdateListener(BACKGROUND);
		boardUpdateListener = new ImageUpdateListener(BOARD);
		mainBackgroundImgSaveListener = new BackgroundImageSaveListener(BACKGROUND);
		boardImgSaveListener = new BackgroundImageSaveListener(BOARD);

		imageDownloader = new ImageDownloaderToListener(getContext());

	}

	private void widgetsInit(View view) {

		Resources resources = getResources();

		boardPreviewImg = (ProgressImageView) view.findViewById(R.id.boardPreviewImg);
		piecePreviewImg = (PiecePreviewImg) view.findViewById(R.id.piecePreviewImg);

		{ // background params
			backgroundPreviewImg = (ProgressImageView) view.findViewById(R.id.backImg);
			int imageHeight = (int) (screenWidth / 2.9f);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
			backgroundPreviewImg.setLayoutParams(params);

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

		AssetManager assetManager = getActivity().getAssets();
		Bitmap blackPawn = null;
		Bitmap blackKnight = null;
		Bitmap whitePawn = null;
		Bitmap whiteKnight = null;
		try {
			blackPawn = BitmapFactory.decodeStream(assetManager.open("pieces/nature/bp.png"));
			blackKnight = BitmapFactory.decodeStream(assetManager.open("pieces/nature/bn.png"));
			whitePawn = BitmapFactory.decodeStream(assetManager.open("pieces/nature/wp.png"));
			whiteKnight = BitmapFactory.decodeStream(assetManager.open("pieces/nature/wn.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap[] whiteBitmaps = new Bitmap[]{whitePawn, whiteKnight};
		Bitmap[] blackBitmaps = new Bitmap[]{blackPawn, blackKnight};
		Bitmap[][] bitmaps = new Bitmap[][]{blackBitmaps, whiteBitmaps};
		piecePreviewImg.setPiecesBitmaps(bitmaps);

		// Menu sample
		rowSampleTitleTxt = (TextView) view.findViewById(R.id.rowSampleTitleTxt);
		int fontColor = Color.parseColor("#" + themeItem.getFontColor());
		rowSampleTitleTxt.setTextColor(fontColor);

		//spinners
		view.findViewById(R.id.backgroundView).setOnClickListener(this);
		view.findViewById(R.id.piecesView).setOnClickListener(this);
		view.findViewById(R.id.boardView).setOnClickListener(this);
		view.findViewById(R.id.soundsView).setOnClickListener(this);
		view.findViewById(R.id.colorsView).setOnClickListener(this);


		String username = getUsername();

		// Board
		boardsSpinner = (Spinner) view.findViewById(R.id.boardsSpinner);
		boardsSpinner.setAdapter(new SelectionAdapter(getActivity(), boardsList));
		int boardsPosition = preferences.getInt(username + AppConstants.PREF_BOARD_STYLE, 0);
		boardsSpinner.setSelection(boardsPosition);
		boardsSpinner.setOnItemSelectedListener(this);
		boardsList.get(boardsPosition).setChecked(true);

		// Pieces
		piecesSpinner = (Spinner) view.findViewById(R.id.piecesSpinner);
		piecesSpinner.setAdapter(new SelectionAdapter(getActivity(), piecesList));
		int piecesPosition = preferences.getInt(username + AppConstants.PREF_PIECES_SET, 0);
		piecesSpinner.setSelection(piecesPosition);
		piecesList.get(piecesPosition).setChecked(true);
		piecesSpinner.setOnItemSelectedListener(this);

		// Backgrounds
		backgroundNameTxt = (TextView) view.findViewById(R.id.backgroundNameTxt);
		backgroundNameTxt.setText(themeItem.getThemeName());

		imageLoader.download(themeItem.getBackgroundPreviewUrl(), backgroundPreviewImg, screenWidth, screenWidth);

		// Sounds
		soundsSpinner = (Spinner) view.findViewById(R.id.soundsSpinner);
		soundsSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), soundsList));
		int soundsPosition = preferences.getInt(username + AppConstants.PREF_SOUNDS_SET, 0);
		soundsSpinner.setSelection(soundsPosition);
		soundsSpinner.setOnItemSelectedListener(this);

		// Colors
		colorsSpinner = (Spinner) view.findViewById(R.id.colorsSpinner);
		colorsSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), colorsList));
		int colorPosition = preferences.getInt(username + AppConstants.PREF_COLORS_SET, 0);
		colorsSpinner.setSelection(colorPosition);
		colorsSpinner.setOnItemSelectedListener(this);

		// Coordinates
		coordinatesSpinner = (Spinner) view.findViewById(R.id.coordinatesSpinner);
		coordinatesSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), colorsList));
		int coordinatesPosition = preferences.getInt(username + AppConstants.PREF_COORDINATES_SET, 0);
		coordinatesSpinner.setSelection(coordinatesPosition);
		coordinatesSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onValueSelected(int code) {
		selectedThemeItem = backgroundsFragment.getItemByCode(code);

		imageLoader.download(selectedThemeItem.getBackgroundPreviewUrl(), backgroundPreviewImg, screenWidth);
		backgroundNameTxt.setText(selectedThemeItem.getThemeName());

		backgroundsFragment.dismiss();
		backgroundsFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		backgroundsFragment = null;
	}
}
