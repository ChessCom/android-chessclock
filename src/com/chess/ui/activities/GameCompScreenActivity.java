package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Game;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.IntentConstants;
import com.chess.ui.core.MainApp;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MopubHelper;
import com.chess.utilities.Web;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameCompScreenActivity extends GameBaseActivity implements View.OnClickListener {

	private MenuOptionsDialogListener menuOptionsDialogListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_comp);

		init();
		widgetsInit();
		onPostCreate();
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		if (MainApp.isComputerGameMode(boardView.getBoardFace())
				&& !mainApp.getSharedData().getString(AppConstants.SAVED_COMPUTER_GAME, "").equals("")) { // if load game
			loadSavedGame();

			if (MainApp.isComputerVsHumanBlackGameMode(boardView.getBoardFace()))
				boardView.getBoardFace().setReside(true);

		} else {
			if (MainApp.isComputerVsHumanBlackGameMode(boardView.getBoardFace())) {
				boardView.getBoardFace().setReside(true);
				boardView.invalidate();
				boardView.computerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getUserName() + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
			}
			if (MainApp.isComputerVsComputerGameMode(boardView.getBoardFace())) {
				boardView.computerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getUserName() + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
			}
		}

		gamePanelView.changeGameButton(GamePanelView.B_NEW_GAME_ID, R.drawable.ic_new_game);
		gamePanelView.hideGameButton(GamePanelView.B_CHAT_ID);
		gamePanelView.addControlButton(1, GamePanelView.B_HINT_ID, R.drawable.button_emboss_mid_selector); // add hint button at second position
	}

	@Override
	protected void init() {
		super.init();
		menuOptionsItems = new CharSequence[]{
				getString(R.string.ngwhite),
				getString(R.string.ngblack),
				getString(R.string.emailgame),
				getString(R.string.settings)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
	}

	@Override
	protected void onDrawOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			String Draw = AppConstants.OFFERDRAW;
			if (mainApp.acceptdraw)
				Draw = AppConstants.ACCEPTDRAW;
			String result = Web.Request("http://www." + LccHolder.HOST + AppConstants.API_SUBMIT_ECHESS_ACTION_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + AppConstants.CHESSID_PARAMETER + mainApp.getCurrentGameId()  + AppConstants.COMMAND_PARAMETER + Draw + AppConstants.TIMESTAMP_PARAMETER + mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP), "GET", null, null);
			if (result.contains(AppConstants.SUCCESS)) {
				mainApp.showDialog(coreContext, "", getString(R.string.drawoffered));
			} else if (result.contains(AppConstants.ERROR_PLUS)) {
				mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
			} else {
				//mainApp.showDialog(Game.this, "Error", result);
			}
		}
	}

	@Override
	protected void onAbortOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			String result = Web.Request("http://www." + LccHolder.HOST
					+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID
					+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
					+ AppConstants.CHESSID_PARAMETER + mainApp.getCurrentGameId()
					+ AppConstants.COMMAND_RESIGN__AND_TIMESTAMP_PARAMETER
					+ mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP), "GET", null, null);
			if (result.contains(AppConstants.SUCCESS)) {
				if (MopubHelper.isShowAds(mainApp)) {
					sendBroadcast(new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP)
							.putExtra(AppConstants.MESSAGE, "GAME OVER")
							.putExtra(AppConstants.FINISHABLE, true));
				} else {
					finish();
				}
			} else if (result.contains(AppConstants.ERROR_PLUS)) {
				mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
			} else {
				//mainApp.showDialog(Game.this, "Error", result);
			}
		}
	}


	@Override
	protected void getOnlineGame(long game_id) {
		super.getOnlineGame(game_id);

		if (appService != null) {
			appService.RunSingleTask(CALLBACK_GAME_STARTED,
					"http://www." + LccHolder.HOST + AppConstants.API_V3_GET_GAME_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + game_id,
					null/*progressDialog = MyProgressDialog.show(this, null, getString(R.string.loading), true)*/);
		}
	}

	@Override
	public void update(int code) {
//		int UPDATE_DELAY = 10000;
		int[] moveFT;
		switch (code) {
			case ERROR_SERVER_RESPONSE:
				if (!MainApp.isTacticsGameMode(boardView.getBoardFace()))
					onBackPressed();
				break;
			case INIT_ACTIVITY:

				if (boardView.getBoardFace().isInit() && MainApp.isLiveOrEchessGameMode(boardView.getBoardFace())
						|| MainApp.isFinishedEchessGameMode(boardView.getBoardFace())) {
					//System.out.println("@@@@@@@@ POINT 1 mainApp.getGameId()=" + mainApp.getGameId());
					getOnlineGame(mainApp.getGameId());
					boardView.getBoardFace().setInit(false);
				}
				break;
			case CALLBACK_REPAINT_UI: {
				switch (boardView.getBoardFace().getMode()) {
					case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE: {	//w - human; b - comp
						whitePlayerLabel.setText(getString(R.string.Human));
						blackPlayerLabel.setText(getString(R.string.Computer));
						break;
					}
					case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK: {	//w - comp; b - human
						whitePlayerLabel.setText(getString(R.string.Computer));
						blackPlayerLabel.setText(getString(R.string.Human));
						break;
					}
					case AppConstants.GAME_MODE_HUMAN_VS_HUMAN: {	//w - human; b - human
						whitePlayerLabel.setText(getString(R.string.Human));
						blackPlayerLabel.setText(getString(R.string.Human));
						break;
					}
					case AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER: {	//w - comp; b - comp
						whitePlayerLabel.setText(getString(R.string.Computer));
						blackPlayerLabel.setText(getString(R.string.Computer));
						break;
					}

					default:
						break;
				}

//				movelist.setText(boardView.getBoardFace().getMoveListSAN());

				boardView.addMove2Log(boardView.getBoardFace().getMoveListSAN());
				boardView.invalidate();

				new Handler().post(new Runnable() {
					@Override
					public void run() {
						boardView.requestFocus();
					}
				});
				break;
			}
			case CALLBACK_COMP_MOVE: {
				whitePlayerLabel.setVisibility(View.GONE);
				blackPlayerLabel.setVisibility(View.GONE);
				thinking.setVisibility(View.VISIBLE);
				break;
			}
			case CALLBACK_PLAYER_MOVE: {
				whitePlayerLabel.setVisibility(View.VISIBLE);
				blackPlayerLabel.setVisibility(View.VISIBLE);
				thinking.setVisibility(View.GONE);
				break;
			}
			case CALLBACK_GAME_REFRESH:

				if (mainApp.getCurrentGame() == null || game == null) {
					return;
				}

				if (!mainApp.getCurrentGame().equals(game)) {
					if (!mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).equals(game.values.get(AppConstants.MOVE_LIST))) {
						mainApp.setCurrentGame(game);
						String[] Moves;

						if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")
								|| ((mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView.getBoardFace())))) {

							int beginIndex = (mainApp.isLiveChess()
									&& MainApp.isLiveOrEchessGameMode(boardView.getBoardFace())) ? 0 : 1;

							Moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST)
									.replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(beginIndex).split(" ");

							if (Moves.length - boardView.getBoardFace().getMovesCount() == 1) {
								if (mainApp.isLiveChess()) {
									moveFT = MoveParser.parseCoordinate(boardView.getBoardFace(), Moves[Moves.length - 1]);
								} else {
									moveFT = MoveParser.parse(boardView.getBoardFace(), Moves[Moves.length - 1]);
								}
								boolean playSound = (mainApp.isLiveChess() && lccHolder.getGame(mainApp.getCurrentGameId()).getSeq() == Moves.length)
										|| !mainApp.isLiveChess();

								if (moveFT.length == 4) {
									Move m;
									if (moveFT[3] == 2)
										m = new Move(moveFT[0], moveFT[1], 0, 2);
									else
										m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
									boardView.getBoardFace().makeMove(m, playSound);
								} else {
									Move m = new Move(moveFT[0], moveFT[1], 0, 0);
									boardView.getBoardFace().makeMove(m, playSound);
								}
								//mainApp.showToast("Move list updated!");
								boardView.getBoardFace().setMovesCount(Moves.length);
								boardView.invalidate();
								update(CALLBACK_REPAINT_UI);
							}
						}
						return;
					}

				}
				break;
			case CALLBACK_GAME_STARTED:
				getSoundPlayer().playGameStart();

				mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(response));

				if (mainApp.getCurrentGame().values.get(GameListItem.GAME_TYPE).equals("2"))
					boardView.getBoardFace().setChess960(true);

				if (!isUserColorWhite()) {
					boardView.getBoardFace().setReside(true);
				}
				String[] moves = {};

				if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
					boardView.getBoardFace().setMovesCount(moves.length);
				} else if (!mainApp.isLiveChess()) {
					boardView.getBoardFace().setMovesCount(0);
				}

				Game game = lccHolder.getGame(mainApp.getGameId());
				if (game != null && game.getSeq() > 0) {
					lccHolder.doReplayMoves(game);
				}

				String FEN = mainApp.getCurrentGame().values.get(GameItem.STARTING_FEN_POSITION);
				if (!FEN.equals("")) {
					boardView.getBoardFace().genCastlePos(FEN);
					MoveParser.fenParse(FEN, boardView.getBoardFace());
				}

				int i;
				//System.out.println("@@@@@@@@ POINT 2 boardView.getBoardFaceFace().movesCount=" + boardView.getBoardFaceFace().movesCount);
				//System.out.println("@@@@@@@@ POINT 3 Moves=" + Moves);

				for (i = 0; i < boardView.getBoardFace().getMovesCount(); i++) {
					//System.out.println("@@@@@@@@ POINT 4 i=" + i);
					//System.out.println("================ POINT 5 Moves[i]=" + Moves[i]);
					moveFT = MoveParser.parse(boardView.getBoardFace(), moves[i]);
					if (moveFT.length == 4) {
						Move m;
						if (moveFT[3] == 2) {
							m = new Move(moveFT[0], moveFT[1], 0, 2);
						} else {
							m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
						}
						boardView.getBoardFace().makeMove(m, false);
					} else {
						Move m = new Move(moveFT[0], moveFT[1], 0, 0);
						boardView.getBoardFace().makeMove(m, false);
					}
				}

				update(CALLBACK_REPAINT_UI);
				boardView.getBoardFace().takeBack();
				boardView.invalidate();

				playLastMoveAnimation();
				break;
			default:
				break;
		}
	}

	@Override
	public void showOptions() {
		boardView.stopThinking = true;

		new AlertDialog.Builder(this)
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void showChoosePieceDialog(int col, int row) {
	}

	@Override
	public void switch2Analysis(boolean isAnalysis) {
		if(isAnalysis){
			boardView.stopThinking = true;
		}else {
//			boardView.stopThinking = true;
			boardView.compmoving = false;
//			restoreGame();
		}
		super.switch2Analysis(isAnalysis);
	}

    @Override
	protected void restoreGame(){
		boardView.setBoardFace(new ChessBoard(this));
		boardView.setGameActivityFace(this);
		boardView.getBoardFace().setInit(true);//init = true;
		boardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
		boardView.getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
		loadSavedGame();
	}

	private void loadSavedGame(){
		int i;
		String[] moves = mainApp.getSharedData().getString(AppConstants.SAVED_COMPUTER_GAME, "").split("[|]");
		for (i = 1; i < moves.length; i++) {
			String[] move = moves[i].split(":");
			boardView.getBoardFace().makeMove(new Move(
					Integer.parseInt(move[0]),
					Integer.parseInt(move[1]),
					Integer.parseInt(move[2]),
					Integer.parseInt(move[3])), false);
		}

		playLastMoveAnimation();
	}

	@Override
	public void newGame() {
		boardView.stopThinking = true;
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
			case R.id.menu_new_game: // TODO move to action bar
				newGame();
				break;
			case R.id.menu_options:	 // TODO move to action bar
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

				isMoveNav = true;
				break;
			case R.id.menu_next:
				boardView.moveForward();
				isMoveNav = true;
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener {
		final CharSequence[] items;
		private final int NEW_GAME_WHITE = 0;
		private final int NEW_GAME_BLACK = 1;
		private final int EMAIL_GAME = 2;
		private final int SETTINGS = 3;

		private MenuOptionsDialogListener(CharSequence[] items) {
			this.items = items;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			Toast.makeText(getApplicationContext(), items[i], Toast.LENGTH_SHORT).show();
			switch (i) {
				case NEW_GAME_WHITE: {
					boardView.setBoardFace(new ChessBoard(GameCompScreenActivity.this));
					boardView.getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);
					boardView.getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
					boardView.invalidate();
					update(CALLBACK_REPAINT_UI);
					break;
				}
				case NEW_GAME_BLACK: {
					// TODO encapsulate
					boardView.setBoardFace(new ChessBoard(GameCompScreenActivity.this));
					boardView.getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);
					boardView.getBoardFace().setReside(true);
					boardView.getBoardFace().genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
					boardView.invalidate();
					update(CALLBACK_REPAINT_UI);
					boardView.computerMove(mainApp.strength[mainApp.getSharedData()
							.getInt(mainApp.getUserName()
									+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
					break;
				}
				case EMAIL_GAME: {
//					String moves = movelist.getText().toString();
					String moves = "";
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType(AppConstants.MIME_TYPE_TEXT_PLAIN);
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Chess Game on Android - Chess.com");
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "[Site \"Chess.com Android\"]\n [White \""
							+ mainApp.getUserName() + "\"]\n [White \""
							+ mainApp.getUserName() + "\"]\n [Result \"X-X\"]\n \n \n "
							+ moves + " \n \n Sent from my Android");
					startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail) /*"Send mail..."*/));
					break;
				}

				case SETTINGS: {
					startActivity(new Intent(coreContext, PreferencesScreenActivity.class));
					break;
				}
			}
		}
	}

	@Override
	protected void onGameEndMsgReceived() {
	}


}