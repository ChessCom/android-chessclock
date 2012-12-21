package com.chess.backend.interfaces;

import android.content.Context;
import com.chess.backend.statics.StaticData;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public abstract class AbstractUpdateListener<ItemType> implements TaskUpdateInterface<ItemType> {

	private Context context;
	protected boolean useList;
	private Class<ItemType> typeClass;

	public AbstractUpdateListener(Context context, Class<ItemType> clazz) {
		this.context = context;
		typeClass = clazz;
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
	}


	@Override
	public void errorHandle(String resultMessage) {
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
	public Type getListType() {
		return new TypeToken<List<ItemType>>() { }.getType();
	}

	@Override
	public Class<ItemType> getClassType() {
		return typeClass;
	}

}
