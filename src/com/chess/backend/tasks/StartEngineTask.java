package com.chess.ui.engine.stockfish;

import android.content.Context;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.model.CompEngineItem;
import com.chess.statics.StaticData;
import com.chess.ui.interfaces.game_ui.GameCompFace;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 20.04.13
 * Time: 13:30
 */
public class StartEngineTask extends AbstractUpdateTask<CompEngineHelper, Void> {

	private final GameCompFace gameCompActivityFace;
	private final Context context;
	private final CompEngineItem compEngineItem;

	public StartEngineTask(CompEngineItem compEngineItem, GameCompFace gameCompActivityFace,
						   TaskUpdateInterface<CompEngineHelper> taskFace) {
		super(taskFace);
		this.gameCompActivityFace = gameCompActivityFace;
		this.context = taskFace.getMeContext();
		this.compEngineItem = compEngineItem;
	}

	@Override
	protected Integer doTheTask(Void... params) {
		CompEngineHelper.getInstance().startGame(context, compEngineItem, gameCompActivityFace);
		return StaticData.RESULT_OK;
	}
}
