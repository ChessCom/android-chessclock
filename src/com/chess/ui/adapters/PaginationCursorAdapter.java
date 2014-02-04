package com.chess.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.commonsware.cwac.endless.EndlessAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.09.13
 * Time: 12:46
 */
public abstract class PaginationCursorAdapter<T> extends EndlessAdapter {

	private static final String TAG = "PaginationCursorAdapter";
	public static final String TASK_FACE_IS_ALREADY_DEAD = "TaskFace is already dead";

	private final View mPendingView;
	protected List<T> mAllItems;
	protected List<T> mNewItems;
	protected List<T> itemList;
	private int page;
	protected Context context;
	private TaskUpdateInterface<T> taskFace;
	protected int result;
	protected LoadItem loadItem;

	protected int maxItems = RestHelper.MAX_ITEMS_CNT;
	protected boolean taskCanceled;

	public PaginationCursorAdapter(Context context, ItemsCursorAdapter adapter, TaskUpdateInterface<T> taskFace) {
		super(adapter);
		this.context = context;
		this.taskFace = taskFace;
		mPendingView = LayoutInflater.from(context).inflate(R.layout.pending_list_item, null);
		result = StaticData.EMPTY_DATA;
	}

	protected void setFirstPage(int page) {
		this.page = page;
	}

	protected abstract List<T> fetchMoreItems(int page);

	@Override
	protected void showLoad() {
		getTaskFace().showProgress(true);
	}

	@Override
	protected void dismissLoad() {
		getTaskFace().showProgress(false);
	}

	/**
	 * @return true if we need to continue load data,
	 * false if we reached maximum, or didn't receive data at all
	 */
	@Override
	protected final boolean cacheInBackground() {
		mNewItems = fetchMoreItems(page);
		page++;

		if (mNewItems == null || result == StaticData.MAX_REACHED)
			return false;

		if (maxItems != 0 && mNewItems.size() >= maxItems) {
			result = StaticData.MAX_REACHED;
			return false;
		}

		return true;
	}

	@Override
	protected void appendCachedData() {
		if (mNewItems == null) {
			taskFace.errorHandle(StaticData.EMPTY_DATA);
			return;
		}
		if (notValidToReturnForFragment()) {
			Log.d(TAG, " fragment is not valid to return data");
			return;
		}
		if (result == StaticData.RESULT_OK && !taskCanceled) {
			taskFace.updateListData(itemList);
		} else {
			taskFace.errorHandle(result);
		}

		ArrayList<T> items = new ArrayList<T>();

		if (mAllItems != null) {
			items.addAll(mAllItems);
		}

		items.addAll(mNewItems);
		mNewItems = null;
		mAllItems = items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ItemsCursorAdapter getWrappedAdapter() {
		return (ItemsCursorAdapter) super.getWrappedAdapter();
	}

	@Override
	protected View getPendingView(ViewGroup parent) {
		return mPendingView;
	}

	private boolean notValidToReturnForFragment() {
//		if (getTaskFace().isUsedForFragment()) {
//			Log.d(TAG, "getTaskFace().getStartedFragment() == null = " + (getTaskFace().getStartedFragment() == null));
//			Log.d(TAG, "getTaskFace().getStartedFragment().getActivity() == null = " + (getTaskFace().getStartedFragment().getActivity() == null));
//			Log.d(TAG, "!getTaskFace().getStartedFragment().isVisible() = " + (!getTaskFace().getStartedFragment().isVisible()));
//		}
		return getTaskFace().isUsedForFragment() && (getTaskFace().getStartedFragment() == null
				|| getTaskFace().getStartedFragment().getActivity() == null
				|| !getTaskFace().getStartedFragment().isVisible());
	}

	protected TaskUpdateInterface<T> getTaskFace() throws IllegalStateException {
		if (taskFace == null) {
			Log.d(TAG, "taskFace == null");
			throw new IllegalStateException(TASK_FACE_IS_ALREADY_DEAD);
		} else {
			return taskFace;
		}
	}

	public void updateAppendFlag(boolean append) {
		setKeepOnAppending(append);
	}

	public void cancelLoad() {
		taskCanceled = true;
	}

	protected boolean isTaskCanceled() {
		return taskCanceled;
	}

	public void updateLoadItem(LoadItem loadItem) {
		this.loadItem = loadItem;
		setFirstPage(0);
		setKeepOnAppending(true);
	}

}