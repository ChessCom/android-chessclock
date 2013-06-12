package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RadioButton;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.01.13
 * Time: 12:23
 */
public class NewGameCompView extends NewGameDefaultView {

	private CompGameConfig.Builder gameConfigBuilder;
	private RadioButton blackHuman;
	private RadioButton whiteHuman;
	private boolean haveSavedGame;

	public NewGameCompView(Context context) {
		super(context);
	}

	public NewGameCompView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		gameConfigBuilder = new CompGameConfig.Builder();

	}

	@Override
	public void toggleOptions() {
		super.toggleOptions();

		int expandVisibility = optionsVisible? VISIBLE: GONE;

		optionsView.setVisibility(expandVisibility);

		if(optionsVisible) {
			ButtonDrawableBuilder.setBackgroundToView(compactRelLay, R.style.ListItem);
		} else {
			ButtonDrawableBuilder.setBackgroundToView(compactRelLay, R.style.ListItem_Header);

		}
		compactRelLay.setPadding(COMPACT_PADDING, 0, COMPACT_PADDING, COMPACT_PADDING);
	}

	public void setConfig(ViewCompConfig viewConfig) {
		haveSavedGame = viewConfig.haveSavedGame();
		super.setConfig(viewConfig);
	}

	@Override
	public void addOptionsView() {
		optionsView = LayoutInflater.from(getContext()).inflate(R.layout.new_game_option_comp_view, null, false);
		optionsView.setVisibility(GONE);
		addView(optionsView);

		whiteHuman = (RadioButton) optionsView.findViewById(R.id.whiteHumanRadioBtn);
		blackHuman = (RadioButton) optionsView.findViewById(R.id.blackHumanRadioBtn);

		Button loadCompPlayBtn = (Button) optionsView.findViewById(R.id.loadCompPlayBtn);
		loadCompPlayBtn.setVisibility(haveSavedGame ? VISIBLE : GONE);
	}

	public CompGameConfig getNewCompGameConfig(){
		int mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
		if (!whiteHuman.isChecked() && blackHuman.isChecked()) {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
		} else if (whiteHuman.isChecked() && blackHuman.isChecked()) {
			mode = AppConstants.GAME_MODE_HUMAN_VS_HUMAN;
		} else if (!whiteHuman.isChecked() && !blackHuman.isChecked()) {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
		}

		return gameConfigBuilder.setMode(mode).build();
	}

	public static class ViewCompConfig extends ViewConfig {
	    private boolean haveSavedGame;

		public boolean haveSavedGame() {
			return haveSavedGame;
		}

		public void setHaveSavedGame(boolean haveSavedGame) {
			this.haveSavedGame = haveSavedGame;
		}
	}

}
