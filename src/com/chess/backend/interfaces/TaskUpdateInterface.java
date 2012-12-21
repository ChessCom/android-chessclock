package com.chess.backend.interfaces;

import android.content.Context;

import java.lang.reflect.Type;
import java.util.List;

/**
 * TaskUpdateInterface.java
 *
 * @author alien_roger
 * @version 1.0.1
 * @created 07.11.2011
 * @modified 16.12.2012
 */
public interface TaskUpdateInterface<ItemType> {
	boolean useList();

	void showProgress(boolean show);

	void updateListData(List<ItemType> itemsList);

	void updateData(ItemType returnedObj);

	void errorHandle(Integer resultCode);

	void errorHandle(String resultMessage);

	Context getMeContext();

	void releaseContext();

	Type getListType();

	Class<ItemType> getClassType();
}
