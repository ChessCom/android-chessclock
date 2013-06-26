package com.chess.ui.activities.old;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.ui.activities.LiveBaseActivity;

public class LiveNewGameActivity extends LiveBaseActivity {

	private Button currentGameBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_new_game);

		initUpgradeAndAdWidgets();
	   /*moPubView = (MoPubView) findViewById(R.id.mopub_adview);
        MopubHelper.showBannerAd(upgradeBtn, moPubView, this);*/
		findViewById(R.id.friendchallenge).setOnClickListener(this);
		findViewById(R.id.challengecreate).setOnClickListener(this);

		currentGameBtn = (Button) findViewById(R.id.currentGameBtn);
		currentGameBtn.setOnClickListener(this);

//		getAppData().setLiveChessMode(this, true); // should not duplicate logic
		Log.d("TEST", " new game onCreate");
	}

	@Override
	protected void onLiveServiceConnected() {
		super.onLiveServiceConnected();
		if (liveService.isCurrentGameExist()) {
			currentGameBtn.setVisibility(View.VISIBLE);
		} else {
			currentGameBtn.setVisibility(View.GONE);
		}

		/*moPubView = (MoPubView) findViewById(R.id.mopub_adview);
        MopubHelper.showBannerAd(upgradeBtn, moPubView, this);*/
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(getAppData().getMembershipAndroidIntent());
		} else if (view.getId() == R.id.friendchallenge) {
			startActivity(new Intent(this, LiveFriendChallengeActivity.class));
		} else if (view.getId() == R.id.challengecreate) {
			startActivity(new Intent(this, LiveOpenChallengeActivity.class));
		} else if (view.getId() == R.id.currentGameBtn) {
			liveService.checkAndProcessFullGame();
		}
	}
}
