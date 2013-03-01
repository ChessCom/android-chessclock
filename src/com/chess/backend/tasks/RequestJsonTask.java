package com.chess.backend.tasks;

import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;

public class RequestJsonTask<ItemType> extends AbstractUpdateTask<ItemType, LoadItem> {

	public RequestJsonTask(TaskUpdateInterface<ItemType> taskFace) {
		super(taskFace);
	}

	@Override
	protected Integer doTheTask(LoadItem... loadItems) {
		try {
			item = RestHelper.requestData(loadItems[0], getTaskFace().getClassType());
		} catch (InternalErrorException e) {
			e.logMe();
			result = e.getCode();
		}

		if (item != null) {
			result = StaticData.RESULT_OK;
		}
		return result;
	}

}
