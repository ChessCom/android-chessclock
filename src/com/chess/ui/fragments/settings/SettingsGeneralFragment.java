package com.chess.ui.fragments.settings;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.widgets.SwitchButton;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.drawables.RatingProgressDrawable;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.04.13
 * Time: 5:23
 */
public class SettingsGeneralFragment extends CommonLogicFragment implements SwitchButton.SwitchChangeListener {

	private SwitchButton coordinatesSwitch;
	private SwitchButton highlightLastMoveSwitch;
	//	private SwitchButton alwaysShowWhiteBottomSwitch;
	private SwitchButton soundsSwitch;
	private SwitchButton showLegalMovesSwitch;
	private TextView strengthValueBtn;
	private int selectedCompLevel;
	private SwitchButton autoFlipSwitch;
	private ProgressImageView piecesLineImage;
	private ProgressImageView boardLineImage;
	private SparseArray<String> defaultPiecesNamesMap;
	private SparseArray<String> defaultBoardNamesMap;
	private int previewLineWidth;
	private EnhancedImageDownloader imageLoader;
	private SwitchButton notificationsSwitch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		imageLoader = new EnhancedImageDownloader(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_board_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.general);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		// show selected pieces line preview
		if (getAppData().isUseThemePieces()) {

			// Load line preview image
			String piecesPreviewUrl = getAppData().getThemePiecesPreviewUrl();
			imageLoader.download(piecesPreviewUrl, piecesLineImage, previewLineWidth);
		} else {
			String themePiecesName = getAppData().getThemePiecesName();
			if (themePiecesName.equals(Symbol.EMPTY)) {
				piecesLineImage.setImageDrawable(getResources().getDrawable(R.drawable.pieces_game));
			} else {
				for (int i = 0; i < defaultPiecesNamesMap.size(); i++) {
					int key = defaultPiecesNamesMap.keyAt(i);
					String value = defaultPiecesNamesMap.valueAt(i);
					if (value.equals(themePiecesName)) {
						piecesLineImage.setImageDrawable(getResources().getDrawable(key));
					}
				}
			}
		}

