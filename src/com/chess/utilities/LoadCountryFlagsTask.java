package com.chess.utilities;

import android.content.Context;
import android.content.res.Resources;
import com.chess.R;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;

import java.util.ArrayList;

/**
 * @author alien_roger
 * @created 18.09.12
 * @modified 18.09.12
 */
public class LoadCountryFlagsTask extends AbstractUpdateTask<CountryItem, Void> {

	public LoadCountryFlagsTask(TaskUpdateInterface<CountryItem> taskUpdateInterface){
		super(taskUpdateInterface, new ArrayList<CountryItem>());
	}

	@Override
	protected Integer doTheTask(Void... params) {
		Context context = getTaskFace().getMeContext();
		Resources resources = context.getResources();
		String[] names = resources.getStringArray(R.array.new_countries);
		String[] codes = resources.getStringArray(R.array.new_countries_codes);

		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			String description = codes[i];
			CountryItem countryItem = new CountryItem(name, description);
			countryItem.setIcon(AppUtils.getCountryFlag(context, name));
			itemList.add(countryItem);
		}

		return StaticData.RESULT_OK;
	}

}
