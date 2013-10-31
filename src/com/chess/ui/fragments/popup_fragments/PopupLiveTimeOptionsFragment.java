package com.chess.ui.fragments.popup_fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.chess.R;
import com.chess.statics.AppData;
import com.chess.statics.Symbol;
import com.chess.ui.interfaces.PopupListSelectionFace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.10.13
 * Time: 12:23
 */
public class PopupLiveTimeOptionsFragment extends SimplePopupDialogFragment implements View.OnClickListener {

	private HashMap<Integer, Button> timeButtonsModeMap;
	private AppData appData;
	private PopupListSelectionFace listener;

	public static PopupLiveTimeOptionsFragment createInstance(PopupListSelectionFace listener) {
		PopupLiveTimeOptionsFragment frag = new PopupLiveTimeOptionsFragment();
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
		return inflater.inflate(R.layout.new_right_live_options_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (listener == null) { // we were restored from killed state
			dismiss();
			return;
		}

		//add padding
		int padding = getResources().getDimensionPixelSize(R.dimen.default_scr_side_padding);
		view.setPadding(padding, padding, padding, padding);

		ArrayList<View> timeOptionsGroup = new ArrayList<View>();
		timeOptionsGroup.add(view.findViewById(R.id.liveLabelStandardTxt));
		timeOptionsGroup.add(view.findViewById(R.id.liveLabelBlitzTxt));
		timeOptionsGroup.add(view.findViewById(R.id.liveLabelBulletTxt));

		timeButtonsModeMap = new HashMap<Integer, Button>();
		timeButtonsModeMap.put(0, (Button) view.findViewById(R.id.standard1SelectBtn));
		timeButtonsModeMap.put(1, (Button) view.findViewById(R.id.blitz1SelectBtn));
		timeButtonsModeMap.put(2, (Button) view.findViewById(R.id.blitz2SelectBtn));
		timeButtonsModeMap.put(3, (Button) view.findViewById(R.id.bullet1SelectBtn));
		timeButtonsModeMap.put(4, (Button) view.findViewById(R.id.standard2SelectBtn));
		timeButtonsModeMap.put(5, (Button) view.findViewById(R.id.blitz3SelectBtn));
		timeButtonsModeMap.put(6, (Button) view.findViewById(R.id.blitz4SelectBtn));
		timeButtonsModeMap.put(7, (Button) view.findViewById(R.id.bullet2SelectBtn));

		int mode = appData.getDefaultLiveMode();
		// set texts to buttons
		int darkBtnColor = getResources().getColor(R.color.text_controls_icons_white);
		for (View timeView : timeOptionsGroup) {
			timeView.setVisibility(View.VISIBLE);
		}
		String[] newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
		for (Map.Entry<Integer, Button> buttonEntry : timeButtonsModeMap.entrySet()) {
			int key = buttonEntry.getKey();
			Button button = buttonEntry.getValue();
			button.setVisibility(View.VISIBLE);
			button.setText(getLiveModeButtonLabel(newGameButtonsArray[key]));
			button.setOnClickListener(this);
			button.setTextColor(darkBtnColor);

			if (key == mode) {
				setDefaultTimeMode(button, buttonEntry.getKey());
			}
		}
	}

	private String getLiveModeButtonLabel(String label) {
		if (label.contains(Symbol.SLASH)) { // "5 | 2"
			return label;
		} else { // "10 min"
			return getString(R.string.min_arg, label);
		}
	}

	private void setDefaultTimeMode(View view, int mode) {
		view.setSelected(true);
		appData.setDefaultLiveMode(mode);
	}

	@Override
	public void onClick(View view) {
		handleLiveModeClicks(view);
	}

	private void handleLiveModeClicks(View view) {
		int id = view.getId();
		boolean liveModeButton = false;
		for (Button button : timeButtonsModeMap.values()) {
			if (id == button.getId()) {
				liveModeButton = true;
				break;
			}
		}

		if (liveModeButton) {
			for (Map.Entry<Integer, Button> buttonEntry : timeButtonsModeMap.entrySet()) {
				Button button = buttonEntry.getValue();
				button.setSelected(false);
				if (id == button.getId()) {
					setDefaultTimeMode(view, buttonEntry.getKey());
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
