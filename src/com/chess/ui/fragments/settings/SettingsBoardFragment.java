package com.chess.ui.fragments.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import com.chess.R;
import com.chess.SwitchButton;
import com.chess.backend.statics.AppConstants;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.SelectionAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.04.13
 * Time: 5:23
 */
public class SettingsBoardFragment extends CommonLogicFragment implements SwitchButton.SwitchChangeListener {

	private ArrayList<SelectionItem> piecesList;
	private ArrayList<SelectionItem> boardsList;
	private Spinner boardsSpinner;
	private Spinner piecesSpinner;
	private SwitchButton coordinatesSwitch;
	private SwitchButton highlightLastMoveSwitch;
	private SwitchButton answerShowBottomSwitch;
	private SwitchButton soundsSwitch;
	private SwitchButton showLegalMovesSwitch;

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
			boardsSpinner.performClick();
		} else if (id == R.id.boardView) {
			boardsSpinner.performClick();
		} else if (id == R.id.coordinatesView) {
			coordinatesSwitch.toggle();
		} else if (id == R.id.highlightLastMoveView) {
			highlightLastMoveSwitch.toggle();
		} else if (id == R.id.showLegalMovesView) {
			showLegalMovesSwitch.toggle();
		} else if (id == R.id.answerShowBottomView) {
			answerShowBottomSwitch.toggle();
		} else if (id == R.id.soundsView) {
			soundsSwitch.toggle();
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
		} else if (switchButton.getId() == R.id.answerShowBottomSwitch) {
			getAppData().setAnswerShowBottom(checked);
		} else if (switchButton.getId() == R.id.soundsSwitch) {
			getAppData().setPlaySounds(getActivity(), checked);
		}
	}

	private AdapterView.OnItemSelectedListener boardSpinnerListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
			for (SelectionItem item : boardsList) {
				item.setChecked(false);
			}

			SelectionItem selectionItem = (SelectionItem) adapterView.getItemAtPosition(pos);
			selectionItem.setChecked(true);

			getAppData().setChessBoardId(pos);

			((BaseAdapter) adapterView.getAdapter()).notifyDataSetChanged();
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};

	private AdapterView.OnItemSelectedListener piecesSpinnerListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
			for (SelectionItem item : piecesList) {
				item.setChecked(false);
			}

			SelectionItem selectionItem = (SelectionItem) adapterView.getItemAtPosition(pos);
			selectionItem.setChecked(true);

			getAppData().setPiecesId(pos);

			((BaseAdapter) adapterView.getAdapter()).notifyDataSetChanged();
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};

	private void widgetsInit(View view) {

		coordinatesSwitch = (SwitchButton) view.findViewById(R.id.coordinatesSwitch);
		highlightLastMoveSwitch = (SwitchButton) view.findViewById(R.id.highlightLastMoveSwitch);
		showLegalMovesSwitch = (SwitchButton) view.findViewById(R.id.showLegalMovesSwitch);
		answerShowBottomSwitch = (SwitchButton) view.findViewById(R.id.answerShowBottomSwitch);
		soundsSwitch = (SwitchButton) view.findViewById(R.id.soundsSwitch);

		coordinatesSwitch.setSwitchChangeListener(this);
		highlightLastMoveSwitch.setSwitchChangeListener(this);
		showLegalMovesSwitch.setSwitchChangeListener(this);
		answerShowBottomSwitch.setSwitchChangeListener(this);
		soundsSwitch.setSwitchChangeListener(this);

		view.findViewById(R.id.coordinatesView).setOnClickListener(this);
		view.findViewById(R.id.highlightLastMoveView).setOnClickListener(this);
		view.findViewById(R.id.showLegalMovesView).setOnClickListener(this);
		view.findViewById(R.id.answerShowBottomView).setOnClickListener(this);
		view.findViewById(R.id.soundsView).setOnClickListener(this);

		String userName = getAppData().getUserName();

		soundsSwitch.setChecked(preferences.getBoolean(userName + AppConstants.PREF_SOUNDS, true));
		coordinatesSwitch.setChecked(preferences.getBoolean(userName + AppConstants.PREF_BOARD_COORDINATES, true));
		highlightLastMoveSwitch.setChecked(preferences.getBoolean(userName + AppConstants.PREF_BOARD_HIGHLIGHT_LAST_MOVE, true));
		showLegalMovesSwitch.setChecked(preferences.getBoolean(userName + AppConstants.PREF_SHOW_LEGAL_MOVES, true));
		answerShowBottomSwitch.setChecked(preferences.getBoolean(userName + AppConstants.PREF_BOARD_SHOW_ANSWER_BOTTOM, true));


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
		int boardsPosition = preferences.getInt(userName + AppConstants.PREF_BOARD_STYLE, 0);
		boardsSpinner.setSelection(boardsPosition);
		boardsSpinner.setOnItemSelectedListener(boardSpinnerListener);
		boardsList.get(boardsPosition).setChecked(true);

		piecesSpinner = (Spinner) view.findViewById(R.id.piecesSpinner);
		piecesSpinner.setAdapter(new SelectionAdapter(getActivity(), piecesList));
		int piecesPosition = preferences.getInt(userName + AppConstants.PREF_PIECES_SET, 0);
		piecesSpinner.setSelection(piecesPosition);
		piecesList.get(piecesPosition).setChecked(true);
		piecesSpinner.setOnItemSelectedListener(piecesSpinnerListener);
	}
}
