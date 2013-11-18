package com.chess.backend.interfaces;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;

import java.util.List;

public abstract class AbstractUpdateListener<ItemType> implements TaskUpdateInterface<ItemType> {

	private boolean usedForFragment;
	private Context context;
	protected boolean useList;
	private Class<ItemType> typeClass;
	private Fragment startedFragment;

	/**
	 * Use this contructor if you need it for fragment. It will handle getActivity() on updateData callback
	 * @param context
	 * @param clazz
	 * @param startedFragment
	 */
	public AbstractUpdateListener(Context context, Fragment startedFragment, Class<ItemType> clazz) {
		this.context = context;
		typeClass = clazz;
		this.startedFragment = startedFragment;
		usedForFragment = true;
	}

	public AbstractUpdateListener(Context context, Class<ItemType> clazz) {
		this.context = context;
		typeClass = clazz;
	}

	/**
	 * Use this contructor if you need it for fragment. It will handle getActivity() on updateData callback
	 * @param context
	 * @param startedFragment
	 */
	public AbstractUpdateListener(Context context, Fragment startedFragment) {
		this.context = context;
		this.startedFragment = startedFragment;
		usedForFragment = true;
	}

	public AbstractUpdateListener(Context context) {
		this.context = context;
	}

	@Override
	public void showProgress(boolean show) {

	}

	@Override
	public boolean useList() {
		return useList;
	}

	@Override
	public void updateListData(List<ItemType> itemsList) {

	}

	@Override
	public void updateData(ItemType returnedObj) {

	}

	@Override
	public void errorHandle(Integer resultCode) {
		// show message only for re-login
		if (RestHelper.containsServerCode(resultCode)) {
			int serverCode = RestHelper.decodeServerCode(resultCode);
			if (serverCode == ServerErrorCodes.INVALID_LOGIN_TOKEN_SUPPLIED) {
				if (context != null) {
					Toast.makeText(context, R.string.pleaseLoginAgain, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	@Override
	public Context getMeContext() throws IllegalStateException{
		if (context == null) {
			throw new IllegalStateException("Context is already dead");
		} else {
			return context;
		}
	}

	@Override
	public void releaseContext() {
		context = null;
	}

	public void setTypeClass(Class<ItemType> typeClass) {
		this.typeClass = typeClass;
	}

	@Override
	public Class<ItemType> getClassType() {
		return typeClass;
	}

	@Override
	public Fragment getStartedFragment() {
		return startedFragment;
	}

	@Override
	public boolean isUsedForFragment() {
		return usedForFragment;
	}

}
