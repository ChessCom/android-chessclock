package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.AppData;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
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
import com.chess.utilities.Utils;

import java.util.ArrayList;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameOnlineScreenActivity extends GameBaseActivity implements View.OnClickListener {

    private int UPDATE_DELAY = 120000;
    private View submitButtonsLay;


    private MenuOptionsDialogListener menuOptionsDialogListener;
    private AbortGameUpdateListener abortGameUpdateListener;
    private DrawOfferUpdateListener drawOfferUpdateListener;
    private GameStateUpdateListener gameStateUpdateListener;
    private StartGameUpdateListener startGameUpdateListener;
    private GetGameUpdateListener getGameUpdateListener;
    private SendMoveUpdateListener sendMoveUpdateListener;
    private GamesListUpdateListener gamesListUpdateListener;
    private ProgressDialog sendMoveUpdateDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardviewlive);
        init();
        widgetsInit();
        onPostCreate();
    }

    @Override
    protected void widgetsInit() {
		super.widgetsInit();

        submitButtonsLay = findViewById(R.id.submitButtonsLay);
        findViewById(R.id.submit).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);

		gamePanelView.changeGameButton(GamePanelView.B_NEW_GAME_ID, R.drawable.ic_next_game);
    }

    @Override
    protected void init() {
        super.init();
        mainApp.setGameId(extras.getLong(GameListItem.GAME_ID));

        menuOptionsItems = new CharSequence[]{
                getString(R.string.settings),
                getString(R.string.backtogamelist),
                getString(R.string.messages),
                getString(R.string.reside),
                getString(R.string.drawoffer),
                getString(R.string.resignorabort)};

        menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
        abortGameUpdateListener = new AbortGameUpdateListener();
        drawOfferUpdateListener = new DrawOfferUpdateListener();

        gameStateUpdateListener = new GameStateUpdateListener();
        startGameUpdateListener = new StartGameUpdateListener();
        getGameUpdateListener = new GetGameUpdateListener();
        sendMoveUpdateListener = new SendMoveUpdateListener();
        gamesListUpdateListener = new GamesListUpdateListener();

        sendMoveUpdateDialog = new ProgressDialog(this);
        sendMoveUpdateDialog.setMessage(getString(R.string.sendinggameinfo));
        sendMoveUpdateDialog.setIndeterminate(true);
        sendMoveUpdateDialog.setCancelable(false);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (MainApp.isFinishedEchessGameMode(boardView.getBoardFace())) {
            boardView.setBoardFace(new ChessBoard(this));
            boardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
        }

        registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));

        updateGameSate();
		handler.postDelayed(updateGameStateOrder, UPDATE_DELAY);  // run repeatable task
    }

    private Runnable updateGameStateOrder = new Runnable() {
        @Override
        public void run() {
            updateGameSate();
            handler.removeCallbacks(this);
            handler.postDelayed(this, UPDATE_DELAY);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

		handler.removeCallbacks(updateGameStateOrder);
		unregisterReceiver(chatMessageReceiver);
    }


    private void updateGameSate() {

        if (boardView.getBoardFace().isInit() || MainApp.isFinishedEchessGameMode(boardView.getBoardFace())) {
            getOnlineGame(mainApp.getGameId());
            boardView.getBoardFace().setInit(false);
        } else if (!boardView.getBoardFace().isInit()) {

            LoadItem loadItem = new LoadItem();
            loadItem.setLoadPath(RestHelper.GET_GAME_V3);
            loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
            loadItem.addRequestParams(RestHelper.P_GID, String.valueOf(mainApp.getGameId()));

            new GetStringObjTask(gameStateUpdateListener).execute(loadItem);
        }
    }

    @Override
    protected void getOnlineGame(long game_id) {
        super.getOnlineGame(game_id);

        LoadItem loadItem = new LoadItem();
        loadItem.setLoadPath(RestHelper.GET_GAME_V3);
        loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
        loadItem.addRequestParams(RestHelper.P_GID, String.valueOf(game_id));

        new GetStringObjTask(startGameUpdateListener).execute(loadItem);
    }

    private class StartGameUpdateListener extends AbstractUpdateListener<String> { // TODO hide logic to Game Manager class

        public StartGameUpdateListener() {
            super(coreContext);
        }

        @Override
        public void updateData(String returnedObj) {
			Log.d("TEST", "returnedObj " + returnedObj);
			showSubmitButtonsLay(false);
            getSoundPlayer().playGameStart();

            mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(returnedObj));

            if (openChatActivity()) {
                return;
            }

            if (mainApp.getCurrentGame().values.get(GameListItem.GAME_TYPE).equals("2"))
                boardView.getBoardFace().setChess960(true);


            if (!isUserColorWhite()) {
                boardView.getBoardFace().setReside(true);
            }
            String[] moves = {};

            if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")) {
                moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]", AppConstants.SYMBOL_EMPTY)
                        .replaceAll("  ", " ").substring(1).split(" ");
                boardView.getBoardFace().setMovesCount(moves.length);
            } else if (!mainApp.isLiveChess()) {
                boardView.getBoardFace().setMovesCount(0);
            }

            Game game = lccHolder.getGame(mainApp.getGameId());
            if (game != null && game.getSeq() > 0) {
                lccHolder.doReplayMoves(game);
            }

            String FEN = mainApp.getCurrentGame().values.get(GameItem.STARTING_FEN_POSITION);
            if (!FEN.equals(AppConstants.SYMBOL_EMPTY)) {
                boardView.getBoardFace().genCastlePos(FEN);
                MoveParser.fenParse(FEN, boardView.getBoardFace());
            }


            for (int i = 0, cnt = boardView.getBoardFace().getMovesCount(); i < cnt; i++) {
                int[] moveFT = MoveParser.parse(boardView.getBoardFace(), moves[i]);
                if (moveFT.length == 4) {
                    Move move;
                    if (moveFT[3] == 2) {
                        move = new Move(moveFT[0], moveFT[1], 0, 2);
                    } else {
                        move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
                    }
                    boardView.getBoardFace().makeMove(move, false);
                } else {
                    Move move = new Move(moveFT[0], moveFT[1], 0, 0);
                    boardView.getBoardFace().makeMove(move, false);
                }
            }

            invalidateGameScreen();
            boardView.getBoardFace().takeBack();
            boardView.invalidate();

            playLastMoveAnimation();

            updateGameSate();
        }
    }

    private class GameStateUpdateListener extends AbstractUpdateListener<String> { // TODO hide logic to Game Manager class

        public GameStateUpdateListener() {
            super(coreContext);
        }

        @Override
        public void updateData(String returnedObj) {
            if (boardView.getBoardFace().isAnalysis())
                return;

            game = ChessComApiParser.GetGameParseV3(returnedObj);

            if (mainApp.getCurrentGame() == null || game == null) {
                return;
            }

            if (!mainApp.getCurrentGame().equals(game)) {
                // check if moves on board was changed
                if (!mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).equals(game.values.get(AppConstants.MOVE_LIST))) {
                    updateGameBoardMoves();
                }
                checkMessages();
            }
			invalidateGameScreen();
        }
    }

    private void updateGameBoardMoves() { // TODO hide logic to Game Manager class
		mainApp.setCurrentGame(game);
        String[] moves;
        int[] moveFT;

        if (mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).contains("1.")
                || ((mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView.getBoardFace())))) {

            int beginIndex = (mainApp.isLiveChess() && MainApp.isLiveOrEchessGameMode(boardView.getBoardFace())) ? 0 : 1;

            moves = mainApp.getCurrentGame().values.get(AppConstants.MOVE_LIST).replaceAll("[0-9]{1,4}[.]",
                    AppConstants.SYMBOL_EMPTY).replaceAll("  ", " ").substring(beginIndex).split(" ");

            if (moves.length - boardView.getBoardFace().getMovesCount() == 1) {
                /*if (mainApp.isLiveChess()) {
                    moveFT = MoveParser.parseCoordinate(boardView.getBoardFace(), moves[moves.length - 1]);
                } else {*/
                    moveFT = MoveParser.parse(boardView.getBoardFace(), moves[moves.length - 1]);
//                }
                boolean playSound = (mainApp.isLiveChess() && lccHolder.getGame(mainApp.getCurrentGameId()).getSeq() == moves.length)
                        || !mainApp.isLiveChess();

                if (moveFT.length == 4) {
                    Move move;
                    if (moveFT[3] == 2)
                        move = new Move(moveFT[0], moveFT[1], 0, 2);
                    else
                        move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
                    boardView.getBoardFace().makeMove(move, playSound);
                } else {
                    Move move = new Move(moveFT[0], moveFT[1], 0, 0);
                    boardView.getBoardFace().makeMove(move, playSound);
                }

                boardView.getBoardFace().setMovesCount(moves.length);
                boardView.invalidate();
                invalidateGameScreen();
            }
        }
    }

    public void invalidateGameScreen() {  // TODO hide logic to Game Manager class
        if (boardView.getBoardFace().isSubmit())
            showSubmitButtonsLay(true);

        if (mainApp.getCurrentGame() != null) {
            whitePlayerLabel.setText(mainApp.getWhitePlayerName());
            blackPlayerLabel.setText(mainApp.getBlackPlayerName());
		}

		boardView.addMove2Log(boardView.getBoardFace().getMoveListSAN());
        boardView.invalidate();
        boardView.requestFocus();
    }


    @Override
    public void updateAfterMove() { // TODO hide logic to Game Manager class
        showSubmitButtonsLay(false);

        if (mainApp.getCurrentGame() == null) { // if we don't have Game entity
            if (appService.getRepeatableTimer() != null) {
                appService.getRepeatableTimer().cancel();
                appService.setRepeatableTimer(null);
            }

            // get game entity
            LoadItem loadItem = new LoadItem();
            loadItem.setLoadPath(RestHelper.GET_GAME_V3);
            loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
            loadItem.addRequestParams(RestHelper.P_GID, String.valueOf(mainApp.getGameId()));

            new GetStringObjTask(getGameUpdateListener).execute(loadItem);

//                appService.RunSingleTask(CALLBACK_GET_ECHESS_GAME_AND_SEND_MOVE,
//                        "http://www." + LccHolder.HOST + AppConstants.API_V3_GET_GAME_ID +
//                                mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
//                                + "&gid=" + mainApp.getGameId(),
//                        null);
        } else {
            sendMove();
        }
    }

    private void sendMove() { // TODO hide logic to Game Manager class
        LoadItem loadItem = new LoadItem();
        loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
        loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
        loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(mainApp.getCurrentGameId()));
        loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_SUBMIT);
        loadItem.addRequestParams(RestHelper.P_NEWMOVE, boardView.getBoardFace().convertMoveEchess());
        loadItem.addRequestParams(RestHelper.P_TIMESTAMP, mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP));

        new GetStringObjTask(sendMoveUpdateListener).execute(loadItem);
    }


    private class GetGameUpdateListener extends AbstractUpdateListener<String> {
        public GetGameUpdateListener() {
            super(coreContext);
        }

        @Override
        public void updateData(String returnedObj) {
            mainApp.setCurrentGame(ChessComApiParser.GetGameParseV3(returnedObj));
            sendMove();
        }
    }

    private class SendMoveUpdateListener extends AbstractUpdateListener<String> {
        public SendMoveUpdateListener() {
            super(coreContext);
        }

        @Override
        public void showProgress(boolean show) {

            getActionBarHelper().setRefreshActionItemState(show);
            if (GameOnlineScreenActivity.this.isFinishing())
                return;

            if (show) {
                sendMoveUpdateDialog.show();
            } else
                sendMoveUpdateDialog.dismiss();
        }


        @Override
        public void updateData(String returnedObj) {
            moveWasSent();

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(R.id.notification_message);
        }
    }

    private void moveWasSent(){
		showSubmitButtonsLay(false);
        int action = AppData.getInstance().getAfterMoveAction(coreContext);
        if(action == StaticData.AFTER_MOVE_RETURN_TO_GAME_LIST)
            finish();
        else if (action == StaticData.AFTER_MOVE_GO_TO_NEXT_GAME) {
            getGamesList();
        }
    }


    private void getGamesList(){
        LoadItem listLoadItem = new LoadItem();
        listLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
        listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
        listLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ALL_USERS_GAMES);

        new GetStringObjTask(gamesListUpdateListener).execute(listLoadItem);
    }

    private class GamesListUpdateListener extends AbstractUpdateListener<String> {
        public GamesListUpdateListener() {
            super(coreContext);
        }

        @Override
        public void showProgress(boolean show) {
            getActionBarHelper().setRefreshActionItemState(show);
        }

        @Override
        public void updateData(String returnedObj) {
            if (returnedObj.contains(RestHelper.R_SUCCESS)) {

                ArrayList<GameListItem> currentGames = new ArrayList<GameListItem>();

                for (GameListItem gameListItem : ChessComApiParser.getCurrentOnlineGames(returnedObj)) {
                    if (gameListItem.type == GameListItem.LIST_TYPE_CURRENT && gameListItem.values.get(GameListItem.IS_MY_TURN).equals("1")) {
                        currentGames.add(gameListItem);
                    }
                }
				for (GameListItem currentGame : currentGames) {
					if(currentGame.getGameId() != mainApp.getCurrentGameId()){
						showSubmitButtonsLay(false);
						boardView.setBoardFace(new ChessBoard(GameOnlineScreenActivity.this));
						boardView.getBoardFace().setAnalysis(false);
						boardView.getBoardFace().setMode(AppConstants.GAME_MODE_LIVE_OR_ECHESS);
						getOnlineGame(currentGame.getGameId()); // if next game
						return;
					}
				}
                finish();

            } else if (returnedObj.contains(RestHelper.R_ERROR)) {
                mainApp.showDialog(coreContext, AppConstants.ERROR, returnedObj.split("[+]")[1]);
            }
        }
    }

    @Override
    public void update(int code) {
    }

    private boolean openChatActivity() {
        if (!chat)
            return false;

        mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, mainApp.getCurrentGame().values.get(
                isUserColorWhite() ? AppConstants.BLACK_USERNAME : AppConstants.WHITE_USERNAME));
        mainApp.getSharedDataEditor().commit();

        mainApp.getCurrentGame().values.put(GameItem.HAS_NEW_MESSAGE, "0");
        gamePanelView.haveNewMessage(false);

        Intent intent = new Intent(coreContext, ChatActivity.class);
        intent.putExtra(GameListItem.GAME_ID, mainApp.getCurrentGameId());
        intent.putExtra(GameListItem.TIMESTAMP, mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP));
        startActivity(intent);

        chat = false;
        return true;
    }


    private void checkMessages() {
        if (game.values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
            mainApp.setCurrentGame(game);
            // show notification instead
            gamePanelView.haveNewMessage(true);
            Utils.showNotification(coreContext, AppConstants.SYMBOL_EMPTY, mainApp.getGameId(), AppConstants.SYMBOL_EMPTY, AppConstants.SYMBOL_EMPTY, ChatActivity.class);
        }
    }

    @Override
    public void newGame() {
		getGamesList();
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
        boardView.getBoardFace().setSubmit(show);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.game_echess, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_next_game: // TODO move to action bar
                newGame();
                break;
            case R.id.menu_options: // TODO move to action bar
                showOptions();
                break;
            case R.id.menu_analysis:
                boardView.switchAnalysis();
                break;
            case R.id.menu_chat:
                chat = true;
                getOnlineGame(mainApp.getGameId());
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
        private final int ECHESS_SETTINGS = 0;
        private final int ECHESS_BACK_TO_GAME_LIST = 1;
        private final int ECHESS_MESSAGES = 2;
        private final int ECHESS_RESIDE = 3;
        private final int ECHESS_DRAW_OFFER = 4;
        private final int ECHESS_RESIGN_OR_ABORT = 5;

        private MenuOptionsDialogListener(CharSequence[] items) {
            this.items = items;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
                case ECHESS_SETTINGS:
                    startActivity(new Intent(coreContext, PreferencesScreenActivity.class));
                    break;
                case ECHESS_BACK_TO_GAME_LIST:
                    onBackPressed();
                    break;
                case ECHESS_MESSAGES:
                    chat = true;
                    getOnlineGame(mainApp.getGameId());
                    break;
                case ECHESS_RESIDE:
                    boardView.getBoardFace().setReside(!boardView.getBoardFace().isReside());
                    boardView.invalidate();
                    break;
                case ECHESS_DRAW_OFFER:
                    showDialog(DIALOG_DRAW_OFFER);
                    break;
                case ECHESS_RESIGN_OR_ABORT:
                    showDialog(DIALOG_ABORT_OR_RESIGN);
                    break;
            }
        }
    }

    protected void changeChatIcon(Menu menu) {
        if (mainApp.getCurrentGame().values.get(GameItem.HAS_NEW_MESSAGE).equals("1")) {
            menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat_nm);
        } else {
            menu.findItem(R.id.menu_chat).setIcon(R.drawable.chat);
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
    protected void onDrawOffered(int whichButton) {
        if (whichButton == DialogInterface.BUTTON_POSITIVE) {
            String draw = AppConstants.OFFERDRAW;
            if (mainApp.acceptdraw)
                draw = AppConstants.ACCEPTDRAW;               // hide to resthelper


            LoadItem loadItem = new LoadItem();
            loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
            loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));

            loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(mainApp.getCurrentGameId()));
            loadItem.addRequestParams(RestHelper.P_COMMAND, draw);
            loadItem.addRequestParams(RestHelper.P_TIMESTAMP, mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP));

            new GetStringObjTask(drawOfferUpdateListener).execute(loadItem);
        }
    }

    @Override
    protected void onAbortOffered(int whichButton) {
        if (whichButton == DialogInterface.BUTTON_POSITIVE) {

            LoadItem loadItem = new LoadItem();
            loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
            loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));

            loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(mainApp.getCurrentGameId()));
            loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
            loadItem.addRequestParams(RestHelper.P_TIMESTAMP, mainApp.getCurrentGame().values.get(GameListItem.TIMESTAMP));

            new GetStringObjTask(abortGameUpdateListener).execute(loadItem);
        }
    }

    private class AbortGameUpdateListener extends AbstractUpdateListener<String> {
        public AbortGameUpdateListener() {
            super(coreContext);
        }

        @Override
        public void updateData(String returnedObj) {
            if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
                if (MopubHelper.isShowAds(mainApp)) {
                    sendBroadcast(new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP)
                            .putExtra(AppConstants.MESSAGE, "GAME OVER")
                            .putExtra(AppConstants.FINISHABLE, true));
                } else {
                    finish();
                }
            } else if (returnedObj.contains(RestHelper.R_ERROR)) {
                mainApp.showDialog(coreContext, AppConstants.ERROR, returnedObj.split("[+]")[1]);
            }
        }
    }

    private class DrawOfferUpdateListener extends AbstractUpdateListener<String> {
        public DrawOfferUpdateListener() {
            super(coreContext);
        }

        @Override
        public void updateData(String returnedObj) {
            if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
                mainApp.showDialog(coreContext, AppConstants.SYMBOL_EMPTY, getString(R.string.drawoffered));
            } else if (returnedObj.contains(RestHelper.R_ERROR)) {
                mainApp.showDialog(coreContext, AppConstants.ERROR, returnedObj.split("[+]")[1]);
            }
        }
    }





    @Override
    protected void onGameEndMsgReceived() {
        showSubmitButtonsLay(false);
        gamePanelView.haveNewMessage(true);
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

            boardView.getBoardFace().takeBack();
            boardView.getBoardFace().decreaseMovesCount();
            boardView.invalidate();
        } else if (view.getId() == R.id.submit) {
            sendMove();
        }
    }

}
