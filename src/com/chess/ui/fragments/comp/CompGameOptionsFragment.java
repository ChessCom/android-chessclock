package com.chess.ui.fragments.comp;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.chess.R;
import com.chess.widgets.RoboRadioButton;
import com.chess.widgets.SwitchButton;
import com.chess.statics.AppConstants;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.drawables.RatingProgressDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.04.13
 * Time: 7:06
 */
public class CompGameOptionsFragment extends CommonLogicFragment implements SwitchButton.SwitchChangeListener,
		RadioGroup.OnCheckedChangeListener {

	private CompGameConfig.Builder gameConfigBuilder;

	private int selectedCompLevel;
	private TextView strengthValueBtn;
	private RoboRadioButton whitePlayerBtn;
	private RoboRadioButton blackPlayerBtn;
	private View autoFlipView;
	private SwitchButton autoFlipSwitch;
	private RoboRadioButton whiteCompBtn;
	private RoboRadioButton blackCompBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameConfigBuilder = new CompGameConfig.Builder();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_comp_options_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		autoFlipView = view.findViewById(R.id.autoFlipView);
		autoFlipView.setOnClickListener(this);
		autoFlipSwitch = (SwitchButton) view.findViewById(R.id.autoFlipSwitch);
		autoFlipSwitch.setSwitchChangeListener(this);

		RadioGroup whiteRadioGroup = (RadioGroup) view.findViewById(R.id.whiteRadioGroup);
		RadioGroup blackRadioGroup = (RadioGroup) view.findViewById(R.id.blackRadioGroup);
		whitePlayerBtn = (RoboRadioButton) view.findViewById(R.id.whitePlayerBtn);
		whiteCompBtn = (RoboRadioButton) view.findViewById(R.id.whiteCompBtn);
		blackPlayerBtn = (RoboRadioButton) view.findViewById(R.id.blackPlayerBtn);
		blackCompBtn = (RoboRadioButton) view.findViewById(R.id.blackCompBtn);

		whiteRadioGroup.setOnCheckedChangeListener(this);
		blackRadioGroup.setOnCheckedChangeListener(this);

		int mode = getAppData().getCompGameMode();
		if (mode == AppConstants.GAME_MODE_2_PLAYERS) {
			whitePlayerBtn.setChecked(true);
			blackPlayerBtn.setChecked(true);

			autoFlipView.setVisibility(View.VISIBLE);
		} else if (mode == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
			whiteCompBtn.setChecked(true);
			blackCompBtn.setChecked(true);
		} else if (mode == AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE) {
			whitePlayerBtn.setChecked(true);
			blackCompBtn.setChecked(true);
		} else /*if (mode == AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK)*/ {
			blackPlayerBtn.setChecked(true);
			whiteCompBtn.setChecked(true);
		}

		autoFlipSwitch.setChecked(getAppData().isAutoFlipFor2Players());

		strengthValueBtn = (TextView) view.findViewById(R.id.compLevelValueBtn);
		selectedCompLevel = getAppData().getCompLevel();

		SeekBar strengthBar = (SeekBar) view.findViewById(R.id.strengthBar);
		strengthBar.setOnSeekBarChangeListener(ratingBarChangeListener);
		strengthBar.setProgressDrawable(new RatingProgressDrawable(getContext(), strengthBar));
		strengthBar.setProgress(selectedCompLevel);
		strengthValueBtn.setText(String.valueOf(selectedCompLevel + 1));

		if (JELLY_BEAN_PLUS_API) {
			ViewGroup compStrengthView = (ViewGroup) view.findViewById(R.id.compLevelView);
			LayoutTransition layoutTransition = compStrengthView.getLayoutTransition();
			layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		}

		view.findViewById(R.id.playBtn).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.playBtn) {
			startGame();
		} else if (view.getId() == R.id.autoFlipView) {
			autoFlipSwitch.toggle();
		}
	}

	protected void startGame() {
		ChessBoardComp.resetInstance();
		getAppData().clearSavedCompGame();

		CompGameConfig config = getNewCompGameConfig();

		GameCompFragment gameCompFragment;
		if (!isTablet) {
			gameCompFragment = (GameCompFragment) getFragmentManager().findFragmentByTag(GameCompFragment.class.getSimpleName());
		} else {
			gameCompFragment = (GameCompFragmentTablet) getFragmentManager().findFragmentByTag(GameCompFragmentTablet.class.getSimpleName());
		}

		if (gameCompFragment == null) {
			if (!isTablet) {
				gameCompFragment = new GameCompFragment();
			} else {
				gameCompFragment = new GameCompFragmentTablet();
			}
		}

		if (gameCompFragment.isVisible()) {
			gameCompFragment.updateConfig(config);
		} else {
			getActivityFace().openFragment(gameCompFragment);
		}

		getActivityFace().toggleRightMenu();
	}

	private SeekBar.OnSeekBarChangeListener ratingBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			selectedCompLevel = progress;
			strengthValueBtn.setText(String.valueOf(progress + 1));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};

	protected CompGameConfig getNewCompGameConfig(){
		getAppData().setCompLevel(selectedCompLevel);
		gameConfigBuilder.setStrength(selectedCompLevel);

		int mode = AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE;
		if (!whitePlayerBtn.isChecked() && blackPlayerBtn.isChecked()) {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK;
		} else if (whitePlayerBtn.isChecked() && blackPlayerBtn.isChecked()) {
			mode = AppConstants.GAME_MODE_2_PLAYERS;
		} else if (!whitePlayerBtn.isChecked() && !blackPlayerBtn.isChecked()) {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
		}

		getAppData().setCompGameMode(mode);
		getAppData().setAutoFlipFor2Players(autoFlipSwitch.isChecked());

		return gameConfigBuilder.setMode(mode).build();
	}

	@Override
	public void onSwitchChanged(SwitchButton switchButton, boolean checked) {
		getAppData().setAutoFlipFor2Players(checked);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateBackgroundForButton(whitePlayerBtn, R.style.Button_White_50);
		updateBackgroundForButton(whiteCompBtn, R.style.Button_White_50);
		updateBackgroundForButton(blackPlayerBtn, R.style.Button_Black_65);
		updateBackgroundForButton(blackCompBtn, R.style.Button_Black_65);

		whitePlayerBtn.setSelected(whitePlayerBtn.isChecked());
		whiteCompBtn.setSelected(whiteCompBtn.isChecked());
		blackPlayerBtn.setSelected(blackPlayerBtn.isChecked());
		blackCompBtn.setSelected(blackCompBtn.isChecked());

		if (whitePlayerBtn.isChecked() && blackPlayerBtn.isChecked()) {
			autoFlipView.setVisibility(View.VISIBLE);
		} else {
			autoFlipView.setVisibility(View.GONE);
		}
	}

	private void updateBackgroundForButton(RoboRadioButton button, int styleId) {
		if (button.isChecked()) {
			button.setDrawableStyle(styleId);
		} else {
			button.setBackgroundResource(R.drawable.empty);
		}
	}
}
