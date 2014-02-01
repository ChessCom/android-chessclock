package com.chess.ui.fragments.popup_fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.chess.R;
import com.chess.statics.AppData;
import com.chess.ui.interfaces.PopupListSelectionFace;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 31.10.13
 * Time: 16:36
 */
public class PopupDailyTimeOptionsFragment extends SimplePopupDialogFragment implements View.OnClickListener {

	private HashMap<Integer, Button> timeButtonsModeMap;
	private AppData appData;
	private PopupListSelectionFace listener;

	public static PopupDailyTimeOptionsFragment createInstance(PopupListSelectionFace listener) {
		PopupDailyTimeOptionsFragment frag = new PopupDailyTimeOptionsFragment();
		frag.listener = listener;
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		appData = new AppData(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_daily_home_time_options_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (listener == null) { // we were restored from killed state
			dismiss();
			return;
		}

		timeButtonsModeMap = new HashMap<Integer, Button>();
		timeButtonsModeMap.put(0, (Button) view.findViewById(R.id.time1SelectBtn));
		timeButtonsModeMap.put(1, (Button) view.findViewById(R.id.time2SelectBtn));
		timeButtonsModeMap.put(2, (Button) view.findViewById(R.id.time3SelectBtn));
		timeButtonsModeMap.put(3, (Button) view.findViewById(R.id.time4SelectBtn));
		timeButtonsModeMap.put(4, (Button) view.findViewById(R.id.time5SelectBtn));
		timeButtonsModeMap.put(5, (Button) view.findViewById(R.id.time6SelectBtn));

		int mode = appData.getDefaultDailyMode();
//		darkBtnColor = getResources().getColor(R.color.text_controls_icons_white);
		// set texts to buttons
		int[] newGameButtonsArray = getResources().getIntArray(R.array.days_per_move_array);
		for (Map.Entry<Integer, Button> buttonEntry : timeButtonsModeMap.entrySet()) {
			int key = buttonEntry.getKey();
			Button button = buttonEntry.getValue();
			button.setVisibility(View.VISIBLE);
			if (newGameButtonsArray[key] > 1) {
				button.setText(getString(R.string.arg_days, newGameButtonsArray[key]));
			} else {
				button.setText(getString(R.string.arg_day, newGameButtonsArray[key]));
			}
			button.setOnClickListener(this);

			if (key == mode) {
				button.setSelected(true);
			}
		}
	}

	@Override
	public void onClick(View view) {
		handleTimeModeClicks(view);
	}

	private void handleTimeModeClicks(View view) {
		int id = view.getId();
		boolean timeModeButton = false;
		for (Button button : timeButtonsModeMap.values()) {
			if (id == button.getId()) {
				timeModeButton = true;
				break;
			}
		}

		if (timeModeButton) {
			for (Map.Entry<Integer, Button> buttonEntry : timeButtonsModeMap.entrySet()) {
				Button button = buttonEntry.getValue();
				button.setSelected(false);
				if (id == button.getId()) {
					view.setSelected(true);
					listener.onValueSelected(buttonEntry.getKey());
				}
			}
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		if (listener != null) {
			listener.onDialogCanceled();
		}
	}
}
