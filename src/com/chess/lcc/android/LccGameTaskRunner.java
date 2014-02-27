package com.chess.lcc.android;

import com.bugsense.trace.BugSenseHandler;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.live.client.Game;
import com.chess.live.client.LiveChessClient;
import com.chess.statics.FlurryData;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.utilities.LogMe;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;

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
			liveChessClient.makeDraw(game[0], Symbol.EMPTY);
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
			liveChessClient.makeResign(game[0], Symbol.EMPTY);
			return StaticData.RESULT_OK;
		}
	}

	public void runMakeResignAndExitTask() {
		new LiveMakeResignAndExitTask().executeTask(lccHelper.getCurrentGame());
	}

	private class LiveMakeResignAndExitTask extends AbstractUpdateTask<Game, Game> {
		public LiveMakeResignAndExitTask() {
			super(gameTaskFace);
		}

		@Override
		protected Integer doTheTask(Game... game) {
			liveChessClient.exitGame(game[0]);
			liveChessClient.makeResign(game[0], Symbol.EMPTY);
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
			liveChessClient.rejectDraw(game[0], Symbol.EMPTY);
			return StaticData.RESULT_OK;
		}
	}

	public void runMakeMoveTask(Game game, String move, String debugInfo, ActionBarUpdateListener<Game> makeMoveTaskListener) {
		new MakeMoveTask(move, debugInfo, makeMoveTaskListener).executeTask(game);
	}

	private class MakeMoveTask extends AbstractUpdateTask<Game, Game> {
		private String move;
		private String debugInfo;

		public MakeMoveTask(String move, String debugInfo, ActionBarUpdateListener<Game> makeMoveTaskListener) {
			super(makeMoveTaskListener);
			this.move = move;
			this.debugInfo = debugInfo;
		}

		@Override
		protected Integer doTheTask(Game... game) {
			try {

				long threadId = Thread.currentThread().getId();

				LogMe.dl("DEBUG: MakeMoveTask: move=" + move + ", game=" + game[0].getId() +
						", threadId=" + threadId + ", threadName=" + Thread.currentThread().getName());

				MoveInfo latestMoveInfo = lccHelper.getLatestMoveInfo();
				if (latestMoveInfo != null) {
					latestMoveInfo.setMoveFirstThreadId(-1); // we do not interesting anymore on First thread if Second one is reached
					latestMoveInfo.setMoveSecondThreadId(threadId);
					lccHelper.setLatestMoveInfo(latestMoveInfo);
				}

				liveChessClient.makeMove(game[0], move);

			} catch (IllegalArgumentException e) {
				BugSenseHandler.addCrashExtraData("APP_LCC_MAKE_MOVE", debugInfo);
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("DEBUG", debugInfo);

				FlurryAgent.logEvent(FlurryData.ILLEGAL_MOVE_DEBUG, params);

				BugSenseHandler.sendExceptionMap(params, e);
				return StaticData.INTERNAL_ERROR;
			}

			return StaticData.RESULT_OK;
		}
	}
}
