package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.YourMoveUpdateService;
import com.chess.backend.entity.AppData;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.lcc.android.LccHolder;
import com.chess.model.GameListItem;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.adapters.OnlineGamesAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * OnlineScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:12
 */
public class OnlineScreenActivity extends LiveBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener {
	private ListView gamesList;
	private Spinner gamesTypeSpinner;
	private OnlineGamesAdapter gamesAdapter;

	private static final int UPDATE_DELAY = 120000;
	private int temp_pos = -1;
	private int currentListType;

	public static int ONLINE_CALLBACK_CODE = 32;

	private GameListItem gameListElement;
	private static final int ACCEPT_DRAW = 0;
	private static final int DECLINE_DRAW = 1;
	private int successToastMsgId;

	private ChallengeInviteUpdateListener challengeInviteUpdateListener;
	private AcceptDrawUpdateListener acceptDrawUpdateListener;
	private ListUpdateListener listUpdateListener;
	private LoadItem selectedLoadItem;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_screen);
//		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		if (MopubHelper.isShowAds(mainApp)) {
            moPubView = (MoPubView) findViewById(R.id.mopub_adview);
			MopubHelper.showBannerAd(upgradeBtn, moPubView, mainApp);
		}

		init();

		gamesTypeSpinner = (Spinner) findViewById(R.id.gamestypes);
		gamesTypeSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.onlineSpinner));

		mainApp.setLiveChess(false);

		gamesTypeSpinner.setSelection(currentListType);
		gamesTypeSpinner.setOnItemSelectedListener(this);
        selectUpdateType(gamesTypeSpinner.getSelectedItemPosition());

		gamesList = (ListView) findViewById(R.id.onlineGamesList);
		gamesList.setOnItemClickListener(this);
		gamesList.setOnItemLongClickListener(this);

		findViewById(R.id.tournaments).setOnClickListener(this);
		findViewById(R.id.stats).setOnClickListener(this);
		findViewById(R.id.start).setOnClickListener(this);
	}

	private void init() {
		// set default load item to load current games // TODO must be adjustable
//				"http://www." + LccHolder.HOST + AppConstants.API_V2_GET_ECHESS_CURRENT_GAMES_ID
//				+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
//				+ "&all=1",

        selectedLoadItem = new LoadItem();

		challengeInviteUpdateListener = new ChallengeInviteUpdateListener();
		acceptDrawUpdateListener = new AcceptDrawUpdateListener();
		listUpdateListener = new ListUpdateListener();
	}

	@Override
	protected void onResume() {
		/*if (isShowAds() && (!mainApp.isLiveChess() || (mainApp.isLiveChess() && lccHolder.isConnected()))) {
			  MobclixHelper.showBannerAd(getBannerAdviewWrapper(), removeAds, this, mainApp);
			}*/
		super.onResume();
		updateList(selectedLoadItem);
		handler.postDelayed(updateListOrder, UPDATE_DELAY);
	}

	@Override
	protected void onPause() {
		super.onPause();

		handler.removeCallbacks(updateListOrder);
	}

	private void updateList(LoadItem listLoadItem){
		new GetStringObjTask(listUpdateListener).execute(listLoadItem);
	}

	private Runnable updateListOrder = new Runnable() {
		@Override
		public void run() {
            updateList(selectedLoadItem);

			handler.removeCallbacks(this);
			handler.postDelayed(this, UPDATE_DELAY);
		}
	};

	private class ListUpdateListener extends AbstractUpdateListener<String> {
		public ListUpdateListener() {
			super(coreContext);
		}

		@Override
		public void showProgress(boolean show) {
			getActionBarHelper().setRefreshActionItemState(show);
			gamesTypeSpinner.setEnabled(false);
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {

				ArrayList<GameListItem> listItems = new ArrayList<GameListItem>();

				if (currentListType == GameListItem.LIST_TYPE_CURRENT) {
					listItems.addAll(ChessComApiParser.getCurrentOnlineGames(returnedObj));
				}
				if (currentListType == GameListItem.LIST_TYPE_CHALLENGES) {
					listItems.addAll(ChessComApiParser.getChallengesGames(returnedObj));
				}
				if (currentListType == GameListItem.LIST_TYPE_FINISHED) {
					listItems.addAll(ChessComApiParser.getFinishedOnlineGames(returnedObj));
				}

				if (gamesAdapter == null) {
					gamesAdapter = new OnlineGamesAdapter(OnlineScreenActivity.this,
							R.layout.gamelistelement, listItems);
					gamesList.setAdapter(gamesAdapter);
				}else{
					gamesAdapter.setItemsList(listItems);
				}

                gamesTypeSpinner.setSelection(currentListType);  // TODO handle mess later
				gamesTypeSpinner.setEnabled(true);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				String status = returnedObj.split("[+]")[1];
				if(!isFinishing())
					mainApp.showDialog(coreContext, AppConstants.ERROR, status);

				if(status.equals(RestHelper.R_PLEASE_LOGIN_AGAIN))
					stopService(new Intent(coreContext, YourMoveUpdateService.class));
			}
		}

        @Override
        public void errorHandle(Integer resultCode) {
            gamesTypeSpinner.setEnabled(true);
        }
    }


	@Override
	public void update(int code) {
	}

	private class ChallengeInviteUpdateListener extends AbstractUpdateListener<String> {
		public ChallengeInviteUpdateListener() {
			super(coreContext);
		}

		@Override
		public void updateData(String returnedObj) {
			if(isFinishing())
				return;

			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				showToast(successToastMsgId);
				updateList(selectedLoadItem);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				mainApp.showDialog(coreContext, AppConstants.ERROR, returnedObj.split("[+]")[1]);
			}
		}
	}

	private class AcceptDrawUpdateListener extends AbstractUpdateListener<String> {
		public AcceptDrawUpdateListener() {
			super(coreContext);
		}

		@Override
		public void updateData(String returnedObj) {
			if(isFinishing())
				return;

			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
				updateList(selectedLoadItem);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				mainApp.showDialog(coreContext, AppConstants.ERROR, returnedObj.split("[+]")[1]);
			}
		}
	}

	private DialogInterface.OnClickListener acceptDrawDialogListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if(whichButton == DialogInterface.BUTTON_POSITIVE){
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
				loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListElement.getGameId()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_ACCEPTDRAW);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));

				new GetStringObjTask(acceptDrawUpdateListener).execute(loadItem);
			}else if(whichButton == DialogInterface.BUTTON_NEUTRAL){
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
				loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListElement.getGameId()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_DECLINEDRAW);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));

				new GetStringObjTask(acceptDrawUpdateListener).execute(loadItem);

			}else  if(whichButton == DialogInterface.BUTTON_NEGATIVE){
				startActivity(new Intent(coreContext, GameOnlineScreenActivity.class).
					putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS).
					putExtra(GameListItem.GAME_ID, gameListElement.getGameId()));
			}
		}
	};



	private DialogInterface.OnClickListener gameListItemDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, ChatActivity.class);
				intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
				intent.putExtra(GameListItem.TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));
				startActivity(intent);
			} else if (pos == 1) {
				String Draw = AppConstants.OFFERDRAW;
				if (gameListElement.values.get(GameListItem.IS_DRAW_OFFER_PENDING).equals("p"))
					Draw = AppConstants.ACCEPTDRAW;

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
				loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListElement.getGameId()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, Draw);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));

				new GetStringObjTask(acceptDrawUpdateListener).execute(loadItem);


