package com.chess.backend.tasks;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
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
		blockScreenRotation(true);
		taskFace.showProgress(true);
	}

	@Override
	protected Integer doInBackground(Input... params) {
		if(isCancelled()) {
			result = StaticData.EMPTY_DATA;
			return result;
		}
		return doTheTask(params);
	}

	protected abstract Integer doTheTask(Input... params);

	protected void blockScreenRotation(boolean block){
		if (taskFace.getMeContext() instanceof Activity) {
			Activity activity = (Activity) taskFace.getMeContext();
			if(block){
				// Stop the screen orientation changing during an event
				if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}else{
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
			} else {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			}
		}
	}

	@Override
	protected void onCancelled(Integer result) {
		super.onCancelled(result);
		blockScreenRotation(false);
		taskFace.errorHandle(StaticData.TASK_CANCELED);
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		blockScreenRotation(false);

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
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
			executeOnExecutor(THREAD_POOL_EXECUTOR, input);
		}else
			execute(input);
		return this;
	}
}
