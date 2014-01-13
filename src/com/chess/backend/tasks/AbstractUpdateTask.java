package com.chess.backend.tasks;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.chess.backend.image_load.bitmapfun.AsyncTask;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;

import java.util.List;

public abstract class AbstractUpdateTask<ItemType, Input> extends AsyncTask<Input, Void, Integer> {

	private static final String TAG = "AbstractUpdateTask";
	public static final String TASK_FACE_IS_ALREADY_DEAD = "TaskFace is already dead";
	private TaskUpdateInterface<ItemType> taskFace; // SoftReferences & WeakReferences are not reliable, because they become killed even at the same activity and task become unfinished
	protected ItemType item;
	protected final List<ItemType> itemList;
	protected boolean useList;
	protected int result;

	public AbstractUpdateTask(TaskUpdateInterface<ItemType> taskFace) {
		init(taskFace);
		this.itemList = null;
	}

	public AbstractUpdateTask(TaskUpdateInterface<ItemType> taskFace, List<ItemType> itemList) {
		init(taskFace);
		this.itemList = itemList;
	}

	private void init(TaskUpdateInterface<ItemType> taskFace) {
		if (taskFace == null || taskFace.getMeContext() == null) { // we may start task right after another but listener at this time will be already killed
			cancel(true);
			Log.e(TAG, "TaskFace is null, not running task");
			return;
		}
		this.taskFace = taskFace;
		useList = taskFace.useList();
		result = StaticData.EMPTY_DATA;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
//		blockScreenRotation(true);
		try {
			getTaskFace().showProgress(true);
		} catch (IllegalStateException ex) {
			Log.d(TAG, "onPreExecute " + ex.toString());
		}
	}

	@Override
	protected Integer doInBackground(Input... params) {
		if (isCancelled()) {
			return StaticData.EMPTY_DATA;
		}
		return doTheTask(params);
	}

	protected abstract Integer doTheTask(Input... params);

//	protected void blockScreenRotation(boolean block) {  // Don't remove until move to Loaders
//		try {
////			Context context = getTaskFace().getMeContext();
////			if (context instanceof Activity) {
////				Activity activity = (Activity) context;
////				if(block){
////					// Stop the screen orientation changing during an event
////					if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
////						activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
////					}else{
////						activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
////					}
////				} else {
////					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
////				}
////			}
//		} catch (IllegalStateException ex) {
//			Log.d(TAG, "blockScreenRotation " + ex.toString());
//		}
//	}

	@Override
	protected void onCancelled(Integer result) {
		super.onCancelled(result);
//		blockScreenRotation(false);
		try {
			getTaskFace().errorHandle(StaticData.TASK_CANCELED);
		} catch (IllegalStateException ex) {
			Log.d(TAG, "getTaskFace().errorHandle fails, due to killed state" + ex.toString());
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (result == null) {
			result = StaticData.UNKNOWN_ERROR;
		}
//		blockScreenRotation(false);

		if (isCancelled()) {   // no need to check as we catch it
			return;
		}

		try {
			if (notValidToReturnForFragment()) {
				Log.d(TAG, ">>>>> fragment (" + getTaskFace().getStartedFragment() + ") is not valid to return data <<<<<");
				return;
			}
			getTaskFace().showProgress(false);
			if (result == StaticData.RESULT_OK) {
				if (useList) {
					getTaskFace().updateListData(itemList);
				} else {
					getTaskFace().updateData(item);
				}
			} else {
				getTaskFace().errorHandle(result);
			}

		} catch (IllegalStateException ex) {
			Log.d(TAG, "getTaskFace() at onPostExecute fails, due to killed state" + ex.toString());
		}
	}

	private boolean notValidToReturnForFragment() {
		Fragment startedFragment = getTaskFace().getStartedFragment();
//		if (getTaskFace().isUsedForFragment()) {
//			Log.d(TAG, "_____________________________________________________________");
//
//			Log.d(TAG, "fragment = " + startedFragment + ", taskFace = " + taskFace + ", task = " + this
//					+ ", fragment == null = " + (startedFragment == null));
//			if (startedFragment != null) {
//				Log.d(TAG, "startedFragment.getActivity() == null = " + (startedFragment.getActivity() == null));
//				Log.d(TAG, "!startedFragment.isVisible() = " + (!startedFragment.isVisible()));
//			}
//		}
		return getTaskFace().isUsedForFragment() && (startedFragment == null
				|| startedFragment.getActivity() == null
				|| !startedFragment.isVisible());
	}

	protected TaskUpdateInterface<ItemType> getTaskFace() throws IllegalStateException {
		if (taskFace == null) {
			Log.d(TAG, "taskFace == null");
			throw new IllegalStateException(TASK_FACE_IS_ALREADY_DEAD);
		} else {
			return taskFace;
		}
	}

	public AbstractUpdateTask<ItemType, Input> executeTask(Input... input) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
//			executeOnExecutor(THREAD_POOL_EXECUTOR, input);
			executeOnExecutor(DUAL_THREAD_EXECUTOR, input);
		} else {
			execute(input);
		}
		return this;
	}

}
