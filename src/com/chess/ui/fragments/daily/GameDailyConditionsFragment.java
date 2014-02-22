package com.chess.ui.fragments.daily;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.views.NotationsView;
import com.chess.ui.views.chess_boards.ChessBoardAnalysisView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.01.14
 * Time: 15:49
 */
public class GameDailyConditionsFragment extends GameDailyAnalysisFragment implements AdapterView.OnItemClickListener,
		ItemClickListenerFace {

	private TextView predictOpponentsMoveTxt;
	private ListView listView;
	private ConditionsAdapter conditionsAdapter;
	private List<String> conditionsList;

	public GameDailyConditionsFragment() {
	}

	public static GameDailyConditionsFragment createInstance(long gameId, String username, boolean isFinished) {
		GameDailyConditionsFragment fragment = new GameDailyConditionsFragment();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putString(USERNAME, username);
		arguments.putBoolean(IS_FINISHED, isFinished);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		conditionsList = new ArrayList<String>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.game_conditions_frame, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (isUserMove()) {
			predictOpponentsMoveTxt.setText(R.string.make_your_conditional_move);
		} else {
			predictOpponentsMoveTxt.setText(R.string.predict_opponent_next_move);
		}
	}

	@Override
	public void updateAfterMove() {
		// get notations and add them to the line (in adapter)
	}

	@Override
	protected void updateControls() {
		// do nothing
	}


	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.addNewLineBtn) {
			showToast("footer");

		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		boolean footerAdded = listView.getFooterViewsCount() > 0; // used to check if header added
		int offset = footerAdded ? -1 : 0;

		if (position == parent.getCount()) {
		}
//		conditionsList.add("test");
//		conditionsAdapter
	}

	@Override
	public void invalidateGameScreen() {

//		boardView.updateNotations(getBoardFace().getNotationsArray());
	}

	private boolean isUserMove() {
		userPlayWhite = currentGame.getWhiteUsername().equals(username);

		return currentGame.isMyTurn();
	}

	@Override
	protected void adjustBoardForGame() {
		userPlayWhite = currentGame.getWhiteUsername().equals(username);

		if (userPlayWhite) {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
		} else {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
		}

		getBoardFace().setFinished(false);


		resetInstance();
		BoardFace boardFace = getBoardFace();
		if (currentGame.getGameType() == RestHelper.V_GAME_CHESS_960) {
			boardFace.setChess960(true);
		} else {
			boardFace.setChess960(false);
		}

		if (boardFace.isChess960()) {// we need to setup only position not made moves.
			// Daily games tournaments already include those moves in movesList
			boardFace.setupBoard(currentGame.getStartingFenPosition());
		}

		boardFace.setReside(!userPlayWhite);

		boardFace.checkAndParseMovesList(currentGame.getMoveList());

		boardView.resetValidMoves();

		invalidateGameScreen();
		boardFace.takeBack();

		playLastMoveAnimation();

//		boardFace.setJustInitialized(false);
		boardFace.setAnalysis(true);
	}

	@Override
	protected void widgetsInit(View view) {
		predictOpponentsMoveTxt = (TextView) view.findViewById(R.id.predictOpponentsMoveTxt);
		predictOpponentsMoveTxt.setText(R.string.predict_move_or_add_new_line);

		{ // list of predictions
			View footerView = LayoutInflater.from(getActivity()).inflate(R.layout.add_new_line_view, null, false);
			footerView.findViewById(R.id.addNewLineBtn).setOnClickListener(this);
			conditionsAdapter = new ConditionsAdapter(this, null);
			listView = (ListView) view.findViewById(R.id.listView);
			listView.addFooterView(footerView);
			listView.setAdapter(conditionsAdapter);
		}

		{ // setup board
			boardView = (ChessBoardAnalysisView) view.findViewById(R.id.boardview);
			boardView.setFocusable(true);
			setBoardView(boardView);
			boardView.setGameActivityFace(this);
		}
	}

	private class ConditionalItem {
		private String[] notations;
		private NotationsView.BoardForNotationFace notationFace;
		private int ply;
	}

	private class ConditionsAdapter extends ItemsAdapter<ConditionalItem> {

		public ConditionsAdapter(ItemClickListenerFace clickListenerFace, List<ConditionalItem> itemList) {
			super(clickListenerFace.getMeContext(), itemList);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.condition_list_item, null, false);
			ViewHolder holder = new ViewHolder();

			holder.lineBtn = (Button) view.findViewById(R.id.lineBtn);
			holder.notationsView = (NotationsView) view.findViewById(R.id.notationsView);
			holder.closeBtn = (TextView) view.findViewById(R.id.closeBtn);

			return view;
		}

		@Override
		protected void bindView(ConditionalItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.notationsView.updateNotations(item.notations, item.notationFace, item.ply);
		}

		private class ViewHolder {
			Button lineBtn;
			NotationsView notationsView;
			TextView closeBtn;
		}
	}
}
