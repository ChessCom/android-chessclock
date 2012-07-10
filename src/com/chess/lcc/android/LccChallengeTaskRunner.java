package com.chess.lcc.android;

import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.live.client.Challenge;
import com.chess.live.client.LiveChessClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * LccChallengeTaskRunner class
 *
 * @author alien_roger
 * @created at: 25.05.12 4:38
 */
public class LccChallengeTaskRunner {

	private LiveChessClient liveChessClient;
	private LccChallengeListener challengeListener;
	private TaskUpdateInterface<Challenge> challengeTaskFace;

	public LccChallengeTaskRunner(TaskUpdateInterface<Challenge> challengeTaskFace) {
		this.challengeTaskFace = challengeTaskFace;
		this.liveChessClient = LccHolder.getInstance(challengeTaskFace.getMeContext()).getClient();
		challengeListener = LccHolder.getInstance(challengeTaskFace.getMeContext()).getChallengeListener();
	}

	public void runSendChallengeTask(Challenge challenge) {
		new LiveSendChallengeTask().executeTask(challenge);
	}

	private class LiveSendChallengeTask extends AbstractUpdateTask<Challenge, Challenge> {
		public LiveSendChallengeTask() {
			super(challengeTaskFace);
		}

		@Override
		protected Integer doTheTask(Challenge... challenge) {
			liveChessClient.sendChallenge(challenge[0], challengeListener);
			return StaticData.RESULT_OK;
		}
	}

	public void runCancelChallengeTask(Challenge challenge) {
		new LiveCancelChallengeTask().executeTask(challenge);
	}

	private class LiveCancelChallengeTask extends AbstractUpdateTask<Challenge, Challenge> {
		public LiveCancelChallengeTask() {
			super(challengeTaskFace);
		}

		@Override
		protected Integer doTheTask(Challenge... challenge) {
			liveChessClient.cancelChallenge(challenge[0]);
			return StaticData.RESULT_OK;
		}
	}

    public void declineAllChallenges(Challenge acceptedChallenge, HashMap<Long, Challenge> challenges) {
        // TODO decline all challenges except acceptedChallenge

        List<Challenge> removeMe = new ArrayList<Challenge>();
        for (Challenge challenge : challenges.values()) {
            if(!challenge.equals(acceptedChallenge))
                removeMe.add(challenge);
        }
        Challenge[] declinedChallenges = new Challenge[removeMe.size()];
        for (int i = 0, removeMeSize = removeMe.size(); i < removeMeSize; i++) {
            Challenge challenge = removeMe.get(i);
            declinedChallenges[i] = challenge;
        }

        runRejectBatchChallengeTask(declinedChallenges);
        challengeListener.getOuterChallengeListener().hidePopups();
    }


    public void declineCurrentChallenge(Challenge currentChallenge, HashMap<Long, Challenge> challenges) {
        runRejectChallengeTask(currentChallenge);
        final List<Challenge> retainMe = new ArrayList<Challenge>();
        for (Challenge challenge : challenges.values()) {
            if(!challenge.equals(currentChallenge))
                retainMe.add(challenge);
        }

        if (retainMe.size() > 0)
            challengeListener.getOuterChallengeListener().showDelayedDialog(retainMe.get(retainMe.size() - 1));
    }



	public void runAcceptChallengeTask(Challenge challenge) {
		new LiveAcceptChallengeTask().executeTask(challenge);
	}

	private class LiveAcceptChallengeTask extends AbstractUpdateTask<Challenge, Challenge> {
		public LiveAcceptChallengeTask() {
			super(challengeTaskFace);
		}

		@Override
		protected Integer doTheTask(Challenge... challenge) {
			liveChessClient.acceptChallenge(challenge[0], challengeListener);
			return StaticData.RESULT_OK;
		}
	}

	public void runRejectChallengeTask(Challenge challenge) {
		new LiveRejectChallengeTask().executeTask(challenge);
	}

	private class LiveRejectChallengeTask extends AbstractUpdateTask<Challenge, Challenge> {
		public LiveRejectChallengeTask() {
			super(challengeTaskFace);
		}

		@Override
		protected Integer doTheTask(Challenge... challenge) {
			liveChessClient.rejectChallenge(challenge[0], challengeListener);
			return StaticData.RESULT_OK;
		}
	}

	public void runRejectBatchChallengeTask(Challenge[] challenge) {
		new LiveRejectBatchChallengeTask().executeTask(challenge);
	}

	private class LiveRejectBatchChallengeTask extends AbstractUpdateTask<Challenge, Challenge> {
		public LiveRejectBatchChallengeTask() {
			super(challengeTaskFace);
		}

		@Override
		protected Integer doTheTask(Challenge... challenges) {
			for (Challenge challenge : challenges) {
				liveChessClient.rejectChallenge(challenge, challengeListener);
			}
			return StaticData.RESULT_OK;
		}
	}
}
