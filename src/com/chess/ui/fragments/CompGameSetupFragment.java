package com.chess.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.fragments.game.GameCompFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.04.13
 * Time: 7:06
 */
public class CompGameSetupFragment extends CommonLogicFragment {

	private RadioButton whiteHuman;
	private RadioButton blackHuman;
	private CompGameConfig.Builder gameConfigBuilder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameConfigBuilder = new CompGameConfig.Builder();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_comp_setup_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		whiteHuman = (RadioButton) view.findViewById(R.id.wHuman);
		blackHuman = (RadioButton) view.findViewById(R.id.bHuman);
		view.findViewById(R.id.startPlayBtn).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.startPlayBtn) {

			ChessBoardComp.resetInstance();
			preferencesEditor.putString(AppData.getUserName(getActivity()) + AppConstants.SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY);
			preferencesEditor.commit();

			CompGameConfig config = getNewCompGameConfig();

			getActivityFace().openFragment(GameCompFragment.newInstance(config));
		}
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
}
