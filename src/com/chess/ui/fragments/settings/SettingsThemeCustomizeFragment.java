package com.chess.ui.fragments.settings;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Debug;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ThemeItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.adapters.SelectionAdapter;
import com.chess.ui.adapters.StringSpinnerAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.PiecePreviewImg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.07.13
 * Time: 21:07
 */
public class SettingsThemeCustomizeFragment extends CommonLogicFragment implements AdapterView.OnItemSelectedListener {

	public static final int PREVIEW_IMG_SIZE = 180;


	private List<SelectionItem> piecesList;
	private List<SelectionItem> boardsList;
	private List<String> colorsList;
	private Spinner boardsSpinner;
	private Spinner piecesSpinner;

	private ThemeItem.Data themeItem;
	private Spinner backgroundsSpinner;
	private Spinner soundsSpinner;
	private Spinner colorsSpinner;
	private Spinner coordinatesSpinner;
	private int screenWidth;
	private float density;
	private SettingsThemeCustomizeFragment.BackgroundsUpdateListener backgroundsUpdateListener;
	private List<SelectionItem> backgroundsList;
	private BackgroundsAdapter backgroundsAdapter;
	private EnhancedImageDownloader imageLoader;
	private ProgressImageView boardPreviewImg;
	private PiecePreviewImg piecePreviewImg;
	private ProgressImageView backgroundPreviewImg;
	private List<String> soundsList;
	private TextView rowSampleTitleTxt;
	private int lightColor;
	private int darkColor;

	public SettingsThemeCustomizeFragment() {}

	public static SettingsThemeCustomizeFragment createInstance(ThemeItem.Data themeItem) {
		SettingsThemeCustomizeFragment fragment = new SettingsThemeCustomizeFragment();
		fragment.themeItem = themeItem;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

	}

	@Override
	public void onStart() {
		super.onStart();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_THEMES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<ThemeItem>(backgroundsUpdateListener).executeTask(loadItem);
	}

	private class BackgroundsUpdateListener extends ChessLoadUpdateListener<ThemeItem> {

		private BackgroundsUpdateListener() {
			super(ThemeItem.class);
		}

		@Override
		public void updateData(ThemeItem returnedObj) {
			List<ThemeItem.Data> backgroundsThemeList = returnedObj.getData();

			for (ThemeItem.Data theme : backgroundsThemeList) {
				SelectionItem selectionItem = new SelectionItem(null, theme.getBackgroundPreviewUrl());
				selectionItem.setCode(theme.getThemeName());
				backgroundsList.add(selectionItem);
			}

			backgroundsAdapter.setItemsList(backgroundsList);
		}
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
			Debug.startMethodTracing("spinner");
			backgroundsSpinner.performClick();
			Debug.stopMethodTracing();
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
		} else if (adapterView.getId() == R.id.backgroundSpinner){
			for (SelectionItem item : backgroundsList) {
				item.setChecked(false);
			}
			SelectionItem selectionItem = (SelectionItem) adapterView.getItemAtPosition(pos);
			imageLoader.download(selectionItem.getText(), backgroundPreviewImg, screenWidth, screenWidth);

			getAppData().setBackgroundSetId(pos);

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

			imageLoader.download(boardPreviewUrl, boardPreviewImg, PREVIEW_IMG_SIZE);
		}


		((BaseAdapter) adapterView.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
	}

	private void init() {
		density = getResources().getDisplayMetrics().density;
		screenWidth = getResources().getDisplayMetrics().widthPixels;
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

		backgroundsList = new ArrayList<SelectionItem>();

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

		backgroundsUpdateListener = new BackgroundsUpdateListener();
	}

	private void widgetsInit(View view) {

		Resources resources = getResources();

		boardPreviewImg = (ProgressImageView) view.findViewById(R.id.boardPreviewImg);
		piecePreviewImg = (PiecePreviewImg) view.findViewById(R.id.piecePreviewImg);
		backgroundPreviewImg = (ProgressImageView) view.findViewById(R.id.backImg);
		int imageHeight = (int) (screenWidth / 2.9f);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
		backgroundPreviewImg.setLayoutParams(params);

		// Change Placeholder
		int backIMgColor = resources.getColor(R.color.upgrade_toggle_button_p);
		backgroundPreviewImg.placeholder = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);

		// Change Image params
		FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(screenWidth, imageHeight);
		backgroundPreviewImg.getImageView().setLayoutParams(imageParams);
		backgroundPreviewImg.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

		// Change ProgressBar params
		FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		progressParams.gravity = Gravity.CENTER;
		backgroundPreviewImg.getProgressBar().setLayoutParams(progressParams);

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


		//spinners
		view.findViewById(R.id.backgroundView).setOnClickListener(this);
		view.findViewById(R.id.piecesView).setOnClickListener(this);
		view.findViewById(R.id.boardView).setOnClickListener(this);
		view.findViewById(R.id.soundsView).setOnClickListener(this);
		view.findViewById(R.id.colorsView).setOnClickListener(this);


		String userName = getUsername();

