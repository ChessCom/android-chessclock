package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.ExplorerMovesItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;

import java.util.ArrayList;
import java.util.List;


public class SaveExplorerMovesListTask extends AbstractUpdateTask<ExplorerMovesItem.Move, Long> {

	private String fen;

	private ContentResolver contentResolver;
	protected static String[] sArguments = new String[2];

	public SaveExplorerMovesListTask(TaskUpdateInterface<ExplorerMovesItem.Move> taskFace, List<ExplorerMovesItem.Move> currentItems,
									 ContentResolver resolver, String fen) {
		super(taskFace, new ArrayList<ExplorerMovesItem.Move>());
		this.itemList.addAll(currentItems);
		this.contentResolver = resolver;
		this.fen = fen;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (ExplorerMovesItem.Move currentItem : itemList) {
			final String[] arguments = sArguments;
			arguments[0] = fen;
			arguments[1] = currentItem.getMove();

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbScheme.uriArray[DbScheme.Tables.EXPLORER_MOVES.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_FEN_AND_MOVE,
					DbDataManager.SELECTION_FEN_AND_MOVE, arguments, null);

			ContentValues values = DbDataManager.putExplorerMoveItemToValues(currentItem, fen);

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}

		return StaticData.RESULT_OK;
	}

}
