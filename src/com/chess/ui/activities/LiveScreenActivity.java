package com.chess.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.User;
import com.chess.model.NewGameButtonItem;
import com.chess.ui.adapters.NewGamesButtonsAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;
import com.chess.utilities.InneractiveAdHelper;
import com.chess.utilities.MopubHelper;
import com.inneractive.api.ads.InneractiveAd;
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
	private TextView bulletRatingTxt;
	private TextView blitzRatingTxt;
	private TextView standardRatingTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_screen);

		init();
		widgetsInit();
	}

	 private void init() {
		AppData.setLiveChessMode(this, true);

		infoGroup = new ArrayList<View>();

		String[] newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
		List<NewGameButtonItem> newGameButtonItems = new ArrayList<NewGameButtonItem>();
		for (String label : newGameButtonsArray) {
			newGameButtonItems.add(NewGameButtonItem.createNewButtonFromLabel(label, this));
		}

		newGamesButtonsAdapter = new NewGamesButtonsAdapter(this, newGameButtonItems);
	}

    protected void widgetsInit(){
		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		loadingView = (ViewGroup) findViewById(R.id.loadingView);
		emptyView = findViewById(R.id.emptyView);
		
		moPubView = (MoPubView) findViewById(R.id.mopub_adview); // init anyway as it is declared in layout
		if (AppUtils.isNeedToUpgrade(this)) {
			if (InneractiveAdHelper.IS_SHOW_BANNER_ADS) {
				InneractiveAdHelper.showBannerAd(upgradeBtn, (InneractiveAd) findViewById(R.id.inneractiveAd), this);
			} else {
				MopubHelper.showBannerAd(upgradeBtn, moPubView, this);
			}
		}

		Button statsBtn = (Button) findViewById(R.id.statsBtn);
		statsBtn.setOnClickListener(this);


		LinearLayout ratingView = (LinearLayout) findViewById(R.id.ratingLay);

		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(newGamesButtonsAdapter);

		infoGroup.add(statsBtn);
		infoGroup.add(gridView);
		infoGroup.add(ratingView);

		bulletRatingTxt = (TextView) findViewById(R.id.bulletRatingTxt);
		blitzRatingTxt = (TextView) findViewById(R.id.blitzRatingTxt);
		standardRatingTxt = (TextView) findViewById(R.id.standardRatingTxt);

		currentGame = (Button) findViewById(R.id.currentGameBtn);
		currentGame.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		showLoadingView(!getLccHolder().isConnected());

		if (getLccHolder().currentGameExist()) {
			currentGame.setVisibility(View.VISIBLE);
		} else {
			currentGame.setVisibility(View.GONE);
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
		showLoadingView(false);
	}

	private synchronized void showLoadingView(final boolean show){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				loadingView.setVisibility(show? View.VISIBLE: View.GONE);
				emptyView.setVisibility(View.GONE);

				int infoVisibility = show? View.GONE: View.VISIBLE;
				for (View view : infoGroup) {
					view.setVisibility(infoVisibility);
				}
				showActionNewGame = !show;
				getActionBarHelper().showMenuItemById(R.id.menu_new_game, showActionNewGame);

				User user = getLccHolder().getUser();
				if(!show && user != null){
					bulletRatingTxt.setText(getString(R.string.bullet_, user.getQuickRating()));
					blitzRatingTxt.setText(getString(R.string.blitz_, user.getBlitzRating()));
					standardRatingTxt.setText(getString(R.string.standard_, user.getStandardRating()));
				}
			}
		});
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNegativeBtnClick(fragment);
			return;
		}

		if(tag.equals(NETWORK_CHECK_TAG)){
			emptyView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
		super.onNegativeBtnClick(fragment);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));

		} else if (view.getId() == R.id.currentGameBtn) {
			getLccHolder().checkAndProcessFullGame();

		} else if(view.getId() == R.id.statsBtn){
			String playerStatsLink = RestHelper.formStatsLink(AppData.getUserToken(this), AppData.getUserName(this));
			Intent intent = new Intent(this, WebViewActivity.class);
			intent.putExtra(AppConstants.EXTRA_WEB_URL, playerStatsLink);
			intent.putExtra(AppConstants.EXTRA_TITLE, getString(R.string.stats));
			startActivity(intent);

		} else if(view.getId() == R.id.newGameBtn){
			Integer pos = (Integer) view.getTag(R.id.list_item_id);
			NewGameButtonItem buttonItem = newGamesButtonsAdapter.getItem(pos);

			preferencesEditor.putString(AppConstants.CHALLENGE_INITIAL_TIME, StaticData.SYMBOL_EMPTY + buttonItem.getMin());
			preferencesEditor.putString(AppConstants.CHALLENGE_BONUS_TIME, StaticData.SYMBOL_EMPTY + buttonItem.getSec());
			preferencesEditor.commit();
			startActivity(new Intent(getContext(), LiveOpenChallengeActivity.class));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.menu_new_game:
				startActivity(new Intent(this, LiveNewGameActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void afterLogin() {
		restartActivity();
	}

	@Override
	public Context getMeContext() {
		return this;
	}
}