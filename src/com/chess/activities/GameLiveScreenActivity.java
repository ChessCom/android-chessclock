package com.chess.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.core.IntentConstants;
import com.chess.core.MainApp;
import com.chess.engine.Board2;
import com.chess.engine.Move;
import com.chess.engine.MoveParser2;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.User;
import com.chess.model.GameListElement;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MobclixHelper;

import java.util.ArrayList;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameLiveScreenActivity extends GameBaseActivity implements View.OnClickListener {


	private final static int CALLBACK_ECHESS_MOVE_WAS_SENT = 8;
	private final static int CALLBACK_SEND_MOVE = 1;

	private MenuOptionsDialogListener menuOptionsDialogListener;

	private CharSequence[] menuOptionsItems;

	private RelativeLayout chatPanel;

	private int resignOrAbort = R.string.resign;
	private View submitButtonsLay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardviewlive2);
		init();
		widgetsInit();
		onPostCreate();
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();
		chatPanel = (RelativeLayout) findViewById(R.id.chatPanel);
		ImageButton chatButton = (ImageButton) findViewById(R.id.chat);
		chatButton.setOnClickListener(this);

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		findViewById(R.id.submit).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
//		findViewById(R.id.prev).setOnClickListener(this);
//		findViewById(R.id.next).setOnClickListener(this);

		if (/*mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(extras.getInt(AppConstants.GAME_MODE))
				&& */lccHolder.getWhiteClock() != null && lccHolder.getBlackClock() != null) {
			whiteClockView.setVisibility(View.VISIBLE);
			blackClockView.setVisibility(View.VISIBLE);
			lccHolder.getWhiteClock().paint();
			lccHolder.getBlackClock().paint();
			final com.chess.live.client.Game game = lccHolder.getGame(new Long(extras.getString(AppConstants.GAME_ID)));
			final User whiteUser = game.getWhitePlayer();
			final User blackUser = game.getBlackPlayer();
			final Boolean isWhite = (!game.isMoveOf(whiteUser) && !game.isMoveOf(blackUser)) ? null : game.isMoveOf(whiteUser);
			lccHolder.setClockDrawPointer(isWhite);
		}

		if (newBoardView.getBoardFace() == null) {
			newBoardView.setBoardFace(new Board2(this));
			newBoardView.getBoardFace().setInit(true);
			newBoardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
			newBoardView.getBoardFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

			if (MainApp.isComputerVsHumanBlackGameMode(newBoardView.getBoardFace())) {
				newBoardView.getBoardFace().setReside(true);
				newBoardView.invalidate();
				newBoardView.computerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
			}
			if (MainApp.isComputerVsComputerGameMode(newBoardView.getBoardFace())) {
				newBoardView.computerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
			}
			if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) || MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace()))
				mainApp.setGameId(extras.getString(AppConstants.GAME_ID));
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_DRAW_OFFER:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.drawoffer)
						.setMessage(getString(R.string.are_you_sure_q))
						.setPositiveButton(getString(R.string.ok), drawOfferDialogListener)
						.setNegativeButton(getString(R.string.cancel), drawOfferDialogListener)
						.create();
			case DIALOG_ABORT_OR_RESIGN:
				return new AlertDialog.Builder(this)
						.setTitle(R.string.abort_resign_game)
						.setMessage(getString(R.string.are_you_sure_q))
						.setPositiveButton(R.string.ok, abortGameDialogListener)
						.setNegativeButton(R.string.cancel, abortGameDialogListener)
						.create();

			default:
				break;
		}
		return super.onCreateDialog(id);
	}

	protected void init() {
		super.init();
		changeResigntTitle();

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(resignOrAbort),
				getString(R.string.messages)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
	}

	@Override
	protected void onDrawOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			final com.chess.live.client.Game game = lccHolder.getGame(mainApp.getGameId());
			LccHolder.LOG.info("Request draw: " + game);
			lccHolder.getAndroid().runMakeDrawTask(game);
		}
	}

	@Override
	protected void onAbortOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			final com.chess.live.client.Game game = lccHolder.getGame(mainApp.getGameId());

			if (lccHolder.isFairPlayRestriction(mainApp.getGameId())) {
				System.out.println("LCCLOG: resign game by fair play restriction: " + game);
				LccHolder.LOG.info("Resign game: " + game);
				lccHolder.getAndroid().runMakeResignTask(game);
			} else if (lccHolder.isAbortableBySeq(mainApp.getGameId())) {
				LccHolder.LOG.info("LCCLOG: abort game: " + game);
				lccHolder.getAndroid().runAbortGameTask(game);
			} else {
				LccHolder.LOG.info("LCCLOG: resign game: " + game);
				lccHolder.getAndroid().runMakeResignTask(game);
			}
			finish();
		}
	}

	@Override
	public void update(int code) {
		switch (code) {
			case ERROR_SERVER_RESPONSE:
				if (!MainApp.isTacticsGameMode(newBoardView.getBoardFace()))
					onBackPressed();
				break;
			case INIT_ACTIVITY:
				if (newBoardView.getBoardFace().isInit() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) || MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace())) {
					//System.out.println("@@@@@@@@ POINT 1 mainApp.getGameId()=" + mainApp.getGameId());
					getOnlineGame(mainApp.getGameId());
					newBoardView.getBoardFace().setInit(false);
				} else if (!newBoardView.getBoardFace().isInit()) {
					if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) && appService != null
							&& appService.getRepeatableTimer() == null) {
						if (progressDialog != null) {
							progressDialog.dismiss();
							progressDialog = null;
						}
					}
				}
				break;
			case CALLBACK_REPAINT_UI: {
				if (newBoardView.getBoardFace().isSubmit())
					showSubmitButtonsLay(true);

				whitePlayerLabel.setVisibility(View.VISIBLE);
				blackPlayerLabel.setVisibility(View.VISIBLE);


				if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) || MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace())) {
					if (mainApp.getCurrentGame() != null) {
						whitePlayerLabel.setText(mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME) + "\n(" + mainApp.getCurrentGame().values.get("white_rating") + ")");
						blackPlayerLabel.setText(mainApp.getCurrentGame().values.get(AppConstants.BLACK_USERNAME) + "\n(" + mainApp.getCurrentGame().values.get("black_rating") + ")");
					}
				}

				newBoardView.addMove2Log(newBoardView.getBoardFace().MoveListSAN());
				newBoardView.invalidate();

				new Handler().post(new Runnable() {
					@Override
					public void run() {
						newBoardView.requestFocus();
					}
				});
				break;
			}
			case CALLBACK_SEND_MOVE: {
				showSubmitButtonsLay(false);


				//String myMove = newBoardView.getBoardFace().MoveSubmit();
				if (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())) {
					final String move = newBoardView.getBoardFace().convertMoveLive();
					LccHolder.LOG.info("LCC make move: " + move);
					try {
						lccHolder.makeMove(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID), move);
					} catch (IllegalArgumentException e) {
						LccHolder.LOG.info("LCC illegal move: " + move);
						e.printStackTrace();
					}
				}
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
			case CALLBACK_ECHESS_MOVE_WAS_SENT:
				// move was made
				if (mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")
						+ AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0) == 2) {
					finish();
				} else if (mainApp.getSharedData().getInt(mainApp.getSharedData()
						.getString(AppConstants.USERNAME, "") + AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0) == 0) {

					int i;
					ArrayList<GameListElement> currentGames = new ArrayList<GameListElement>();
					for (GameListElement gle : mainApp.getGameListItems()) {
						if (gle.type == 1 && gle.values.get("is_my_turn").equals("1")) {
							currentGames.add(gle);
						}
					}
					for (i = 0; i < currentGames.size(); i++) {
						if (currentGames.get(i).values.get(AppConstants.GAME_ID)
								.contains(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID))) {
							if (i + 1 < currentGames.size()) {
								newBoardView.setBoardFace(new Board2(this));
								newBoardView.getBoardFace().setAnalysis(false);
								newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);

								if (progressDialog != null) {
									progressDialog.dismiss();
									progressDialog = null;
								}

								getOnlineGame(currentGames.get(i + 1).values.get(AppConstants.GAME_ID));
								return;
							} else {
								finish();
								return;
							}
						}
					}
					finish();
					return;
				}
				break;
			case CALLBACK_GAME_REFRESH:
				if (newBoardView.getBoardFace().isAnalysis())
					return;
				if (!mainApp.isLiveChess()) {
					game = ChessComApiParser.GetGameParseV3(responseRepeatable);
				}
				//System.out.println("!!!!!!!! mainApp.getCurrentGame() " + mainApp.getCurrentGame());
				//System.out.println("!!!!!!!! game " + game);

				if (mainApp.getCurrentGame() == null || game == null) {
					return;
				}

				int[] moveFT;
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
									if (moveFT[3] == 2) {
										m = new Move(moveFT[0], moveFT[1], 0, 2);
									} else {
										m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
									}
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

				mainApp.setCurrentGame(new com.chess.model.Game(lccHolder.getGameData(mainApp.getGameId(), -1), true));
				executePausedActivityGameEvents();
				//lccHolder.setActivityPausedMode(false);
				lccHolder.getWhiteClock().paint();
				lccHolder.getBlackClock().paint();
				/*int time = lccHolder.getGame(mainApp.getGameId()).getGameTimeConfig().getBaseTime() * 100;
											   lccHolder.setWhiteClock(new ChessClock(this, whiteClockView, time));
											   lccHolder.setBlackClock(new ChessClock(this, blackClockView, time));*/

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
				//System.out.println("@@@@@@@@ POINT 2 newBoardView.getBoardFace().getMovesCount() =" + newBoardView.getBoardFace().getMovesCount() );
				//System.out.println("@@@@@@@@ POINT 3 Moves=" + Moves);

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

				if (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace()) && appService != null && appService.getRepeatableTimer() == null) {
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
				}
				break;

			default:
				break;
		}
	}

	@Override
	public void switch2Chat() {

	}


	@Override
	public void showOptions() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {
		submitButtonsLay.setVisibility(show? View.VISIBLE: View.GONE);
		newBoardView.getBoardFace().setSubmit(show);
	}

	@Override
	public void showChoosePieceDialog(final int col,final int row) {
		new AlertDialog.Builder(this)
		.setTitle("Choose a piece ")
		.setItems(new String[]{"Queen", "Rook", "Bishop", "Knight", "Cancel"},
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (which == 4) {
							newBoardView.invalidate();
							return;
						}
						newBoardView.promote(4 - which, col, row);
			}
		}).setCancelable(false)
		.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.game_live, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_options:
				showOptions();
				break;
			case R.id.menu_chat:
				chat = true;
				getOnlineGame(mainApp.getGameId());
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener {
		private final int LIVE_SETTINGS = 0;
		private final int LIVE_RESIDE = 1;
		private final int LIVE_DRAW_OFFER = 2;
		private final int LIVE_RESIGN_OR_ABORT = 3;
		private final int LIVE_MESSAGES = 4;

		final CharSequence[] items;

		private MenuOptionsDialogListener(CharSequence[] items) {
			this.items = items;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
//			Toast.makeText(getApplicationContext(), items[i], Toast.LENGTH_SHORT).show();
			switch (i) {
				case LIVE_SETTINGS:
					startActivity(new Intent(coreContext, PreferencesScreenActivity.class));
					break;
				case LIVE_RESIDE:
					newBoardView.getBoardFace().setReside(!newBoardView.getBoardFace().isReside());
					newBoardView.invalidate();
					break;
				case LIVE_DRAW_OFFER:
					showDialog(DIALOG_DRAW_OFFER);
					break;
				case LIVE_RESIGN_OR_ABORT:
					showDialog(DIALOG_ABORT_OR_RESIGN);
					break;
				case LIVE_MESSAGES:
					chat = true;
					getOnlineGame(mainApp.getGameId());
					break;
			}
		}
	}

	protected void changeChatIcon(Menu menu) {
		if (mainApp.getCurrentGame().values.get("has_new_message").equals("1")) {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat_nm);
		} else {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat);
		}
	}


	protected void changeResigntTitle() {
		if (lccHolder.isFairPlayRestriction(mainApp.getGameId())) {
			resignOrAbort = R.string.resign;
		} else if (lccHolder.isAbortableBySeq(mainApp.getGameId())) {
			resignOrAbort = R.string.abort;
		} else {
			resignOrAbort = R.string.resign;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mainApp.getCurrentGame() != null && (MainApp.isLiveOrEchessGameMode(newBoardView.getBoardFace())
				|| MainApp.isFinishedEchessGameMode(newBoardView.getBoardFace()))) {
			changeChatIcon(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (extras.containsKey(AppConstants.LIVE_CHESS)) {
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

		registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));

		if (mainApp.isLiveChess() && mainApp.getGameId() != null && !mainApp.getGameId().equals("")
				&& lccHolder.getGame(mainApp.getGameId()) != null) {
			game = new com.chess.model.Game(lccHolder.getGameData(mainApp.getGameId(),
					lccHolder.getGame(mainApp.getGameId()).getSeq() - 1), true);
//			lccHolder.getAndroid().setGameActivity(this); // TODO
			if (lccHolder.isActivityPausedMode()) {
				executePausedActivityGameEvents();
				lccHolder.setActivityPausedMode(false);
			}
			//lccHolder.updateClockTime(lccHolder.getGame(mainApp.getGameId()));
		}
	}


	private BroadcastReceiver gameMoveReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
			game = (com.chess.model.Game) intent.getSerializableExtra(AppConstants.OBJECT);
			update(CALLBACK_GAME_REFRESH);
		}
	};

	protected void onGameEndMsgReceived() {
		showSubmitButtonsLay(false);
		chatPanel.setVisibility(View.GONE);
	}

	private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
			chatPanel.setVisibility(View.VISIBLE);
		}
	};

	private BroadcastReceiver showGameEndPopupReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			if (!MobclixHelper.isShowAds(mainApp)) {
				return;
			}

			final LayoutInflater inflater = (LayoutInflater) coreContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.ad_popup, (ViewGroup) findViewById(R.id.layout_root));
			showGameEndPopup(layout, intent.getExtras().getString(AppConstants.MESSAGE));

			final Button ok = (Button) layout.findViewById(R.id.home);
			ok.setText(getString(R.string.okay));
			ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (adPopup != null) {
						try {
							adPopup.dismiss();
						} catch (Exception e) {
						}
						adPopup = null;
					}
					if (intent.getBooleanExtra(AppConstants.FINISHABLE, false)) {
						finish();
					}
				}
			});
			ok.setVisibility(View.VISIBLE);
		}
	};



	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.chat) {
			chat = true;
			getOnlineGame(mainApp.getGameId());
			chatPanel.setVisibility(View.GONE);
//		} else if (view.getId() == R.id.prev) {
//			newBoardView.finished = false;
//			newBoardView.sel = false;
//			newBoardView.getBoardFace().takeBack();
//			newBoardView.invalidate();
//			update(CALLBACK_REPAINT_UI);
//			isMoveNav = true;
//		} else if (view.getId() == R.id.next) {
//			newBoardView.getBoardFace().takeNext();
//			newBoardView.invalidate();
//			update(CALLBACK_REPAINT_UI);
//			isMoveNav = true;
		} else if (view.getId() == R.id.cancel) {
			showSubmitButtonsLay(false);

			newBoardView.getBoardFace().takeBack();
			newBoardView.getBoardFace().decreaseMovesCount();
			newBoardView.invalidate();
		} else if (view.getId() == R.id.submit) {
			update(CALLBACK_SEND_MOVE);
		} else if (view.getId() == R.id.newGame) {
			startActivity(new Intent(this, OnlineNewGameActivity.class));
		}
	}
}