//				String result = Web.Request("http://www." + LccHolder.HOST
//						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID
//						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
//						+ AppConstants.CHESSID_PARAMETER + gameListElement.getGameId()
//						+ AppConstants.COMMAND_PARAMETER + Draw + AppConstants.TIMESTAMP_PARAMETER
//						+ gameListElement.values.get(GameListItem.TIMESTAMP), "GET", null, null);

//				if (result.contains(RestHelper.R_SUCCESS)) {
//					showToast(R.string.accepted);   // TODO
//					update(1);
//				} else if (result.contains("Error+")) {
//					mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
//				}

			} else if (pos == 2) {

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
				loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListElement.getGameId()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));

				new GetStringObjTask(acceptDrawUpdateListener).execute(loadItem);

//				String result = Web.Request("http://www." + LccHolder.HOST
//						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID
//						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
//						+ AppConstants.CHESSID_PARAMETER + gameListElement.getGameId()
//						+ AppConstants.COMMAND_RESIGN__AND_TIMESTAMP_PARAMETER
//						+ gameListElement.values.get(GameListItem.TIMESTAMP), "GET", null, null);
//
//				if (result.contains(RestHelper.R_SUCCESS)) {
//					update(1);
//				} else if (result.contains("Error+")) {
//					mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
//				}

			}
		}
	};


	private DialogInterface.OnClickListener nonLiveDialogListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface d, int pos) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));

			if (pos == ACCEPT_DRAW) {
				loadItem.addRequestParams(RestHelper.P_ACCEPTINVITEID, String.valueOf(gameListElement.getGameId()));
				successToastMsgId = R.string.challengeaccepted;
			} else if (pos == DECLINE_DRAW) {
				loadItem.addRequestParams(RestHelper.P_DECLINEINVITEID, String.valueOf(gameListElement.getGameId()));
				successToastMsgId = R.string.challengedeclined;
			}

			new GetStringObjTask(challengeInviteUpdateListener).execute(loadItem);
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case 0: {
				if (temp_pos > -1) {
					return new AlertDialog.Builder(this)
							.setTitle(getString(R.string.accept_draw_q))
							.setPositiveButton(getString(R.string.accept), acceptDrawDialogListener)
							.setNeutralButton(getString(R.string.decline), acceptDrawDialogListener)
							.setNegativeButton(getString(R.string.game), acceptDrawDialogListener).create();
				}
			}
			default:
				break;
		}
		return super.onCreateDialog(id);
	}


	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(mainApp.getMembershipAndroidIntent());
		} else if (view.getId() == R.id.tournaments) {
			
			String tournamentsUrl = "http://www." + LccHolder.HOST + AppConstants.TOURNAMENTS;
			
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www."
					+ LccHolder.HOST + AppConstants.LOGIN_HTML_ALS
					+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
					+ "&goto=" + tournamentsUrl)));
		} else if (view.getId() == R.id.stats) {

			String GOTO = "http://www." + LccHolder.HOST + AppConstants.ECHESS_MOBILE_STATS
					+ mainApp.getUserName();
			try {
				GOTO = URLEncoder.encode(GOTO, AppConstants.UTF_8);
			} catch (UnsupportedEncodingException ignored) {
				showToast(R.string.username_cannot_be_encoded);
			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www."
					+ LccHolder.HOST + AppConstants.LOGIN_HTML_ALS
					+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
					+ "&goto=" + GOTO)));
		} else if (view.getId() == R.id.start) {
			startActivity(new Intent(this, OnlineNewGameActivity.class));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
		gameListElement = (GameListItem) adapterView.getItemAtPosition(pos);

		if (gameListElement.type == GameListItem.LIST_TYPE_CHALLENGES) {
			clickOnChallenge();
		} else if (gameListElement.type == GameListItem.LIST_TYPE_CURRENT) {
			mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
			mainApp.getSharedDataEditor().commit();

			if (gameListElement.values.get(GameListItem.IS_DRAW_OFFER_PENDING).equals("p")) {
				mainApp.acceptdraw = true;
				temp_pos = pos;
				showDialog(0);
			} else {
				mainApp.acceptdraw = false;

				Intent intent = new Intent(coreContext, GameOnlineScreenActivity.class);
				intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS);
				intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
				startActivity(intent);
			}
		} else if (gameListElement.type == GameListItem.LIST_TYPE_FINISHED) {
			mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
			mainApp.getSharedDataEditor().commit();

//			Intent intent = new Intent(coreContext, GameOnlineScreenActivity.class);
			Intent intent = new Intent(coreContext, GameFinishedScreenActivity.class);
//			intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS);
			intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		gameListElement = (GameListItem) adapterView.getItemAtPosition(pos);

		if (gameListElement.type == GameListItem.LIST_TYPE_CHALLENGES) {
			clickOnChallenge();
		} else if (gameListElement.type == GameListItem.LIST_TYPE_CURRENT) {
			new AlertDialog.Builder(coreContext)
					.setItems(new String[]{
							getString(R.string.chat),
							getString(R.string.drawoffer),
							getString(R.string.resignorabort)},
							gameListItemDialogListener)
					.create().show();
		} else if (gameListElement.type == GameListItem.LIST_TYPE_FINISHED) {
			mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
			mainApp.getSharedDataEditor().commit();

			Intent intent = new Intent(coreContext, ChatActivity.class);
			intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
			intent.putExtra(GameListItem.TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));
			startActivity(intent);
		}
		return true;
	}

	private void clickOnChallenge() {
		final String title = "Win: " + gameListElement.values.get(GameListItem.OPPONENT_WIN_COUNT)
				+ " Loss: " + gameListElement.values.get(GameListItem.OPPONENT_LOSS_COUNT)
				+ " Draw: " + gameListElement.values.get(GameListItem.OPPONENT_DRAW_COUNT);

		new AlertDialog.Builder(coreContext)
				.setTitle(title)
				.setItems(new String[]{
						getString(R.string.accept),
						getString(R.string.decline)}, nonLiveDialogListener
				)
				.create().show();
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        selectUpdateType(pos);
	}

    private void selectUpdateType(int pos) {
        currentListType = pos;
        if (pos == GameListItem.LIST_TYPE_CURRENT) {
            selectedLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
            selectedLoadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
            selectedLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ALL_USERS_GAMES);

        } else if (pos == GameListItem.LIST_TYPE_CHALLENGES){
            selectedLoadItem.setLoadPath(RestHelper.ECHESS_CHALLENGES);
            selectedLoadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));

        } else if (pos == GameListItem.LIST_TYPE_FINISHED) {
            selectedLoadItem.setLoadPath(RestHelper.ECHESS_FINISHED_GAMES);
            selectedLoadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance().getUserToken(coreContext));
        }

        updateList(selectedLoadItem);

    }

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
	}
}
