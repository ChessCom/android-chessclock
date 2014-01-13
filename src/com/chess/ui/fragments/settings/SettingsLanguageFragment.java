package com.chess.ui.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.chess.R;
import com.chess.ui.adapters.StringSpinnerAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.01.14
 * Time: 17:03
 */
public class SettingsLanguageFragment extends CommonLogicFragment implements AdapterView.OnItemSelectedListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_language_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.language);

		Spinner langSpinner = (Spinner) view.findViewById(R.id.languageSpinner);
		langSpinner.setAdapter(new StringSpinnerAdapter(getActivity(), getItemsFromEntries(R.array.languages)));
		langSpinner.setOnItemSelectedListener(this);
		langSpinner.setSelection(getAppData().getLanguageCode());
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		int prevCode = getAppData().getLanguageCode();
		if (prevCode != position) {
			getAppData().setLanguageCode(position);

			getActivityFace().updateLocale();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
