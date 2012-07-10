package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
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
public class LiveScreenActivity extends LiveBaseActivity {

	private Button currentGame;
	private ViewGroup loadingView;
	private List<View> infoGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_screen);

		init();
		widgetsInit();
	}

    protected void widgetsInit(){
		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		loadingView = (ViewGroup) findViewById(R.id.loadingView);

		moPubView = (MoPubView) findViewById(R.id.mopub_adview); // init anyway as it is declared in layout
		if (MopubHelper.isShowAds(this)) {
			MopubHelper.showBannerAd(upgradeBtn, moPubView, this);
		}

		TextView startNewGameTitle = (TextView) findViewById(R.id.startNewGameTitle);

		Button startBtn = (Button) findViewById(R.id.start);
		startBtn.setOnClickListener(this);

		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(new NewGamesButtonsAdapter());

		infoGroup.add(startNewGameTitle);
		infoGroup.add(startBtn);
		infoGroup.add(gridView);

		currentGame = (Button) findViewById(R.id.currentGame);
		currentGame.setOnClickListener(this);
	}

	private void init() {
		DataHolder.getInstance().setLiveChess(true);

		infoGroup = new ArrayList<View>();
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
		Log.d("TEST", "Live Screen onLccConnected, lcc connect state = "
				+ LccHolder.getInstance(this).isConnected());
		showLoadingView(false);
	}

	private void showLoadingView(final boolean show){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				loadingView.setVisibility(show? View.VISIBLE: View.GONE);
				int infoVisibility = show? View.GONE: View.VISIBLE;
				for (View view : infoGroup) {
					view.setVisibility(infoVisibility);
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private class NewGamesButtonsAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		private NewGamesButtonsAdapter() {
			this.inflater = LayoutInflater.from(getContext());
		}

		@Override
		public int getCount() {
			return StartNewGameButtonsEnum.values().length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Button button;
			if (convertView == null) {
				button = (Button) inflater.inflate(R.layout.default_button_grey, null, false);
			} else {
				button = (Button) convertView;
			}
			StartNewGameButtonsEnum.values();
			final StartNewGameButtonsEnum startNewGameButton = StartNewGameButtonsEnum.values()[position];
			button.setText(startNewGameButton.getText());
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					preferencesEditor.putString(AppConstants.CHALLENGE_INITIAL_TIME, StaticData.SYMBOL_EMPTY + startNewGameButton.getMin());
					preferencesEditor.putString(AppConstants.CHALLENGE_BONUS_TIME, StaticData.SYMBOL_EMPTY + startNewGameButton.getSec());
					preferencesEditor.commit();
					startActivity(new Intent(getContext(), LiveCreateChallengeActivity.class));
				}
			});
			return button;
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));

		} else if (view.getId() == R.id.currentGame) {
			getLccHolder().checkAndProcessFullGame();

		} else if (view.getId() == R.id.start) {
			startActivity(new Intent(this, LiveNewGameActivity.class));
		}
	}

	private enum StartNewGameButtonsEnum {
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

}