package com.chess.ui.engine.stockfish;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.model.CompEngineItem;
import com.chess.ui.interfaces.game_ui.GameCompFace;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 20.04.13
 * Time: 13:30
 */
public class StartEngineTask extends AbstractUpdateTask<CompEngineHelper, Void> {

	private final GameCompFace gameCompActivityFace;
	private final Bundle savedInstanceState;
	private final Context context;
	private final SharedPreferences settings;
	private final CompEngineItem compEngineItem;

	public StartEngineTask(CompEngineItem compEngineItem, GameCompFace gameCompActivityFace, SharedPreferences settings, Bundle savedInstanceState, Context context, TaskUpdateInterface<CompEngineHelper> taskFace) {
		super(taskFace);
		this.gameCompActivityFace = gameCompActivityFace;
		this.savedInstanceState = savedInstanceState;
		this.context = context;
		this.settings = settings;
		this.compEngineItem = compEngineItem;
	}

	@Override
	protected Integer doTheTask(Void... params) {

		CompEngineHelper.getInstance().startGame(context, compEngineItem, gameCompActivityFace, settings, savedInstanceState);

		return StaticData.RESULT_OK;
	}
}
