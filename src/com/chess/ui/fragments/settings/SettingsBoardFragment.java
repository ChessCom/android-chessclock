package com.chess.ui.fragments.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.SwitchButton;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.SelectionAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.drawables.RatingProgressDrawable;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.04.13
 * Time: 5:23
 */
public class SettingsBoardFragment extends CommonLogicFragment implements SwitchButton.SwitchChangeListener,
		AdapterView.OnItemSelectedListener {

	private List<SelectionItem> piecesList;
	private List<SelectionItem> boardsList;
	private Spinner boardsSpinner;
	private Spinner piecesSpinner;
	private SwitchButton coordinatesSwitch;
	private SwitchButton highlightLastMoveSwitch;
//	private SwitchButton alwaysShowWhiteBottomSwitch;
	private SwitchButton soundsSwitch;
	private SwitchButton showLegalMovesSwitch;
	private TextView strengthValueBtn;
	private int selectedCompLevel;
	private SwitchButton autoFlipSwitch;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_board_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.board_and_pieces);

		widgetsInit(view);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.piecesView) {
			piecesSpinner.performClick();
		} else if (id == R.id.boardView) {
			boardsSpinner.performClick();
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
		} else if (view.getId() == R.id.autoFlipView) {
			autoFlipSwitch.toggle();
		}
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
		} else if (switchButton.getId() == R.id.autoFlipSwitch) {
			getAppData().setAutoFlipFor2Players(autoFlipSwitch.isChecked());
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
		if (adapterView.getId() == R.id.piecesSpinner) {
			for (SelectionItem item : piecesList) {
				item.setChecked(false);
			}

			SelectionItem selectionItem = (SelectionItem) adapterView.getItemAtPosition(pos);
			selectionItem.setChecked(true);

			getAppData().setPiecesId(pos);

			((BaseAdapter) adapterView.getAdapter()).notifyDataSetChanged();
		} else if (adapterView.getId() == R.id.boardsSpinner) {
			for (SelectionItem item : boardsList) {
				item.setChecked(false);
			}

			SelectionItem selectionItem = (SelectionItem) adapterView.getItemAtPosition(pos);
			selectionItem.setChecked(true);

			getAppData().setChessBoardId(pos);

			((BaseAdapter) adapterView.getAdapter()).notifyDataSetChanged();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
	}

	private void widgetsInit(View view) {

		coordinatesSwitch = (SwitchButton) view.findViewById(R.id.coordinatesSwitch);
		highlightLastMoveSwitch = (SwitchButton) view.findViewById(R.id.highlightLastMoveSwitch);
		showLegalMovesSwitch = (SwitchButton) view.findViewById(R.id.showLegalMovesSwitch);
//		alwaysShowWhiteBottomSwitch = (SwitchButton) view.findViewById(R.id.answerShowBottomSwitch);
		soundsSwitch = (SwitchButton) view.findViewById(R.id.soundsSwitch);
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
		autoFlipView.setOnClickListener(this);
		autoFlipSwitch.setSwitchChangeListener(this);

		view.findViewById(R.id.coordinatesView).setOnClickListener(this);
		view.findViewById(R.id.highlightLastMoveView).setOnClickListener(this);
		view.findViewById(R.id.showLegalMovesView).setOnClickListener(this);
//		view.findViewById(R.id.alwaysShowWhiteBottomView).setOnClickListener(this);
		view.findViewById(R.id.soundsView).setOnClickListener(this);

		String username = getAppData().getUsername();

		soundsSwitch.setChecked(AppUtils.getSoundsPlayFlag(getActivity()));
		coordinatesSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_BOARD_COORDINATES, true));
		highlightLastMoveSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_BOARD_HIGHLIGHT_LAST_MOVE, true));
		showLegalMovesSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_SHOW_LEGAL_MOVES, true));
//		alwaysShowWhiteBottomSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_BOARD_SHOW_ANSWER_BOTTOM, true));
		autoFlipSwitch.setChecked(preferences.getBoolean(username + AppConstants.PREF_AUTO_FLIP, true));

		//spinners
		view.findViewById(R.id.piecesView).setOnClickListener(this);
		view.findViewById(R.id.boardView).setOnClickListener(this);

		Resources resources = getResources();
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

		boardsSpinner = (Spinner) view.findViewById(R.id.boardsSpinner);
		boardsSpinner.setAdapter(new SelectionAdapter(getActivity(), boardsList));
		int boardsPosition = preferences.getInt(username + AppConstants.PREF_BOARD_STYLE, 0);
		boardsSpinner.setSelection(boardsPosition);
		boardsSpinner.setOnItemSelectedListener(this);
		boardsList.get(boardsPosition).setChecked(true);

		piecesSpinner = (Spinner) view.findViewById(R.id.piecesSpinner);
		piecesSpinner.setAdapter(new SelectionAdapter(getActivity(), piecesList));
		int piecesPosition = preferences.getInt(username + AppConstants.PREF_PIECES_SET, 0);
		piecesSpinner.setSelection(piecesPosition);
		piecesList.get(piecesPosition).setChecked(true);
		piecesSpinner.setOnItemSelectedListener(this);

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
