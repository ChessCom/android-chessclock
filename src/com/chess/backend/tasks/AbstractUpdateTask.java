package com.chess.backend.tasks;

import android.os.AsyncTask;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;

import java.util.List;

public abstract class AbstractUpdateTask<T, Input> extends AsyncTask<Input, Void, Integer> {

	protected TaskUpdateInterface<T> taskFace;
	protected T item;
	protected List<T> itemList;
	protected boolean useList;
	protected int result;

	public AbstractUpdateTask(TaskUpdateInterface<T> taskFace) {
		this.taskFace = taskFace;
		useList = taskFace.useList();
		result = StaticData.EMPTY_DATA;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		taskFace.showProgress(true);
	}

	@Override
	protected Integer doInBackground(Input... params) {
		if(isCancelled())
			result = StaticData.EMPTY_DATA;
		return doTheTask(params);
	}

	protected abstract Integer doTheTask(Input... params);

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		taskFace.showProgress(false);
		if (result == StaticData.RESULT_OK) {
			if (useList)
				taskFace.updateListData(itemList);
			else
				taskFace.updateData(item);
		} else {
			taskFace.errorHandle(result);
		}
	}

}
