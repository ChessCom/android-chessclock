package com.chess.backend.interfaces;

import android.content.Context;

import java.util.List;

/**
 * TaskUpdateInterface.java
 *
 * @author alien_roger
 * @version 1.0.1
 * @created 07.11.2011
 * @modified 16.12.2012
 */
public interface TaskUpdateInterface<T> {
	boolean useList();

	void showProgress(boolean show);

	void updateListData(List<T> itemsList);

	void updateData(T returnedObj);

	void errorHandle(Integer resultCode);

	void errorHandle(String resultMessage);

	Context getMeContext();

	void releaseContext();
}
