package com.chess.backend.tasks;

import android.util.Log;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;

public class RequestJsonTask<ItemType> extends AbstractUpdateTask<ItemType, LoadItem> {

	public RequestJsonTask(TaskUpdateInterface<ItemType> taskFace) {
		super(taskFace);
	}

	@Override
	protected Integer doTheTask(LoadItem... loadItems) {
		try {
			item = RestHelper.getInstance().requestData(loadItems[0], getTaskFace().getClassType(), getTaskFace().getMeContext());
		} catch (IllegalStateException ex) {
			Log.d("RequestJsonTask", "getTaskFace().getClassType() fails, due to killed state" + ex.toString());
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
