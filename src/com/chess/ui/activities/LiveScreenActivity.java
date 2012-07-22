package com.chess.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.model.NewGameButtonItem;
import com.chess.ui.adapters.NewGamesButtonsAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;
import java.util.List;

/**
 * LiveScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class LiveScreenActivity extends LiveBaseActivity implements ItemClickListenerFace {

	private Button currentGame;
	private ViewGroup loadingView;
	private List<View> infoGroup;
	private View emptyView;
	private NewGamesButtonsAdapter newGamesButtonsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_screen);

		init();
		widgetsInit();
	}

	private void init() {
		DataHolder.getInstance().setLiveChess(true);

		infoGroup = new ArrayList<View>();

		String[] newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
		List<NewGameButtonItem> newGameButtonItems = new ArrayList<NewGameButtonItem>();
		for (String label : newGameButtonsArray) {
			newGameButtonItems.add(NewGameButtonItem.createNewButtonFromLabel(label));
		}

		newGamesButtonsAdapter = new NewGamesButtonsAdapter(this, newGameButtonItems);
	}

    protected void widgetsInit(){
		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		loadingView = (ViewGroup) findViewById(R.id.loadingView);
		emptyView = findViewById(R.id.emptyView);
		
		moPubView = (MoPubView) findViewById(R.id.mopub_adview); // init anyway as it is declared in layout
		if (MopubHelper.isShowAds(this)) {
			MopubHelper.showBannerAd(upgradeBtn, moPubView, this);
		}

		TextView startNewGameTitle = (TextView) findViewById(R.id.startNewGameTitle);

		Button startBtn = (Button) findViewById(R.id.start);
		startBtn.setOnClickListener(this);

		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(newGamesButtonsAdapter);

		infoGroup.add(startNewGameTitle);
		infoGroup.add(startBtn);
		infoGroup.add(gridView);

		currentGame = (Button) findViewById(R.id.currentGameBtn);
		currentGame.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		showLoadingView(!getLccHolder().isConnected());

		if (getLccHolder().getCurrentGameId() == null) {
			currentGame.setVisibility(View.GONE);
		} else {
			currentGame.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onConnecting() {
		super.onConnecting();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showLoadingView(true);
			}
		});
	}

	@Override
	public void onConnectionEstablished() {
		super.onConnectionEstablished();
		Log.d("TEST", "Live Screen onLccConnected, lcc connect state = " + LccHolder.getInstance(this).isConnected());
		showLoadingView(false);
	}

	private void showLoadingView(final boolean show){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				loadingView.setVisibility(show? View.VISIBLE: View.GONE);
				emptyView.setVisibility(View.GONE);

				int infoVisibility = show? View.GONE: View.VISIBLE;
				for (View view : infoGroup) {
					view.setVisibility(infoVisibility);
				}
			}
		});
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);
		if(fragment.getTag().equals(NETWORK_CHECK_TAG)){
			emptyView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));

		} else if (view.getId() == R.id.currentGameBtn) {
			getLccHolder().checkAndProcessFullGame();

		} else if (view.getId() == R.id.start) {
			startActivity(new Intent(this, LiveNewGameActivity.class));
		} else if(view.getId() == R.id.newGameBtn){
			Integer pos = (Integer) view.getTag(R.id.list_item_id);
			NewGameButtonItem buttonItem = newGamesButtonsAdapter.getItem(pos);

			preferencesEditor.putString(AppConstants.CHALLENGE_INITIAL_TIME, StaticData.SYMBOL_EMPTY + buttonItem.getMin());
			preferencesEditor.putString(AppConstants.CHALLENGE_BONUS_TIME, StaticData.SYMBOL_EMPTY + buttonItem.getSec());
			preferencesEditor.commit();
			startActivity(new Intent(getContext(), LiveCreateChallengeActivity.class));
		}
	}

	@Override
	public Context getMeContext() {
		return this;
	}
}