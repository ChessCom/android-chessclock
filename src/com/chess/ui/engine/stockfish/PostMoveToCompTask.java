package com.chess.ui.engine.stockfish;


import com.chess.backend.image_load.bitmapfun.AsyncTask;
import com.chess.model.ComputeMoveItem;
import com.chess.ui.interfaces.game_ui.GameCompFace;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 27.06.13
 * Time: 13:11
 */
public class PostMoveToCompTask extends AsyncTask<Void, Void, Void> {

	private ComputeMoveItem computeMoveItem;
	private CompEngineHelper engine;
	private GameCompFace gameCompActivityFace;

	public PostMoveToCompTask(ComputeMoveItem computeMoveItem, CompEngineHelper engine, GameCompFace gameCompActivityFace) {
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
