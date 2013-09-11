package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.ExplorerMovesItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;

import java.util.ArrayList;
import java.util.List;


public class SaveExplorerMovesListTask extends AbstractUpdateTask<ExplorerMovesItem.Move, Long> {

	//private final String username;
	private String fen;

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveExplorerMovesListTask(TaskUpdateInterface<ExplorerMovesItem.Move> taskFace, List<ExplorerMovesItem.Move> currentItems,
									 ContentResolver resolver, String fen) {
		super(taskFace, new ArrayList<ExplorerMovesItem.Move>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		//username = appData.getUsername();
		this.fen = fen;
	}

	@Override
	protected Integer doTheTask(Long... ids) {

		synchronized (itemList) {
			for (ExplorerMovesItem.Move currentItem : itemList) { // if
				arguments[0] = fen;

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DbScheme.uriArray[DbScheme.Tables.EXPLORER_MOVES.ordinal()];
				Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_FEN, DbDataManager.SELECTION_FEN, arguments, null);

				ContentValues values = DbDataManager.putExplorerMoveItemToValues(currentItem, fen);

				DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);

			}
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
