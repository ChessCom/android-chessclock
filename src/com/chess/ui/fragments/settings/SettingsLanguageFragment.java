package com.chess.ui.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.SelectionAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.01.14
 * Time: 17:03
 */
public class SettingsLanguageFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.language);

		ListView listView = (ListView) view.findViewById(R.id.listView);

		String[] array = getResources().getStringArray(R.array.languages);
		List<SelectionItem> itemsList = new ArrayList<SelectionItem>();
		int prevCode = getAppData().getLanguageCode();
		String[] languageCodes = getResources().getStringArray(R.array.languages_codes);

		String currentLocale = languageCodes[prevCode];
		for (int i = 0; i < array.length; i++) {
			String language = array[i];
			String code = languageCodes[i];
			SelectionItem selectionItem = new SelectionItem(null, language);
			if (currentLocale.equals(code)) {
				selectionItem.setChecked(true);
			}
			itemsList.add(selectionItem);
		}

		listView.setAdapter(new SelectionAdapter(getActivity(), itemsList));
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int prevCode = getAppData().getLanguageCode();
		if (prevCode != position) {
			getAppData().setLanguageCode(position);

			getActivityFace().updateLocale();
		}
	}
}
