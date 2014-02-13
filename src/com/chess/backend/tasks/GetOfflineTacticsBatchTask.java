package com.chess.backend.tasks;

import android.content.res.Resources;
import com.chess.backend.entity.api.TacticProblemItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

/**
 * @author alien_roger
 * @created 24.12.12
 * @modified 24.12.12
 */
public class GetOfflineTacticsBatchTask extends AbstractUpdateTask<TacticProblemItem.Data, Integer> {

	private Resources resources;

	public GetOfflineTacticsBatchTask(TaskUpdateInterface<TacticProblemItem.Data> taskUpdateInterface, Resources resources) {
		super(taskUpdateInterface, new ArrayList<TacticProblemItem.Data>());
		this.resources = resources;
	}

	@Override
	protected Integer doTheTask(Integer... ids) {
		int resourceId = ids[0];
		InputStream inputStream = resources.openRawResource(resourceId);
		try {

			TacticProblemItem tacticProblemItem = parseJson(inputStream);

			itemList.clear();
			itemList.addAll(tacticProblemItem.getData());

			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return StaticData.INTERNAL_ERROR;
		}

		return StaticData.RESULT_OK;
	}

	private TacticProblemItem parseJson(InputStream jRespString) {
		Gson gson = new Gson();
		Reader reader = new InputStreamReader(jRespString);
		return gson.fromJson(reader, TacticProblemItem.class);
	}
}