		// show selected board line preview
		if (getAppData().isUseThemeBoard()) {

			// Load line preview image
			String boardPreviewUrl = getAppData().getThemeBoardPreviewUrl();
			imageLoader.download(boardPreviewUrl, boardLineImage, previewLineWidth);
		} else {
			String themeBoardsName = getAppData().getThemeBoardName();
			if (themeBoardsName.equals(Symbol.EMPTY)) {
				boardLineImage.setImageDrawable(getResources().getDrawable(R.drawable.board_sample_wood_dark));
			} else {
				for (int i = 0; i < defaultBoardNamesMap.size(); i++) {
					int key = defaultBoardNamesMap.keyAt(i);
					String value = defaultBoardNamesMap.valueAt(i);
					if (value.equals(themeBoardsName)) {
						boardLineImage.setImageDrawable(getResources().getDrawable(key));
					}
				}
			}
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.piecesView) {
			openFragment(new SettingsThemePiecesFragment());
		} else if (id == R.id.boardView) {
			openFragment(new SettingsThemeBoardsFragment());
		} else if (id == R.id.coordinatesView) {
			coordinatesSwitch.toggle();
		} else if (id == R.id.highlightLastMoveView) {
			highlightLastMoveSwitch.toggle();
		} else if (id == R.id.showLegalMovesView) {
			showLegalMovesSwitch.toggle();
//		} else if (id == R.id.alwaysShowWhiteBottomView) {
//			alwaysShowWhiteBottomSwitch.toggle();
		} else if (id == R.id.soundsView) {
			soundsSwitch.toggle();
		} else if (id == R.id.notificationsView) {
			notificationsSwitch.toggle();
		} else if (view.getId() == R.id.autoFlipView) {
			autoFlipSwitch.toggle();
		}
	}

	protected void openFragment(BasePopupsFragment fragment) {
		getActivityFace().openFragment(fragment);
	}

	@Override
	public void onSwitchChanged(SwitchButton switchButton, boolean checked) {
		if (switchButton.getId() == R.id.coordinatesSwitch) {
			getAppData().setShowCoordinates(checked);
		} else if (switchButton.getId() == R.id.highlightLastMoveSwitch) {
			getAppData().setHighlightLastMove(checked);
		} else if (switchButton.getId() == R.id.showLegalMovesSwitch) {
			getAppData().setShowLegalMoves(checked);
//		} else if (switchButton.getId() == R.id.answerShowBottomSwitch) {
//			getAppData().setAnswerShowBottom(checked);
		} else if (switchButton.getId() == R.id.soundsSwitch) {
			int appSoundMode = checked ? AppData.TRUE : AppData.FALSE;
			getAppData().setPlaySounds(getActivity(), appSoundMode);
		} else if (switchButton.getId() == R.id.notificationsSwitch) {
			getAppData().setNotificationsEnabled(checked);
		} else if (switchButton.getId() == R.id.autoFlipSwitch) {
			getAppData().setAutoFlipFor2Players(autoFlipSwitch.isChecked());
		}
	}

	private void widgetsInit(View view) {

		coordinatesSwitch = (SwitchButton) view.findViewById(R.id.coordinatesSwitch);
		highlightLastMoveSwitch = (SwitchButton) view.findViewById(R.id.highlightLastMoveSwitch);
		showLegalMovesSwitch = (SwitchButton) view.findViewById(R.id.showLegalMovesSwitch);
//		alwaysShowWhiteBottomSwitch = (SwitchButton) view.findViewById(R.id.answerShowBottomSwitch);
		soundsSwitch = (SwitchButton) view.findViewById(R.id.soundsSwitch);
		notificationsSwitch = (SwitchButton) view.findViewById(R.id.notificationsSwitch);
		View autoFlipView = view.findViewById(R.id.autoFlipView);
		autoFlipSwitch = (SwitchButton) view.findViewById(R.id.autoFlipSwitch);

		if (getAppData().getCompGameMode() == AppConstants.GAME_MODE_2_PLAYERS) {
			autoFlipView.setVisibility(View.VISIBLE);
		} else {
			autoFlipView.setVisibility(View.GONE);
		}

		coordinatesSwitch.setSwitchChangeListener(this);
		highlightLastMoveSwitch.setSwitchChangeListener(this);
		showLegalMovesSwitch.setSwitchChangeListener(this);
//		alwaysShowWhiteBottomSwitch.setSwitchChangeListener(this);
		soundsSwitch.setSwitchChangeListener(this);
		notificationsSwitch.setSwitchChangeListener(this);
		autoFlipView.setOnClickListener(this);
		autoFlipSwitch.setSwitchChangeListener(this);

		view.findViewById(R.id.coordinatesView).setOnClickListener(this);
		view.findViewById(R.id.highlightLastMoveView).setOnClickListener(this);
		view.findViewById(R.id.showLegalMovesView).setOnClickListener(this);
//		view.findViewById(R.id.alwaysShowWhiteBottomView).setOnClickListener(this);
		view.findViewById(R.id.soundsView).setOnClickListener(this);
		view.findViewById(R.id.notificationsView).setOnClickListener(this);

		String username = getAppData().getUsername();

		soundsSwitch.setChecked(AppUtils.getSoundsPlayFlag(getActivity()));
		coordinatesSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_BOARD_COORDINATES, true));
		highlightLastMoveSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_BOARD_HIGHLIGHT_LAST_MOVE, true));
		showLegalMovesSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_SHOW_LEGAL_MOVES, true));
//		alwaysShowWhiteBottomSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_BOARD_SHOW_ANSWER_BOTTOM, true));
		autoFlipSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_AUTO_FLIP, true));
		notificationsSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_DAILY_NOTIFICATIONS, true));

		//spinners
		view.findViewById(R.id.piecesView).setOnClickListener(this);
		view.findViewById(R.id.boardView).setOnClickListener(this);

		Resources resources = getResources();
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

		{// Board bitmaps list init
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

		boardLineImage = (ProgressImageView) view.findViewById(R.id.boardLineImage);
		Drawable piecesDrawableExample = resources.getDrawable(R.drawable.pieces_alpha);
		previewLineWidth = piecesDrawableExample.getIntrinsicWidth();
		int imageHeight = piecesDrawableExample.getIntrinsicHeight();

		piecesLineImage = (ProgressImageView) view.findViewById(R.id.piecesLineImage);

		// Change Image params
		RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(previewLineWidth, imageHeight);
		piecesLineImage.getImageView().setLayoutParams(imageParams);
		piecesLineImage.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

		boardLineImage.getImageView().setLayoutParams(imageParams);
		boardLineImage.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

		{ // Comp level
			strengthValueBtn = (TextView) view.findViewById(R.id.compLevelValueBtn);
			selectedCompLevel = getAppData().getCompLevel();

			SeekBar strengthBar = (SeekBar) view.findViewById(R.id.strengthBar);
			strengthBar.setOnSeekBarChangeListener(ratingBarChangeListener);
			strengthBar.setProgressDrawable(new RatingProgressDrawable(getContext(), strengthBar));
			strengthBar.setProgress(selectedCompLevel);
			strengthValueBtn.setText(String.valueOf(selectedCompLevel + 1));

		}
	}

	private SeekBar.OnSeekBarChangeListener ratingBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			selectedCompLevel = progress;
			strengthValueBtn.setText(String.valueOf(progress + 1));

			getAppData().setCompLevel(selectedCompLevel);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};
}
