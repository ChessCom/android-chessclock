package com.chess.backend.tasks;

import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHelper;

/**
 * SendLiveMessageTask class
 *
 * @author alien_roger
 * @created at: 26.07.12 23:10
 */
public class SendLiveMessageTask extends AbstractUpdateTask<String, Long> {

	private String message;

	public SendLiveMessageTask(TaskUpdateInterface<String> taskFace, String message) {
		super(taskFace);
		this.message = message;
	}

	@Override
	protected Integer doTheTask(Long... params) {
		LccHelper.getInstance(getTaskFace().getMeContext()).sendChatMessage(params[0], message);
		return StaticData.RESULT_OK;
	}
}
