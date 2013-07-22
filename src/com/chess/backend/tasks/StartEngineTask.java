package com.chess.ui.engine.stockfish;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.ui.interfaces.game_ui.GameCompFace;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 20.04.13
 * Time: 13:30
 */
public class StartEngineTask extends AbstractUpdateTask<CompEngineHelper, Void> {

	private int gameMode;
	//private String initialFen;
	private GameCompFace gameCompActivityFace;
	private Bundle savedInstanceState;
	private final Context context;
	private SharedPreferences settings;
	private int strength;
	private int time;
	private int depth;
	private boolean restoreGame;
	private String fen;

	public StartEngineTask(int gameMode, boolean restoreGame, String fen, int strength, int time, int depth, GameCompFace gameCompActivityFace, SharedPreferences settings, Bundle savedInstanceState, Context context, TaskUpdateInterface<CompEngineHelper> taskFace) {
		super(taskFace);
		// todo @compengine: extract method parameters to data object
		this.gameMode = gameMode;
		//this.initialFen = initialFen;
		this.gameCompActivityFace = gameCompActivityFace;
		this.savedInstanceState = savedInstanceState;
		this.context = context;
		this.settings = settings;
		this.strength = strength;
		this.time = time;
		this.depth = depth;
		this.restoreGame = restoreGame;
		this.fen = fen;
	}

	@Override
	protected Integer doTheTask(Void... params) {

		CompEngineHelper.getInstance().init(context);

		CompEngineHelper.getInstance().startGame(gameMode, restoreGame, fen, strength, time, depth, gameCompActivityFace, settings, savedInstanceState);

		return StaticData.RESULT_OK;
	}
}
