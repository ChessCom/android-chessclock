package com.chess.ui.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.VacationItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.StringSpinnerAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.widgets.SwitchButton;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.09.13
 * Time: 5:53
 */
public class SettingsDailyChessFragment extends CommonLogicFragment implements SwitchButton.SwitchChangeListener,
		AdapterView.OnItemSelectedListener {

	private static final String SHOW_GENERAL = "show_general";

	private SwitchButton showSubmitSwitch;
	private SwitchButton onVacationSwitch;
	private Spinner afterMoveSpinner;
	private VacationUpdateListener getVacationUpdateListener;
	private VacationUpdateListener postVacationUpdateListener;
	private VacationUpdateListener deleteVacationUpdateListener;
	private boolean showGeneralSettings;

	public SettingsDailyChessFragment() {
		Bundle bundle = new Bundle();
		bundle.putBoolean(SHOW_GENERAL, false);
		setArguments(bundle);
	}

	public static SettingsDailyChessFragment createInstance(boolean showGeneralSettings){
		SettingsDailyChessFragment fragment = new SettingsDailyChessFragment();
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

		getVacationUpdateListener = new VacationUpdateListener(VacationUpdateListener.GET);
		postVacationUpdateListener = new VacationUpdateListener(VacationUpdateListener.POST);
		deleteVacationUpdateListener = new VacationUpdateListener(VacationUpdateListener.DELETE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_daily_chess_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.daily_chess);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		LoadItem loadItem = LoadHelper.getOnVacation(getUserToken());

		new RequestJsonTask<VacationItem>(getVacationUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(SHOW_GENERAL, showGeneralSettings);
	}

	private class VacationUpdateListener extends ChessLoadUpdateListener<VacationItem> {

		static final int GET = 0;
		static final int POST = 1;
		static final int DELETE = 2;
		private int listenerCode;

		private VacationUpdateListener(int listenerCode) {
			super(VacationItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(VacationItem returnedObj) {
			super.updateData(returnedObj);

			switch (listenerCode) {
				case GET:
					onVacationSwitch.setChecked(returnedObj.getData().isOnVacation());
					getAppData().setOnVacation(returnedObj.getData().isOnVacation());
					break;
				case POST:
					showToast(R.string.vacation_on);
//					onVacationSwitch.setChecked(true);
					getAppData().setOnVacation(true);
					break;
				case DELETE:
					showToast(R.string.vacation_off);
					getAppData().setOnVacation(false);

//					onVacationSwitch.setChecked(false);
					break;
			}
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.afterMoveView) {
			afterMoveSpinner.performClick();
		} else if (id == R.id.showSubmitView) {
			showSubmitSwitch.toggle();
		} else if (id == R.id.onVacationView) {
			onVacationSwitch.toggle();
		} else if (id == R.id.generalView) {
			getActivityFace().openFragment(new SettingsGeneralFragment());
		}
	}

	@Override
	public void onSwitchChanged(SwitchButton switchButton, boolean checked) {
		if (switchButton.getId() == R.id.showSubmitSwitch) {
			getAppData().setShowSubmitButtonsDaily(checked);
		} else if (switchButton.getId() == R.id.onVacationSwitch) {
			LoadItem loadItem;
			if (checked && !getAppData().isOnVacation()) {
				loadItem = LoadHelper.postOnVacation(getUserToken());
				new RequestJsonTask<VacationItem>(postVacationUpdateListener).executeTask(loadItem);
			} else if (!checked && getAppData().isOnVacation()){
				loadItem = LoadHelper.deleteOnVacation(getUserToken());
				new RequestJsonTask<VacationItem>(deleteVacationUpdateListener).executeTask(loadItem);
			}
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
		onVacationSwitch = (SwitchButton) view.findViewById(R.id.onVacationSwitch);

		showSubmitSwitch.setSwitchChangeListener(this);
		onVacationSwitch.setSwitchChangeListener(this);

		view.findViewById(R.id.afterMoveView).setOnClickListener(this);
		view.findViewById(R.id.showSubmitView).setOnClickListener(this);
		view.findViewById(R.id.onVacationView).setOnClickListener(this);

		showSubmitSwitch.setChecked(getAppData().getShowSubmitButtonsDaily());

		if (showGeneralSettings) {
			view.findViewById(R.id.generalView).setVisibility(View.VISIBLE);
			view.findViewById(R.id.generalView).setOnClickListener(this);
		}

		afterMoveSpinner = (Spinner) view.findViewById(R.id.afterMoveSpinner);
		afterMoveSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), getItemsFromEntries(R.array.afterMyMove)));
		afterMoveSpinner.setSelection(getAppData().getAfterMoveAction());
		afterMoveSpinner.setOnItemSelectedListener(this);
	}

}

