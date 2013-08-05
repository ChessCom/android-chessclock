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

	private int[] compStrengthArray;
	private RoboRadioButton myWhiteColorBtn;
	private int selectedStrength;
	private SwitchButton humanVsHumanSwitch;
	private SwitchButton compVsCompSwitch;
	private TextView strengthValueBtn;
	private View compStrengthView;
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
		compStrengthView = view.findViewById(R.id.compStrengthView);

		humanVsHumanSwitch.setChecked(false);
		compVsCompSwitch.setChecked(false);
		humanVsHumanSwitch.setSwitchChangeListener(this);
		compVsCompSwitch.setSwitchChangeListener(this);

		myWhiteColorBtn = (RoboRadioButton) view.findViewById(R.id.myWhiteColorBtn);
		strengthValueBtn = (TextView) view.findViewById(R.id.strengthValueBtn);


		compStrengthArray = getResources().getIntArray(R.array.comp_strength);
		selectedStrength = getAppData().getCompStrength(getActivity());

		SeekBar strengthBar = (SeekBar) view.findViewById(R.id.strengthBar);
		strengthBar.setOnSeekBarChangeListener(ratingBarChangeListener);
		strengthBar.setProgressDrawable(new RatingProgressDrawable(getContext(), strengthBar));
		strengthBar.setProgress(selectedStrength);
		strengthValueBtn.setText(String.valueOf(compStrengthArray[selectedStrength]));

		if (JELLY_BEAN_PLUS_API) {
			ViewGroup compStrengthView = (ViewGroup) view.findViewById(R.id.compStrengthView);
			LayoutTransition layoutTransition = compStrengthView.getLayoutTransition();
			layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		}

		view.findViewById(R.id.playBtn).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.playBtn) {
			ChessBoardComp.resetInstance();
			preferencesEditor.putString(getAppData().getUsername() + AppConstants.SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY);
			preferencesEditor.commit();

			CompGameConfig config = getNewCompGameConfig();

			getActivityFace().openFragment(GameCompFragment.createInstance(config));
			getActivityFace().toggleRightMenu();
		}
	}

	private SeekBar.OnSeekBarChangeListener ratingBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			selectedStrength = progress;
			strengthValueBtn.setText(String.valueOf(compStrengthArray[selectedStrength]));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};

	public CompGameConfig getNewCompGameConfig(){
		getAppData().setCompStrength(getActivity(), selectedStrength);
		int strengthValue = compStrengthArray[selectedStrength];
		gameConfigBuilder.setStrength(strengthValue);

		int mode;
		if (humanVsHumanSwitch.isChecked()) {
			mode = AppConstants.GAME_MODE_HUMAN_VS_HUMAN;
		} else if (compVsCompSwitch.isChecked()) {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
		} else if (myWhiteColorBtn.isChecked()) {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
		} else /*if (myBlackColorBtn.isChecked())*/ {
			mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
		}

		return gameConfigBuilder.setMode(mode).build();
	}

	@Override
	public void onSwitchChanged(SwitchButton switchButton, boolean checked) {
		if (switchButton.getId() == R.id.humanVsHumanSwitch && checked) {
			compVsCompSwitch.setChecked(false);
			iPlayAsView.setEnabled(false);
			compStrengthView.setVisibility(View.GONE);
		} else if (switchButton.getId() == R.id.humanVsHumanSwitch && !checked){
			iPlayAsView.setEnabled(true);
			compStrengthView.setVisibility(View.VISIBLE);

		} else if (switchButton.getId() == R.id.compVsCompSwitch && checked){
			humanVsHumanSwitch.setChecked(false);
			compStrengthView.setVisibility(View.VISIBLE);
		} else if (switchButton.getId() == R.id.compVsCompSwitch && !checked){
			iPlayAsView.setEnabled(true);
		}
	}
}
