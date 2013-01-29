package com.chess.backend.tasks;

import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;

/**
 * SendLiveMessageTask class
 *
 * @author alien_roger
 * @created at: 26.07.12 23:10
 */
public class SendLiveMessageTask extends AbstractUpdateTask<String, Long> {

	private String message;
	private LccHolder lccHolder;

	public SendLiveMessageTask(TaskUpdateInterface<String> taskFace, String message, LccHolder lccHolder) {
		super(taskFace);
		this.message = message;
		this.lccHolder = lccHolder;
	}

	@Override
	protected Integer doTheTask(Long... params) {
		lccHolder.sendChatMessage(params[0], message);
		return StaticData.RESULT_OK;
	}
}
