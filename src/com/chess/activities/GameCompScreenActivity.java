package com.chess.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.core.IntentConstants;
import com.chess.core.MainApp;
import com.chess.engine.Board2;
import com.chess.engine.Move;
import com.chess.engine.MoveParser2;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MobclixHelper;
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

		if (newBoardView.getBoardFace() == null) {
			newBoardView.setBoardFace(new Board2(this));
			newBoardView.getBoardFace().setInit(true);//init = true;
			newBoardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
			newBoardView.getBoardFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

			if (MainApp.isComputerGameMode(newBoardView.getBoardFace())
					&& !mainApp.getSharedData().getString(AppConstants.SAVED_COMPUTER_GAME, "").equals("")) {
				int i;
				String[] moves = mainApp.getSharedData().getString(AppConstants.SAVED_COMPUTER_GAME, "").split("[|]");
				for (i = 1; i < moves.length; i++) {
					String[] move = moves[i].split(":");
					newBoardView.getBoardFace().makeMove(new Move(
							Integer.parseInt(move[0]),
							Integer.parseInt(move[1]),
							Integer.parseInt(move[2]),
							Integer.parseInt(move[3])), false);
				}
				if (MainApp.isComputerVsHumanBlackGameMode(newBoardView.getBoardFace()))
					newBoardView.getBoardFace().setReside(true);
			} else {
				if (MainApp.isComputerVsHumanBlackGameMode(newBoardView.getBoardFace())) {
					newBoardView.getBoardFace().setReside(true);
					newBoardView.invalidate();
					newBoardView.ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				}
				if (MainApp.isComputerVsComputerGameMode(newBoardView.getBoardFace())) {
					newBoardView.ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				}
				if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) || MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace()))
					mainApp.setGameId(extras.getString(AppConstants.GAME_ID));
			}
		}
	}

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
			String Draw = "OFFERDRAW";
			if (mainApp.acceptdraw)
				Draw = "ACCEPTDRAW";
			String result = Web.Request("http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&chessid=" + mainApp.getCurrentGame().values.get(AppConstants.GAME_ID) + "&command=" + Draw + "&timestamp=" + mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP), "GET", null, null);
			if (result.contains("Success")) {
				mainApp.ShowDialog(coreContext, "", getString(R.string.drawoffered));
			} else if (result.contains("Error+")) {
				mainApp.ShowDialog(coreContext, "Error", result.split("[+]")[1]);
			} else {
				//mainApp.ShowDialog(Game.this, "Error", result);
			}
		}
	}

	@Override
	protected void onAbortOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			String result = Web.Request("http://www." + LccHolder.HOST
					+ "/api/submit_echess_action?id="
					+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
					+ "&chessid=" + mainApp.getCurrentGame().values.get(AppConstants.GAME_ID)
					+ "&command=RESIGN&timestamp="
					+ mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP), "GET", null, null);
			if (result.contains("Success")) {
				if (MobclixHelper.isShowAds(mainApp)) {
					sendBroadcast(new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP)
							.putExtra(AppConstants.MESSAGE, "GAME OVER")
							.putExtra(AppConstants.FINISHABLE, true));
				} else {
					finish();
				}
			} else if (result.contains("Error+")) {
				mainApp.ShowDialog(coreContext, "Error", result.split("[+]")[1]);
			} else {
				//mainApp.ShowDialog(Game.this, "Error", result);
			}
		}
	}


	protected void getOnlineGame(final String game_id) {
		super.getOnlineGame(game_id);

		if (appService != null) {
			appService.RunSingleTask(CALLBACK_GAME_STARTED,
					"http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + game_id,
					null/*progressDialog = MyProgressDialog.show(this, null, getString(R.string.loading), true)*/);
		}
	}

	@Override
	public void update(int code) {
		int UPDATE_DELAY = 10000;
		int[] moveFT = new int[]{};
		switch (code) {
			case ERROR_SERVER_RESPONSE:
				if (!MainApp.isTacticsGameMode(newBoardView.getBoardFace()))
					onBackPressed();
				break;
			case INIT_ACTIVITY:

				if (newBoardView.getBoardFace().isInit() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())
						|| MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace())) {
					//System.out.println("@@@@@@@@ POINT 1 mainApp.getGameId()=" + mainApp.getGameId());
					getOnlineGame(mainApp.getGameId());
					newBoardView.getBoardFace().setInit(false);
				}
				break;
			case CALLBACK_REPAINT_UI: {
				switch (newBoardView.getBoardFace().getMode()) {
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

				movelist.setText(newBoardView.getBoardFace().MoveListSAN());
				newBoardView.invalidate();

				new Handler().post(new Runnable() {
					@Override
					public void run() {
						newBoardView.requestFocus();
					}
				});
				break;
			}
			case 2: {
				whitePlayerLabel.setVisibility(View.GONE);
				blackPlayerLabel.setVisibility(View.GONE);
				thinking.setVisibility(View.VISIBLE);
				break;
			}
			case 3: {
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
					if (!mainApp.getCurrentGame().values.get("move_list").equals(game.values.get("move_list"))) {
						mainApp.setCurrentGame(game);
						String[] Moves = {};

						if (mainApp.getCurrentGame().values.get("move_list").contains("1.")
								|| ((mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())))) {

							int beginIndex = (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) ? 0 : 1;

							Moves = mainApp.getCurrentGame().values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(beginIndex).split(" ");

							if (Moves.length - newBoardView.getBoardFace().getMovesCount() == 1) {
								if (mainApp.isLiveChess()) {
									moveFT = MoveParser2.parseCoordinate(newBoardView.getBoardFace(), Moves[Moves.length - 1]);
								} else {
									moveFT = MoveParser2.Parse(newBoardView.getBoardFace(), Moves[Moves.length - 1]);
								}
								boolean playSound = (mainApp.isLiveChess() && lccHolder.getGame(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID)).getSeq() == Moves.length)
										|| !mainApp.isLiveChess();

								if (moveFT.length == 4) {
									Move m;
									if (moveFT[3] == 2)
										m = new Move(moveFT[0], moveFT[1], 0, 2);
									else
										m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
									newBoardView.getBoardFace().makeMove(m, playSound);
								} else {
									Move m = new Move(moveFT[0], moveFT[1], 0, 0);
									newBoardView.getBoardFace().makeMove(m, playSound);
								}
								//mainApp.ShowMessage("Move list updated!");
								newBoardView.getBoardFace().setMovesCount(Moves.length);
								newBoardView.invalidate();
								update(CALLBACK_REPAINT_UI);
							}
						}
						return;
					}
					if (game.values.get("has_new_message").equals("1")) {
						mainApp.setCurrentGame(game);
						if (!msgShowed) {
							msgShowed = true;
							new AlertDialog.Builder(coreContext)
									.setIcon(android.R.drawable.ic_dialog_alert)
									.setTitle(getString(R.string.you_got_new_msg))
									.setPositiveButton(R.string.browse, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
											chat = true;
											getOnlineGame(mainApp.getGameId());
											msgShowed = false;
										}
									})
									.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
										}
									}).create().show();
						}
						return;
					} else {
						msgShowed = false;
					}
				}
				break;

			case CALLBACK_GAME_STARTED:
				getSoundPlayer().playGameStart();

				mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(response));

				if (chat) {
					if (!isUserColorWhite())
						mainApp.getSharedDataEditor().putString("opponent", mainApp.getCurrentGame()
								.values.get(AppConstants.WHITE_USERNAME));
					else
						mainApp.getSharedDataEditor().putString("opponent", mainApp.getCurrentGame()
								.values.get(AppConstants.BLACK_USERNAME));
					mainApp.getSharedDataEditor().commit();
					mainApp.getCurrentGame().values.put("has_new_message", "0");
					startActivity(new Intent(coreContext, mainApp.isLiveChess() ? ChatLive.class : Chat.class).
							putExtra(AppConstants.GAME_ID, mainApp.getCurrentGame().values.get(AppConstants.GAME_ID)).
							putExtra(AppConstants.TIMESTAMP, mainApp.getCurrentGame().values.get(AppConstants.TIMESTAMP)));
					chat = false;
					return;
				}

				if (mainApp.getCurrentGame().values.get("game_type").equals("2"))
					newBoardView.getBoardFace().setChess960(true);

				if (!isUserColorWhite()) {
					newBoardView.getBoardFace().setReside(true);
				}
				String[] Moves = {};

				if (mainApp.getCurrentGame().values.get("move_list").contains("1.")) {
					Moves = mainApp.getCurrentGame().values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
					newBoardView.getBoardFace().setMovesCount(Moves.length);
				} else if (!mainApp.isLiveChess()) {
					newBoardView.getBoardFace().setMovesCount(0);
				}

				final com.chess.live.client.Game game = lccHolder.getGame(mainApp.getGameId());
				if (game != null && game.getSeq() > 0) {
					lccHolder.doReplayMoves(game);
				}

				String FEN = mainApp.getCurrentGame().values.get("starting_fen_position");
				if (!FEN.equals("")) {
					newBoardView.getBoardFace().genCastlePos(FEN);
					MoveParser2.FenParse(FEN, newBoardView.getBoardFace());
				}

				int i;
				//System.out.println("@@@@@@@@ POINT 2 newBoardView.getBoardFaceFace().movesCount=" + newBoardView.getBoardFaceFace().movesCount);
				//System.out.println("@@@@@@@@ POINT 3 Moves=" + Moves);

				for (i = 0; i < newBoardView.getBoardFace().getMovesCount(); i++) {
					//System.out.println("@@@@@@@@ POINT 4 i=" + i);
					//System.out.println("================ POINT 5 Moves[i]=" + Moves[i]);
					moveFT = MoveParser2.Parse(newBoardView.getBoardFace(), Moves[i]);
					if (moveFT.length == 4) {
						Move m;
						if (moveFT[3] == 2) {
							m = new Move(moveFT[0], moveFT[1], 0, 2);
						} else {
							m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
						}
						newBoardView.getBoardFace().makeMove(m, false);
					} else {
						Move m = new Move(moveFT[0], moveFT[1], 0, 0);
						newBoardView.getBoardFace().makeMove(m, false);
					}
				}

				update(CALLBACK_REPAINT_UI);
				newBoardView.getBoardFace().takeBack();
				newBoardView.invalidate();

				//last move anim
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1300);
							newBoardView.getBoardFace().takeNext();
							update.sendEmptyMessage(0);
						} catch (Exception e) {
						}
					}

					private Handler update = new Handler() {
						@Override
						public void dispatchMessage(Message msg) {
							super.dispatchMessage(msg);
							update(CALLBACK_REPAINT_UI);
							newBoardView.invalidate();
						}
					};
				}).start();
				break;
			default:
				break;
		}
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
			case R.id.menu_new_game:
				newBoardView.stopThinking = true;
				onBackPressed();
				break;
			case R.id.menu_options:
				newBoardView.stopThinking = true;

				new AlertDialog.Builder(this)
						.setTitle(R.string.options)
						.setItems(menuOptionsItems, menuOptionsDialogListener).show();
				break;
			case R.id.menu_reside:
				newBoardView.stopThinking = true;
				if (!newBoardView.compmoving) {
					newBoardView.getBoardFace().setReside(!newBoardView.getBoardFace().isReside());
					if (MainApp.isComputerVsHumanGameMode(newBoardView.getBoardFace())) {
						if (MainApp.isComputerVsHumanWhiteGameMode(newBoardView.getBoardFace())) {
							newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);
						} else if (MainApp.isComputerVsHumanBlackGameMode(newBoardView.getBoardFace())) {
							newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);
						}
						//newBoardView.getBoardFaceFace().mode ^= 1;
						newBoardView.ComputerMove(mainApp.strength[mainApp.getSharedData()
								.getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
										+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
					}
					newBoardView.invalidate();
					update(CALLBACK_REPAINT_UI);
				}
				break;
			case R.id.menu_hint:
				newBoardView.stopThinking = true;
				if (!newBoardView.compmoving) {
					newBoardView.hint = true;
					newBoardView.ComputerMove(mainApp.strength[mainApp.getSharedData()
							.getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
									+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				}
				break;
			case R.id.menu_previous:
				newBoardView.stopThinking = true;
				if (!newBoardView.compmoving) {
					newBoardView.finished = false;
					newBoardView.sel = false;
					newBoardView.getBoardFace().takeBack();
					newBoardView.invalidate();
					update(CALLBACK_REPAINT_UI);
					isMoveNav = true;
				}
				break;
			case R.id.menu_next:
				newBoardView.stopThinking = true;
				if (!newBoardView.compmoving) {
					newBoardView.sel = false;
					newBoardView.getBoardFace().takeNext();
					newBoardView.invalidate();
					update(CALLBACK_REPAINT_UI);
					isMoveNav = true;
				}
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
					newBoardView.setBoardFace(new Board2(GameCompScreenActivity.this));
					newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);
					newBoardView.getBoardFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
					newBoardView.invalidate();
					update(CALLBACK_REPAINT_UI);
					break;
				}
				case NEW_GAME_BLACK: {
					// TODO encapsulate
					newBoardView.setBoardFace(new Board2(GameCompScreenActivity.this));
					newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);
					newBoardView.getBoardFace().setReside(true);
					newBoardView.getBoardFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
					newBoardView.invalidate();
					update(CALLBACK_REPAINT_UI);
					newBoardView.ComputerMove(mainApp.strength[mainApp.getSharedData()
							.getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
									+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
					break;
				}
				case EMAIL_GAME: {
					String moves = movelist.getText().toString();
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Chess Game on Android - Chess.com");
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "[Site \"Chess.com Android\"]\n [White \"" + mainApp.getSharedData().getString(AppConstants.USERNAME, "") + "\"]\n [White \"" + mainApp.getSharedData().getString(AppConstants.USERNAME, "") + "\"]\n [Result \"X-X\"]\n \n \n " + moves + " \n \n Sent from my Android");
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
	protected void onResume() {
		super.onResume();
		if (/*!mainApp.isNetworkChangedNotification() && */extras.containsKey(AppConstants.LIVE_CHESS)) {
			mainApp.setLiveChess(extras.getBoolean(AppConstants.LIVE_CHESS));
			if (!mainApp.isLiveChess()) {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... voids) {
						mainApp.getLccHolder().logout();
						return null;
					}
				}.execute();
			}
		}
	}

	@Override
	protected void onGameEndMsgReceived() {
	}


}