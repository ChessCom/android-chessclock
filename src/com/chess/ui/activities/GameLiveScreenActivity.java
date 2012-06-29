package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Game;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.ui.core.MainApp;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.utilities.AppUtils;
import com.chess.utilities.ChessComApiParser;

import java.util.ArrayList;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameLiveScreenActivity extends GameBaseActivity implements View.OnClickListener {

	private MenuOptionsDialogListener menuOptionsDialogListener;
	private CharSequence[] menuOptionsItems;

	private int resignOrAbort = R.string.resign;
	private View submitButtonsLay;
//	private boolean chat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardviewlive);

		init();
		widgetsInit();
		onPostCreate();

		// change labels and label's drawables according player color
		// so current player(user) name must be always at the bottom
		String blackPlayerName = lccHolder.getGame(mainApp.getGameId()).getBlackPlayer().getUsername();
		String userName = lccHolder.getUser().getUsername();
		userPlayWhite = !userName.equals(blackPlayerName);
		int opponentIndicator = userPlayWhite? R.drawable.player_indicator_black: R.drawable.player_indicator_white;
		
		whitePlayerLabel.setCompoundDrawablesWithIntrinsicBounds(opponentIndicator,0,0,0);
		gamePanelView.setWhiteIndicator(userPlayWhite);
		// change players colors
		changePlayersLabelColors();
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		submitButtonsLay = findViewById(R.id.submitButtonsLay);
		findViewById(R.id.submit).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);

		if (lccHolder.getWhiteClock() != null && lccHolder.getBlackClock() != null) { // TODO check if needed
			lccHolder.getWhiteClock().paint();
			lccHolder.getBlackClock().paint();
		}

		// hide black dot for right label
		blackPlayerLabel.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
		whitePlayerLabel.setMaxWidth(getResources().getDisplayMetrics().widthPixels);

		gamePanelView.enableAnalysisMode(false);
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

	@Override
	protected void init() {
		super.init();
        mainApp.setGameId(extras.getLong(GameListItem.GAME_ID));
		changeResignTitle();

		menuOptionsItems = new CharSequence[]{
				getString(R.string.settings),
				getString(R.string.reside),
				getString(R.string.drawoffer),
				getString(resignOrAbort),
				getString(R.string.messages)};

		menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
	}

    @Override
    protected void onResume() {
        super.onResume();
        mainApp.setLiveChess(true);

        registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));

        if (mainApp.getGameId() > 0 && lccHolder.getGame(mainApp.getGameId()) != null) {
            game = new GameItem(lccHolder.getGameData(mainApp.getGameId(),
                    lccHolder.getGame(mainApp.getGameId()).getSeq() - 1), true);
            lccHolder.getAndroidStuff().setGameActivity(this);
            if (lccHolder.isActivityPausedMode()) {
                executePausedActivityGameEvents();
                lccHolder.setActivityPausedMode(false);
            }
            lccHolder.updateClockTime(lccHolder.getGame(mainApp.getGameId()));
        }
    }

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(chatMessageReceiver);
	}

	@Override
	protected void onDrawOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			Game game = lccHolder.getGame(mainApp.getGameId());
			LccHolder.LOG.info(AppConstants.REQUEST_DRAW + game);
			lccHolder.getAndroidStuff().runMakeDrawTask(game);
		}
	}

	@Override
	protected void onAbortOffered(int whichButton) {
		if (whichButton == DialogInterface.BUTTON_POSITIVE) {
			Game game = lccHolder.getGame(mainApp.getGameId());

			if (lccHolder.isFairPlayRestriction(mainApp.getGameId())) {
				System.out.println(AppConstants.LCCLOG_RESIGN_GAME_BY_FAIR_PLAY_RESTRICTION + game);
				LccHolder.LOG.info(AppConstants.RESIGN_GAME + game);
				lccHolder.getAndroidStuff().runMakeResignTask(game);
			} else if (lccHolder.isAbortableBySeq(mainApp.getGameId())) {
				LccHolder.LOG.info(AppConstants.LCCLOG_ABORT_GAME + game);
				lccHolder.getAndroidStuff().runAbortGameTask(game);
			} else {
				LccHolder.LOG.info(AppConstants.LCCLOG_RESIGN_GAME + game);
				lccHolder.getAndroidStuff().runMakeResignTask(game);
			}
			finish();
		}
	}

	@Override
	protected void getOnlineGame(long game_id) {
		super.getOnlineGame(game_id);
		update(CALLBACK_GAME_STARTED);
	}

	@Override
	public void update(int code) {  // TODO eliminate
		switch (code) {
			case ERROR_SERVER_RESPONSE:
				if (!MainApp.isTacticsGameMode(getBoardFace()))
					onBackPressed();
				break;
			case INIT_ACTIVITY:
				if (getBoardFace().isInit()) {
					getOnlineGame(mainApp.getGameId());
					getBoardFace().setInit(false);
				} else if (!getBoardFace().isInit() && appService != null && appService.getRepeatableTimer() == null && progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
				break;
			case CALLBACK_REPAINT_UI: {
				if (getBoardFace().isSubmit())
					showSubmitButtonsLay(true);

				whitePlayerLabel.setVisibility(View.VISIBLE);
				blackPlayerLabel.setVisibility(View.VISIBLE);

				if (MainApp.isLiveOrEchessGameMode(getBoardFace()) || MainApp.isFinishedEchessGameMode(getBoardFace())) {
					if (mainApp.getCurrentGame() != null) {
						updatePlayerLabels();
					}
				}

				boardView.addMove2Log(getBoardFace().getMoveListSAN());
				break;
			}
			case CALLBACK_SEND_MOVE: {
				showSubmitButtonsLay(false);

				//String myMove = getBoardFace().MoveSubmit();
				final String move = getBoardFace().convertMoveLive();
				LccHolder.LOG.info("LCC make move: " + move);
				/*try {*/
					lccHolder.makeMove(mainApp.getCurrentGameId(), move);
				/*} catch (IllegalArgumentException e) {
					LccHolder.LOG.info("LCC illegal move: " + move);
					e.printStackTrace();
				}*/
				break;
			}
			case CALLBACK_ECHESS_MOVE_WAS_SENT: // todo: probably this case should be removed from Live
				// move was made
				if (preferences.getInt(AppData.getUserName(getContext())
						+ AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0) == 2) {
					finish();
				} else if (preferences.getInt(preferences
						.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY) + AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0) == 0) {

					int i;
					ArrayList<GameListItem> currentGames = new ArrayList<GameListItem>();
					for (GameListItem gameListItem : mainApp.getGameListItems()) {
						if (gameListItem.type == 1 && gameListItem.values.get(GameListItem.IS_MY_TURN).equals("1")) {
							currentGames.add(gameListItem);
						}
					}
					for (i = 0; i < currentGames.size(); i++) {
						if (currentGames.get(i).getGameId() == mainApp.getCurrentGameId()) {
							if (i + 1 < currentGames.size()) {
								boardView.setBoardFace(new ChessBoard(this));
								getBoardFace().setAnalysis(false);
								getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);

								if (progressDialog != null) {
									progressDialog.dismiss();
									progressDialog = null;
								}

								getOnlineGame(currentGames.get(i + 1).getGameId());
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
				if (getBoardFace().isAnalysis())
					return;

				if (mainApp.getCurrentGame() == null || game == null) {
					return;
				}

				int[] moveFT;
				if (!mainApp.getCurrentGame().equals(game)) {
					if (!mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).equals(game.values.get(AppConstants.MOVE_LIST))) {
						mainApp.setCurrentGame(game);
						String[] moves;

						moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", " ").substring(0).split(" ");

						if (moves.length - getBoardFace().getMovesCount() == 1) {
							moveFT = MoveParser.parseCoordinate(getBoardFace(), moves[moves.length - 1]);

							boolean playSound = lccHolder.getGame(mainApp.getCurrentGameId()).getSeq() == moves.length;

							if (moveFT.length == 4) {
								Move move;
								if (moveFT[3] == 2) {
									move = new Move(moveFT[0], moveFT[1], 0, 2);
								} else {
									move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
								}
								getBoardFace().makeMove(move, playSound);
							} else {
								Move move = new Move(moveFT[0], moveFT[1], 0, 0);
								getBoardFace().makeMove(move, playSound);
							}
							//mainApp.showToast("Move list updated!");
							getBoardFace().setMovesCount(moves.length);
							boardView.invalidate();
							update(CALLBACK_REPAINT_UI);
						}
						return;
					}

                    checkMessages();
				}
				break;

			case CALLBACK_GAME_STARTED:
				getSoundPlayer().playGameStart();

				mainApp.setCurrentGame(new GameItem(lccHolder.getGameData(mainApp.getGameId(), -1), true));
				executePausedActivityGameEvents();
				//lccHolder.setActivityPausedMode(false);
				lccHolder.getWhiteClock().paint();
				lccHolder.getBlackClock().paint();
				/*int time = lccHolder.getGame(mainApp.getGameId()).getGameTimeConfig().getBaseTime() * 100;
											   lccHolder.setWhiteClock(new ChessClock(this, whiteClockView, time));
											   lccHolder.setBlackClock(new ChessClock(this, blackClockView, time));*/

				checkMessages();

				if (mainApp.getCurrentGame().values.get(GameListItem.GAME_TYPE).equals("2"))
					getBoardFace().setChess960(true);


				if (!isUserColorWhite()) {
					getBoardFace().setReside(true);
				}
				String[] moves;


				if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")) {
					moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY).replaceAll("  ", " ").substring(1).split(" ");
					getBoardFace().setMovesCount(moves.length);
				}

				Game game = lccHolder.getGame(mainApp.getGameId());
				if (game != null && game.getSeq() > 0) {
					lccHolder.doReplayMoves(game);
				}

				String FEN = mainApp.getCurrentGame().values.get(GameItem.STARTING_FEN_POSITION);
				if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
					getBoardFace().genCastlePos(FEN);
					MoveParser.fenParse(FEN, getBoardFace());
				}

				update(CALLBACK_REPAINT_UI);
				getBoardFace().takeBack();
				boardView.invalidate();

				playLastMoveAnimation();

				if (MainApp.isLiveOrEchessGameMode(getBoardFace()) && appService != null && appService.getRepeatableTimer() == null) {
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

	private void updatePlayerLabels() {
        if(userPlayWhite){
            whitePlayerLabel.setText(mainApp.getBlackPlayerName());
            gamePanelView.setWhiteTimer(mainApp.getWhitePlayerName().toString());
        }else{
            whitePlayerLabel.setText(mainApp.getWhitePlayerName());
            gamePanelView.setWhiteTimer(mainApp.getBlackPlayerName().toString());
        }
	}

	@Override
	public void switch2Chat() {
		openChatActivity();
	}

	private boolean openChatActivity(){
        preferencesEditor.putString(AppConstants.OPPONENT, mainApp.getCurrentGame().values.get(
                isUserColorWhite() ? AppConstants.BLACK_USERNAME : AppConstants.WHITE_USERNAME));
        preferencesEditor.commit();

        mainApp.getCurrentGame().values.put(GameItem.HAS_NEW_MESSAGE, "0");
        gamePanelView.haveNewMessage(false);

        Intent intent = new Intent(this, ChatLiveActivity.class);
        intent.putExtra(GameListItem.GAME_ID, mainApp.getCurrentGameId() );
        intent.putExtra(GameListItem.TIMESTAMP, mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP));
        startActivity(intent);

        return true;
    }
    

    private void checkMessages(){
        if (game.values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
            mainApp.setCurrentGame(game);
            // show notification instead
            gamePanelView.haveNewMessage(true);
            AppUtils.showNotification(getContext(), StaticData.SYMBOL_EMPTY, mainApp.getGameId(), StaticData.SYMBOL_EMPTY, StaticData.SYMBOL_EMPTY, ChatLiveActivity.class);
        }
    }

	@Override
	public void newGame() {
		startActivity(new Intent(this, LiveNewGameActivity.class));
	}

    @Override
    public void updateAfterMove() {
    }

    @Override
    public void invalidateGameScreen() {
        //TODO To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void showOptions() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {
		submitButtonsLay.setVisibility(show ? View.VISIBLE : View.GONE);
		getBoardFace().setSubmit(show);
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
				// test
//				chat = true;
//				getOnlineGame(mainApp.getGameId());
				openChatActivity();
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
			switch (i) {
				case LIVE_SETTINGS:
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));
					break;
				case LIVE_RESIDE:
					getBoardFace().setReside(!getBoardFace().isReside());
					boardView.invalidate();
					break;
				case LIVE_DRAW_OFFER:
					showDialog(DIALOG_DRAW_OFFER);
					break;
				case LIVE_RESIGN_OR_ABORT:
					showDialog(DIALOG_ABORT_OR_RESIGN);
					break;
				case LIVE_MESSAGES:
//					chat = true;
//					getOnlineGame(mainApp.getGameId());
					openChatActivity();
					break;
			}
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		if (fragment.getTag().equals(LOGOUT_TAG)) {
			lccHolder.logout();
			backToHomeActivity();
		} else if(fragment.getTag().equals(CHALLENGE_TAG)) {
			LccHolder.LOG.info("Accept challenge: " + currentChallenge);
			lccHolder.getAndroidStuff().runAcceptChallengeTask(currentChallenge);
			lccHolder.declineAllChallenges(currentChallenge);
		}
		fragment.getDialog().dismiss();
	}

	protected void changeChatIcon(Menu menu) {
		if (mainApp.getCurrentGame().values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat_nm);
		} else {
			menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat);
		}
	}

	protected void changeResignTitle() {
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
		if (mainApp.getCurrentGame() != null) {
			changeChatIcon(menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void updatePlayerLabels(Game game, int newWhiteRating, int newBlackRating) {
        if(userPlayWhite){
            whitePlayerLabel.setText(game.getBlackPlayer().getUsername() + "(" + newBlackRating + ")");
            gamePanelView.setWhiteTimer(game.getWhitePlayer().getUsername() + "(" + newWhiteRating + ")"); // always at the bottom
        }else{
            whitePlayerLabel.setText(game.getWhitePlayer().getUsername() + "(" + newWhiteRating + ")");
            gamePanelView.setWhiteTimer(game.getBlackPlayer().getUsername() + "(" + newBlackRating + ")");
        }
	}

	@Override
	protected void onGameEndMsgReceived() {
		showSubmitButtonsLay(false);
		gamePanelView.enableAnalysisMode(true);
	}

	private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			gamePanelView.haveNewMessage(true);
		}
	};

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.cancel) {
			showSubmitButtonsLay(false);

			getBoardFace().takeBack();
			getBoardFace().decreaseMovesCount();
			boardView.invalidate();
		} else if (view.getId() == R.id.submit) {
			update(CALLBACK_SEND_MOVE);
		}
	}
}

