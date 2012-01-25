package com.chess.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.activities.tabs.Online;
import com.chess.core.CoreActivity;
import com.chess.core.Tabs;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.model.GameListElement;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MobclixHelper;
import com.chess.utilities.Web;
import com.chess.views.OnlineGamesAdapter;

import java.util.ArrayList;

public class OnlineNewGame extends CoreActivity {
	private ListView OpenChallengesLV;
	private ArrayList<GameListElement> GameListItems = new ArrayList<GameListElement>();
	private OnlineGamesAdapter GamesAdapter = null;
	private int UPDATE_DELAY = 120000;
	private Button challengecreate;
	private Button currentGame;
	private TextView removeAds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.onlinenewgame);

		if (MobclixHelper.isShowAds(mainApp)) {
			if (MobclixHelper.getBannerAdviewWrapper(mainApp) == null || MobclixHelper.getBannerAdview(mainApp) == null) {
				MobclixHelper.initializeBannerAdView(this, mainApp);
			}
		}

		removeAds = (TextView) findViewById(R.id.removeAds);
		removeAds.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
						"http://www." + LccHolder.HOST + "/login.html?als=" + mainApp.getSharedData().getString("user_token", "") +
								"&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html?c=androidads")));
			}
		});
		OpenChallengesLV = (ListView) this.findViewById(R.id.openChallenges);
		OpenChallengesLV.setAdapter(GamesAdapter);
		OpenChallengesLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
				final GameListElement el = GameListItems.get(pos);
				if (el.type == 0) {
					final String title = mainApp.isLiveChess() ?
							el.values.get("opponent_chess_title") :
							"Win: " + el.values.get("opponent_win_count") + " Loss: " + el.values.get("opponent_loss_count")
									+ " Draw: " + el.values.get("opponent_draw_count");

					if (mainApp.isLiveChess()) {
						if (el.values.get("is_direct_challenge").equals("1") && el.values.get("is_released_by_me").equals("0")) {
							new AlertDialog.Builder(OnlineNewGame.this)
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
							new AlertDialog.Builder(OnlineNewGame.this)
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
							new AlertDialog.Builder(OnlineNewGame.this)
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
						new AlertDialog.Builder(OnlineNewGame.this)
								.setTitle(title)
								.setItems(new String[]{getString(R.string.accept), getString(R.string.decline)}, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface d, int pos) {
										if (pos == 0) {
											String result = Web.Request("http://www." + LccHolder.HOST + "/api/echess_open_invites?id=" + mainApp.getSharedData().getString("user_token", "") + "&acceptinviteid=" + el.values.get("game_id"), "GET", null, null);
											if (result.contains("Success")) {
												Update(2);
											} else if (result.contains("Error+")) {
												mainApp.ShowDialog(OnlineNewGame.this, "Error", result.split("[+]")[1]);
											} else {
												//mainApp.ShowDialog(OnlineNewGame.this, "Error", result);
											}
										} else if (pos == 1) {

											String result = Web.Request("http://www." + LccHolder.HOST + "/api/echess_open_invites?id=" + mainApp.getSharedData().getString("user_token", "") + "&declineinviteid=" + el.values.get("game_id"), "GET", null, null);
											if (result.contains("Success")) {
												Update(3);
											} else if (result.contains("Error+")) {
												mainApp.ShowDialog(OnlineNewGame.this, "Error", result.split("[+]")[1]);
											} else {
												//mainApp.ShowDialog(OnlineNewGame.this, "Error", result);
											}
										}
									}
								})
								.create().show();
					}

				}
			}
		});
		findViewById(R.id.friendchallenge).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(OnlineNewGame.this, FriendChallenge.class));
			}
		});
		challengecreate = (Button) findViewById(R.id.challengecreate);
		challengecreate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(OnlineNewGame.this, CreateChallenge.class));
			}
		});
		currentGame = (Button) findViewById(R.id.currentGame);
		currentGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (lccHolder.getCurrentGameId() != null && lccHolder.getGame(lccHolder.getCurrentGameId()) != null) {
					lccHolder.processFullGame(lccHolder.getGame(lccHolder.getCurrentGameId()));
				}
			}
		});
	}

	protected void onResume() {
		if (MobclixHelper.isShowAds(mainApp)) {
			MobclixHelper.showBannerAd(MobclixHelper.getBannerAdviewWrapper(mainApp), removeAds, this, mainApp);
		}
		registerReceiver(challengesListUpdateReceiver, new IntentFilter("com.chess.lcc.android-challenges-list-update"));
		super.onResume();
		if (lccHolder.getCurrentGameId() == null) {
			currentGame.setVisibility(View.GONE);
		} else if (mainApp.isLiveChess()) {
			currentGame.setVisibility(View.VISIBLE);
		}
		disableScreenLock();
	}

	@Override
	protected void onPause() {
		if (MobclixHelper.isShowAds(mainApp)) {
			MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
		}
		unregisterReceiver(challengesListUpdateReceiver);
		super.onPause();
		enableScreenLock();
	}

	@Override
	public void LoadNext(int code) {
	}

	@Override
	public void LoadPrev(int code) {
		finish();
		startActivity(new Intent(this, Tabs.class).putExtra("tab", mainApp.isLiveChess() ? 1 : 2));
	}

	@Override
	public void Update(int code) {
		if (code == -1) {
			if (appService != null) {
				if (!mainApp.isLiveChess()) {
					appService.RunRepeatbleTask(Online.ONLINE_CALLBACK_CODE, 0, UPDATE_DELAY,
							"http://www." + LccHolder.HOST + "/api/echess_open_invites?id=" +
									mainApp.getSharedData().getString("user_token", ""),
							null/*PD = MyProgressDialog
                                        .show(OnlineNewGame.this, null, getString(R.string.loadinggames), true)*/);
				} else {
					/*appService.RunRepeatble(Online.ONLINE_CALLBACK_CODE, 0, 2000,
													  PD = MyProgressDialog
														.show(OnlineNewGame.this, null, getString(R.string.updatinggameslist), true));*/
					Update(Online.ONLINE_CALLBACK_CODE);
				}
			}
		} else if (code == Online.ONLINE_CALLBACK_CODE) {
			OpenChallengesLV.setVisibility(View.GONE);
			GameListItems.clear();
			if (mainApp.isLiveChess()) {
				GameListItems.addAll(lccHolder.getChallengesAndSeeksData());
			} else {
				GameListItems.addAll(ChessComApiParser.ViewOpenChallengeParse(rep_response));
			}
			if (GamesAdapter == null) {
				GamesAdapter = new OnlineGamesAdapter(this, R.layout.gamelistelement, GameListItems);
				OpenChallengesLV.setAdapter(GamesAdapter);
			} /*else{*/
			GamesAdapter.notifyDataSetChanged();
			OpenChallengesLV.setVisibility(View.VISIBLE);
			/*}*/
		} else if (code == 2) {
			mainApp.ShowMessage(getString(R.string.challengeaccepted));
			onPause();
			onResume();
		} else if (code == 3) {
			mainApp.ShowMessage(getString(R.string.challengedeclined));
			onPause();
			onResume();
		} else if (code == 4) {
			onPause();
			onResume();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		System.out.println("LCCLOG: onWindowFocusChanged hasFocus " + hasFocus);
		if (hasFocus && MobclixHelper.isShowAds(mainApp) && mainApp.isForceBannerAdOnFailedLoad()) {
			MobclixHelper.showBannerAd(MobclixHelper.getBannerAdviewWrapper(mainApp), removeAds, this, mainApp);
		}
	}
}
