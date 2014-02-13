package com.chess.lcc.android;

import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.live.client.Challenge;
import com.chess.live.client.LiveChessClient;
import com.chess.statics.StaticData;
import com.chess.utilities.LogMe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * LccChallengeTaskRunner class
 *
 * @author alien_roger
 * @created at: 25.05.12 4:38
 */
public class LccChallengeTaskRunner {

	private LccChallengeListener challengeListener;
	private TaskUpdateInterface<Challenge> challengeTaskFace;
	private LccHelper lccHelper;

	public LccChallengeTaskRunner(TaskUpdateInterface<Challenge> challengeTaskFace, LccHelper lccHelper) {
		this.challengeTaskFace = challengeTaskFace;
		this.lccHelper = lccHelper;
		challengeListener = lccHelper.getChallengeListener();
	}

	private LiveChessClient getClient() {
		return lccHelper.getClient();
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
			getClient().sendChallenge(challenge[0], challengeListener);
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
			getClient().cancelChallenge(challenge[0]);
			return StaticData.RESULT_OK;
		}
	}

	public void runCancelBatchChallengeTask(Challenge[] challenge) {
		new LiveCancelBatchChallengeTask().executeTask(challenge);
	}

	private class LiveCancelBatchChallengeTask extends AbstractUpdateTask<Challenge, Challenge> {
		public LiveCancelBatchChallengeTask() {
			super(challengeTaskFace);
		}

		@Override
		protected Integer doTheTask(Challenge... challenges) {
			for (Challenge challenge : challenges) {
				getClient().cancelChallenge(challenge);
			}
			return StaticData.RESULT_OK;
		}
	}

	public void cancelAllOwnChallenges(HashMap<Long, Challenge> challengesMap) {
		if (!challengesMap.isEmpty()) {
			Collection<Challenge> challenges = challengesMap.values();
			int size = challenges.size();
			runCancelBatchChallengeTask(challenges.toArray(new Challenge[size]));
		}
	}

	public void declineAllChallenges(Challenge acceptedChallenge, HashMap<Long, Challenge> challenges) {
		// TODO decline all challenges except acceptedChallenge

		List<Challenge> removeMe = new ArrayList<Challenge>();
		for (Challenge challenge : challenges.values()) {
			if (!challenge.equals(acceptedChallenge))
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
			if (!challenge.equals(currentChallenge))
				retainMe.add(challenge);
		}

		if (retainMe.size() > 0) {
			challengeListener.getOuterChallengeListener().showDelayedDialog(retainMe.get(retainMe.size() - 1));
		}
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
			LogMe.dl("LiveAcceptChallengeTask liveChessClient=" + getClient());
			LogMe.dl("LiveAcceptChallengeTask challenge=" + challenge);
			LogMe.dl("LiveAcceptChallengeTask challenge[0]=" + challenge[0]);
			getClient().acceptChallenge(challenge[0], challengeListener);
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
			getClient().rejectChallenge(challenge[0], challengeListener);
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
				getClient().rejectChallenge(challenge, challengeListener);
			}
			return StaticData.RESULT_OK;
		}
	}
}
