package com.chess.backend.tasks;

import android.os.AsyncTask;
import com.chess.model.ComputeMoveItem;
import com.chess.ui.engine.CompEngineHelper;
import com.chess.ui.interfaces.GameCompActivityFace;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 27.06.13
 * Time: 13:11
 */
public class PostMoveToCompTask extends AsyncTask<Void, Void, Void> {

	private ComputeMoveItem computeMoveItem;
	private CompEngineHelper engine;
	private GameCompActivityFace gameCompActivityFace;

	public PostMoveToCompTask(ComputeMoveItem computeMoveItem, CompEngineHelper engine, GameCompActivityFace gameCompActivityFace) {
		this.computeMoveItem = computeMoveItem;
		this.engine = engine;
		this.gameCompActivityFace = gameCompActivityFace;
	}

	@Override
	protected Void doInBackground(Void... voids) {
		engine.makeMove(computeMoveItem.getMove(), gameCompActivityFace);
		return null;
	}
}
