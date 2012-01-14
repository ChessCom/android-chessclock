package com.chess.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.AsyncTask;
import android.widget.*;
import com.chess.utilities.Notifications;
import org.apache.http.util.ByteArrayBuffer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.chess.R;
import com.chess.core.CoreActivity;
import com.chess.core.MainApp;
import com.chess.core.Tabs;
import com.chess.engine.Board;
import com.chess.engine.Move;
import com.chess.engine.MoveParser;
import com.chess.lcc.android.GameEvent;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.User;
import com.chess.model.GameListElement;
import com.chess.model.Tactic;
import com.chess.model.TacticResult;
import com.chess.utilities.MobclixAdViewListenerImpl;
import com.chess.utilities.MobclixHelper;
import com.chess.utilities.MyProgressDialog;
import com.chess.utilities.Web;
import com.chess.views.BoardView;
import com.chess.utilities.ChessComApiParser;
import com.flurry.android.FlurryAgent;
import com.mobclix.android.sdk.MobclixIABRectangleMAdView;

public class Game extends CoreActivity {
	public BoardView BV;
	private LinearLayout analysisLL;
  private LinearLayout analysisButtons;
  private RelativeLayout chatPanel;
  private ImageButton chatButton;
	private TextView white, black, thinking, timer, movelist;
	private Timer OnlineGameUpdate = null, TacticsTimer = null;
	private boolean msgShowed = false, isMoveNav = false, chat = false;
	//private String gameId = "";
	private int UPDATE_DELAY = 10000;
	private int resignOrAbort = R.string.resign;;

  private com.chess.model.Game OG;

  private TextView whiteClockView;
  private TextView blackClockView;

