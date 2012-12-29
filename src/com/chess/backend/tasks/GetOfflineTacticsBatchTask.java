package com.chess.backend.tasks;

import android.content.res.Resources;
import com.chess.backend.entity.new_api.TacticItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author alien_roger
 * @created 24.12.12
 * @modified 24.12.12
 */
public class GetOfflineTacticsBatchTask extends AbstractUpdateTask<TacticItem.Data, Integer> {

	private Resources resources;

	public GetOfflineTacticsBatchTask(TaskUpdateInterface<TacticItem.Data> taskUpdateInterface, Resources resources){
		super(taskUpdateInterface);
		this.resources = resources;
	}

	@Override
	protected Integer doTheTask(Integer... ids) {
		int resourceId = ids[0];
		InputStream inputStream = resources.openRawResource(resourceId);
		try {

			TacticItem tacticItem = parseJson(inputStream);

			itemList = tacticItem.getData();

			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return StaticData.INTERNAL_ERROR;
		}

		return StaticData.RESULT_OK;
	}

	private TacticItem parseJson(InputStream jRespString) {
		Gson gson = new Gson();
		Reader reader = new InputStreamReader(jRespString);
		return gson.fromJson(reader, TacticItem.class);
	}
}
