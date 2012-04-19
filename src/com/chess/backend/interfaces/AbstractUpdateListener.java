package com.chess.backend.interfaces;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.statics.StaticData;

import java.util.List;

public abstract class AbstractUpdateListener<T, Input> implements TaskUpdateInterface<T, Input> {

	private View progressView;
	private Context context;
	protected boolean useList;

	public AbstractUpdateListener(Context context, View progressView) {
		this.progressView = progressView;
		this.context = context;
	}

	@Override
	public void showProgress(boolean show) {
		if (progressView != null)
			progressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	public boolean useList() {
		return useList;
	}

	@Override
	public void updateListData(List<T> itemsList) {

	}

	@Override
	public T backgroundMethod(Input params) {
		return null;
	}

	@Override
	public void updateData(T returnedObj) {

	}


	@Override
	public void errorHandle(Integer resultCode) {
		switch (resultCode) {
			case StaticData.UNKNOWN_ERROR:
				Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
		}
	}

	@Override
	public Context getMeContext() {
		return context;
	}
}
