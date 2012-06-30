package com.chess.backend.tasks;

import android.content.Context;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;

/**
 * ConnectLiveChessTask class
 *
 * @author alien_roger
 * @created at: 11.06.12 20:35
 */
public class ConnectLiveChessTask extends AbstractUpdateTask<Void, Void> {

	public ConnectLiveChessTask(TaskUpdateInterface<Void> taskFace) {
		super(taskFace);
	}

	@Override
	protected Integer doTheTask(Void... params) {
        Context context = taskFace.getMeContext();
        LccHolder.getInstance(context).performConnect();

		return StaticData.RESULT_OK;
	}
}
