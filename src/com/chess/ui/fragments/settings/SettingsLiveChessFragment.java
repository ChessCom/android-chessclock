package com.chess.ui.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import com.chess.R;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.widgets.SwitchButton;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.11.13
 * Time: 5:32
 */
public class SettingsLiveChessFragment extends CommonLogicFragment implements SwitchButton.SwitchChangeListener,
		AdapterView.OnItemSelectedListener {
	private static final String SHOW_GENERAL = "show_general";

	private SwitchButton showSubmitSwitch;
	private boolean showGeneralSettings;

	public SettingsLiveChessFragment() {
		Bundle bundle = new Bundle();
		bundle.putBoolean(SHOW_GENERAL, false);
		setArguments(bundle);
	}

	public static SettingsLiveChessFragment createInstance(boolean showGeneralSettings){
		SettingsLiveChessFragment fragment = new SettingsLiveChessFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(SHOW_GENERAL, showGeneralSettings);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			showGeneralSettings = getArguments().getBoolean(SHOW_GENERAL);
		} else {
			showGeneralSettings = savedInstanceState.getBoolean(SHOW_GENERAL);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_live_chess_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.live_chess);

		widgetsInit(view);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(SHOW_GENERAL, showGeneralSettings);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.showSubmitView) {
			showSubmitSwitch.toggle();
		} else if (id == R.id.generalView) {
			getActivityFace().openFragment(new SettingsGeneralFragment());
		}
	}

	@Override
	public void onSwitchChanged(SwitchButton switchButton, boolean checked) {
		if (switchButton.getId() == R.id.showSubmitSwitch) {
			getAppData().setShowSubmitButtonsLive(checked);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
		getAppData().setAfterMoveAction(pos);

		((BaseAdapter) adapterView.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
	}

	private void widgetsInit(View view) {
		showSubmitSwitch = (SwitchButton) view.findViewById(R.id.showSubmitSwitch);
		showSubmitSwitch.setSwitchChangeListener(this);
		view.findViewById(R.id.showSubmitView).setOnClickListener(this);

		showSubmitSwitch.setChecked(getAppData().getShowSubmitButtonsLive());

		// TODO add auto-queen promotion switch
		if (showGeneralSettings) {
			view.findViewById(R.id.generalView).setVisibility(View.VISIBLE);
			view.findViewById(R.id.generalView).setOnClickListener(this);
		}
	}
}
