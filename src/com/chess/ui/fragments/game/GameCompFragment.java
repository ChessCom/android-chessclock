package com.chess.ui.fragments.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.fragments.CompGameSetupFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsBoardFragment;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.GameCompActivityFace;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardCompView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsCompView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.01.13
 * Time: 6:42
 */
public class GameCompFragment extends GameBaseFragment implements GameCompActivityFace, PopupListSelectionFace {

	private static final String OPTION_SELECTION = "option select popup";
	private static final String MODE = "mode";
	private static final String COMP_DELAY = "comp_delay";
	// Quick action ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_EMAIL_GAME = 1;
	private static final int ID_FLIP_BOARD = 2;
	private static final int ID_SETTINGS = 3;

	private ChessBoardCompView boardView;

	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;

	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private ControlsCompView controlsCompView;

	private LabelsConfig labelsConfig;
	private boolean labelsSet;

	private NotationView notationsView;
	private boolean humanBlack;
	private ArrayList<String> optionsList;
	private PopupOptionsMenuFragment optionsSelectFragment;

	public GameCompFragment() {
		CompGameConfig config = new CompGameConfig.Builder().build();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE,  config.getMode());
		bundle.putInt(COMP_DELAY, config.getMode());
		setArguments(bundle);
	}

	public static GameCompFragment newInstance(CompGameConfig config) {
		GameCompFragment frag = new GameCompFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, config.getMode());
		bundle.putInt(COMP_DELAY, config.getCompDelay());
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		labelsConfig = new LabelsConfig();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_boardview_comp, container, false);
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

		init();

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		ChessBoardComp.resetInstance();
		getBoardFace().setMode(getArguments().getInt(MODE));
		if (AppData.haveSavedCompGame(getActivity())) {
			loadSavedGame();
		}
		resideBoardIfCompWhite();
		invalidateGameScreen();

		if (!getBoardFace().isAnalysis()) {

			boolean isComputerMove = (AppData.isComputerVsComputerGameMode(getBoardFace()))
					|| (AppData.isComputerVsHumanWhiteGameMode(getBoardFace()) && !getBoardFace().isWhiteToMove())
					|| (AppData.isComputerVsHumanBlackGameMode(getBoardFace()) && getBoardFace().isWhiteToMove());

			if (isComputerMove) {
				computerMove();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (AppData.isComputerVsComputerGameMode(getBoardFace()) || AppData.isComputerVsHumanGameMode(getBoardFace())
				&& boardView.isComputerMoving()) { // probably isComputerMoving() is only necessary to check without extra check of game mode

			boardView.stopComputerMove();
			ChessBoardComp.resetInstance();
		}

		// there is shouldn't be such logic for fragment
//		if (getBoardFace().getMode() != getArguments().getInt(AppConstants.GAME_MODE)) {
//			Intent intent = getIntent();
//			intent.putExtra(AppConstants.GAME_MODE, getBoardFace().getMode());
//			getIntent().replaceExtras(intent);
//		}
	}

	@Override
	public String getWhitePlayerName() {
		return null;
	}

	@Override
	public String getBlackPlayerName() {  // TODO use correct interfaces
		return null;
	}

	@Override
	public boolean currentGameExist() {
		return true;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardComp.getInstance(this);
	}

	@Override
	public void showOptions(View view) {
		if (optionsSelectFragment != null) {
			return;
		}
		optionsSelectFragment = PopupOptionsMenuFragment.newInstance(this, optionsList);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION);
	}

	@Override
	public void updateAfterMove() {
	}

	@Override
	public void invalidateGameScreen() {
		if (!labelsSet) {
			String userName = AppData.getUserName(getActivity());
			switch (getBoardFace().getMode()) {
				case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE: {    //w - human; b - comp
					humanBlack = false;
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerLabel = getString(R.string.computer);
					labelsConfig.bottomPlayerLabel = userName;
					break;
				}
				case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK: {    //w - comp; b - human
					humanBlack = true;
					labelsConfig.userSide = ChessBoard.BLACK_SIDE;

					labelsConfig.topPlayerLabel = getString(R.string.computer);
					labelsConfig.bottomPlayerLabel = userName;
					break;
				}
				case AppConstants.GAME_MODE_HUMAN_VS_HUMAN: {    //w - human; b - human
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerLabel = userName;
					labelsConfig.bottomPlayerLabel = userName;
					break;
				}
				case AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER: {    //w - comp; b - comp
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerLabel = getString(R.string.computer);
					labelsConfig.bottomPlayerLabel = getString(R.string.computer);
					break;
				}
			}
			labelsSet = true;
		}
		topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
		bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);

		topPanelView.setSide(labelsConfig.getOpponentSide());
		bottomPanelView.setSide(labelsConfig.userSide);

		int topSide;
		int bottomSide;

		if (humanBlack) {
			if (getBoardFace().isReside()) {  // if user on top
				topSide = ChessBoard.NO_SIDE;
				bottomSide = labelsConfig.userSide;
			} else {
				topSide = labelsConfig.getOpponentSide();
				bottomSide = ChessBoard.NO_SIDE;
			}
		} else {
			if (getBoardFace().isReside()) {
				topSide = labelsConfig.getOpponentSide();
				bottomSide = ChessBoard.NO_SIDE;
			} else {
				topSide = ChessBoard.NO_SIDE;
				bottomSide = labelsConfig.userSide;
			}
		}

		if (getBoardFace().getMode() == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
			topSide = ChessBoard.NO_SIDE;
			bottomSide = ChessBoard.NO_SIDE;
		}

		labelsConfig.topAvatar.setSide(topSide);
		labelsConfig.bottomAvatar.setSide(bottomSide);

		topPanelView.setPlayerName(labelsConfig.topPlayerLabel);
		bottomPanelView.setPlayerName(labelsConfig.bottomPlayerLabel);

		if (topSide == ChessBoard.NO_SIDE) {
			topPanelView.showFlags(false);
			bottomPanelView.showFlags(true);
		} else {
			topPanelView.showFlags(true);
			bottomPanelView.showFlags(false);
		}

		boardView.updateNotations(getBoardFace().getNotationArray());
	}

	@Override
	public void onPlayerMove() {
//		controlsCompView.enableGameControls(true);
	}

	@Override
	public void onCompMove() {
//		controlsCompView.enableGameControls(false);
	}

	@Override
	public void toggleSides() {
		if (labelsConfig.userSide == ChessBoard.WHITE_SIDE) {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
		} else {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
		}
		BoardAvatarDrawable tempDrawable = labelsConfig.topAvatar;
		labelsConfig.topAvatar = labelsConfig.bottomAvatar;
		labelsConfig.bottomAvatar = tempDrawable;

		String tempLabel = labelsConfig.topPlayerLabel;
		labelsConfig.topPlayerLabel = labelsConfig.bottomPlayerLabel;
		labelsConfig.bottomPlayerLabel = tempLabel;
	}

	@Override
	protected void restoreGame() {
		ChessBoardComp.resetInstance();
		ChessBoardComp.getInstance(this).setJustInitialized(false);
		boardView.setGameActivityFace(this);
		getBoardFace().setMode(getArguments().getInt(AppConstants.GAME_MODE));
		loadSavedGame();

		resideBoardIfCompWhite();
	}

	private void loadSavedGame() {
		String[] moves = AppData.getCompSavedGame(getActivity()).split(RestHelper.SYMBOL_PARAMS_SPLIT_SLASH);

		BoardFace boardFace = getBoardFace();
		boardFace.setMovesCount(moves.length);

		int i;
		for (i = 1; i < moves.length; i++) {
			String[] move = moves[i].split(RestHelper.SYMBOL_PARAMS_SPLIT);
			try {
				getBoardFace().makeMove(new Move(
						Integer.parseInt(move[0]),
						Integer.parseInt(move[1]),
						Integer.parseInt(move[2]),
						Integer.parseInt(move[3])), false);
			} catch (Exception e) {
				String debugInfo = "move=" + moves[i] + AppData.getCompSavedGame(getActivity());
				BugSenseHandler.addCrashExtraData("APP_COMP_DEBUG", debugInfo);
				throw new IllegalArgumentException(debugInfo, e);
			}
		}

		playLastMoveAnimation();
	}

	@Override
	public void newGame() {
		getActivityFace().openFragment(new CompGameSetupFragment());
	}

	@Override
	public void switch2Analysis() {
		ChessBoardComp.resetInstance();

	}

	@Override
	public Boolean isUserColorWhite() {
		return AppData.isComputerVsHumanWhiteGameMode(getBoardFace());
	}

	@Override
	public Long getGameId() {
		return null;
	}

	private void sendPGN() {
		/*
				[Event "Let's Play!"]
				[Site "Chess.com"]
				[Date "2012.09.13"]
				[White "anotherRoger"]
				[Black "alien_roger"]
				[Result "0-1"]
				[WhiteElo "1221"]
				[BlackElo "1119"]
				[TimeControl "1 in 1 day"]
				[Termination "alien_roger won on time"]
				 */
		CharSequence moves = getBoardFace().getMoveListSAN();
		String whitePlayerName = AppData.getUserName(getContext());
		String blackPlayerName = getString(R.string.comp);
		String result = GAME_GOES;

		if (getBoardFace().isFinished()) {// means in check state
			if (getBoardFace().getSide() == ChessBoard.WHITE_SIDE) {
				result = BLACK_WINS;
			} else {
				result = WHITE_WINS;
			}
		}
		if (!isUserColorWhite()) {
			whitePlayerName = getString(R.string.comp);
			blackPlayerName = AppData.getUserName(getContext());
		}
		String date = datePgnFormat.format(Calendar.getInstance().getTime());

		StringBuilder builder = new StringBuilder();
		builder.append("\n [Site \" Chess.com\"]")
				.append("\n [Date \"").append(date).append("\"]")
				.append("\n [White \"").append(whitePlayerName).append("\"]")
				.append("\n [Black \"").append(blackPlayerName).append("\"]")
				.append("\n [Result \"").append(result).append("\"]");

		builder.append("\n ").append(moves)
				.append("\n \n Sent from my Android");

		sendPGN(builder.toString());
	}

	@Override
	protected void showGameEndPopup(View layout, String message) {
		((TextView) layout.findViewById(R.id.endGameTitleTxt)).setText(message);
		String winner;
		if (message.equals(getString(R.string.black_wins))) {
			if (labelsConfig.userSide == ChessBoard.BLACK_SIDE) {
				winner = labelsConfig.bottomPlayerLabel;
			} else { // labelsConfig.userSide == ChessBoard.WHITE_SIDE
				winner = labelsConfig.topPlayerLabel;
			}

		} else { // message.equals(getString(R.string.white_wins))
			if (labelsConfig.userSide == ChessBoard.WHITE_SIDE) {
				winner = labelsConfig.bottomPlayerLabel;
			} else { // labelsConfig.userSide == ChessBoard.BLACK_SIDE
				winner = labelsConfig.topPlayerLabel;
			}
//			winner = labelsConfig.topPlayerLabel;
		}
		((TextView) layout.findViewById(R.id.endGameReasonTxt)).setText(getString(R.string.won_by_checkmate, winner)); // TODO adjust
		layout.findViewById(R.id.ratingTitleTxt).setVisibility(View.GONE);
		layout.findViewById(R.id.yourRatingTxt).setVisibility(View.GONE);

//		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
//		MopubHelper.showRectangleAd(adViewWrapper, getActivity());
		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.shareBtn).setOnClickListener(this);

		AppData.clearSavedCompGame(getActivity());

		controlsCompView.enableHintButton(false);
