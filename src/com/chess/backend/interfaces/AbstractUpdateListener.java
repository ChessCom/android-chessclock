package com.chess.backend.interfaces;

import android.content.Context;
import com.chess.backend.statics.StaticData;

import java.lang.ref.SoftReference;
import java.util.List;

public abstract class AbstractUpdateListener<T> implements TaskUpdateInterface<T> {

	private SoftReference<Context> context;
	protected boolean useList;

	public AbstractUpdateListener(Context context) {
		this.context = new SoftReference<Context>(context);
	}

	@Override
	public void showProgress(boolean show) {

	}

	@Override
	public boolean useList() {
		return useList;
	}

	@Override
	public void updateListData(List<T> itemsList) {

	}

	@Override
	public void updateData(T returnedObj) {

	}


	@Override
	public void errorHandle(Integer resultCode) {
		switch (resultCode) {
			case StaticData.UNKNOWN_ERROR:
//				Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
		}
	}


	@Override
	public void errorHandle(String resultMessage) {
	}

	@Override
	public Context getMeContext() throws IllegalStateException{
		if (context == null || context.get() == null) {
			throw new IllegalStateException("Context is already dead");
		} else {
			return context.get();
		}
	}

	@Override
	public void releaseContext() {
		context = null;
	}
}
