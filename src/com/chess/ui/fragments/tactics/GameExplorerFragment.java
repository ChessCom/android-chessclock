package com.chess.ui.fragments.tactics;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.ExplorerMovesItem;
import com.chess.backend.image_load.ImageReadyListenerLight;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveExplorerMovesListTask;
import com.chess.ui.adapters.ExplorerMovesCursorAdapter;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.interfaces.game_ui.GameCompFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardCompView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsCompView;
import com.chess.utilities.AppUtils;
import org.petero.droidfish.gamelogic.Move;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.09.13
 * Time: 6:42
 */
public class GameExplorerFragment extends GameBaseFragment implements GameCompFace, ItemClickListenerFace {


	private ControlsCompView controlsCompView;
	private NotationView notationsView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private LabelsConfig labelsConfig;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private ChessBoardCompView boardView;

	private ExplorerMovesUpdateListener explorerMovesUpdateListener;
	private SaveExplorerMovesListUpdateListener saveExplorerMovesListUpdateListener;
	private ExplorerMovesCursorUpdateListener explorerMovesCursorUpdateListener;
	private ExplorerMovesCursorAdapter explorerMovesCursorAdapter;

	private String fen = "rnbqkb1r/ppp2ppp/4pn2/3p4/2P5/2N2N2/PP1PPPPP/R1BQKB1R w KQkq"; // TODO: use real fen

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_comp_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.vs_computer);

		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		widgetsInit(view);

//		{ // Engine init
//			engineThinkingPath = (TextView) view.findViewById(R.id.engineThinkingPath);
//			compStrengthArray = getResources().getIntArray(R.array.comp_strength);
//			compTimeLimitArray = getResources().getStringArray(R.array.comp_time_limit);
//			compDepth = getResources().getStringArray(R.array.comp_book_depth);
//		}

	}

	private void init() {
		labelsConfig = new LabelsConfig();
		explorerMovesUpdateListener = new ExplorerMovesUpdateListener();
		saveExplorerMovesListUpdateListener = new SaveExplorerMovesListUpdateListener();
		explorerMovesCursorUpdateListener = new ExplorerMovesCursorUpdateListener();
		explorerMovesCursorAdapter = new ExplorerMovesCursorAdapter(this, null);
	}

	private void widgetsInit(View view) {

		controlsCompView = (ControlsCompView) view.findViewById(R.id.controlsCompView);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		{// set avatars
			Drawable user = new IconDrawable(getActivity(), R.string.ic_profile,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);
			Drawable src = new IconDrawable(getActivity(), R.string.ic_comp_game,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);

			labelsConfig.topAvatar = new BoardAvatarDrawable(getActivity(), src);
			labelsConfig.bottomAvatar = new BoardAvatarDrawable(getActivity(), user);

			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		}
		// hide timeLeft
		topPanelView.showTimeRemain(false);
		bottomPanelView.showTimeRemain(false);

		boardView = (ChessBoardCompView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);

		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsCompView);
		boardView.setNotationsView(notationsView);

		boardView.setGameActivityFace(this);
		setBoardView(boardView);

		boardView.lockBoard(true);

		controlsCompView.enableHintButton(true);

	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update){
			boolean haveSavedData = DbDataManager.haveSavedExplorerMoves(getActivity(), fen);

			if (AppUtils.isNetworkAvailable(getActivity())) {
				updateData(fen);
			} else if(!haveSavedData){
				/*emptyView.setText(R.string.no_network);
				showEmptyView(true);*/
			}

			if (haveSavedData) {
				loadFromDb();
			}
		}
	}

	private void updateData(String fen) {
		LoadItem loadItem = LoadHelper.getExplorerMoves(getUserToken(), fen);
		new RequestJsonTask<ExplorerMovesItem>(explorerMovesUpdateListener).executeTask(loadItem);
	}

	private class ExplorerMovesUpdateListener extends ChessUpdateListener<ExplorerMovesItem> {

		public ExplorerMovesUpdateListener() {
			super(ExplorerMovesItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			//showLoadingView(show);
		}

		@Override
		public void updateData(ExplorerMovesItem returnedObj) {
			super.updateData(returnedObj);

			Log.d("apitest", "" + returnedObj);

			new SaveExplorerMovesListTask(saveExplorerMovesListUpdateListener, returnedObj.getData().getMoves(),
					getContentResolver(), fen).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (resultCode == StaticData.INTERNAL_ERROR) {
				/*emptyView.setText("Internal error occurred");
				showEmptyView(true);*/
			}
		}
	}

	private class SaveExplorerMovesListUpdateListener extends ChessUpdateListener<ExplorerMovesItem.Move> {
		public SaveExplorerMovesListUpdateListener() {
			super(ExplorerMovesItem.Move.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			//showLoadingView(show);
		}

		@Override
		public void updateData(ExplorerMovesItem.Move returnedObj) {
			super.updateData(returnedObj);

			//loadFromDb();
		}
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(explorerMovesCursorUpdateListener,
				DbHelper.getExplorerMovesForFen(fen, DbScheme.Tables.EXPLORER_MOVES),
				getContentResolver()).executeTask();
	}

	private class ExplorerMovesCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			//showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			explorerMovesCursorAdapter.changeCursor(returnedObj);
			//listView.setAdapter(friendsAdapter);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				//emptyView.setText();
			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
				//emptyView.setText();
			}
			//showEmptyView(true);
		}
	}

	@Override
	public Boolean isUserColorWhite() {
		return null;
	}

	@Override
	public Long getGameId() {
		return null;
	}

	@Override
	public void showOptions(View view) {

	}

	@Override
	public void newGame() {

	}

	@Override
	public void switch2Analysis() {

	}

	@Override
	public void updateAfterMove() {

	}

	@Override
	public void invalidateGameScreen() {

	}

	@Override
	public String getWhitePlayerName() {
		return null;
	}

	@Override
	public String getBlackPlayerName() {
		return null;
	}

	@Override
	public boolean currentGameExist() {
		return false;
	}

	@Override
	public ChessBoardComp getBoardFace() {
		return ChessBoardComp.getInstance(this);
	}

	@Override
	public void toggleSides() {

	}

	@Override
	protected void restoreGame() {

	}

	@Override
	public void onPlayerMove() {

	}

	@Override
	public void onCompMove() {

	}

	@Override
	public void updateEngineMove(Move engineMove) {

	}

	@Override
	public void onEngineThinkingInfo(String engineThinkingInfo, String variantStr, ArrayList<ArrayList<Move>> pvMoves, ArrayList<Move> variantMoves, ArrayList<Move> bookMoves) {

	}

	@Override
	public void run(Runnable runnable) {

	}

	private void releaseResources() {
		// todo: implement
	}

	private class ImageUpdateListener extends ImageReadyListenerLight {

		private static final int TOP_AVATAR = 0;
		private static final int BOTTOM_AVATAR = 1;
		private int code;

		private ImageUpdateListener(int code) {
			this.code = code;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}
			switch (code) {
				case TOP_AVATAR:
					labelsConfig.topAvatar = new BoardAvatarDrawable(getContext(), bitmap);

					labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
					topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
					topPanelView.invalidate();
					break;
				case BOTTOM_AVATAR:
					labelsConfig.bottomAvatar = new BoardAvatarDrawable(getContext(), bitmap);

					labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
					bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
					bottomPanelView.invalidate();
					break;
			}
		}
	}
}
