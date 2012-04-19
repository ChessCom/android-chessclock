package com.chess.backend.interfaces;

import android.content.Context;

import java.util.List;

/**
 * TaskUpdateInterface.java
 *
 * @author Alexey Schekin (schekin@azoft.com)
 * @version 1.0.1
 * @created 07.11.2011
 * @modified 07.11.2011
 */
public interface TaskUpdateInterface<T, Input> {
	boolean useList();

	void showProgress(boolean show);

	void updateListData(List<T> itemsList);

	void updateData(T returnedObj);

	T backgroundMethod(Input params);

	void errorHandle(Integer resultCode);

	Context getMeContext();
}
