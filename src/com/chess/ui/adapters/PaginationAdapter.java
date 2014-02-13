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

public abstract class PaginationAdapter<T> extends EndlessAdapter {

	private final View mPendingView;
	protected List<T> mAllItems;
	protected List<T> mNewItems;
	protected List<T> itemList;
	private int page;
	protected Context context;
	protected TaskUpdateInterface<T> taskFace;
	protected int result;
	protected LoadItem loadItem;

	protected int maxItems = RestHelper.MAX_ITEMS_CNT;
	protected boolean taskCanceled;

	public PaginationAdapter(Context context, ItemsAdapter<T> adapter, TaskUpdateInterface<T> taskFace) {
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
		taskFace.showProgress(true);
	}

	@Override
	protected void dismissLoad() {
		taskFace.showProgress(false);
	}

	/**
	 * @return true if we need to continue load data,
	 * false if we reached maximum, or did't receive data at all
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
			Log.d("PaginationAdapter", " fragment is not valid to return data");
			return;
		}

		if (result == StaticData.RESULT_OK && !taskCanceled) {
			taskFace.updateListData(itemList);
		} else {
			taskFace.errorHandle(result);
		}

		ArrayList<T> items = new ArrayList<T>();

		if (mAllItems != null)
			items.addAll(mAllItems);

		items.addAll(mNewItems);
		mNewItems = null;
		mAllItems = items;
		getWrappedAdapter().setItemsList(items);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ItemsAdapter<T> getWrappedAdapter() {
		return (ItemsAdapter<T>) super.getWrappedAdapter();
	}

	@Override
	protected View getPendingView(ViewGroup parent) {
		return mPendingView;
	}

	private boolean notValidToReturnForFragment() {
		return taskFace.isUsedForFragment() && (taskFace.getStartedFragment() == null
				|| taskFace.getStartedFragment().getActivity() == null
				|| !taskFace.getStartedFragment().isVisible());
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
