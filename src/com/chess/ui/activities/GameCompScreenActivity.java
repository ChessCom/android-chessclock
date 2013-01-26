package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.Move;
import com.chess.ui.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.GameCompActivityFace;
import com.chess.ui.views.ChessBoardCompView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;

import java.util.Calendar;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameCompScreenActivity extends GameBaseActivity implements GameCompActivityFace {

    private MenuOptionsDialogListener menuOptionsDialogListener;
	private ChessBoardCompView boardView;
	protected TextView thinking;
	private int[] compStrengthArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_comp);

		init();
		widgetsInit();
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		thinking = (TextView) findViewById(R.id.thinking);

		boardView = (ChessBoardCompView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGameControlsView(gameControlsView);

		ChessBoardComp chessBoardComp = ChessBoardComp.getInstance(this);
//		boardView.setBoardFace(chessBoardComp);
		boardView.setGameActivityFace(this);
		setBoardView(boardView);

        getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));

		gameControlsView.turnCompMode();

        if (getBoardFace().isAnalysis()) {
            boardView.enableAnalysis();
            return;
        }

		if (AppData.haveSavedCompGame(this) && chessBoardComp.isJustInitialized()) {
			chessBoardComp.setJustInitialized(false);
			loadSavedGame();
		}
		resideBoardIfCompWhite();
	}

	public void init() {
		menuOptionsItems = new CharSequence[]{
				getString(R.string.new_game_white),
				getString(R.string.new_game_black),
				getString(R.string.email_game),
				getString(R.string.settings)};

		menuOptionsDialogListener = new MenuOptionsDialogListener();

		compStrengthArray = getResources().getIntArray(R.array.comp_strength);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (boardView.isComputerMoving()) { // explicit init
			ChessBoardComp.getInstance(this);
		}

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
	protected void onPause() {
		super.onPause();
		if (AppData.isComputerVsComputerGameMode(getBoardFace()) || AppData.isComputerVsHumanGameMode(getBoardFace())
				&& boardView.isComputerMoving()) { // probably isComputerMoving() is only necessary to check without extra check of game mode
			//boardView.stopThinking();
			boardView.stopComputerMove();
			ChessBoardComp.resetInstance(); // how we restore it after resume?
			// - it was implicitly initialized in onResume - onDraw - isComputerMoving method.
			// we reset the current instance because Comp Search method stains the current board instance when searches a move.
			// one of the alternative solutions is to clone Board instance somehow and put it to Comp Search method in order to hold original board
		}

		if (getBoardFace().getMode() != extras.getInt(AppConstants.GAME_MODE)) {
			Intent intent = getIntent();
			intent.putExtra(AppConstants.GAME_MODE, getBoardFace().getMode());
			getIntent().replaceExtras(intent);
		}
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
		return true;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardComp.getInstance(this);
	}

	@Override
	public void showOptions() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {
	}

    @Override
    public void updateAfterMove() {
    }

	@Override
	public void invalidateGameScreen() {
		switch (getBoardFace().getMode()) {
			case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE: {	//w - human; b - comp
				whitePlayerLabel.setText(AppData.getUserName(this));
				blackPlayerLabel.setText(getString(R.string.computer));
				updatePlayerDots(getBoardFace().isWhiteToMove());
				break;
			}
			case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK: {	//w - comp; b - human
				whitePlayerLabel.setText(getString(R.string.computer));
				blackPlayerLabel.setText(AppData.getUserName(this));
				updatePlayerDots(getBoardFace().isWhiteToMove());
				break;
			}
			case AppConstants.GAME_MODE_HUMAN_VS_HUMAN: {	//w - human; b - human
				whitePlayerLabel.setText(getString(R.string.human));
				blackPlayerLabel.setText(getString(R.string.human));
				updatePlayerDots(getBoardFace().isWhiteToMove());
				break;
			}
			case AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER: {	//w - comp; b - comp
				whitePlayerLabel.setText(getString(R.string.computer));
				blackPlayerLabel.setText(getString(R.string.computer));
				break;
			}
		}

//		boardView.updateNotations(getBoardFace().getMoveListSAN());
		boardView.updateNotations(getBoardFace().getNotationArray());
	}

	@Override
	public void onPlayerMove() {
		whitePlayerLabel.setVisibility(View.VISIBLE);
		blackPlayerLabel.setVisibility(View.VISIBLE);
		thinking.setVisibility(View.GONE);
	}

	@Override
	public void onCompMove() {
		whitePlayerLabel.setVisibility(View.GONE);
		blackPlayerLabel.setVisibility(View.GONE);
		thinking.setVisibility(View.VISIBLE);
	}

    @Override
	protected void restoreGame() {
		ChessBoardComp.resetInstance();
		ChessBoardComp chessBoardComp = ChessBoardComp.getInstance(this);
//		boardView.setBoardFace(chessBoardComp);
		boardView.setGameActivityFace(this);
		getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
		loadSavedGame();
		chessBoardComp.setJustInitialized(false);

		resideBoardIfCompWhite();
	}

	private void loadSavedGame() {

		int i;
		String[] moves = AppData.getCompSavedGame(this).split(RestHelper.SYMBOL_ITEM_SPLIT);
		for (i = 1; i < moves.length; i++) {
			String[] move = moves[i].split(RestHelper.SYMBOL_PARAMS_SPLIT);
			try {
				getBoardFace().makeMove(new Move(
						Integer.parseInt(move[0]),
						Integer.parseInt(move[1]),
						Integer.parseInt(move[2]),
						Integer.parseInt(move[3])), false);
			} catch (Exception e) {
				String debugInfo = "move=" + moves[i] + AppData.getCompSavedGame(this);
				BugSenseHandler.addCrashExtraData("APP_COMP_DEBUG", debugInfo);
				throw new IllegalArgumentException(debugInfo, e);
			}
		}

		playLastMoveAnimation();
	}

	@Override
	public void newGame() {
		onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.game_comp, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_newGame:
				newGame();
				break;
			case R.id.menu_options:
				showOptions();
				break;
			case R.id.menu_reside:
				boardView.flipBoard();
				break;
			case R.id.menu_hint:
				boardView.showHint();
				break;
			case R.id.menu_previous:
				boardView.moveBack();
				break;
			case R.id.menu_next:
				boardView.moveForward();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Boolean isUserColorWhite() {
		return AppData.isComputerVsHumanWhiteGameMode(getBoardFace());
	}

	public Long getGameId() {
		return null;
	}

	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener {
		private final int NEW_GAME_WHITE = 0;
		private final int NEW_GAME_BLACK = 1;
		private final int EMAIL_GAME = 2;
		private final int SETTINGS = 3;

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case NEW_GAME_WHITE: {
					ChessBoardComp.resetInstance();
//					boardView.setBoardFace(getBoardFace());
					boardView.setGameActivityFace(GameCompScreenActivity.this);
					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);
					boardView.invalidate();
					invalidateGameScreen();
					break;
				}
				case NEW_GAME_BLACK: {
					// TODO encapsulate
					ChessBoardComp.resetInstance();
//					boardView.setBoardFace(getBoardFace());
					boardView.setGameActivityFace(GameCompScreenActivity.this);
					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);
					getBoardFace().setReside(true);
					boardView.invalidate();
					invalidateGameScreen();

					computerMove();
					break;
				}
				case EMAIL_GAME: {
					sendPGN();
					break;
				}

				case SETTINGS: {
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));
					break;
				}
			}
		}
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
		if(boardView.isFinished()){// means in check state
			if (getBoardFace().getSide() == ChessBoard.LIGHT) {
				result = BLACK_WINS;
			} else {
				result = WHITE_WINS;
			}
		}
		if(!isUserColorWhite()){
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

        TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
        endGameReasonTxt.setText(message);

        LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
        MopubHelper.showRectangleAd(adViewWrapper, this);
        PopupItem popupItem = new PopupItem();
        popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
        endPopupFragment.show(getSupportFragmentManager(), END_GAME_TAG);

        layout.findViewById(R.id.newGamePopupBtn).setVisibility(View.GONE);
        layout.findViewById(R.id.rematchPopupBtn).setVisibility(View.GONE);
        layout.findViewById(R.id.homePopupBtn).setVisibility(View.GONE);
        Button reviewBtn = (Button) layout.findViewById(R.id.reviewPopupBtn);
        reviewBtn.setText(R.string.ok);
        reviewBtn.setOnClickListener(this);
		if(AppUtils.isNeedToUpgrade(this)) {
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		}
	}

	private void resideBoardIfCompWhite() {
		if (AppData.isComputerVsHumanBlackGameMode(getBoardFace())) {
			getBoardFace().setReside(true);
			boardView.invalidate();
		}
	}

	private void computerMove() {
		boardView.computerMove(compStrengthArray[AppData.getCompStrength(getContext())]);
	}

}