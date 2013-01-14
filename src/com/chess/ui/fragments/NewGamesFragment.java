package com.chess.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.ui.views.NewDailyGameView;
import com.chess.ui.views.NewDefaultGameView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.01.13
 * Time: 9:04
 */
public class NewGamesFragment extends CommonLogicFragment {

	private static final int DAILY_BASE_ID = 0x00001000;
	private static final int LIVE_BASE_ID = 0x00002000;
	private static final int COMP_BASE_ID = 0x00003000;

	private NewDailyGameView dailyGamesSetupView;
	private NewDefaultGameView liveGamesSetupView;
	private NewDefaultGameView compGamesSetupView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_new_games_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		dailyGamesSetupView = (NewDailyGameView) view.findViewById(R.id.dailyGamesSetupView);

		NewDefaultGameView.ConfigItem dailyConfig = new NewDefaultGameView.ConfigItem();
		dailyConfig.setBaseId(DAILY_BASE_ID);
		dailyConfig.setHeaderIcon(R.drawable.ic_daily_game);
		dailyConfig.setHeaderText(R.string.new_daily_chess);
		dailyConfig.setTitleText(R.string.new_per_turn);
		dailyConfig.setLeftButtonText(R.string.days); // TODO set properly
		dailyConfig.setRightButtonText(R.string.random);

		dailyGamesSetupView.setConfig(dailyConfig);

		liveGamesSetupView = (NewDefaultGameView) view.findViewById(R.id.liveGamesSetupView);

		NewDefaultGameView.ConfigItem liveConfig = new NewDefaultGameView.ConfigItem();
		liveConfig.setBaseId(LIVE_BASE_ID);
		liveConfig.setHeaderIcon(R.drawable.ic_live_game);
		liveConfig.setHeaderText(R.string.new_live_chess);
		liveConfig.setTitleText(R.string.new_time);
		liveConfig.setLeftButtonText(R.string.days); // TODO set properly


		liveGamesSetupView.setConfig(liveConfig);

		compGamesSetupView = (NewDefaultGameView) view.findViewById(R.id.compGamesSetupView);

		NewDefaultGameView.ConfigItem compConfig = new NewDefaultGameView.ConfigItem();
		compConfig.setBaseId(COMP_BASE_ID);
		compConfig.setHeaderIcon(R.drawable.ic_comp_game);
		compConfig.setHeaderText(R.string.new_comp_chess);
		compConfig.setTitleText(R.string.new_difficulty);
		compConfig.setLeftButtonText(R.string.days); // TODO set properly

		compGamesSetupView.setConfig(compConfig);


	}
}
