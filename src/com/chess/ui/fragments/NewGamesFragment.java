package com.chess.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.ui.views.NewGameDailyView;
import com.chess.ui.views.NewGameDefaultView;

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

	private final static int DAILY_RIGHT_BUTTON_ID = DAILY_BASE_ID + NewGameDailyView.RIGHT_BUTTON_ID;
	private final static int DAILY_LEFT_BUTTON_ID = DAILY_BASE_ID + NewGameDefaultView.LEFT_BUTTON_ID;
//	private final static int DAILY_OPTIONS_TXT_ID = DAILY_BASE_ID + NewGameDefaultView.OPTIONS_TXT_ID;
	private final static int DAILY_PLAY_BUTTON_ID = DAILY_BASE_ID + NewGameDefaultView.PLAY_BUTTON_ID;

	private final static int LIVE_LEFT_BUTTON_ID = LIVE_BASE_ID + NewGameDefaultView.LEFT_BUTTON_ID;
//	private final static int LIVE_OPTIONS_TXT_ID = LIVE_BASE_ID + NewGameDefaultView.OPTIONS_TXT_ID;
	private final static int LIVE_PLAY_BUTTON_ID = LIVE_BASE_ID + NewGameDefaultView.PLAY_BUTTON_ID;

	private final static int COMP_LEFT_BUTTON_ID = COMP_BASE_ID + NewGameDefaultView.LEFT_BUTTON_ID;
//	private final static int COMP_OPTIONS_TXT_ID = COMP_BASE_ID + NewGameDefaultView.OPTIONS_TXT_ID;
	private final static int COMP_PLAY_BUTTON_ID = COMP_BASE_ID + NewGameDefaultView.PLAY_BUTTON_ID;

	private NewGameDailyView dailyGamesSetupView;
	private NewGameDefaultView liveGamesSetupView;
	private NewGameDefaultView compGamesSetupView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_new_games_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Daily Games setup
		dailyGamesSetupView = (NewGameDailyView) view.findViewById(R.id.dailyGamesSetupView);

		NewGameDefaultView.ConfigItem dailyConfig = new NewGameDefaultView.ConfigItem();
		dailyConfig.setBaseId(DAILY_BASE_ID);
		dailyConfig.setHeaderIcon(R.drawable.ic_daily_game);
		dailyConfig.setHeaderText(R.string.new_daily_chess);
		dailyConfig.setTitleText(R.string.new_per_turn);
		dailyConfig.setLeftButtonText(R.string.days); // TODO set properly
		dailyConfig.setRightButtonText(R.string.random);

		dailyGamesSetupView.setConfig(dailyConfig);
		dailyGamesSetupView.findViewById(DAILY_RIGHT_BUTTON_ID).setOnClickListener(this);
		dailyGamesSetupView.findViewById(DAILY_LEFT_BUTTON_ID).setOnClickListener(this);
//		dailyGamesSetupView.findViewById(DAILY_OPTIONS_TXT_ID).setOnClickListener(this);
		dailyGamesSetupView.findViewById(DAILY_PLAY_BUTTON_ID).setOnClickListener(this);


		// Live Games setup
		liveGamesSetupView = (NewGameDefaultView) view.findViewById(R.id.liveGamesSetupView);

		NewGameDefaultView.ConfigItem liveConfig = new NewGameDefaultView.ConfigItem();
		liveConfig.setBaseId(LIVE_BASE_ID);
		liveConfig.setHeaderIcon(R.drawable.ic_live_game);
		liveConfig.setHeaderText(R.string.new_live_chess);
		liveConfig.setTitleText(R.string.new_time);
		liveConfig.setLeftButtonText(R.string.days); // TODO set properly

		liveGamesSetupView.setConfig(liveConfig);
		liveGamesSetupView.findViewById(LIVE_LEFT_BUTTON_ID).setOnClickListener(this);
//		liveGamesSetupView.findViewById(LIVE_OPTIONS_TXT_ID).setOnClickListener(this);
		liveGamesSetupView.findViewById(LIVE_PLAY_BUTTON_ID).setOnClickListener(this);

		// Comp Games setup
		compGamesSetupView = (NewGameDefaultView) view.findViewById(R.id.compGamesSetupView);

		NewGameDefaultView.ConfigItem compConfig = new NewGameDefaultView.ConfigItem();
		compConfig.setBaseId(COMP_BASE_ID);
		compConfig.setHeaderIcon(R.drawable.ic_comp_game);
		compConfig.setHeaderText(R.string.new_comp_chess);
		compConfig.setTitleText(R.string.new_difficulty);
		compConfig.setLeftButtonText(R.string.days); // TODO set properly

		compGamesSetupView.setConfig(compConfig);
		compGamesSetupView.findViewById(COMP_LEFT_BUTTON_ID).setOnClickListener(this);
//		compGamesSetupView.findViewById(COMP_OPTIONS_TXT_ID).setOnClickListener(this);
		compGamesSetupView.findViewById(COMP_PLAY_BUTTON_ID).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		int id = v.getId();
		if (id == DAILY_RIGHT_BUTTON_ID) {

		} else if (id == DAILY_LEFT_BUTTON_ID) {
//		} else if (id == DAILY_OPTIONS_TXT_ID) {
//			dailyGamesSetupView.toggleOptions();
		} else if (id == DAILY_PLAY_BUTTON_ID) {

		} else if (id == LIVE_LEFT_BUTTON_ID) {
//		} else if (id == LIVE_OPTIONS_TXT_ID) {
//			liveGamesSetupView.toggleOptions();
		} else if (id == LIVE_PLAY_BUTTON_ID) {

		} else if (id == COMP_LEFT_BUTTON_ID) {
//		} else if (id == COMP_OPTIONS_TXT_ID) {
//			compGamesSetupView.toggleOptions();
		} else if (id == COMP_PLAY_BUTTON_ID) {



		}

	}
}