		// Board
		boardsSpinner = (Spinner) view.findViewById(R.id.boardsSpinner);
		boardsSpinner.setAdapter(new SelectionAdapter(getActivity(), boardsList));
		int boardsPosition = preferences.getInt(userName + AppConstants.PREF_BOARD_STYLE, 0);
		boardsSpinner.setSelection(boardsPosition);
		boardsSpinner.setOnItemSelectedListener(this);
		boardsList.get(boardsPosition).setChecked(true);

		// Pieces
		piecesSpinner = (Spinner) view.findViewById(R.id.piecesSpinner);
		piecesSpinner.setAdapter(new SelectionAdapter(getActivity(), piecesList));
		int piecesPosition = preferences.getInt(userName + AppConstants.PREF_PIECES_SET, 0);
		piecesSpinner.setSelection(piecesPosition);
		piecesList.get(piecesPosition).setChecked(true);
		piecesSpinner.setOnItemSelectedListener(this);

		// Backgrounds
		backgroundsSpinner = (Spinner) view.findViewById(R.id.backgroundSpinner);
		backgroundsAdapter = new BackgroundsAdapter(getActivity(), backgroundsList);
		backgroundsSpinner.setAdapter(backgroundsAdapter);
		int backgroundPosition = preferences.getInt(userName + AppConstants.PREF_BACKGROUND_SET, 0);
		backgroundsSpinner.setSelection(backgroundPosition);
		backgroundsSpinner.setOnItemSelectedListener(this);

		// Sounds
		soundsSpinner = (Spinner) view.findViewById(R.id.soundsSpinner);
		soundsSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), soundsList));
		int soundsPosition = preferences.getInt(userName + AppConstants.PREF_SOUNDS_SET, 0);
		soundsSpinner.setSelection(soundsPosition);
		soundsSpinner.setOnItemSelectedListener(this);

		// Colors
		colorsSpinner = (Spinner) view.findViewById(R.id.colorsSpinner);
		colorsSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), colorsList));
		int colorPosition = preferences.getInt(userName + AppConstants.PREF_COLORS_SET, 0);
		colorsSpinner.setSelection(colorPosition);
		colorsSpinner.setOnItemSelectedListener(this);

		// Coordinates
		coordinatesSpinner = (Spinner) view.findViewById(R.id.coordinatesSpinner);
		coordinatesSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), colorsList));
		int coordinatesPosition = preferences.getInt(userName + AppConstants.PREF_COORDINATES_SET, 0);
		coordinatesSpinner.setSelection(coordinatesPosition);
		coordinatesSpinner.setOnItemSelectedListener(this);
	}

	private class BackgroundsAdapter extends ItemsAdapter<SelectionItem> {

		private final int spinnerWidth;
		private final Bitmap placeHolderBitmap;
		private final float aspectRatio;
		private final FrameLayout.LayoutParams imageParams;
		private final RelativeLayout.LayoutParams relLayoutParams;
		private final LinearLayout.LayoutParams linearLayoutParams;
		private final FrameLayout.LayoutParams progressParams;

		public BackgroundsAdapter(Context context, List<SelectionItem> menuItems) {
			super(context, menuItems);
			spinnerWidth = (int) (180 * density);
			aspectRatio = 1f / 6f;

			int backIMgColor = getResources().getColor(R.color.upgrade_toggle_button_p);
			placeHolderBitmap = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);
			int imageHeight = (int) (spinnerWidth * aspectRatio);
			imageParams = new FrameLayout.LayoutParams(spinnerWidth, imageHeight);
			relLayoutParams = new RelativeLayout.LayoutParams(spinnerWidth, imageHeight);
			linearLayoutParams = new LinearLayout.LayoutParams(spinnerWidth, imageHeight);
			progressParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			progressParams.gravity = Gravity.CENTER;

		}

		@Override
		protected View createView(ViewGroup parent) { // View to display in layout
			ProgressImageView view = new ProgressImageView(getActivity(), spinnerWidth);

			view.setLayoutParams(relLayoutParams);

			// Change Placeholder
			view.placeholder = placeHolderBitmap;

			// Change Image params
			view.getImageView().setLayoutParams(imageParams);
			view.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

			// Change ProgressBar params
			view.getProgressBar().setLayoutParams(progressParams);

			return view;
		}

		@Override
		protected void bindView(SelectionItem item, int pos, View view) {
			imageLoader.download(item.getText(), (ProgressImageView) view, spinnerWidth, spinnerWidth);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) { // view that shows that spinner was selected/pressed
			logTest("convertView = " + convertView + ", pos = " + position);

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.selection_load_img_dropdown, parent, false);
				ViewHolder holder = new ViewHolder();
				holder.image = (ProgressImageView) convertView.findViewById(R.id.image);

				holder.image.setLayoutParams(linearLayoutParams);

				// Change Placeholder
				holder.image.placeholder = placeHolderBitmap;

				// Change Image params
				holder.image.getImageView().setLayoutParams(imageParams);
				holder.image.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

				// Change ProgressBar params

				holder.image.getProgressBar().setLayoutParams(progressParams);

				holder.text = (CheckedTextView) convertView.findViewById(R.id.text);

				convertView.setTag(holder);
			}
			bindDropDownView(itemsList.get(position), convertView);

			return convertView;
		}

		private void bindDropDownView(SelectionItem item, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();

			imageLoader.download(item.getText(), holder.image, spinnerWidth, spinnerWidth);
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

}
