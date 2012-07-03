package com.chess.lcc.android;

import android.content.Context;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.live.client.Game;
import com.chess.live.client.LiveChessClient;

/**
 * LccChallengeTaskRunner class
 *
 * @author alien_roger
 * @created at: 25.05.12 4:38
 */
public class LccGameTaskRunner {

	private LiveChessClient liveChessClient;
	private TaskUpdateInterface<Game> gameTaskFace;
	private Context context;

	public LccGameTaskRunner(TaskUpdateInterface<Game> gameTaskFace) {
		this.gameTaskFace = gameTaskFace;
		context = gameTaskFace.getMeContext();
		this.liveChessClient = LccHolder.getInstance(context).getClient();
	}


	public void runMakeDrawTask(Long gameId) {
		new LiveMakeDrawTask().executeTask(LccHolder.getInstance(context).getGame(gameId));
	}

	private class LiveMakeDrawTask extends AbstractUpdateTask<Game, Game> {
		public LiveMakeDrawTask() {
			super(gameTaskFace);
		}

		@Override
		protected Integer doTheTask(Game... game) {
			liveChessClient.makeDraw(game[0], StaticData.SYMBOL_EMPTY);
			return StaticData.RESULT_OK;
		}
	}

	public void runMakeResignTask(Long gameId) {
		new LiveMakeResignTask().executeTask(LccHolder.getInstance(context).getGame(gameId));
	}

	private class LiveMakeResignTask extends AbstractUpdateTask<Game, Game> {
		public LiveMakeResignTask() {
			super(gameTaskFace);
		}

		@Override
		protected Integer doTheTask(Game... game) {
			liveChessClient.makeResign(game[0], StaticData.SYMBOL_EMPTY);
			return StaticData.RESULT_OK;
		}
	}

	public void runAbortGameTask(Long gameId) {
		new LiveAbortGameTask().executeTask(LccHolder.getInstance(context).getGame(gameId));
	}

	private class LiveAbortGameTask extends AbstractUpdateTask<Game, Game> {
		public LiveAbortGameTask() {
			super(gameTaskFace);
		}

		@Override
		protected Integer doTheTask(Game... game) {
			liveChessClient.abortGame(game[0], StaticData.SYMBOL_EMPTY);
			return StaticData.RESULT_OK;
		}
	}

	public void runRejectDrawTask(Long gameId) {
		new LiveRejectDrawTask().executeTask(LccHolder.getInstance(context).getGame(gameId));
	}

	private class LiveRejectDrawTask extends AbstractUpdateTask<Game, Game> {
		public LiveRejectDrawTask() {
			super(gameTaskFace);
		}

		@Override
		protected Integer doTheTask(Game... game) {
			liveChessClient.rejectDraw(game[0], StaticData.SYMBOL_EMPTY);
			return StaticData.RESULT_OK;
		}
	}

}
