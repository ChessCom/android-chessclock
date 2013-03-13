package com.chess.lcc.android;

import com.bugsense.trace.BugSenseHandler;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.live.client.Game;
import com.chess.live.client.LiveChessClient;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * LccGameTaskRunner class
 *
 * @author alien_roger
 * @created at: 25.05.12 4:38
 */
public class LccGameTaskRunner {

	private LiveChessClient liveChessClient;
	private TaskUpdateInterface<Game> gameTaskFace;
	private LccHelper lccHelper;

	public LccGameTaskRunner(TaskUpdateInterface<Game> gameTaskFace, LccHelper lccHelper) {
		this.gameTaskFace = gameTaskFace;
		this.lccHelper = lccHelper;
		this.liveChessClient = lccHelper.getClient();
	}

	public void runMakeDrawTask() {
		new LiveMakeDrawTask().executeTask(lccHelper.getCurrentGame());
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

	public void runMakeResignTask() {
		new LiveMakeResignTask().executeTask(lccHelper.getCurrentGame());
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

	public void runAbortGameTask() {
		new LiveAbortGameTask().executeTask(lccHelper.getCurrentGame());
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

	public void runRejectDrawTask() {
		new LiveRejectDrawTask().executeTask(lccHelper.getCurrentGame());
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

	public void runMakeMoveTask(Game game, String move, String debugInfo) {
		new MakeMoveTask(move, debugInfo).executeTask(game);
	}

	private class MakeMoveTask extends AbstractUpdateTask<Game, Game> {
		private String move;
		private String debugInfo;

		public MakeMoveTask(String move, String debugInfo) {
			super(gameTaskFace);
			this.move = move;
			this.debugInfo = debugInfo;
		}

		@Override
		protected Integer doTheTask(Game... game) {

			try {
				liveChessClient.makeMove(game[0], move);
			} catch (IllegalArgumentException e) {
				BugSenseHandler.addCrashExtraData("APP_LCC_MAKE_MOVE", debugInfo);
				Map<String, String> params = new HashMap<String, String>();
				params.put("DEBUG", debugInfo);
				FlurryAgent.logEvent(FlurryData.ILLEGAL_MOVE_DEBUG, params);
				throw new IllegalArgumentException(debugInfo, e);
			}

			return StaticData.RESULT_OK;
		}
	}
}
