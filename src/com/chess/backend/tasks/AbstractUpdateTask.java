package com.chess.backend.tasks;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
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
		if(isCancelled()) {
			result = StaticData.EMPTY_DATA;
			Log.d("TEST", "isCancelled() from doInBackground called");
			return result;
		}
		return doTheTask(params);
	}

	protected abstract Integer doTheTask(Input... params);


	@Override
	protected void onCancelled(Integer result) {
		super.onCancelled(result);
		taskFace.errorHandle(StaticData.TASK_CANCELED);
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if(isCancelled()) {
			return;
		}
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

	public AbstractUpdateTask<T, Input> executeTask(Input... input){
		if(Build.VERSION.SDK_INT > StaticData.SDK_HONEYCOMB){
			executeOnExecutor(THREAD_POOL_EXECUTOR, input);
		}else
			execute(input);
		return this;
	}
}
