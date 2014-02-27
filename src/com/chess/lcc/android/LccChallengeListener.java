package com.chess.lcc.android;

import com.chess.live.client.Challenge;
import com.chess.live.client.ChallengeListener;
import com.chess.statics.AppConstants;
import com.chess.utilities.LogMe;

import java.util.Collection;

public class LccChallengeListener implements ChallengeListener {
	private OuterChallengeListener outerChallengeListener;
	private static final String TAG = "LCCLOG-CHALLENGE";
	private final LccHelper lccHelper;

	public LccChallengeListener(LccHelper lccHelper) {
		this.lccHelper = lccHelper;
	}

	@Override
	public void onChallengeListReceived(Collection<Challenge> challenges) {
		String text = "CHALLENGE LISTENER. Private Seek/Challenge List received: user: "
				+ lccHelper.getUser().getUsername() + ", size: " + challenges.size();
		for (Challenge ch : challenges) {
			text += "\n  Private Seek/Challenge: user: " + lccHelper.getUser().getUsername()
					+ ", challengeId=" + ch.getId() + ", from=" + ch.getFrom().getUsername() + ", to=" + ch.getTo();
		}
//		LogMe.dl(TAG, text);
		lccHelper.clearChallengesData();
		for (Challenge challenge : challenges) {
			if (isMy(challenge)) {
				lccHelper.addOwnChallenge(challenge);
			}
		}
	}

	@Override
	public void onChallengeReceived(Challenge challenge) {
//		LogMe.dl(TAG, "CHALLENGE LISTENER. Challenge received: " + challenge);
		if (challenge.getFrom().isComputer()) {
			LogMe.dl(TAG, "Challenge received: ignore computer player");
			return;
		}
		if (lccHelper.isUserBlocked(challenge.getFrom().getUsername())) {
			LogMe.dl(TAG, "Challenge received: blocked user");
			return;
		}
		if (isMy(challenge)) {
			lccHelper.addOwnChallenge(challenge);
			if (lccHelper.getOwnSeeksCount() > LccHelper.OWN_SEEKS_LIMIT) {
				lccHelper.getClient().cancelChallenge(challenge);
				LogMe.dl(TAG, "Challenge received: cancel own challenge because of challenges count limit");
				//todo: lccHelper.showOwnSeeksLimitMessage();
				return;
			}
		}
		if (challenge.isSeek()) {
			if (isMy(challenge)) {
				LogMe.dl(TAG, "My seek added: user: " + lccHelper.getUser().getUsername() + ", seek: " + challenge);
				lccHelper.putSeek(challenge);
			}
			return;
		}
		if (lccHelper.isUserBlocked(challenge.getBy())) {
			LogMe.dl(TAG, "CHALLENGE LISTENER. Challenge received: blocked user");
			return;
		}
		// todo: fix!
		if (lccHelper.isUserPlaying()) {
			lccHelper.getClient().rejectChallenge(challenge, this);
			LogMe.dl(TAG, "CHALLENGE LISTENER. Challenge received (automatically rejected because of active game): " + challenge);
			return;
		}

		if (challenge.getTo().equals(lccHelper.getUser().getUsername())) {
			// show popup dialog with challenge invitation
			if (!lccHelper.TESTING_GAME) {
				outerChallengeListener.showDialog(challenge);
			} else {
				lccHelper.getClient().acceptChallenge(challenge, this);
			}
		}
		lccHelper.putChallenge(challenge.getId(), challenge);

	}

	@Override
	public void onChallengeAccepted(Long challengeId, String by, String warning) {
		LogMe.dl(TAG, "CHALLENGE LISTENER. Seek/Challenge accepted: user: " + lccHelper.getUser().getUsername() + AppConstants.CHALLENGE +
				challengeId + ", by: " + by + AppConstants.WARNING + warning);
		lccHelper.removeChallenge(challengeId);

		lccHelper.addPendingWarning(warning, by);
	}

	@Override
	public void onChallengeRejected(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		LogMe.dl(TAG, "CHALLENGE LISTENER. Seek/Challenge rejected: user: " + lccHelper.getUser().getUsername() + AppConstants.CHALLENGE +
				challengeId + ", by: " + by + AppConstants.WARNING + warning);
		lccHelper.removeChallenge(challengeId);

		if (warning == null) {
			lccHelper.onChallengeRejected(by);
		} else {
			lccHelper.addPendingWarning(warning, by);
		}
	}

	@Override
	public void onChallengeCancelled(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		LogMe.dl(TAG, "CHALLENGE LISTENER. Seek/Challenge cancelled: user: " + lccHelper.getUser().getUsername() + AppConstants.CHALLENGE +
				challengeId + ", by: " + by + AppConstants.WARNING + warning);


		lccHelper.removeChallenge(challengeId);
		lccHelper.addPendingWarning(warning, by);
	}

	public void setOuterChallengeListener(OuterChallengeListener outerChallengeListener) {
		this.outerChallengeListener = outerChallengeListener;
	}

	public OuterChallengeListener getOuterChallengeListener() {
		return outerChallengeListener;
	}

	private boolean isMy(Challenge challenge) {
		return challenge.getFrom().getUsername().equals(lccHelper.getUser().getUsername());
	}
}
