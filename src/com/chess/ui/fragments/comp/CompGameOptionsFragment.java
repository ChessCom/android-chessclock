package com.chess.ui.fragments.comp;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.chess.R;
import com.chess.RoboRadioButton;
import com.chess.SwitchButton;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
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
public class CompGameOptionsFragment extends CommonLogicFragment implements SwitchButton.SwitchChangeListener {

	private CompGameConfig.Builder gameConfigBuilder;

	private RoboRadioButton myWhiteColorBtn;
	private RoboRadioButton myBlackColorBtn;
	private int selectedCompLevel;
	private SwitchButton humanVsHumanSwitch;
	private SwitchButton compVsCompSwitch;
	private TextView strengthValueBtn;
	private View compLevelView;
	private View iPlayAsView;

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

		humanVsHumanSwitch = (SwitchButton) view.findViewById(R.id.humanVsHumanSwitch);
		compVsCompSwitch = (SwitchButton) view.findViewById(R.id.compVsCompSwitch);
		iPlayAsView = view.findViewById(R.id.iPlayAsView);
		compLevelView = view.findViewById(R.id.compLevelView);

		humanVsHumanSwitch.setChecked(false);
		compVsCompSwitch.setChecked(false);
		humanVsHumanSwitch.setSwitchChangeListener(this);
		compVsCompSwitch.setSwitchChangeListener(this);

		myWhiteColorBtn = (RoboRadioButton) view.findViewById(R.id.myWhiteColorBtn);
		myBlackColorBtn = (RoboRadioButton) view.findViewById(R.id.myBlackColorBtn);
		strengthValueBtn = (TextView) view.findViewById(R.id.compLevelValueBtn);

		selectedCompLevel = getAppData().getCompLevel();

		int mode = getAppData().getCompGameMode();
		if (mode == AppConstants.GAME_MODE_2_PLAYERS) {
			humanVsHumanSwitch.setChecked(true);
		} else if (mode == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
			compVsCompSwitch.setChecked(true);
		} else if (mode == AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE) {
			myWhiteColorBtn.setChecked(true);
		} else /*if (mode == AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK)*/ {
			myBlackColorBtn.setChecked(true);
		}

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
		}
	}

	protected void startGame() {
		ChessBoardComp.resetInstance();
		preferencesEditor.putString(getAppData().getUsername() + AppConstants.SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY);
		preferencesEditor.commit();

		CompGameConfig config = getNewCompGameConfig();

		getActivityFace().openFragment(GameCompFragment.createInstance(config));
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

		int mode;
		if (humanVsHumanSwitch.isChecked()) {
			mode = AppConstants.GAME_MODE_2_PLAYERS;
		} else if (compVsCompSwitch.isChecked()) {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
		} else if (myWhiteColorBtn.isChecked()) {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE;
		} else /*if (myBlackColorBtn.isChecked())*/ {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK;
		}
		getAppData().setCompGameMode(mode);

		return gameConfigBuilder.setMode(mode).build();
	}

	@Override
	public void onSwitchChanged(SwitchButton switchButton, boolean checked) {
		if (switchButton.getId() == R.id.humanVsHumanSwitch && checked) {
			compVsCompSwitch.setChecked(false);
			iPlayAsView.setEnabled(false);
			compLevelView.setVisibility(View.GONE);
		} else if (switchButton.getId() == R.id.humanVsHumanSwitch && !checked){
			iPlayAsView.setEnabled(true);
			compLevelView.setVisibility(View.VISIBLE);

		} else if (switchButton.getId() == R.id.compVsCompSwitch && checked){
			humanVsHumanSwitch.setChecked(false);
			compLevelView.setVisibility(View.VISIBLE);
		} else if (switchButton.getId() == R.id.compVsCompSwitch && !checked){
			iPlayAsView.setEnabled(true);
		}
	}
}
