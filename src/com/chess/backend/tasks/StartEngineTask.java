package com.chess.backend.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.CompEngineHelper;
import com.chess.ui.interfaces.GameCompActivityFace;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 20.04.13
 * Time: 13:30
 */
public class StartEngineTask extends AbstractUpdateTask<CompEngineHelper, Void> {

	private int gameMode;
	//private String initialFen;
	private GameCompActivityFace gameCompActivityFace;
	private Bundle savedInstanceState;
	private final Context context;
	private SharedPreferences settings;
	private int strength;
	private int time;
	private int depth;
	private boolean restoreGame;

	public StartEngineTask(int gameMode, boolean restoreGame, int strength, int time, int depth, GameCompActivityFace gameCompActivityFace, SharedPreferences settings, Bundle savedInstanceState, Context context, TaskUpdateInterface<CompEngineHelper> taskFace) {
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
	}

	@Override
	protected Integer doTheTask(Void... params) {

		final CompEngineHelper engine = new CompEngineHelper();
		engine.init(context);

		AppData.setCompEngineHelper(engine);

		engine.startGame(gameMode, restoreGame, strength, time, depth, gameCompActivityFace, settings, savedInstanceState);

		return StaticData.RESULT_OK;
	}
}
