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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.core.*;
import com.chess.engine.Board2;
import com.chess.engine.Move;
import com.chess.engine.MoveParser2;
import com.chess.lcc.android.GameEvent;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.*;
import com.chess.views.NewBoardView;
import com.mobclix.android.sdk.MobclixIABRectangleMAdView;

import java.util.Timer;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameCompScreenActivity extends CoreActivityActionBar implements View.OnClickListener {

    private final static int DIALOG_DRAW_OFFER = 4;
    private final static int DIALOG_ABORT_OR_RESIGN = 5;

    private final static int CALLBACK_GAME_STARTED = 10;
    private final static int CALLBACK_REPAINT_UI = 0;
    private final static int CALLBACK_GAME_REFRESH = 9;

    public NewBoardView newBoardView;
    private TextView whitePlayerLabel;
    private TextView blackPlayerLabel;
    private TextView thinking;
    private TextView movelist;
    private Timer onlineGameUpdate = null;
    private boolean msgShowed;
    private boolean isMoveNav;
    private boolean chat;

    private com.chess.model.Game game;

    private TextView whiteClockView;
    private TextView blackClockView;

    protected AlertDialog adPopup;
    private TextView endOfGameMessage;
    private LinearLayout adViewWrapper;

    private DrawOfferDialogListener drawOfferDialogListener;
    private AbortGameDialogListener abortGameDialogListener;
    private MenuOptionsDialogListener menuOptionsDialogListener;

    private CharSequence[] menuOptionsItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (CommonUtils.needFullScreen(this)) {
            setFullscreen();
            savedInstanceState = new Bundle();
            savedInstanceState.putBoolean(AppConstants.SMALL_SCREEN, true);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.boardview_comp);

        init();

        whitePlayerLabel = (TextView) findViewById(R.id.white);
        blackPlayerLabel = (TextView) findViewById(R.id.black);
        thinking = (TextView) findViewById(R.id.thinking);
        movelist = (TextView) findViewById(R.id.movelist);

        whiteClockView = (TextView) findViewById(R.id.whiteClockView);
        blackClockView = (TextView) findViewById(R.id.blackClockView);


        endOfGameMessage = (TextView) findViewById(R.id.endOfGameMessage);

        newBoardView = (NewBoardView) findViewById(R.id.boardview);
        newBoardView.setFocusable(true);
        newBoardView.setBoardFace((Board2) getLastNonConfigurationInstance());

        lccHolder = mainApp.getLccHolder();

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

        if (MobclixHelper.isShowAds(mainApp) /*&& getRectangleAdview() == null*/
                && mainApp.getTabHost() != null && !mainApp.getTabHost().getCurrentTabTag().equals("tab4")) {
            setRectangleAdview(new MobclixIABRectangleMAdView(this));
            getRectangleAdview().setRefreshTime(-1);
            getRectangleAdview().addMobclixAdViewListener(new MobclixAdViewListenerImpl(true, mainApp));
            mainApp.setForceRectangleAd(false);
        }

        Update(CALLBACK_REPAINT_UI);
    }


    private class DrawOfferDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
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
    }

    private class AbortGameDialogListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
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

    private void init() {
        menuOptionsItems = new CharSequence[]{
                getString(R.string.ngwhite),
                getString(R.string.ngblack),
                getString(R.string.emailgame),
                getString(R.string.settings)};

        drawOfferDialogListener = new DrawOfferDialogListener();
        abortGameDialogListener = new AbortGameDialogListener();
        menuOptionsDialogListener = new MenuOptionsDialogListener(menuOptionsItems);
    }


    private void GetOnlineGame(final String game_id) {
        if (appService != null && appService.getRepeatableTimer() != null) {
            appService.getRepeatableTimer().cancel();
            appService.setRepeatableTimer(null);
        }
        mainApp.setGameId(game_id);

        if (appService != null) {
            appService.RunSingleTask(CALLBACK_GAME_STARTED,
                    "http://www." + LccHolder.HOST + "/api/v3/get_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&gid=" + game_id,
                    null/*progressDialog = MyProgressDialog.show(this, null, getString(R.string.loading), true)*/);
        }
    }

    public void LoadPrev(int code) {
        if (newBoardView.getBoardFace() != null && MainApp.isTacticsGameMode(newBoardView.getBoardFace())) {
            newBoardView.getBoardFace().setTacticCanceled(true);
            onBackPressed();
        } else {
            onBackPressed();
        }
    }

    @Override
    public void Update(int code) {
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
                    GetOnlineGame(mainApp.getGameId());
                    newBoardView.getBoardFace().setInit(false);
                }
                break;
            case CALLBACK_REPAINT_UI: {
                switch (newBoardView.getBoardFace().getMode()) {
                    case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE: {    //w - human; b - comp
                        whitePlayerLabel.setText(getString(R.string.Human));
                        blackPlayerLabel.setText(getString(R.string.Computer));
                        break;
                    }
                    case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK: {    //w - comp; b - human
                        whitePlayerLabel.setText(getString(R.string.Computer));
                        blackPlayerLabel.setText(getString(R.string.Human));
                        break;
                    }
                    case AppConstants.GAME_MODE_HUMAN_VS_HUMAN: {    //w - human; b - human
                        whitePlayerLabel.setText(getString(R.string.Human));
                        blackPlayerLabel.setText(getString(R.string.Human));
                        break;
                    }
                    case AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER: {    //w - comp; b - comp
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
                                Update(CALLBACK_REPAINT_UI);
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
                                            GetOnlineGame(mainApp.getGameId());
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

                Update(CALLBACK_REPAINT_UI);
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
                            Update(CALLBACK_REPAINT_UI);
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
    public Object onRetainNonConfigurationInstance() {
        return newBoardView.getBoardFace();
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
                    Update(CALLBACK_REPAINT_UI);
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
                    Update(CALLBACK_REPAINT_UI);
                    isMoveNav = true;
                }
                break;
            case R.id.menu_next:
                newBoardView.stopThinking = true;
                if (!newBoardView.compmoving) {
                    newBoardView.sel = false;
                    newBoardView.getBoardFace().takeNext();
                    newBoardView.invalidate();
                    Update(CALLBACK_REPAINT_UI);
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
                    Update(CALLBACK_REPAINT_UI);
                    break;
                }
                case NEW_GAME_BLACK: {
                    // TODO encapsulate
                    newBoardView.setBoardFace(new Board2(GameCompScreenActivity.this));
                    newBoardView.getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);
                    newBoardView.getBoardFace().setReside(true);
                    newBoardView.getBoardFace().genCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
                    newBoardView.invalidate();
                    Update(CALLBACK_REPAINT_UI);
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
    public void onOptionsMenuClosed(Menu menu) {
        if (isMoveNav) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    openOptionsMenu();
                }
            }, 10);
            isMoveNav = false;
        }
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        newBoardView.requestFocus();
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onResume() {
        if (MobclixHelper.isShowAds(mainApp) && mainApp.getTabHost() != null && !mainApp.getTabHost().getCurrentTabTag().equals("tab4") && adViewWrapper != null && getRectangleAdview() != null) {
            adViewWrapper.addView(getRectangleAdview());
            if (mainApp.isForceRectangleAd()) {
                getRectangleAdview().getAd();
            }
        }

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

        super.onResume();

        registerReceiver(gameMoveReceiver, new IntentFilter(IntentConstants.ACTION_GAME_MOVE));
        registerReceiver(gameEndMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_END));
        registerReceiver(gameInfoMessageReceived, new IntentFilter(IntentConstants.ACTION_GAME_INFO));
        registerReceiver(showGameEndPopupReceiver, new IntentFilter(IntentConstants.ACTION_SHOW_GAME_END_POPUP));

        MobclixHelper.pauseAdview(mainApp.getBannerAdview(), mainApp);

        disableScreenLock();
    }

    @Override
    protected void onPause() {
        System.out.println("LCCLOG2: GAME ONPAUSE");
        unregisterReceiver(gameMoveReceiver);
        unregisterReceiver(gameEndMessageReceiver);
        unregisterReceiver(gameInfoMessageReceived);
        unregisterReceiver(showGameEndPopupReceiver);

        super.onPause();
        System.out.println("LCCLOG2: GAME ONPAUSE adViewWrapper="
                + adViewWrapper + ", getRectangleAdview() " + getRectangleAdview());
        if (adViewWrapper != null && getRectangleAdview() != null) {
            System.out.println("LCCLOG2: GAME ONPAUSE 1");
            getRectangleAdview().cancelAd();
            System.out.println("LCCLOG2: GAME ONPAUSE 2");
            adViewWrapper.removeView(getRectangleAdview());
            System.out.println("LCCLOG2: GAME ONPAUSE 3");
        }
        lccHolder.setActivityPausedMode(true);
        lccHolder.getPausedActivityGameEvents().clear();

        newBoardView.stopThinking = true;

        if (onlineGameUpdate != null)
            onlineGameUpdate.cancel();

        enableScreenLock();
    }

    private BroadcastReceiver gameMoveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
            game = (com.chess.model.Game) intent.getSerializableExtra(AppConstants.OBJECT);
            Update(CALLBACK_GAME_REFRESH);
        }
    };

    private BroadcastReceiver gameEndMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());

            final com.chess.live.client.Game game = lccHolder.getGame(mainApp.getGameId());
            Integer newWhiteRating = null;
            Integer newBlackRating = null;
            switch (game.getGameTimeConfig().getGameTimeClass()) {
                case BLITZ: {
                    newWhiteRating = game.getWhitePlayer().getBlitzRating();
                    newBlackRating = game.getBlackPlayer().getBlitzRating();
                    break;
                }
                case LIGHTNING: {
                    newWhiteRating = game.getWhitePlayer().getQuickRating();
                    newBlackRating = game.getBlackPlayer().getQuickRating();
                    break;
                }
                case STANDARD: {
                    newWhiteRating = game.getWhitePlayer().getStandardRating();
                    newBlackRating = game.getBlackPlayer().getStandardRating();
                    break;
                }
            }
            /*final String whiteRating =
                       (newWhiteRating != null && newWhiteRating != 0) ?
                       newWhiteRating.toString() : mainApp.getCurrentGame().values.get("white_rating");
                     final String blackRating =
                       (newBlackRating != null && newBlackRating != 0) ?
                       newBlackRating.toString() : mainApp.getCurrentGame().values.get("black_rating");*/
            whitePlayerLabel.setText(game.getWhitePlayer().getUsername() + "(" + newWhiteRating + ")");
            blackPlayerLabel.setText(game.getBlackPlayer().getUsername() + "(" + newBlackRating + ")");
            newBoardView.finished = true;

            if (MobclixHelper.isShowAds(mainApp)) {
                final LayoutInflater inflater = (LayoutInflater) coreContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                final View layout = inflater.inflate(R.layout.ad_popup,
                        (ViewGroup) findViewById(R.id.layout_root));
                showGameEndPopup(layout, intent.getExtras().getString(AppConstants.TITLE) + ": " + intent.getExtras().getString(AppConstants.MESSAGE));

                final View newGame = layout.findViewById(R.id.newGame);
                newGame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adPopup != null) {
                            try {
                                adPopup.dismiss();
                            } catch (Exception e) {
                            }
                            adPopup = null;
                        }
                        startActivity(new Intent(coreContext, OnlineNewGame.class));
                    }
                });
                newGame.setVisibility(View.VISIBLE);

                final View home = layout.findViewById(R.id.home);
                home.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adPopup != null) {
                            try {
                                adPopup.dismiss();
                            } catch (Exception e) {
                            }
                            adPopup = null;
                        }
                        startActivity(new Intent(coreContext, Tabs.class));
                    }
                });
                home.setVisibility(View.VISIBLE);
            }

            endOfGameMessage.setText(/*intent.getExtras().getString(AppConstants.TITLE) + ": " +*/ intent.getExtras().getString(AppConstants.MESSAGE));
            //mainApp.ShowDialog(Game.this, intent.getExtras().getString(AppConstants.TITLE), intent.getExtras().getString(AppConstants.MESSAGE));
            findViewById(R.id.endOfGameButtons).setVisibility(View.VISIBLE);
            findViewById(R.id.newGame).setOnClickListener(GameCompScreenActivity.this);
            findViewById(R.id.home).setOnClickListener(GameCompScreenActivity.this);
            getSoundPlayer().playGameEnd();
        }
    };

    private BroadcastReceiver gameInfoMessageReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
            mainApp.ShowDialog(coreContext, intent.getExtras()
                    .getString(AppConstants.TITLE), intent.getExtras().getString(AppConstants.MESSAGE));
        }
    };

    public TextView getWhiteClockView() {
        return whiteClockView;
    }

    public TextView getBlackClockView() {
        return blackClockView;
    }

    private void executePausedActivityGameEvents() {
        if (/*lccHolder.isActivityPausedMode() && */lccHolder.getPausedActivityGameEvents().size() > 0) {
            //boolean fullGameProcessed = false;
            GameEvent gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.Move);
            if (gameEvent != null &&
                    (lccHolder.getCurrentGameId() == null
                            || lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
                //lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
                //fullGameProcessed = true;
                lccHolder.getPausedActivityGameEvents().remove(gameEvent);
                //lccHolder.getAndroid().processMove(gameEvent.getGameId(), gameEvent.moveIndex);
                game = new com.chess.model.Game(lccHolder.getGameData(
                        gameEvent.getGameId().toString(), gameEvent.getMoveIndex()), true);
                Update(CALLBACK_GAME_REFRESH);
            }

            gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.DrawOffer);
            if (gameEvent != null &&
                    (lccHolder.getCurrentGameId() == null
                            || lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
                /*if (!fullGameProcessed)
                            {
                              lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
                              fullGameProcessed = true;
                            }*/
                lccHolder.getPausedActivityGameEvents().remove(gameEvent);
                lccHolder.getAndroid().processDrawOffered(gameEvent.getDrawOffererUsername());
            }

            gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.EndOfGame);
            if (gameEvent != null &&
                    (lccHolder.getCurrentGameId() == null || lccHolder.getCurrentGameId().equals(gameEvent.getGameId()))) {
                /*if (!fullGameProcessed)
                            {
                              lccHolder.processFullGame(lccHolder.getGame(gameEvent.getGameId().toString()));
                              fullGameProcessed = true;
                            }*/
                lccHolder.getPausedActivityGameEvents().remove(gameEvent);
                lccHolder.getAndroid().processGameEnd(gameEvent.getGameEndedMessage());
            }
        }
    }

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

    public void showGameEndPopup(final View layout, final String message) {
        if (!MobclixHelper.isShowAds(mainApp)) {
            return;
        }

        if (adPopup != null) {
            try {
                adPopup.dismiss();
            } catch (Exception e) {
                System.out.println("MOBCLIX: EXCEPTION IN showGameEndPopup");
                e.printStackTrace();
            }
            adPopup = null;
        }

        try {
            if (adViewWrapper != null && getRectangleAdview() != null) {
                adViewWrapper.removeView(getRectangleAdview());
            }
            adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
            System.out.println("MOBCLIX: GET WRAPPER " + adViewWrapper);
            adViewWrapper.addView(getRectangleAdview());

            adViewWrapper.setVisibility(View.VISIBLE);
            //showGameEndAds(adViewWrapper);

            TextView endOfGameMessagePopup = (TextView) layout.findViewById(R.id.endOfGameMessage);
            endOfGameMessagePopup.setText(message);

            adPopup.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialogInterface) {
                    if (adViewWrapper != null && getRectangleAdview() != null) {
                        adViewWrapper.removeView(getRectangleAdview());
                    }
                }
            });
            adPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    if (adViewWrapper != null && getRectangleAdview() != null) {
                        adViewWrapper.removeView(getRectangleAdview());
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("MOBCLIX: EXCEPTION IN showGameEndPopup");
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                AlertDialog.Builder builder;
                //Context mContext = getApplicationContext();
                builder = new AlertDialog.Builder(coreContext);
                builder.setView(layout);
                adPopup = builder.create();
                adPopup.setCancelable(true);
                adPopup.setCanceledOnTouchOutside(true);
                try {
                    adPopup.show();
                } catch (Exception e) {
                }
            }
        }, 1500);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (newBoardView.getBoardFace().isAnalysis()) {
                newBoardView.setBoardFace(new Board2(this));
                newBoardView.getBoardFace().setInit(true);
                newBoardView.getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));

                if (mainApp.getCurrentGame().values.get("game_type").equals("2"))
                    newBoardView.getBoardFace().setChess960(true);

                if (!isUserColorWhite()) {
                    newBoardView.getBoardFace().setReside(true);
                }
                String[] Moves = {};
                if (mainApp.getCurrentGame().values.get("move_list").contains("1.")) {
                    Moves = mainApp.getCurrentGame().values.get("move_list")
                            .replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
                    newBoardView.getBoardFace().setMovesCount(Moves.length);
                }

                String FEN = mainApp.getCurrentGame().values.get("starting_fen_position");
                if (!FEN.equals("")) {
                    newBoardView.getBoardFace().genCastlePos(FEN);
                    MoveParser2.FenParse(FEN, newBoardView.getBoardFace().getBoard());
                }

                int i;
                for (i = 0; i < newBoardView.getBoardFace().getMovesCount(); i++) {

                    int[] moveFT = mainApp.isLiveChess() ?
                            MoveParser2.parseCoordinate(newBoardView.getBoardFace().getBoard(), Moves[i]) :
                            MoveParser2.Parse(newBoardView.getBoardFace().getBoard(), Moves[i]);
                    if (moveFT.length == 4) {
                        Move m;
                        if (moveFT[3] == 2)
                            m = new Move(moveFT[0], moveFT[1], 0, 2);
                        else
                            m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
                        newBoardView.getBoardFace().makeMove(m, false);
                    } else {
                        Move m = new Move(moveFT[0], moveFT[1], 0, 0);
                        newBoardView.getBoardFace().makeMove(m, false);
                    }
                }
                Update(CALLBACK_REPAINT_UI);
                newBoardView.getBoardFace().takeBack();
                newBoardView.invalidate();

                //last move anim
                new Thread(new Runnable() {
                    @Override
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
                            Update(CALLBACK_REPAINT_UI);
                            newBoardView.invalidate();
                        }
                    };
                }).start();
            } else {
                LoadPrev(MainApp.loadPrev);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.newGame) {
            startActivity(new Intent(this, OnlineNewGame.class));
        } else if (view.getId() == R.id.home) {
            startActivity(new Intent(this, Tabs.class));
        }
    }
}