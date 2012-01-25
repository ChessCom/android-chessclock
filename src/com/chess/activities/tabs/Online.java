package com.chess.activities.tabs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import com.chess.R;
import com.chess.activities.Chat;
import com.chess.activities.CreateChallenge;
import com.chess.activities.Game;
import com.chess.activities.OnlineNewGame;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.model.GameListElement;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.Web;
import com.chess.views.OnlineGamesAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class Online extends CoreActivity {
	private ListView GamesList;
	private Spinner GamesType;
	private OnlineGamesAdapter GamesAdapter = null;
	private TextView challengesListTitle;
	private TextView startNewGameTitle;
	private TextView tournaments;
	private TextView stats;
	private Button currentGame;
	private Button start;
	private GridView gridview;

	private String[] queries;
	private boolean compleated = false;
	private int UPDATE_DELAY = 120000;
	private int temp_pos = -1;

	public static int ONLINE_CALLBACK_CODE = 32;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case 0: {
				if (temp_pos > -1) {
					final GameListElement el = mainApp.getGameListItems().get(temp_pos);
					return new AlertDialog.Builder(this)
							.setTitle("     Accept Draw?     ")
							.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									if (appService != null) {
										appService.RunSingleTask(4,
												"http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" + mainApp.getSharedData().getString("user_token", "") + "&chessid=" + el.values.get("game_id") + "&command=ACCEPTDRAW&timestamp=" + el.values.get("timestamp"),
												null/*PD = MyProgressDialog.show(Online.this, null, getString(R.string.loading), true)*/
										);
									}
								}
							})
							.setNeutralButton(getString(R.string.decline), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									if (appService != null) {
										appService.RunSingleTask(4,
												"http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" + mainApp.getSharedData().getString("user_token", "") + "&chessid=" + el.values.get("game_id") + "&command=DECLINEDRAW&timestamp=" + el.values.get("timestamp"),
												null/*PD = MyProgressDialog.show(Online.this, null, getString(R.string.loading), true)*/
										);
									}
								}
							})
							.setNegativeButton(getString(R.string.game), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									startActivity(new Intent(Online.this, Game.class).
											putExtra("mode", 4).
											putExtra("game_id", el.values.get("game_id")));
								}
							}).create();
				}
			}
			default:
				break;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onStop() {
		GamesList.setVisibility(View.GONE);
		super.onStop();
	}

	@Override
	protected void onRestart() {
		GamesList.setVisibility(View.VISIBLE);
		super.onRestart();
	}

	protected void onResume() {
		/*if (isShowAds() && (!mainApp.isLiveChess() || (mainApp.isLiveChess() && lccHolder.isConnected()))) {
			  MobclixHelper.showBannerAd(getBannerAdviewWrapper(), removeAds, this, mainApp);
			}*/
		/*if (!mainApp.isNetworkChangedNotification())
			{*/
		mainApp.setLiveChess(extras.getBoolean("liveChess"));
		//}
		if (mainApp.isLiveChess() && !lccHolder.isConnected()) {
			new Handler().post(new Runnable() {
				public void run() {
					start.setVisibility(View.GONE);
					gridview.setVisibility(View.GONE);
					challengesListTitle.setVisibility(View.GONE);
					startNewGameTitle.setVisibility(View.GONE);
				}
			});
		}
		registerReceiver(this.lccLoggingInInfoReceiver, new IntentFilter("com.chess.lcc.android-logging-in-info"));
		if (mainApp.isLiveChess()) {
			registerReceiver(challengesListUpdateReceiver, new IntentFilter("com.chess.lcc.android-challenges-list-update"));
		} else {
			// if connected
			//System.out.println("MARKER++++++++++++++++++++++++++++++++++++++++++++++++++++ LOGOUT");
			lccHolder.logout();
		}
		super.onResume();
		currentGame.post(new Runnable() {
			public void run() {
				if (mainApp.isLiveChess() && lccHolder.getCurrentGameId() != null &&
						lccHolder.getGame(lccHolder.getCurrentGameId()) != null) {
					currentGame.setVisibility(View.VISIBLE);
				} else {
					currentGame.setVisibility(View.GONE);
				}
			}
		});
		GamesList.setVisibility(View.VISIBLE);

		/*if (GamesAdapter != null)
			{
			  GamesAdapter.clear();
			  GamesAdapter = null;
			  mainApp.getGameListItems().clear();
			  GamesAdapter.notifyDataSetChanged();
			}
			else
			{
			  mainApp.getGameListItems().clear();
			}*/

		new Handler().post(new Runnable() {
			public void run() {
				disableScreenLock();
			}
		});
		if (mainApp.isLiveChess()) {
			start.setText("Custom Challenge");
		} else {
			start.setText("Challenge");
		}
	}

	@Override
	protected void onPause() {
		GamesList.setVisibility(View.GONE);
		unregisterReceiver(this.lccLoggingInInfoReceiver);
		if (mainApp.isLiveChess()) {
			/*// if connected
				  System.out.println("MARKER++++++++++++++++++++++++++++++++++++++++++++++++++++ LOGOUT");
				  lccHolder.logout();*/
			unregisterReceiver(challengesListUpdateReceiver);
		}
		super.onPause();
		enableScreenLock();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online);

		queries = new String[]{
				"http://www." + LccHolder.HOST + "/api/echess_challenges?id=" + mainApp.getSharedData().getString("user_token", ""),
				"http://www." + LccHolder.HOST + "/api/v2/get_echess_current_games?id=" + mainApp.getSharedData().getString("user_token", "") + "&all=1",
				"http://www." + LccHolder.HOST + "/api/v2/get_echess_finished_games?id=" + mainApp.getSharedData().getString("user_token", "")};

		GamesType = (Spinner) findViewById(R.id.gamestypes);
		challengesListTitle = (TextView) findViewById(R.id.challengesListTitle);
		startNewGameTitle = (TextView) findViewById(R.id.startNewGameTitle);
		tournaments = (TextView) findViewById(R.id.tournaments);
		stats = (TextView) findViewById(R.id.stats);

		start = (Button) findViewById(R.id.start);
		start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoadNext(0);
			}
		});

		/*if (!mainApp.isNetworkChangedNotification())
			{*/
		mainApp.setLiveChess(extras.getBoolean("liveChess"));
		//}
		if (mainApp.isLiveChess()) {
			tournaments.setVisibility(View.GONE);
			stats.setVisibility(View.GONE);
			GamesType.setVisibility(View.GONE);
			gridview = (GridView) findViewById(R.id.gridview);
		} else {
			tournaments.setVisibility(View.VISIBLE);
			stats.setVisibility(View.VISIBLE);
			GamesType.setVisibility(View.VISIBLE);
			start.setVisibility(View.VISIBLE);
			challengesListTitle.setVisibility(View.GONE);
			startNewGameTitle.setVisibility(View.GONE);
		}

		if (mainApp.isLiveChess() && lccHolder.isConnected()) {
			start.setVisibility(View.VISIBLE);
			gridview.setVisibility(View.VISIBLE);
			challengesListTitle.setVisibility(View.VISIBLE);
			startNewGameTitle.setVisibility(View.VISIBLE);
		}

		GamesType.post(new Runnable() {
			@Override
			public void run() {
				GamesType.setSelection(mainApp.getSharedData().getInt("gamestype", 1));
			}
		});
		GamesType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				GamesAdapter = null;
				mainApp.getSharedDataEditor().putInt("gamestype", pos);
				mainApp.getSharedDataEditor().commit();
				if (compleated && appService != null && appService.repeatble != null) {
					onPause();
					onResume();
				}
				compleated = true;
			}

			@Override
			public void onNothingSelected(AdapterView<?> a) {
			}
		});

		GamesList = (ListView) findViewById(R.id.GamesList);
		GamesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
				final GameListElement el = mainApp.getGameListItems().get(pos);
				if (el.type == 0) {
					final String title = mainApp.isLiveChess() ?
							el.values.get("opponent_chess_title") :
							"Win: " + el.values.get("opponent_win_count") + " Loss: " + el.values.get("opponent_loss_count") + " Draw: " + el.values.get("opponent_draw_count");

					if (mainApp.isLiveChess()) {
						if (el.values.get("is_direct_challenge").equals("1") && el.values.get("is_released_by_me").equals("0")) {
							new AlertDialog.Builder(Online.this)
									.setTitle(title)
									.setItems(new String[]{getString(R.string.accept), getString(R.string.decline)}, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface d, int pos) {
											if (pos == 0) {
												final Challenge challenge = lccHolder.getChallenge(el.values.get("game_id"));
												LccHolder.LOG.info("Accept challenge: " + challenge);
												lccHolder.getAndroid().runAcceptChallengeTask(challenge);
												lccHolder.removeChallenge(el.values.get("game_id"));
												Update(2);
											} else if (pos == 1) {
												final Challenge challenge = lccHolder.getChallenge(el.values.get("game_id"));
												LccHolder.LOG.info("Decline challenge: " + challenge);
												lccHolder.getAndroid().runRejectChallengeTask(challenge);
												lccHolder.removeChallenge(el.values.get("game_id"));
												Update(3);
											}
										}
									})
									.create().show();
						} else if (el.values.get("is_direct_challenge").equals("1") && el.values.get("is_released_by_me").equals("1")) {
							new AlertDialog.Builder(Online.this)
									.setTitle(title)
									.setItems(new String[]{"Cancel", "Keep"}, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface d, int pos) {
											if (pos == 0) {
												final Challenge challenge = lccHolder.getChallenge(el.values.get("game_id"));
												LccHolder.LOG.info("Cancel my challenge: " + challenge);
												lccHolder.getAndroid().runCancelChallengeTask(challenge);
												lccHolder.removeChallenge(el.values.get("game_id"));
												Update(4);
											} else if (pos == 1) {
												final Challenge challenge = lccHolder.getChallenge(el.values.get("game_id"));
												LccHolder.LOG.info("Just keep my challenge: " + challenge);
											}
										}
									})
									.create().show();
						} else if (el.values.get("is_direct_challenge").equals("0") && el.values.get("is_released_by_me").equals("0")) {
							final Challenge challenge = lccHolder.getSeek(el.values.get("game_id"));
							LccHolder.LOG.info("Accept seek: " + challenge);
							lccHolder.getAndroid().runAcceptChallengeTask(challenge);
							lccHolder.removeSeek(el.values.get("game_id"));
							Update(2);
						} else if (el.values.get("is_direct_challenge").equals("0") && el.values.get("is_released_by_me").equals("1")) {
							new AlertDialog.Builder(Online.this)
									.setTitle(title)
									.setItems(new String[]{"Cancel", "Keep"}, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface d, int pos) {
											if (pos == 0) {
												final Challenge challenge = lccHolder.getSeek(el.values.get("game_id"));
												LccHolder.LOG.info("Cancel my seek: " + challenge);
												lccHolder.getAndroid().runCancelChallengeTask(challenge);
												lccHolder.removeSeek(el.values.get("game_id"));
												Update(4);
											} else if (pos == 1) {
												final Challenge challenge = lccHolder.getSeek(el.values.get("game_id"));
												LccHolder.LOG.info("Just keep my seek: " + challenge);
											}
										}
									})
									.create().show();
						}
					} // echess
					else {
						new AlertDialog.Builder(Online.this)
								.setTitle(title)
								.setItems(new String[]{getString(R.string.accept), getString(R.string.decline)}, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface d, int pos) {
										if (pos == 0) {
											String result = Web.Request("http://www." + LccHolder.HOST + "/api/echess_open_invites?id=" + mainApp.getSharedData().getString("user_token", "") + "&acceptinviteid=" + el.values.get("game_id"), "GET", null, null);
											if (result.contains("Success")) {
												Update(2);
											} else if (result.contains("Error+")) {
												mainApp.ShowDialog(Online.this, "Error", result.split("[+]")[1]);
											} else {
												//mainApp.ShowDialog(Online.this, "Error", result);
											}
										} else if (pos == 1) {

											String result = Web.Request("http://www." + LccHolder.HOST + "/api/echess_open_invites?id=" + mainApp.getSharedData().getString("user_token", "") + "&declineinviteid=" + el.values.get("game_id"), "GET", null, null);
											if (result.contains("Success")) {
												Update(3);
											} else if (result.contains("Error+")) {
												mainApp.ShowDialog(Online.this, "Error", result.split("[+]")[1]);
											} else {
												//mainApp.ShowDialog(Online.this, "Error", result);
											}
										}
									}
								})
								.create().show();
					}

				} else if (el.type == 1) {
					mainApp.getSharedDataEditor().putString("opponent", el.values.get("opponent_username"));
					mainApp.getSharedDataEditor().commit();

					if (el.values.get("is_draw_offer_pending").equals("p")) {
						mainApp.acceptdraw = true;
						temp_pos = pos;
						showDialog(0);
					} else {
						mainApp.acceptdraw = false;
						startActivity(new Intent(Online.this, Game.class).
								putExtra("mode", 4).
								putExtra("game_id", el.values.get("game_id")));
					}
				} else if (el.type == 2) {
					mainApp.getSharedDataEditor().putString("opponent", el.values.get("opponent_username"));
					mainApp.getSharedDataEditor().commit();
					startActivity(new Intent(Online.this, Game.class).
							putExtra("mode", 5).
							putExtra("game_id", el.values.get("game_id")));
				}
			}
		});
		GamesList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> a, View v, int pos, long id) {
				final GameListElement el = mainApp.getGameListItems().get(pos);
				if (el.type == 1) {
					new AlertDialog.Builder(Online.this)
							.setItems(new String[]{getString(R.string.chat), getString(R.string.drawoffer), getString(R.string.resignorabort)}, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface d, int pos) {
									if (pos == 0) {
										mainApp.getSharedDataEditor().putString("opponent", el.values.get("opponent_username"));
										mainApp.getSharedDataEditor().commit();
										startActivity(new Intent(Online.this, Chat.class).
												putExtra("game_id", el.values.get("game_id")).
												putExtra("timestamp", el.values.get("timestamp")));
									} else if (pos == 1) {
										String Draw = "OFFERDRAW";
										if (el.values.get("is_draw_offer_pending").equals("p"))
											Draw = "ACCEPTDRAW";
										String result = Web.Request("http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" + mainApp.getSharedData().getString("user_token", "") + "&chessid=" + el.values.get("game_id") + "&command=" + Draw + "&timestamp=" + el.values.get("timestamp"), "GET", null, null);
										if (result.contains("Success")) {
											mainApp.ShowMessage(getString(R.string.accepted));
											Update(1);
										} else if (result.contains("Error+")) {
											mainApp.ShowDialog(Online.this, "Error", result.split("[+]")[1]);
										} else {
											//mainApp.ShowDialog(Online.this, "Error", result);
										}
									} else if (pos == 2) {
										String result = Web.Request("http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" + mainApp.getSharedData().getString("user_token", "") + "&chessid=" + el.values.get("game_id") + "&command=RESIGN&timestamp=" + el.values.get("timestamp"), "GET", null, null);
										if (result.contains("Success")) {
											Update(1);
										} else if (result.contains("Error+")) {
											mainApp.ShowDialog(Online.this, "Error", result.split("[+]")[1]);
										} else {
											//mainApp.ShowDialog(Online.this, "Error", result);
										}
									}
								}
							})
							.create().show();
				} else if (el.type == 2) {
					mainApp.getSharedDataEditor().putString("opponent", el.values.get("opponent_username"));
					mainApp.getSharedDataEditor().commit();
					startActivity(new Intent(Online.this, Chat.class).
							putExtra("game_id", el.values.get("game_id")).
							putExtra("timestamp", el.values.get("timestamp")));
				}
				return true;
			}
		});
		findViewById(R.id.tournaments).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String GOTO = "http://www." + LccHolder.HOST + "/tournaments";
				try {
					GOTO = URLEncoder.encode(GOTO, "UTF-8");
				} catch (UnsupportedEncodingException e) {
				}
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/login.html?als=" + mainApp.getSharedData().getString("user_token", "") + "&goto=" + GOTO)));
			}
		});
		findViewById(R.id.stats).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String GOTO = "http://www." + LccHolder.HOST + "/echess/mobile-stats/" + mainApp.getSharedData().getString("username", "");
				try {
					GOTO = URLEncoder.encode(GOTO, "UTF-8");
				} catch (UnsupportedEncodingException e) {
				}
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/login.html?als=" + mainApp.getSharedData().getString("user_token", "") + "&goto=" + GOTO)));
			}
		});
		currentGame = (Button) findViewById(R.id.currentGame);
		currentGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*try
							{*/
				if (lccHolder.getCurrentGameId() != null && lccHolder.getGame(lccHolder.getCurrentGameId()) != null) {
					lccHolder.processFullGame(lccHolder.getGame(lccHolder.getCurrentGameId()));
				}
				/*}
							catch(Exception e)
							{
							  e.printStackTrace();
							  System.out.println("!!!!!!!! mainApp.getGameId() " + mainApp.getGameId());
							  System.out.println("!!!!!!!! lccHolder.getGame(mainApp.getGameId()) " + lccHolder.getGame(mainApp.getGameId()));
							}*/
			}
		});
		if (mainApp.isLiveChess()) {
			gridview.setAdapter(new BaseAdapter() {
				public int getCount() {
					return StartNewGameButtonsEnum.values().length;
				}

				public Object getItem(int position) {
					return null;
				}

				public long getItemId(int position) {
					return position;
				}

				public View getView(int position, View convertView, ViewGroup parent) {
					final Button button;
					if (convertView == null) {
						button = new Button(mainApp);
					} else {
						button = (Button) convertView;
					}
					StartNewGameButtonsEnum.values();
					final StartNewGameButtonsEnum startNewGameButton = StartNewGameButtonsEnum.values()[position];
					button.setText(startNewGameButton.getText());
					button.setOnClickListener(new OnClickListener() {
						public void onClick(View view) {
							mainApp.getSharedDataEditor().putString("initialTime", "" + startNewGameButton.getMin());
							mainApp.getSharedDataEditor().putString("bonusTime", "" + startNewGameButton.getSec());
							mainApp.getSharedDataEditor().commit();
							startActivity(new Intent(Online.this, CreateChallenge.class));
						}
					});
					return button;
				}
			}
			);
		}
	}

	@Override
	public void LoadNext(int code) {
		//GamesList.setVisibility(View.GONE);
		finish();
		startActivity(new Intent(this, OnlineNewGame.class));
	}

	@Override
	public void LoadPrev(int code) {
		//finish();
		mainApp.getTabHost().setCurrentTab(0);
	}

	@Override
	public void Update(int code) {
		if (code == -1) {
			if (appService != null) {
				if (!mainApp.isLiveChess()) {
					appService.RunRepeatbleTask(ONLINE_CALLBACK_CODE, 0, UPDATE_DELAY,
							queries[mainApp.getSharedData().getInt("gamestype", 1)],
							null/*PD = MyProgressDialog
                                        .show(Online.this, null, getString(R.string.updatinggameslist), true)*/);
				} else {
					/*appService.RunRepeatble(ONLINE_CALLBACK_CODE, 0, 2000,
													  PD = MyProgressDialog
														.show(Online.this, null, getString(R.string.updatinggameslist), true));*/
					Update(ONLINE_CALLBACK_CODE);
				}
			}
		} else if (code == ONLINE_CALLBACK_CODE) {
			int t = mainApp.getSharedData().getInt("gamestype", 1);
			ArrayList<GameListElement> tmp = new ArrayList<GameListElement>();
			GamesList.setVisibility(View.GONE);
			mainApp.getGameListItems().clear();
			if (GamesAdapter != null) {
				GamesAdapter.notifyDataSetChanged();
			}
			//GamesList.setVisibility(View.VISIBLE);
			if (mainApp.isLiveChess()) {
				tmp.addAll(lccHolder.getChallengesAndSeeksData());
			} else {
				if (t == 0) {
					tmp.addAll(ChessComApiParser.ViewChallengeParse(rep_response));
				}
				if (t == 1) {
					tmp.addAll(ChessComApiParser.GetCurrentOnlineGamesParse(rep_response));
				}
				if (t == 2) {
					tmp.addAll(ChessComApiParser.GetFinishedOnlineGamesParse(rep_response));
				}
			}
			//GamesList.setVisibility(View.GONE);
			mainApp.getGameListItems().addAll(tmp);
			if (GamesAdapter != null) {
				GamesAdapter.notifyDataSetChanged();
			}
			GamesList.setVisibility(View.VISIBLE);
			if (GamesAdapter == null) {

				if (t == 0 || mainApp.isLiveChess()) {
					GamesAdapter = new OnlineGamesAdapter(Online.this, R.layout.gamelistelement, mainApp.getGameListItems());
				}
				if (t == 1 && !mainApp.isLiveChess()) {
					GamesAdapter = new OnlineGamesAdapter(Online.this, R.layout.gamelistelement, mainApp.getGameListItems());
				}
				if (t == 2 && !mainApp.isLiveChess()) {
					GamesAdapter = new OnlineGamesAdapter(Online.this, R.layout.gamelistelement, mainApp.getGameListItems());
				}
				GamesList.setAdapter(GamesAdapter);
			} /*else {*/
			GamesAdapter.notifyDataSetChanged();
			//GamesList.setVisibility(View.VISIBLE);
			/*}*/
		} else if (code == 1) {
			onPause();
			onResume();
		} else if (code == 2) {
			onPause();
			onResume();
			mainApp.ShowMessage(getString(R.string.challengeaccepted));
		} else if (code == 3) {
			onPause();
			onResume();
			mainApp.ShowMessage(getString(R.string.challengedeclined));
		} else if (code == 4) {
			onPause();
			onResume();
		}
	}

	private enum StartNewGameButtonsEnum {
		/*BUTTON_10_0(10, 0, "10 min"),
			BUTTON_5_0(5, 0, "5 min"),
			BUTTON_3_0(3, 0, "3 min"),
			BUTTON_30_0(30, 0, "30 min"),
			BUTTON_2_12(2, 12, "2 | 12"),
			BUTTON_1_5(1, 5, "1 | 5");*/

		BUTTON_10_0(10, 0, "10 min"),
		BUTTON_5_2(5, 2, "5 | 2"),
		BUTTON_15_10(15, 10, "15 | 10"),
		BUTTON_30_0(30, 0, "30 min"),
		BUTTON_5_0(5, 0, "5 min"),
		BUTTON_3_0(3, 0, "3 min"),
		BUTTON_2_1(2, 1, "2 | 1"),
		BUTTON_1_0(1, 0, "1 min");

		private int min;
		private int sec;
		private String text;

		private StartNewGameButtonsEnum(int min, int sec, String text) {
			this.min = min;
			this.sec = sec;
			this.text = text;
		}

		public int getMin() {
			return min;
		}

		public int getSec() {
			return sec;
		}

		public String getText() {
			return text;
		}
	}

	private BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			new Handler().post(new Runnable() {
				public void run() {
					if (mainApp.isLiveChess() && !intent.getExtras().getBoolean("enable")) {
						start.setVisibility(View.VISIBLE);
						if (gridview != null) {
							gridview.setVisibility(View.VISIBLE);
						}
						challengesListTitle.setVisibility(View.VISIBLE);
						startNewGameTitle.setVisibility(View.VISIBLE);
					}
				}
			});
		}
	};
}