  protected AlertDialog adPopup;
  private TextView endOfGameMessage;
  private LinearLayout adviewWrapper;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(BV.board.analysis)
			{
				if(BV.board.mode < 6)
				{
					BV.board = new Board(this);
					BV.board.init = true;
					BV.board.mode = extras.getInt("mode");

					if(App.OnlineGame.values.get("game_type").equals("2"))
						BV.board.chess960 = true;

					if(!isUserColorWhite()){
						BV.board.setReside(true);
					}
					String[] Moves = {};
					if(App.OnlineGame.values.get("move_list").contains("1.")){
						Moves = App.OnlineGame.values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
						BV.board.movesCount = Moves.length;
					}

					String FEN = App.OnlineGame.values.get("starting_fen_position");
					if(!FEN.equals("")){
						BV.board.GenCastlePos(FEN);
						MoveParser.FenParse(FEN, BV.board);
					}

					int i;
					for(i=0; i < BV.board.movesCount; i++){

						int[] moveFT = App.isLiveChess() ? MoveParser.parseCoordinate(BV.board, Moves[i]) : MoveParser.Parse(BV.board, Moves[i]);
						if(moveFT.length == 4){
							Move m;
							if(moveFT[3]==2)
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							else
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							BV.board.makeMove(m, false);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							BV.board.makeMove(m, false);
						}
					}
					Update(0);
					BV.board.takeBack();
					BV.invalidate();

					//last move anim
					new Thread(new Runnable() {
						public void run() {
							try {
								Thread.sleep(1300);
								BV.board.takeNext();
								update.sendEmptyMessage(0);
							} catch (Exception e) {}
						}
						private Handler update = new Handler(){
							@Override
							public void dispatchMessage(Message msg) {
								super.dispatchMessage(msg);
								Update(0);
								BV.invalidate();
							}
						};
					}).start();
				} else if(BV.board.mode == 6)
				{
					if (App.Tactic != null && App.Tactic.values.get("stop").equals("1"))
					{
						openOptionsMenu();
						return true;
					}
					int sec = BV.board.sec;
					if(App.guest || App.noInternet)
					{
						BV.board = new Board(this);
						BV.board.mode = 6;

						String FEN = App.TacticsBatch.get(App.currentTacticProblem).values.get("fen");
						if(!FEN.equals("")){
							BV.board.GenCastlePos(FEN);
							MoveParser.FenParse(FEN, BV.board);
							String[] tmp = FEN.split(" ");
							if(tmp.length > 1){
								if(tmp[1].trim().equals("w")){
									BV.board.setReside(true);
								}
							}
						}
						if(App.TacticsBatch.get(App.currentTacticProblem).values.get("move_list").contains("1.")){
							BV.board.TacticMoves = App.TacticsBatch.get(App.currentTacticProblem).values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" ");
							BV.board.movesCount = 1;
						}
						BV.board.sec = sec;
						BV.board.left = Integer.parseInt(App.TacticsBatch.get(App.currentTacticProblem).values.get("average_seconds"))-sec;
						startTacticsTimer();
						int[] moveFT = MoveParser.Parse(BV.board, BV.board.TacticMoves[0]);
						if(moveFT.length == 4){
							Move m;
							if(moveFT[3]==2)
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							else
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							BV.board.makeMove(m);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							BV.board.makeMove(m);
						}
						Update(0);
						BV.board.takeBack();
						BV.invalidate();

						//last move anim
						new Thread(new Runnable() {
							public void run() {
								try {
									Thread.sleep(1300);
									BV.board.takeNext();
									update.sendEmptyMessage(0);
								} catch (Exception e) {}
							}
							private Handler update = new Handler(){
								@Override
								public void dispatchMessage(Message msg) {
									super.dispatchMessage(msg);
									Update(0);
									BV.invalidate();
								}
							};
						}).start();
					} else
					{
						if (App.Tactic != null && App.Tactic.values.get("stop").equals("1"))
						{
							openOptionsMenu();
							return true;
						}
						BV.board = new Board(this);
						BV.board.mode = 6;

						String FEN = App.Tactic.values.get("fen");
						if(!FEN.equals("")){
							BV.board.GenCastlePos(FEN);
							MoveParser.FenParse(FEN, BV.board);
							String[] tmp2 = FEN.split(" ");
							if(tmp2.length > 1){
								if(tmp2[1].trim().equals("w")){
									BV.board.setReside(true);
								}
							}
						}

						if(App.Tactic.values.get("move_list").contains("1.")){
							BV.board.TacticMoves = App.Tactic.values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" ");
							BV.board.movesCount = 1;
						}
						BV.board.sec = sec;
						BV.board.left = Integer.parseInt(App.Tactic.values.get("average_seconds"))-sec;
						int[] moveFT = MoveParser.Parse(BV.board, BV.board.TacticMoves[0]);
						if(moveFT.length == 4){
							Move m;
							if(moveFT[3]==2)
								m = new Move(moveFT[0], moveFT[1], 0, 2);
							else
								m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
							BV.board.makeMove(m);
						} else {
							Move m = new Move(moveFT[0], moveFT[1], 0, 0);
							BV.board.makeMove(m);
						}
						Update(0);
						BV.board.takeBack();
						BV.invalidate();

						//last move anim
						new Thread(new Runnable() {
							public void run() {
								try {
									Thread.sleep(1300);
									BV.board.takeNext();
									update.sendEmptyMessage(0);
								} catch (Exception e) {}
							}
							private Handler update = new Handler(){
								@Override
								public void dispatchMessage(Message msg) {
									super.dispatchMessage(msg);
									Update(0);
									BV.invalidate();
								}
							};
						}).start();
					}
				}
			}
            else
			{
				LoadPrev(MainApp.loadPrev);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case 0:
				FlurryAgent.onEvent("Tactics Daily Limit Exceded", null);
				return new AlertDialog.Builder(this)
	            .setTitle("Daily Limit Exceeded").setMessage("You have hit your maximum number of tactics for today. Would you like to be able to do more tactics?")
	            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
                    FlurryAgent.onEvent("Upgrade From Tactics", null);
	                	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/login.html?als="+App.sharedData.getString("user_token", "")+"&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html")));
	                }
	            })
	            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	App.mTabHost.setCurrentTab(0);
	                	BV.board.tacticCanceled = true;
	                }
	            })
	            .create();
			case 1:
				return new AlertDialog.Builder(this)
	            .setTitle("Are you ready for your first tactic?")
	            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {

	                	InputStream f = getResources().openRawResource(R.raw.tactics100batch);
                		try {
                			ByteArrayBuffer baf = new ByteArrayBuffer(50);
                            int current = 0;
                            while((current = f.read()) != -1){
                                 baf.append((byte)current);
                            }
            				String input = new String(baf.toByteArray());
            				String[] tmp = input.split("[|]");
            				int count = tmp.length-1;
            				App.TacticsBatch = new ArrayList<Tactic>(count);
            				int i;
            				for(i=1;i<=count;i++){
            					App.TacticsBatch.add(new Tactic(tmp[i].split(":")));
            				}
            				f.close();
            			} catch (IOException e) {
            				e.printStackTrace();
            			}

	                	if(App.guest)
	            			GetGuestTacticsGame();
	                	else
	                		GetTacticsGame("");
	                }
	            })
	            .setNegativeButton("No", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	App.mTabHost.setCurrentTab(0);
	                	BV.board.tacticCanceled = true;
	                }
	            })
	            .create();
			case 2:
				return new AlertDialog.Builder(this)
	            .setTitle("100 tactics complited!")
	            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	App.mTabHost.setCurrentTab(0);
	                	App.currentTacticProblem = 0;
	                }
	            })
	            .create();
			case 3:
				return new AlertDialog.Builder(this)
	            .setTitle("Offline mode")
	            .setMessage("Internet access is not currently available. As a result, your rating will not change after completing problems until your Internet connection returns.")
	            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	GetGuestTacticsGame();
	                }
	            })
	            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	App.mTabHost.setCurrentTab(0);
	                	BV.board.tacticCanceled = true;
	                }
	            })
	            .create();
			case 4:
				return new AlertDialog.Builder(this)
	            .setTitle("Offer a draw:").setMessage("Are you sure?")
	            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
                    if (App.isLiveChess() && BV.board.mode == 4)
                    {
                      final com.chess.live.client.Game game = lccHolder.getGame(App.gameId);
                      LccHolder.LOG.info("Request draw: " + game);
                      lccHolder.getAndroid().runMakeDrawTask(game);
                    }
                    else
                    {
                      String Draw = "OFFERDRAW";
	                	  if(App.acceptdraw)
                        Draw = "ACCEPTDRAW";
                      String result = Web.Request("http://www." + LccHolder.HOST + "/api/submit_echess_action?id="+App.sharedData.getString("user_token", "")+"&chessid="+App.OnlineGame.values.get("game_id")+"&command="+Draw+"&timestamp="+App.OnlineGame.values.get("timestamp"), "GET", null, null);
                      if(result.contains("Success")){
                        App.ShowDialog(Game.this, "", getString(R.string.drawoffered));
                      } else if(result.contains("Error+")){
                        App.ShowDialog(Game.this, "Error", result.split("[+]")[1]);
                      } else{
                        //App.ShowDialog(Game.this, "Error", result);
                      }
                    }
	                }
	            })
	            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                }
	            })
	            .create();
			case 5:
				return new AlertDialog.Builder(this)
	            .setTitle("Abort/Resign a game:").setMessage("Are you sure?")
	            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
                    if (App.isLiveChess() && BV.board.mode == 4)
                    {
                      final com.chess.live.client.Game game = lccHolder.getGame(App.gameId);

                      if (lccHolder.isFairPlayRestriction(App.gameId))
                      {
						System.out.println("LCCLOG: resign game by fair play restriction: " + game);
                        LccHolder.LOG.info("Resign game: " + game);
                        lccHolder.getAndroid().runMakeResignTask(game);
                      }
                      else if (lccHolder.isAbortableBySeq(App.gameId))
                      {
						LccHolder.LOG.info("LCCLOG: abort game: " + game);
                        lccHolder.getAndroid().runAbortGameTask(game);
                      }
                      else
                      {
						LccHolder.LOG.info("LCCLOG: resign game: " + game);
						lccHolder.getAndroid().runMakeResignTask(game);
                      }
                      finish();
                    }
                    else
                    {
                      String result = Web.Request("http://www." + LccHolder.HOST + "/api/submit_echess_action?id="+App.sharedData.getString("user_token", "")+"&chessid="+App.OnlineGame.values.get("game_id")+"&command=RESIGN&timestamp="+App.OnlineGame.values.get("timestamp"), "GET", null, null);
                      if(result.contains("Success")){
                        if (MobclixHelper.isShowAds(App))
                        {
                          sendBroadcast(new Intent("com.chess.lcc.android-show-game-end-popup").putExtra("message", "GAME OVER").putExtra("finishable", true));
                        }
                        else
                        {
                          finish();
                        }
                      } else if(result.contains("Error+")){
                        App.ShowDialog(Game.this, "Error", result.split("[+]")[1]);
                      } else{
                        //App.ShowDialog(Game.this, "Error", result);
                      }
                      }
	                }
	            })
	            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                }
	            })
	            .create();

			default:
				break;
		}
		return super.onCreateDialog(id);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (App.isLiveChess() && extras.getInt("mode") == 4)
	    {
	      setContentView(R.layout.boardviewlive);
        lccHolder.getAndroid().setGameActivity(this);
      }
	    else
	    {
	      setContentView(R.layout.boardview);
	    }

		analysisLL = (LinearLayout)findViewById(R.id.analysis);
    analysisButtons = (LinearLayout)findViewById(R.id.analysisButtons);
    if(App.isLiveChess() && extras.getInt("mode") != 6)
    {
      chatPanel = (RelativeLayout) findViewById(R.id.chatPanel);
      chatButton = (ImageButton) findViewById(R.id.chat);
      chatButton.setOnClickListener(new OnClickListener()
      {
        public void onClick(View view)
        {
          chat = true;
          GetOnlineGame(App.gameId);
          chatPanel.setVisibility(View.GONE);
        }
      });
    }
    if(!App.isLiveChess())
    {
      findViewById(R.id.prev).setOnClickListener(new OnClickListener()
      {
        public void onClick(View view)
        {
          BV.finished = false;
          BV.sel = false;
          BV.board.takeBack();
          BV.invalidate();
          Update(0);
          isMoveNav = true;
        }
      });
      findViewById(R.id.next).setOnClickListener(new OnClickListener()
      {
        public void onClick(View view)
        {
          BV.board.takeNext();
          BV.invalidate();
          Update(0);
          isMoveNav = true;
        }
      });
    }

		white = (TextView)findViewById(R.id.white);
		black = (TextView)findViewById(R.id.black);
		thinking = (TextView)findViewById(R.id.thinking);
		timer = (TextView)findViewById(R.id.timer);
		movelist = (TextView)findViewById(R.id.movelist);

    whiteClockView = (TextView)findViewById(R.id.whiteClockView);
    blackClockView = (TextView)findViewById(R.id.blackClockView);
    if (App.isLiveChess() && extras.getInt("mode") == 4 && lccHolder.getWhiteClock() != null && lccHolder.getBlackClock() != null)
    {
      whiteClockView.setVisibility(View.VISIBLE);
      blackClockView.setVisibility(View.VISIBLE);
      lccHolder.getWhiteClock().paint();
      lccHolder.getBlackClock().paint();
      final com.chess.live.client.Game game = lccHolder.getGame(new Long(extras.getString("game_id")));
      final User whiteUser = game.getWhitePlayer();
      final User blackUser = game.getBlackPlayer();
      final Boolean isWhite = (!game.isMoveOf(whiteUser) && !game.isMoveOf(blackUser)) ? null : game.isMoveOf(whiteUser);
      lccHolder.setClockDrawPointer(isWhite);
    }

    endOfGameMessage = (TextView)findViewById(R.id.endOfGameMessage);

    	BV = (BoardView)findViewById(R.id.boardview);
		BV.setFocusable(true);
		BV.board = (Board)getLastNonConfigurationInstance();

    lccHolder = App.getLccHolder();

		if(BV.board == null){
			BV.board = new Board(this);
			BV.board.init = true;
			BV.board.mode = extras.getInt("mode");
			BV.board.GenCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
      //BV.board.GenCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

			if(BV.board.mode < 4 && !App.sharedData.getString("saving", "").equals("")){
				int i;
				String[] moves = App.sharedData.getString("saving", "").split("[|]");
				for(i=1;i<moves.length;i++){
					String[] move = moves[i].split(":");
					BV.board.makeMove(new Move(
							Integer.parseInt(move[0]),
							Integer.parseInt(move[1]),
							Integer.parseInt(move[2]),
							Integer.parseInt(move[3])	), false);
				}
				if(BV.board.mode == 1)
					BV.board.setReside(true);
			} else{
				if(BV.board.mode == 1){
					BV.board.setReside(true);
					BV.invalidate();
					BV.ComputerMove(App.strength[App.sharedData.getInt(App.sharedData.getString("username", "")+"strength", 0)]);
				}
				if(BV.board.mode == 3){
					BV.ComputerMove(App.strength[App.sharedData.getInt(App.sharedData.getString("username", "")+"strength", 0)]);
				}
				if(BV.board.mode == 4 || BV.board.mode == 5)
					App.gameId = extras.getString("game_id");
			}
			if(BV.board.mode == 6){
				showDialog(1);
				return;
			}
		}

		if (MobclixHelper.isShowAds(App) && getRectangleAdview() == null && !App.mTabHost.getCurrentTabTag().equals("tab4"))
		{
			setRectangleAdview(new MobclixIABRectangleMAdView(this));
			getRectangleAdview().setRefreshTime(-1);
			getRectangleAdview().addMobclixAdViewListener(new MobclixAdViewListenerImpl(true, App));
			App.setForceRectangleAd(false);
		}
		
		Update(0);
	}
	private void GetOnlineGame(final String game_id){
		if(appService != null && appService.repeatble != null){
    		appService.repeatble.cancel();
    		appService.repeatble = null;
    	}
		App.gameId = game_id;

    if (App.isLiveChess() && BV.board.mode == 4)
    {
		  Update(10);
    }
    else
    {
      if(appService != null){
			  appService.RunSingleTask(10,
				"http://www." + LccHolder.HOST + "/api/v3/get_game?id="+App.sharedData.getString("user_token", "")+"&gid="+game_id,
				null/*PD = MyProgressDialog.show(this, null, getString(R.string.loading), true)*/);
      }
    }
	}

	private void GetTacticsGame(final String id){
    FlurryAgent.onEvent("Tactics Session Started For Registered", null);
		if(!App.noInternet){
			BV.board = new Board(this);
			BV.board.mode = 6;

			if(App.Tactic != null && id.equals(App.Tactic.values.get("id")))
      {
				BV.board.retry = true;
				String FEN = App.Tactic.values.get("fen");
				if(!FEN.equals("")){
					BV.board.GenCastlePos(FEN);
					MoveParser.FenParse(FEN, BV.board);
					String[] tmp2 = FEN.split(" ");
					if(tmp2.length > 1){
						if(tmp2[1].trim().equals("w")){
							BV.board.setReside(true);
						}
					}
				}

				if(App.Tactic.values.get("move_list").contains("1.")){
					BV.board.TacticMoves = App.Tactic.values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" ");
					BV.board.movesCount = 1;
				}
				BV.board.sec = 0;
				BV.board.left = Integer.parseInt(App.Tactic.values.get("average_seconds"));
				startTacticsTimer();
				int[] moveFT = MoveParser.Parse(BV.board, BV.board.TacticMoves[0]);
				if(moveFT.length == 4){
					Move m;
					if(moveFT[3]==2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					BV.board.makeMove(m);
				} else {
					Move m = new Move(moveFT[0], moveFT[1], 0, 0);
					BV.board.makeMove(m);
				}
				Update(0);
				BV.board.takeBack();
				BV.invalidate();

				//last move anim
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1300);
							BV.board.takeNext();
							update.sendEmptyMessage(0);
						} catch (Exception e) {}
					}
					private Handler update = new Handler(){
						@Override
						public void dispatchMessage(Message msg) {
							super.dispatchMessage(msg);
							Update(0);
							BV.invalidate();
						}
					};
				}).start();

				return;
			}
		}
		if(appService != null){
			appService.RunSingleTask(7,
				"http://www." + LccHolder.HOST + "/api/tactics_trainer?id="+App.sharedData.getString("user_token", "")+"&tactics_id="+id,
				PD = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), false))
      );
		}
	}
	private void GetGuestTacticsGame(){
		FlurryAgent.onEvent("Tactics Session Started For Guest", null);

    if(App.currentTacticProblem >= App.TacticsBatch.size()){
			showDialog(2);
			return;
		}

		BV.board = new Board(this);
		BV.board.mode = 6;

		String FEN = App.TacticsBatch.get(App.currentTacticProblem).values.get("fen");
		if(!FEN.equals("")){
			BV.board.GenCastlePos(FEN);
			MoveParser.FenParse(FEN, BV.board);
			String[] tmp = FEN.split(" ");
			if(tmp.length > 1){
				if(tmp[1].trim().equals("w")){
					BV.board.setReside(true);
				}
			}
		}
		if(App.TacticsBatch.get(App.currentTacticProblem).values.get("move_list").contains("1.")){
			BV.board.TacticMoves = App.TacticsBatch.get(App.currentTacticProblem).values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" ");
			BV.board.movesCount = 1;
		}
		BV.board.sec = 0;
		BV.board.left = Integer.parseInt(App.TacticsBatch.get(App.currentTacticProblem).values.get("average_seconds"));
		startTacticsTimer();
		int[] moveFT = MoveParser.Parse(BV.board, BV.board.TacticMoves[0]);
		if(moveFT.length == 4){
			Move m;
			if(moveFT[3]==2)
				m = new Move(moveFT[0], moveFT[1], 0, 2);
			else
				m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
			BV.board.makeMove(m);
		} else {
			Move m = new Move(moveFT[0], moveFT[1], 0, 0);
			BV.board.makeMove(m);
		}
		Update(0);
		BV.board.takeBack();
		BV.invalidate();

		//last move anim
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1300);
					BV.board.takeNext();
					update.sendEmptyMessage(0);
				} catch (Exception e) {}
			}
			private Handler update = new Handler(){
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					Update(0);
					BV.invalidate();
				}
			};
		}).start();
	}

	private void ShowAnswer(){
		BV.board = new Board(this);
		BV.board.mode = 6;
		BV.board.retry = true;

		if(App.guest || App.noInternet){
			String FEN = App.TacticsBatch.get(App.currentTacticProblem).values.get("fen");
			if(!FEN.equals("")){
				BV.board.GenCastlePos(FEN);
				MoveParser.FenParse(FEN, BV.board);
				String[] tmp = FEN.split(" ");
				if(tmp.length > 1){
					if(tmp[1].trim().equals("w")){
						BV.board.setReside(true);
					}
				}
			}
			if(App.TacticsBatch.get(App.currentTacticProblem).values.get("move_list").contains("1.")){
				BV.board.TacticMoves = App.TacticsBatch.get(App.currentTacticProblem).values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" ");
				BV.board.movesCount = 1;
			}
		} else {
			String FEN = App.Tactic.values.get("fen");
			if(!FEN.equals("")){
				BV.board.GenCastlePos(FEN);
				MoveParser.FenParse(FEN, BV.board);
				String[] tmp2 = FEN.split(" ");
				if(tmp2.length > 1){
					if(tmp2[1].trim().equals("w")){
						BV.board.setReside(true);
					}
				}
			}

			if(App.Tactic.values.get("move_list").contains("1.")){
				BV.board.TacticMoves = App.Tactic.values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" ");
				BV.board.movesCount = 1;
			}
		}
		BV.invalidate();


		new Thread(new Runnable() {
			public void run() {
        		int i;
    			for(i=0;i < BV.board.TacticMoves.length;i++){
    				int[] moveFT = MoveParser.Parse(BV.board, BV.board.TacticMoves[i]);
    				try {
    					Thread.sleep(1500);
    				} catch (Exception e) {}
    				if(moveFT.length == 4){
    					Move m;
    					if(moveFT[3]==2)
    						m = new Move(moveFT[0], moveFT[1], 0, 2);
    					else
    						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);

    					BV.board.makeMove(m);
    				}else {
    					Move m = new Move(moveFT[0], moveFT[1], 0, 0);
    					BV.board.makeMove(m);
    				}
    				handler.sendEmptyMessage(0);
    			}
			}
			private Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					Update(0);
					BV.invalidate();
				}
			};
		}).start();
	}

	private void CheckTacticMoves(){
		Move m = BV.board.histDat[BV.board.hply-1].m;
		String f = "";
		int p = BV.board.piece[m.to];
		if(p == 1){
			f = "N";
		} else if(p == 2){
			f = "B";
		} else if(p == 3){
			f = "R";
		} else if(p == 4){
			f = "Q";
		} else if(p == 5){
			f = "K";
		}
		String Moveto = MoveParser.positionToString(m.to);
		Log.d("!!!", f+" | "+Moveto+" : "+BV.board.TacticMoves[BV.board.hply-1]);
		if(BV.board.TacticMoves[BV.board.hply-1].contains(f) && BV.board.TacticMoves[BV.board.hply-1].contains(Moveto)){
			BV.board.TacticsCorrectMoves++;
			if(BV.board.movesCount < BV.board.TacticMoves.length-1){
				int[] moveFT = MoveParser.Parse(BV.board, BV.board.TacticMoves[BV.board.hply]);
				if(moveFT.length == 4){
					if(moveFT[3]==2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					BV.board.makeMove(m);
				}else {
					m = new Move(moveFT[0], moveFT[1], 0, 0);
					BV.board.makeMove(m);
				}
				Update(0);
				BV.invalidate();
			}	else{
				if(App.guest || BV.board.retry || App.noInternet){
					new AlertDialog.Builder(this)
		            .setTitle("Correct!")
		            .setItems(getResources().getTextArray(R.array.correcttactic), new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) {
		                	if(which == 1){
		                		if(App.guest){
			                		App.currentTacticProblem++;
			                		GetGuestTacticsGame();
		                		} else{
		                			if(App.noInternet)	App.currentTacticProblem++;
		                			GetTacticsGame("");
		                		}
		                	}
		                }
		            })
		            .create().show();
					stopTacticsTimer();
				}	else{
					if(appService != null){
						appService.RunSingleTask(6,
								"http://www." + LccHolder.HOST + "/api/tactics_trainer?id="+App.sharedData.getString("user_token", "")+"&tactics_id="+App.Tactic.values.get("id")+"&passed="+1+"&correct_moves="+BV.board.TacticsCorrectMoves+"&seconds="+BV.board.sec,
							PD = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true)));
					}
					stopTacticsTimer();
				}
			}
		}	else {
			if(App.guest || BV.board.retry || App.noInternet){
				new AlertDialog.Builder(this)
	            .setTitle("Wrong!")
	            .setItems(getResources().getTextArray(R.array.wrongtactic), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	if(which == 0){
	                		if(App.guest){
		                		App.currentTacticProblem++;
		                		GetGuestTacticsGame();
	                		} else{
	                			if(App.noInternet)	App.currentTacticProblem++;
	                			GetTacticsGame("");
	                		}
	                	}
	                	if(which == 1){
	                		if(App.guest || App.noInternet){
	                			BV.board.retry = true;
		                		GetGuestTacticsGame();
	                		} else{
	                			GetTacticsGame(App.Tactic.values.get("id"));
	                		}
	                	}
						if(which == 2)
                        {
							BV.finished = true;
							App.Tactic.values.put("stop", "1");
						}
	                }
	            })
	            .create().show();
				stopTacticsTimer();
			}	else{
				if(appService != null){
					appService.RunSingleTask(5,
						"http://www." + LccHolder.HOST + "/api/tactics_trainer?id="+App.sharedData.getString("user_token", "")+"&tactics_id="+App.Tactic.values.get("id")+"&passed="+0+"&correct_moves="+BV.board.TacticsCorrectMoves+"&seconds="+BV.board.sec,
						PD = new MyProgressDialog(ProgressDialog.show(this, null, getString(R.string.loading), true)));
				}
				stopTacticsTimer();
			}
		}
	}

	@Override
	public void LoadNext(int code) {

	}
	@Override
	public void LoadPrev(int code) {
    if(BV.board != null && BV.board.mode == 6)
    {
      App.mTabHost.setCurrentTab(0);
      BV.board.tacticCanceled = true;
    }
    else
    {
      finish();
    }
	}
	@Override
	public void Update(int code) {
		switch (code) {
			case -2:
				if(BV.board.mode < 6)
					finish();
				else if(BV.board.mode == 6){
					/*App.mTabHost.setCurrentTab(0);
					BV.board.tacticCanceled = true;*/
					if(App.noInternet){
						if(App.offline){
							GetGuestTacticsGame();
						} else{
							App.offline = true;
							showDialog(3);
						}
						return;
					}
				}
				//finish();
				break;
			case -1:
				if(BV.board.init && BV.board.mode == 4 || BV.board.mode == 5){
          //System.out.println("@@@@@@@@ POINT 1 App.gameId=" + App.gameId);
					GetOnlineGame(App.gameId);
					BV.board.init = false;
				} else if(!BV.board.init){
					if(BV.board.mode == 4 && appService != null && appService.repeatble == null){
						if(PD != null){
							PD.dismiss();
							PD = null;
						}
            if (!App.isLiveChess())
            {
              appService.RunRepeatbleTask(9, UPDATE_DELAY, UPDATE_DELAY,
                                          "http://www." + LccHolder.HOST + "/api/v3/get_game?id="+App.sharedData.getString("user_token", "")+"&gid="+App.gameId,
                                          null/*PD*/
              );
            }
					}
				}
				break;
			case 0:{
				switch (BV.board.mode) {
					case 0:{	//w - human; b - comp
						white.setText(getString(R.string.Human));
						black.setText(getString(R.string.Computer));
						break;
					}
					case 1:{	//w - comp; b - human
						white.setText(getString(R.string.Computer));
						black.setText(getString(R.string.Human));
						break;
					}
					case 2:{	//w - human; b - human
						white.setText(getString(R.string.Human));
						black.setText(getString(R.string.Human));
						break;
					}
					case 3:{	//w - comp; b - comp
						white.setText(getString(R.string.Computer));
						black.setText(getString(R.string.Computer));
						break;
					}
					case 4:{
						if(BV.board.submit)
							findViewById(R.id.moveButtons).setVisibility(View.VISIBLE);
						findViewById(R.id.submit).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Update(1);	//movesubmit
							}
						});
						findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								findViewById(R.id.moveButtons).setVisibility(View.GONE);
								BV.board.takeBack();
								BV.board.movesCount--;
								BV.invalidate();
								BV.board.submit = false;
							}
						});
						if(BV.board.analysis){
							white.setVisibility(View.GONE);
							black.setVisibility(View.GONE);
							analysisLL.setVisibility(View.VISIBLE);
							if (!App.isLiveChess() && analysisButtons!=null)
							{
								showAnalysisButtons();
							}
						} else{
							white.setVisibility(View.VISIBLE);
							black.setVisibility(View.VISIBLE);
							analysisLL.setVisibility(View.GONE);
							if (!App.isLiveChess() && analysisButtons!=null)
							{
								hideAnalysisButtons();
							}
						}

						break;
					}
					default: break;
				}

				if(BV.board.mode < 4) {
					hideAnalysisButtons();
				}

				if(BV.board.mode == 4 || BV.board.mode == 5){
					if(App.OnlineGame != null){
						white.setText(App.OnlineGame.values.get("white_username")+"\n("+App.OnlineGame.values.get("white_rating")+")");
						black.setText(App.OnlineGame.values.get("black_username")+"\n("+App.OnlineGame.values.get("black_rating")+")");
					}
				}

				if(BV.board.mode == 6){
					if(BV.board.analysis){
						timer.setVisibility(View.GONE);
						analysisLL.setVisibility(View.VISIBLE);
						if (!App.isLiveChess() && analysisButtons!=null)
						{
							showAnalysisButtons();
						}
					} else{
						white.setVisibility(View.GONE);
						black.setVisibility(View.GONE);
						timer.setVisibility(View.VISIBLE);
						analysisLL.setVisibility(View.GONE);
						if (!App.isLiveChess() && analysisButtons!=null)
						{
							hideAnalysisButtons();
						}
					}
				}
            movelist.setText(BV.board.MoveListSAN());
		        /*if(App.OnlineGame != null && App.OnlineGame.values.get("move_list") != null)
		        {
		          movelist.setText(App.OnlineGame.values.get("move_list"));
		        }
		        else
	            {
	              movelist.setText(BV.board.MoveListSAN());
	            }*/
				BV.invalidate();

				new Handler().post(new Runnable() {
					@Override
					public void run() {
						BV.requestFocus();
					}
				});
				break;
			}
      case 1:
      {
        // making the move
				findViewById(R.id.moveButtons).setVisibility(View.GONE);
				BV.board.submit = false;
         //String myMove = BV.board.MoveSubmit();
        if (App.isLiveChess() && BV.board.mode == 4)
        {
          final String move = BV.board.convertMoveLive();
          LccHolder.LOG.info("LCC make move: " + move);
          try
          {
            lccHolder.makeMove(App.OnlineGame.values.get("game_id"), move);
          }
          catch (IllegalArgumentException e)
          {
            LccHolder.LOG.info("LCC illegal move: " + move);
            e.printStackTrace();
          }
        }
        else if (!App.isLiveChess() && appService != null)
        {
          if(App.OnlineGame == null)
          {
            if(appService.repeatble != null)
            {
              appService.repeatble.cancel();
              appService.repeatble = null;
            }
            appService.RunSingleTask(12,
                                     "http://www." + LccHolder.HOST + "/api/v3/get_game?id=" +
                                     App.sharedData.getString("user_token", "") + "&gid=" + App.gameId,
                                     null);
          }
          else
          {
          appService.RunSingleTask(8,
                                     "http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" +
                                     App.sharedData.getString("user_token", "") + "&chessid=" +
                                     App.OnlineGame.values.get("game_id") + "&command=SUBMIT&newmove=" +
                                     BV.board.convertMoveEchess() + "&timestamp=" +
                                     App.OnlineGame.values.get("timestamp"),
                                     PD = new MyProgressDialog(
                                       ProgressDialog.show(this, null, getString(R.string.sendinggameinfo), true)));

          NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
          mNotificationManager.cancel(1);
          Notifications.resetCounter();
          }
        }
        break;
      }
      case 12:
      {
    	  App.OnlineGame = ChessComApiParser.GetGameParseV3(response);
        if(!App.isLiveChess() && appService != null)
        {
          appService.RunSingleTask(8,
                                   "http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" +
                                   App.sharedData.getString("user_token", "") + "&chessid=" +
                                   App.OnlineGame.values.get("game_id") + "&command=SUBMIT&newmove=" +
                                   BV.board.convertMoveEchess() + "&timestamp=" +
                                   App.OnlineGame.values.get("timestamp"),
                                   PD = new MyProgressDialog(
                                     ProgressDialog.show(this, null, getString(R.string.sendinggameinfo), true)));
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(1);
            Notifications.resetCounter();
        }
				break;
			}
			case 2:{
				white.setVisibility(View.GONE);
				black.setVisibility(View.GONE);
				thinking.setVisibility(View.VISIBLE);
				break;
			}
			case 3:{
				white.setVisibility(View.VISIBLE);
				black.setVisibility(View.VISIBLE);
				thinking.setVisibility(View.GONE);
				break;
			}
			case 4:{
				CheckTacticMoves();
				break;
			}
			case 5:{
				String[] tmp = response.split("[|]");
				if(tmp.length < 2 || tmp[1].trim().equals("")){
					showDialog(0);
					return;
				}

				TacticResult result = new TacticResult(tmp[1].split(":"));

				new AlertDialog.Builder(this)
	            .setTitle("Wrong! Score: "+result.values.get("user_rating_change")+"\n"+"New rating: "+result.values.get("user_rating"))
	            .setItems(getResources().getTextArray(R.array.wrongtactic), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	if(which == 0){
	                		GetTacticsGame("");
	                	}
	                	if(which == 1){
	                		BV.board.retry = true;
	                		GetTacticsGame(App.Tactic.values.get("id"));
	                	}
						if(which == 2)
                        {
							BV.finished = true;
							App.Tactic.values.put("stop", "1");
						}
	                }
	            })
	            .create().show();
				break;
			}
			case 6:{
				String[] tmp = response.split("[|]");
				if(tmp.length < 2 || tmp[1].trim().equals("")){
					showDialog(0);
					return;
				}

				TacticResult result = new TacticResult(tmp[1].split(":"));

				new AlertDialog.Builder(this)
	            .setTitle("Correct! Score: "+result.values.get("user_rating_change")+"\n"+"New rating: "+result.values.get("user_rating"))
	            .setItems(getResources().getTextArray(R.array.correcttactic), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	if(which == 1){
	                		GetTacticsGame("");
	                	}
	                }
	            })
	            .create().show();
				break;
			}
			case 7:

				BV.board = new Board(this);
				BV.board.mode = 6;

				String[] tmp = response.trim().split("[|]");
				if(tmp.length < 3 || tmp[2].trim().equals("")){
					showDialog(0);
					return;
				}

				App.Tactic = new Tactic(tmp[2].split(":"));

				String FEN = App.Tactic.values.get("fen");
				if(!FEN.equals("")){
					BV.board.GenCastlePos(FEN);
					MoveParser.FenParse(FEN, BV.board);
					String[] tmp2 = FEN.split(" ");
					if(tmp2.length > 1){
						if(tmp2[1].trim().equals("w")){
							BV.board.setReside(true);
						}
					}
				}

				if(App.Tactic.values.get("move_list").contains("1.")){
					BV.board.TacticMoves = App.Tactic.values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("[.]", "").replaceAll("  ", " ").substring(1).split(" ");
					BV.board.movesCount = 1;
				}
				BV.board.sec = 0;
				BV.board.left = Integer.parseInt(App.Tactic.values.get("average_seconds"));
        startTacticsTimer();
				int[] moveFT = MoveParser.Parse(BV.board, BV.board.TacticMoves[0]);
				if(moveFT.length == 4){
					Move m;
					if(moveFT[3]==2)
						m = new Move(moveFT[0], moveFT[1], 0, 2);
					else
						m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
					BV.board.makeMove(m);
				} else {
					Move m = new Move(moveFT[0], moveFT[1], 0, 0);
					BV.board.makeMove(m);
				}
				Update(0);
				BV.board.takeBack();
				BV.invalidate();

				//last move anim
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1300);
							BV.board.takeNext();
							update.sendEmptyMessage(0);
						} catch (Exception e) {}
					}
					private Handler update = new Handler(){
						@Override
						public void dispatchMessage(Message msg) {
							super.dispatchMessage(msg);
							Update(0);
							BV.invalidate();
						}
					};
				}).start();
				break;
			case 8:
        // move was made
				if(App.sharedData.getInt(App.sharedData.getString("username", "")+"aim", 0) == 2){
					finish();
				} else if(App.sharedData.getInt(App.sharedData.getString("username", "")+"aim", 0) == 0){

					int i;
					ArrayList<GameListElement> currentGames = new ArrayList<GameListElement>();
					for(GameListElement gle: App.GameListItems){
						if(gle.type == 1 && gle.values.get("is_my_turn").equals("1")){
							currentGames.add(gle);
						}
					}
					for(i=0;i<currentGames.size();i++){
						if(currentGames.get(i).values.get("game_id").contains(App.OnlineGame.values.get("game_id"))){
							if(i+1 < currentGames.size()){
								BV.board = new Board(this);
								BV.board.analysis = false;
								BV.board.mode = 4;

								if(PD != null){
									PD.dismiss();
									PD = null;
								}

								GetOnlineGame(currentGames.get(i+1).values.get("game_id"));
								return;
							} else{
								finish();
								return;
							}
						}
					}
					finish();
					return;
				}
				break;
			case 9:
        if(BV.board.analysis)
					return;
        if (!App.isLiveChess())
        {
          OG = ChessComApiParser.GetGameParseV3(rep_response);
        }
        //System.out.println("!!!!!!!! App.OnlineGame " + App.OnlineGame);
        //System.out.println("!!!!!!!! OG " + OG);

        if (App.OnlineGame == null || OG == null)
        {
          return;
        }

        if(!App.OnlineGame.equals(OG)){
					if(!App.OnlineGame.values.get("move_list").equals(OG.values.get("move_list"))){
						App.OnlineGame = OG;
						String[] Moves = {};
						if(App.OnlineGame.values.get("move_list").contains("1.") || ((App.isLiveChess() && BV.board.mode == 4))){
              int beginIndex = (App.isLiveChess() && BV.board.mode == 4) ? 0 : 1;
              Moves = App.OnlineGame.values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(beginIndex).split(" ");
              if (Moves.length - BV.board.movesCount == 1) {
                if (App.isLiveChess())
                {
                  moveFT = MoveParser.parseCoordinate(BV.board, Moves[Moves.length - 1]);
                }
                else
                {
                  moveFT = MoveParser.Parse(BV.board, Moves[Moves.length - 1]);
                }
                boolean playSound = (App.isLiveChess() && lccHolder.getGame(App.OnlineGame.values.get("game_id")).getSeq() == Moves.length)
                  || !App.isLiveChess();
								if(moveFT.length == 4){
									Move m;
									if(moveFT[3]==2)
										m = new Move(moveFT[0], moveFT[1], 0, 2);
									else
										m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
									BV.board.makeMove(m, playSound);
								} else {
									Move m = new Move(moveFT[0], moveFT[1], 0, 0);
									BV.board.makeMove(m, playSound);
								}
								//App.ShowMessage("Move list updated!");
								BV.board.movesCount = Moves.length;
								BV.invalidate();
								Update(0);
							}
						}
						return;
					}
					if( OG.values.get("has_new_message").equals("1") ){
						App.OnlineGame = OG;
						if(!msgShowed){
							msgShowed = true;
							new AlertDialog.Builder(Game.this)
							.setIcon(android.R.drawable.ic_dialog_alert)
				            .setTitle("You have got a new message!")
				            .setPositiveButton("Browse", new DialogInterface.OnClickListener() {
				                public void onClick(DialogInterface dialog, int whichButton) {
				                	chat = true;
									GetOnlineGame(App.gameId);
									msgShowed = false;
				                }
				            })
				            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				                public void onClick(DialogInterface dialog, int whichButton) {}
				            }).create().show();
						}
						return;
					} else{
						msgShowed = false;
					}
				}
				break;
			case 10:
        // handle game start

        getSoundPlayer().playGameStart();

        if (App.isLiveChess() && BV.board.mode == 4)
        {
          App.OnlineGame = new com.chess.model.Game(lccHolder.getGameData(App.gameId, -1), true);
          executePausedActivityGameEvents();
          //lccHolder.setActivityPausedMode(false);
          lccHolder.getWhiteClock().paint();
          lccHolder.getBlackClock().paint();
          /*int time = lccHolder.getGame(App.gameId).getGameTimeConfig().getBaseTime() * 100;
          lccHolder.setWhiteClock(new ChessClock(this, whiteClockView, time));
          lccHolder.setBlackClock(new ChessClock(this, blackClockView, time));*/
        }
        else
        {
          App.OnlineGame = ChessComApiParser.GetGameParseV3(response);
        }

				if(chat){
					if(!isUserColorWhite())
						App.SDeditor.putString("opponent", App.OnlineGame.values.get("white_username"));
					else
						App.SDeditor.putString("opponent", App.OnlineGame.values.get("black_username"));
					App.SDeditor.commit();
					App.OnlineGame.values.put("has_new_message", "0");
					startActivity(new Intent(Game.this, App.isLiveChess() ? ChatLive.class : Chat.class).
            putExtra("game_id", App.OnlineGame.values.get("game_id")).
            putExtra("timestamp", App.OnlineGame.values.get("timestamp")));
					chat = false;
					return;
				}

				if(App.OnlineGame.values.get("game_type").equals("2"))
					BV.board.chess960 = true;


				if(!isUserColorWhite()){
					BV.board.setReside(true);
				}
				String[] Moves = {};


                if (App.OnlineGame.values.get("move_list").contains("1.")) {
                    Moves = App.OnlineGame.values.get("move_list").replaceAll("[0-9]{1,4}[.]", "").replaceAll("  ", " ").substring(1).split(" ");
                    BV.board.movesCount = Moves.length;
                } else if (!App.isLiveChess()) {
                    BV.board.movesCount = 0;
                }

              final com.chess.live.client.Game game = lccHolder.getGame(App.gameId);
              if (game != null && game.getSeq() > 0)
              {
                lccHolder.doReplayMoves(game);
              }

				FEN = App.OnlineGame.values.get("starting_fen_position");
				if(!FEN.equals("")){
					BV.board.GenCastlePos(FEN);
					MoveParser.FenParse(FEN, BV.board);
				}

				int i;
        //System.out.println("@@@@@@@@ POINT 2 BV.board.movesCount=" + BV.board.movesCount);
        //System.out.println("@@@@@@@@ POINT 3 Moves=" + Moves);

            if (!App.isLiveChess())
            {
                for (i = 0; i < BV.board.movesCount; i++) {
                    //System.out.println("@@@@@@@@ POINT 4 i=" + i);
                    //System.out.println("================ POINT 5 Moves[i]=" + Moves[i]);
                    moveFT = MoveParser.Parse(BV.board, Moves[i]);
                    if (moveFT.length == 4) {
                        Move m;
                        if (moveFT[3] == 2) {
                            m = new Move(moveFT[0], moveFT[1], 0, 2);
                        } else {
                            m = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
                        }
                        BV.board.makeMove(m, false);
                    } else {
                        Move m = new Move(moveFT[0], moveFT[1], 0, 0);
                        BV.board.makeMove(m, false);
                    }
                }
            }

				Update(0);
				BV.board.takeBack();
				BV.invalidate();

				//last move anim
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1300);
							BV.board.takeNext();
							update.sendEmptyMessage(0);
						} catch (Exception e) {}
					}
					private Handler update = new Handler(){
						@Override
						public void dispatchMessage(Message msg) {
							super.dispatchMessage(msg);
							Update(0);
							BV.invalidate();
						}
					};
				}).start();
				if(BV.board.mode == 4 && appService != null && appService.repeatble == null){
					if(PD != null){
						PD.dismiss();
						PD = null;
					}
          if (!App.isLiveChess())
          {
            appService.RunRepeatbleTask(9, UPDATE_DELAY, UPDATE_DELAY,
                                        "http://www." + LccHolder.HOST + "/api/v3/get_game?id="+App.sharedData.getString("user_token", "")+"&gid="+App.gameId,
                                        null/*PD*/
            );
          }
				}
				break;

			default: break;
		}
	}



	@Override
	public Object onRetainNonConfigurationInstance() {
		return BV.board;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(BV.board.mode < 4){
			menu.add(0, 0, 0, getString(R.string.newgame)).setIcon(R.drawable.newgame);
			SubMenu Options = menu.addSubMenu(0, 1, 0, getString(R.string.options)).setIcon(R.drawable.options);
			menu.add(0, 2, 0, getString(R.string.reside)).setIcon(R.drawable.reside);
			menu.add(0, 3, 0, getString(R.string.hint)).setIcon(R.drawable.hint);
			menu.add(0, 4, 0, getString(R.string.prev)).setIcon(R.drawable.prev);
			menu.add(0, 5, 0, getString(R.string.next)).setIcon(R.drawable.next);

			Options.add(0, 6, 0, getString(R.string.ngwhite));
			Options.add(0, 7, 0, getString(R.string.ngblack));
			Options.add(0, 8, 0, getString(R.string.emailgame));
			Options.add(0, 9, 0, getString(R.string.menuSettings));
		}
    else if(BV.board.mode < 6)
    {
      SubMenu options;
      if(App.isLiveChess() && BV.board.mode == 4)
      {
        options = menu.addSubMenu(0, 0, 0, getString(R.string.options)).setIcon(R.drawable.options);
        if(App.OnlineGame.values.get("has_new_message").equals("1"))
        {
          menu.add(0, 6, 0, getString(R.string.chat)).setIcon(R.drawable.chat_nm);
        }
        else
        {
          menu.add(0, 6, 0, getString(R.string.chat)).setIcon(R.drawable.chat);
        }
      }
      else
      {
        menu.add(0, 0, 0, getString(R.string.nextgame)).setIcon(R.drawable.forward);
        options = menu.addSubMenu(0, 1, 0, getString(R.string.options)).setIcon(R.drawable.options);
        menu.add(0, 2, 0, getString(R.string.analysis)).setIcon(R.drawable.analysis);
        try {
          if(App.OnlineGame.values.get("has_new_message").equals("1"))
          {
            menu.add(0, 3, 0, getString(R.string.chat)).setIcon(R.drawable.chat_nm);
          }
          else
          {
            menu.add(0, 3, 0, getString(R.string.chat)).setIcon(R.drawable.chat);
          }
        } catch (Exception e)
        {
          menu.add(0, 3, 0, getString(R.string.chat)).setIcon(R.drawable.chat);
        }
        menu.add(0, 4, 0, getString(R.string.prev)).setIcon(R.drawable.prev);
        menu.add(0, 5, 0, getString(R.string.next)).setIcon(R.drawable.next);
      }
      if(App.isLiveChess() && BV.board.mode == 4)
      {
        options.add(0, 1, 0, getString(R.string.menuSettings)).setIcon(R.drawable.options);
        options.add(0, 2, 0, getString(R.string.reside)).setIcon(R.drawable.reside);
        options.add(0, 3, 0, getString(R.string.drawoffer));
        options.add(0, 4, 0, getString(resignOrAbort));
        options.add(0, 5, 0, getString(R.string.messages)).setIcon(R.drawable.chat);
      }
      else
      {
        options.add(0, 6, 0, getString(R.string.menuSettings)).setIcon(R.drawable.options);
        options.add(0, 7, 0, getString(R.string.backtogamelist)).setIcon(R.drawable.prev);
        options.add(0, 8, 0, getString(R.string.messages)).setIcon(R.drawable.chat);
        options.add(0, 9, 0, getString(R.string.reside)).setIcon(R.drawable.reside);
        options.add(0, 10, 0, getString(R.string.drawoffer));
        options.add(0, 11, 0, getString(R.string.resignorabort));
      }
    }
    else if(BV.board.mode == 6){
			menu.add(0, 0, 0, getString(R.string.nextgame)).setIcon(R.drawable.forward);
			SubMenu Options = menu.addSubMenu(0, 1, 0, getString(R.string.options)).setIcon(R.drawable.options);
			menu.add(0, 2, 0, getString(R.string.reside)).setIcon(R.drawable.reside);
			menu.add(0, 3, 0, getString(R.string.analysis)).setIcon(R.drawable.analysis);
			menu.add(0, 4, 0, getString(R.string.prev)).setIcon(R.drawable.prev);
			menu.add(0, 5, 0, getString(R.string.next)).setIcon(R.drawable.next);

			Options.add(0, 6, 0, getString(R.string.skipproblem));
			Options.add(0, 7, 0, getString(R.string.showanswer));
			Options.add(0, 8, 0, getString(R.string.menuSettings));

		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if(App.OnlineGame != null && BV.board.mode < 6 && BV.board.mode > 3)
		{
			int itemPosition = App.isLiveChess() ? 1 : 3;
			if( App.OnlineGame.values.get("has_new_message").equals("1"))
				menu.getItem(itemPosition).setIcon(R.drawable.chat_nm);
			else
				menu.getItem(itemPosition).setIcon(R.drawable.chat);
		}

		if(App.isLiveChess() && BV.board.mode == 4)
		{
			final SubMenu options = menu.getItem(0).getSubMenu();
			if (lccHolder.isFairPlayRestriction(App.gameId))
			{
				resignOrAbort = R.string.resign;
			}
			else if (lccHolder.isAbortableBySeq(App.gameId))
			{
				resignOrAbort = R.string.abort;
			}
			else
			{
				resignOrAbort = R.string.resign;
			}
			options.findItem(4).setTitle(resignOrAbort);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(BV.board.mode < 4){
			switch (item.getItemId()) {
				case 0:
					BV.stopThinking = true;
					finish();
			        return true;
			    case 1:
			    	BV.stopThinking = true;
			    	return true;
		    	case 2:
		    		BV.stopThinking = true;
		    		if(!BV.compmoving){
		    			BV.board.setReside(!BV.board.reside);
		    			if(BV.board.mode < 2){
		    				BV.board.mode ^= 1;
		    				BV.ComputerMove(App.strength[App.sharedData.getInt(App.sharedData.getString("username", "")+"strength", 0)]);
		    			}
		    			BV.invalidate();
		    			Update(0);
		    		}
			        return true;
			    case 3:
			    	BV.stopThinking = true;
			    	if(!BV.compmoving){
			    		BV.hint = true;
			    		BV.ComputerMove(App.strength[App.sharedData.getInt(App.sharedData.getString("username", "")+"strength", 0)]);
			    	}
			    	return true;
			    case 4:
			    	BV.stopThinking = true;
			    	if(!BV.compmoving){
			    		BV.finished = false;
			    		BV.sel = false;
				    	BV.board.takeBack();
						BV.invalidate();
						Update(0);
						isMoveNav = true;
			    	}
			        return true;
			    case 5:
			    	BV.stopThinking = true;
			    	if(!BV.compmoving){
			    		BV.sel = false;
					    BV.board.takeNext();
						BV.invalidate();
						Update(0);
						isMoveNav = true;
			    	}
			    	return true;
			    case 6:{
					BV.board = new Board(this);
					BV.board.mode = 0;
					BV.board.GenCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
					BV.invalidate();
					Update(0);
					return true;
				}
				case 7:{
					BV.board = new Board(this);
					BV.board.mode = 1;
					BV.board.setReside(true);
					BV.board.GenCastlePos("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
					BV.invalidate();
					Update(0);
					BV.ComputerMove(App.strength[App.sharedData.getInt(App.sharedData.getString("username", "")+"strength", 0)]);
					return true;
				}
				case 8:{
					String moves = movelist.getText().toString();
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			        emailIntent.setType("plain/text");
			        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Chess Game on Android - Chess.com");
			        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "[Site \"Chess.com Android\"]\n [White \""+App.sharedData.getString("username", "")+"\"]\n [White \""+App.sharedData.getString("username", "")+"\"]\n [Result \"X-X\"]\n \n \n "+moves+" \n \n Sent from my Android");
			        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			        return true;
				}
				case 9:{
					startActivity(new Intent(Game.this, Preferences.class));
					return true;
				}
		    }
    }
    else if(BV.board.mode < 6)
    {
      if(App.isLiveChess() && BV.board.mode == 4)
      {
        switch(item.getItemId())
        {
          case 1:
          {
            startActivity(new Intent(Game.this, Preferences.class));
            return true;
          }
          case 2:
          {
            BV.board.setReside(!BV.board.reside);
            BV.invalidate();
            return true;
          }
          case 3:
          {
            showDialog(4);
            return true;
          }
          case 4:
          {
            showDialog(5);
            return true;
          }
          case 5:
          case 6:
          {
            chat = true;
            GetOnlineGame(App.gameId);
            return true;
				  }
        }
      }
      else
      {
			switch (item.getItemId()) {
				case 0:
					  if(BV.board.mode == 4){
						int i;
						ArrayList<GameListElement> currentGames = new ArrayList<GameListElement>();
						for(GameListElement gle: App.GameListItems){
							if(gle.type == 1 && gle.values.get("is_my_turn").equals("1")){
								currentGames.add(gle);
							}
						}
						for(i=0;i<currentGames.size();i++){
							if(currentGames.get(i).values.get("game_id").contains(App.OnlineGame.values.get("game_id"))){
								if(i+1 < currentGames.size()){
									BV.board.analysis = false;
									BV.board = new Board(this);
									BV.board.mode = 4;
									GetOnlineGame(currentGames.get(i+1).values.get("game_id"));
									return true;
								} else{
									finish();
									return true;
								}
							}
						}
						finish();
						return true;
					} else if(BV.board.mode == 5){
						int i;
						ArrayList<GameListElement> currentGames = new ArrayList<GameListElement>();
						for(GameListElement gle: App.GameListItems){
							if(gle.type == 2){
								currentGames.add(gle);
							}
						}
						for(i=0;i<currentGames.size();i++){
							if(currentGames.get(i).values.get("game_id").contains(App.OnlineGame.values.get("game_id"))){
								if(i+1 < currentGames.size()){
									BV.board.analysis = false;
									BV.board = new Board(this);
									BV.board.mode = 5;
									GetOnlineGame(currentGames.get(i+1).values.get("game_id"));
									return true;
								} else{
									finish();
									return true;
								}
							}
						}
						finish();
						return true;
					}
            return true;
			    case 2:
			    	BV.board.analysis = true;
					Update(0);
			    	return true;
		    	case 3:
		    		chat = true;
		    		GetOnlineGame(App.gameId);
			      return true;
			    case 4:
			    	BV.finished = false;
		    		BV.sel = false;
			    	BV.board.takeBack();
					BV.invalidate();
					Update(0);
					isMoveNav = true;
			    	return true;
			    case 5:
			    	BV.board.takeNext();
					BV.invalidate();
					Update(0);
					isMoveNav = true;
			        return true;
			    case 6:{
			    	startActivity(new Intent(Game.this, Preferences.class));
			    	return true;
				}
				case 7:{
					finish();
					return true;
				}
				case 8:{
					chat = true;
					GetOnlineGame(App.gameId);
					return true;
				}
				case 9:{
					BV.board.setReside(!BV.board.reside);
			    	BV.invalidate();
					return true;
				}
				case 10:{
					showDialog(4);
					return true;
				}
				case 11:{
					showDialog(5);
					return true;
				}
      }
			}
		} else if(BV.board.mode == 6){
			switch (item.getItemId()) {
				case 0:
					if(App.guest){
						App.currentTacticProblem++;
						GetGuestTacticsGame();
					} else{
						if(App.noInternet)	App.currentTacticProblem++;
						closeOptionsMenu();
						GetTacticsGame("");
					}
			        return true;
			    case 2:
			    	BV.board.setReside(!BV.board.reside);
			    	BV.invalidate();
			    	return true;
		    	case 3:
		    		BV.board.analysis = true;
					Update(0);
			        return true;
			    case 4:
			    	BV.finished = false;
		    		BV.sel = false;
			    	BV.board.takeBack();
					BV.invalidate();
					Update(0);
					isMoveNav = true;
			    	return true;
			    case 5:
			    	BV.board.takeNext();
					BV.invalidate();
					Update(0);
					isMoveNav = true;
			        return true;
			    case 6:{
			    	if(App.guest || App.noInternet){
						App.currentTacticProblem++;
						GetGuestTacticsGame();
					} else
						GetTacticsGame("");
			    	return true;
				}
				case 7:{
					ShowAnswer();
					return true;
				}
				case 8:{
					startActivity(new Intent(Game.this, Preferences.class));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		if(isMoveNav){
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
		BV.requestFocus();
		super.onWindowFocusChanged(hasFocus);
	}
	@Override
	protected void onResume() {
		if (MobclixHelper.isShowAds(App) && !App.mTabHost.getCurrentTabTag().equals("tab4") && adviewWrapper != null && getRectangleAdview() != null)
		{
			adviewWrapper.addView(getRectangleAdview());
			if (App.isForceRectangleAd())
			{
				getRectangleAdview().getAd();
			}
		}

    if (/*!App.isNetworkChangedNotification() && */extras.containsKey("liveChess"))
    {
      App.setLiveChess(extras.getBoolean("liveChess"));
      if (!App.isLiveChess())
      {
        new AsyncTask<Void, Void, Void>()
        {
          @Override
          protected Void doInBackground(Void... voids)
          {
            App.getLccHolder().logout();
            return null;
          }
        }.execute();
      }
    }

    super.onResume();

    registerReceiver(gameMoveReceiver, new IntentFilter("com.chess.lcc.android-game-move"));
    registerReceiver(gameEndMessageReceiver, new IntentFilter("com.chess.lcc.android-game-end"));
    registerReceiver(gameInfoMessageReceived, new IntentFilter("com.chess.lcc.android-game-info"));
    registerReceiver(chatMessageReceiver, new IntentFilter("com.chess.lcc.android-game-chat-message"));
    registerReceiver(showGameEndPopupReceiver, new IntentFilter("com.chess.lcc.android-show-game-end-popup"));

		if(BV.board.mode == 6){
			if(BV.board.tacticCanceled){
				BV.board.tacticCanceled = false;
				showDialog(1);
				startTacticsTimer();
			}
			else if (App.Tactic != null && App.Tactic.values.get("stop").equals("0"))
			{
				startTacticsTimer();
			}
		}
    if (App.isLiveChess() && App.gameId != null && App.gameId != "" && lccHolder.getGame(App.gameId) != null)
    {
      OG = new com.chess.model.Game(lccHolder.getGameData(App.gameId, lccHolder.getGame(App.gameId).getSeq()-1), true);
      lccHolder.getAndroid().setGameActivity(this);
      if (lccHolder.isActivityPausedMode())
      {
        executePausedActivityGameEvents();
        lccHolder.setActivityPausedMode(false);
      }
      //lccHolder.updateClockTime(lccHolder.getGame(App.gameId));
    }

	/*MobclixAdView bannerAdview = App.getBannerAdview();
	LinearLayout bannerAdviewWrapper = App.getBannerAdviewWrapper();
	if (bannerAdviewWrapper != null)
	{
		bannerAdviewWrapper.removeView(bannerAdview);
	}*/
	MobclixHelper.pauseAdview(App.getBannerAdview(), App);
	/*App.setBannerAdview(null);
	App.setBannerAdviewWrapper(null);*/
	//App.setForceBannerAdOnFailedLoad(true);

    disableScreenLock();
	}
	@Override
	protected void onPause() {
		System.out.println("LCCLOG2: GAME ONPAUSE");
		unregisterReceiver(gameMoveReceiver);
		unregisterReceiver(gameEndMessageReceiver);
		unregisterReceiver(gameInfoMessageReceived);
		unregisterReceiver(chatMessageReceiver);
		unregisterReceiver(showGameEndPopupReceiver);

		super.onPause();
		System.out.println("LCCLOG2: GAME ONPAUSE adviewWrapper=" + adviewWrapper + ", getRectangleAdview() " + getRectangleAdview());
		if (adviewWrapper != null && getRectangleAdview() != null)
		{
			System.out.println("LCCLOG2: GAME ONPAUSE 1");
			getRectangleAdview().cancelAd();
			System.out.println("LCCLOG2: GAME ONPAUSE 2");
			adviewWrapper.removeView(getRectangleAdview());
			System.out.println("LCCLOG2: GAME ONPAUSE 3");
		}
		lccHolder.setActivityPausedMode(true);
		lccHolder.getPausedActivityGameEvents().clear();

		BV.stopThinking = true;

		stopTacticsTimer();
		if(OnlineGameUpdate != null)
			OnlineGameUpdate.cancel();

		/*if (MobclixHelper.isShowAds(App))
		{
			MobclixHelper.pauseAdview(getRectangleAdview(), App);
		}*/
		
		enableScreenLock();
	}

	public void stopTacticsTimer(){
		if(TacticsTimer != null){
			TacticsTimer.cancel();
			TacticsTimer = null;
		}
	}
	public void startTacticsTimer(){
		stopTacticsTimer();
		BV.finished = false;
		if (App.Tactic != null)
		{
			App.Tactic.values.put("stop", "0");
		}
		TacticsTimer = new Timer();
		TacticsTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if(BV.board.analysis)
					return;
				BV.board.sec++;
				if(BV.board.left>0)
					BV.board.left --;
				update.sendEmptyMessage(0);
			}
			private Handler update = new Handler(){
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					timer.setText("Bonus Time Left: "+BV.board.left+" Time Spent: "+BV.board.sec);
				}
			};
		}, 0, 1000);
	}

  private BroadcastReceiver gameMoveReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
      OG = (com.chess.model.Game) intent.getSerializableExtra("object");
      Update(9);
    }
  };

  private BroadcastReceiver gameEndMessageReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, final Intent intent)
    {
      LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());

      final com.chess.live.client.Game game = lccHolder.getGame(App.gameId);
      Integer newWhiteRating = null;
      Integer newBlackRating = null;
      switch(game.getGameTimeConfig().getGameTimeClass())
      {
        case BLITZ:
        {
          newWhiteRating = game.getWhitePlayer().getBlitzRating();
          newBlackRating = game.getBlackPlayer().getBlitzRating();
          break;
        }
        case LIGHTNING:
        {
          newWhiteRating = game.getWhitePlayer().getQuickRating();
          newBlackRating = game.getBlackPlayer().getQuickRating();
          break;
        }
        case STANDARD:
        {
          newWhiteRating = game.getWhitePlayer().getStandardRating();
          newBlackRating = game.getBlackPlayer().getStandardRating();
          break;
        }
      }
      /*final String whiteRating =
        (newWhiteRating != null && newWhiteRating != 0) ?
        newWhiteRating.toString() : App.OnlineGame.values.get("white_rating");
      final String blackRating =
        (newBlackRating != null && newBlackRating != 0) ?
        newBlackRating.toString() : App.OnlineGame.values.get("black_rating");*/
      white.setText(game.getWhitePlayer().getUsername() + "(" + newWhiteRating + ")");
      black.setText(game.getBlackPlayer().getUsername() + "(" + newBlackRating + ")");
      BV.finished = true;

      if (MobclixHelper.isShowAds(App))
      {
		final LayoutInflater inflater = (LayoutInflater) Game.this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.ad_popup,
				(ViewGroup) findViewById(R.id.layout_root));
		showGameEndPopup(layout, intent.getExtras().getString("title") + ": " + intent.getExtras().getString("message"));

        final View newGame = layout.findViewById(R.id.newGame);
        newGame.setOnClickListener(new OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            if (adPopup != null)
            {
              try
              {
                adPopup.dismiss();
              }
              catch (Exception e)
              {
              }
              adPopup = null;
            }
            startActivity(new Intent(Game.this, OnlineNewGame.class));
          }
        });
        newGame.setVisibility(View.VISIBLE);

        final View home = layout.findViewById(R.id.home);
        home.setOnClickListener(new OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            if (adPopup != null)
            {
              try
              {
                adPopup.dismiss();
              }
              catch (Exception e)
              {
              }
              adPopup = null;
            }
            startActivity(new Intent(Game.this, Tabs.class));
          }
        });
        home.setVisibility(View.VISIBLE);
      }

		endOfGameMessage.setText(/*intent.getExtras().getString("title") + ": " +*/ intent.getExtras().getString("message"));
		//App.ShowDialog(Game.this, intent.getExtras().getString("title"), intent.getExtras().getString("message"));
		findViewById(R.id.moveButtons).setVisibility(View.GONE);
		findViewById(R.id.endOfGameButtons).setVisibility(View.VISIBLE);
		chatPanel.setVisibility(View.GONE);
		findViewById(R.id.newGame).setOnClickListener(new OnClickListener()
		{
		  @Override
		  public void onClick(View v)
		  {
		    startActivity(new Intent(Game.this, OnlineNewGame.class));
		  }
		});
		findViewById(R.id.home).setOnClickListener(new OnClickListener()
		{
		  @Override
		  public void onClick(View v)
		  {
		    startActivity(new Intent(Game.this, Tabs.class));
		  }
		});
      getSoundPlayer().playGameEnd();
    }
  };

  private BroadcastReceiver gameInfoMessageReceived = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
      App.ShowDialog(Game.this, intent.getExtras().getString("title"), intent.getExtras().getString("message"));
    }
  };

  public TextView getWhiteClockView()
  {
    return whiteClockView;
  }

  public TextView getBlackClockView()
  {
    return blackClockView;
  }

  private void executePausedActivityGameEvents()
  {
    if (/*lccHolder.isActivityPausedMode() && */lccHolder.getPausedActivityGameEvents().size() > 0)
    {
      //boolean fullGameProcessed = false;
      GameEvent gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.Move);
      if (gameEvent != null &&
          (lccHolder.getCurrentGameId() == null || lccHolder.getCurrentGameId().equals(gameEvent.gameId)))
      {
        //lccHolder.processFullGame(lccHolder.getGame(gameEvent.gameId.toString()));
        //fullGameProcessed = true;
          lccHolder.getPausedActivityGameEvents().remove(gameEvent);
          //lccHolder.getAndroid().processMove(gameEvent.gameId, gameEvent.moveIndex);
          OG = new com.chess.model.Game(lccHolder.getGameData(gameEvent.gameId.toString(), gameEvent.moveIndex), true);
          Update(9);
      }

      gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.DrawOffer);
      if (gameEvent != null &&
          (lccHolder.getCurrentGameId() == null || lccHolder.getCurrentGameId().equals(gameEvent.gameId)))
      {
        /*if (!fullGameProcessed)
        {
          lccHolder.processFullGame(lccHolder.getGame(gameEvent.gameId.toString()));
          fullGameProcessed = true;
        }*/
        lccHolder.getPausedActivityGameEvents().remove(gameEvent);
        lccHolder.getAndroid().processDrawOffered(gameEvent.drawOffererUsername);
      }

      gameEvent = lccHolder.getPausedActivityGameEvents().get(GameEvent.Event.EndOfGame);
      if (gameEvent != null &&
          (lccHolder.getCurrentGameId() == null || lccHolder.getCurrentGameId().equals(gameEvent.gameId)))
      {
        /*if (!fullGameProcessed)
        {
          lccHolder.processFullGame(lccHolder.getGame(gameEvent.gameId.toString()));
          fullGameProcessed = true;
        }*/
        lccHolder.getPausedActivityGameEvents().remove(gameEvent);
        lccHolder.getAndroid().processGameEnd(gameEvent.gameEndedMessage);
      }
    }
  }

  /*public void onStop()
  {
    App.OnlineGame = null;
    BV.board = null;
    super.onStop();
  }*/

  private void showAnalysisButtons()
  {
    analysisButtons.setVisibility(View.VISIBLE);
    findViewById(R.id.moveButtons).setVisibility(View.GONE);
    /*BV.board.takeBack();
    BV.board.movesCount--;
    BV.invalidate();
    BV.board.submit = false;*/
  }

	private void hideAnalysisButtons()
	{
		analysisButtons.setVisibility(View.GONE);
	}

  private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      //LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
      chatPanel.setVisibility(View.VISIBLE);
    }
  };

  private BroadcastReceiver showGameEndPopupReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, final Intent intent)
    {
        if (!MobclixHelper.isShowAds(App))
        {
          return;
        }

        final LayoutInflater inflater = (LayoutInflater) Game.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.ad_popup, (ViewGroup) findViewById(R.id.layout_root));
        showGameEndPopup(layout, intent.getExtras().getString("message"));

        final Button ok = (Button) layout.findViewById(R.id.home);
        ok.setText("OK");
        ok.setOnClickListener(new OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            if (adPopup != null)
            {
              try
              {
                adPopup.dismiss();
              }
              catch (Exception e)
              {
              }
              adPopup = null;
            }
            if (intent.getBooleanExtra("finishable", false))
            {
              finish();
            }
          }
        });
        ok.setVisibility(View.VISIBLE);
    }
  };

	public void showGameEndPopup(final View layout, final String message) {
		if (!MobclixHelper.isShowAds(App))
		{
			return;
		}

		if (adPopup != null)
		{
			try
			{
				adPopup.dismiss();
			}
			catch (Exception e)
			{
				System.out.println("MOBCLIX: EXCEPTION IN showGameEndPopup");
				e.printStackTrace();
			}
			adPopup = null;
		}

		try
		{
			if (adviewWrapper != null && getRectangleAdview() != null)
			{
				adviewWrapper.removeView(getRectangleAdview());
			}
			adviewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
			System.out.println("MOBCLIX: GET WRAPPER " + adviewWrapper);
			adviewWrapper.addView(getRectangleAdview());

			adviewWrapper.setVisibility(View.VISIBLE);
			//showGameEndAds(adviewWrapper);

			TextView endOfGameMessagePopup = (TextView) layout.findViewById(R.id.endOfGameMessage);
			endOfGameMessagePopup.setText(message);

			adPopup.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialogInterface)
				{
					if (adviewWrapper != null && getRectangleAdview() != null)
					{
						adviewWrapper.removeView(getRectangleAdview());
					}
				}
			});
			adPopup.setOnDismissListener(new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialogInterface)
				{
					if (adviewWrapper != null && getRectangleAdview() != null)
					{
						adviewWrapper.removeView(getRectangleAdview());
					}
				}
			});
		}
		catch (Exception e)
		{
			System.out.println("MOBCLIX: EXCEPTION IN showGameEndPopup");
			e.printStackTrace();
		}

		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				AlertDialog.Builder builder;
				//Context mContext = getApplicationContext();
				builder = new AlertDialog.Builder(Game.this);
				builder.setView(layout);
				adPopup = builder.create();
				adPopup.setCancelable(true);
				adPopup.setCanceledOnTouchOutside(true);
				try
				{
					adPopup.show();
				}
				catch (Exception e)
				{
					return;
				}
			}
		}, 1500);
	}
}