//		if (AppUtils.isNeedToUpgrade(getActivity())) {
//			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
//		}
	}

	private void resideBoardIfCompWhite() {
		if (AppData.isComputerVsHumanBlackGameMode(getBoardFace())) {
			getBoardFace().setReside(true);
			boardView.invalidate();
		}
	}

	private void computerMove() {
		boardView.computerMove(AppData.getCompThinkTime(getContext()));
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.newGamePopupBtn) {
			newGame(); // TODO adjust comp game setup screen
			dismissDialogs();
		} else if (view.getId() == R.id.rematchPopupBtn) {
			newGame();
			dismissDialogs();
		}  else if (view.getId() == R.id.shareBtn) {
			ShareItem shareItem = new ShareItem();

			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareItem.composeMessage());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareItem.getTitle());
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
			dismissDialogs();
		}
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_NEW_GAME) {
//			getActivityFace().openFragment(new CompGameSetupFragment()); // TODO
			newGame();
		} else if (code == ID_FLIP_BOARD) {
			boardView.flipBoard();
		} else if (code == ID_EMAIL_GAME) {
			sendPGN();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(new SettingsBoardFragment());
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	private class LabelsConfig {
		BoardAvatarDrawable topAvatar;
		BoardAvatarDrawable bottomAvatar;
		String topPlayerLabel;
		String bottomPlayerLabel;
		int userSide;

		int getOpponentSide() {
			return userSide == ChessBoard.WHITE_SIDE ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;
		}
	}

	private class ImageUpdateListener implements ImageReadyListener {

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

	public class ShareItem {

		public String composeMessage() {
			String vsStr = getString(R.string.vs);
			String space = StaticData.SYMBOL_SPACE;
			return AppData.getUserName(getActivity())+ space + vsStr + space + getString(R.string.vs_computer)
					+ " - " + getString(R.string.chess) + space	+ getString(R.string.via_chesscom);
		}

		public String getTitle() {
			String vsStr = getString(R.string.vs);
			String space = StaticData.SYMBOL_SPACE;
			return "Chess: " + AppData.getUserName(getActivity())+ space + vsStr + space + getString(R.string.vs_computer); // TODO adjust i18n
		}
	}

	private void init() {
		labelsConfig = new LabelsConfig();
		getBoardFace().setMode(getArguments().getInt(MODE));
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
			if (getBoardFace().getMode() == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
				user = src;
			}
			labelsConfig.bottomAvatar = new BoardAvatarDrawable(getActivity(), user);

			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			if (getBoardFace().getMode() != AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
				ImageDownloaderToListener imageDownloader = new ImageDownloaderToListener(getContext());

				String userAvatarUrl = AppData.getUserAvatar(getContext());
				ImageUpdateListener imageUpdateListener = new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR);
				imageDownloader.download(userAvatarUrl, imageUpdateListener, AVATAR_SIZE);
			}
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

		controlsCompView.enableHintButton(true);

		{// options list setup
			optionsList = new ArrayList<String>();
			optionsList.add(getString(R.string.new_game));
			optionsList.add(getString(R.string.email_game));
			optionsList.add(getString(R.string.flip_board));
			optionsList.add(getString(R.string.settings));
		}
	}

}
