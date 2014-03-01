package com.chess.backend.tasks;

import android.util.Log;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 01.03.14
 * Time: 7:18
 */
public class RequestBatchJsonTask extends AbstractUpdateTask<List, LoadItem> {

	private Class[] desiredClasses;

	public RequestBatchJsonTask(TaskUpdateInterface<List> taskFace, Class[] desiredClasses) {
		super(taskFace);
		this.desiredClasses = desiredClasses;
	}

	@Override
	protected Integer doTheTask(LoadItem... loadItems) {

		try {
			item = RestHelper.getInstance().requestBatchData(loadItems, desiredClasses, getTaskFace().getMeContext());
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