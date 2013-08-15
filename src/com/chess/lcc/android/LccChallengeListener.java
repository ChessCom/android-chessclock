package com.chess.lcc.android;

import android.util.Log;
import com.chess.backend.statics.SoundPlayer;
import com.chess.backend.statics.AppConstants;
import com.chess.live.client.Challenge;
import com.chess.live.client.ChallengeListener;

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
		lccHelper.clearChallenges();
		for (Challenge ch : challenges) {
			text += "\n  Private Seek/Challenge: user: " + lccHelper.getUser().getUsername()
					+ ", challengeId=" + ch.getId() + ", from=" + ch.getFrom().getUsername() + ", to=" + ch.getTo();
		}
		Log.d(TAG, text);
		lccHelper.clearOwnChallenges();
		for (Challenge challenge : challenges) {
			if (challenge.getFrom().getUsername().equals(lccHelper.getUser().getUsername())) {
				lccHelper.addOwnChallenge(challenge);
			}
		}
	}

	@Override
	public void onChallengeReceived(Challenge challenge) {
		Log.d(TAG, "CHALLENGE LISTENER. Challenge received: " + challenge);
		if (challenge.getFrom().isComputer()) {
			Log.d(TAG, "Challenge received: ignore computer player");
			return;
		}
		if (lccHelper.isUserBlocked(challenge.getFrom().getUsername())) {
			Log.d(TAG, "Challenge received: blocked user");
			return;
		}
		if (challenge.getFrom().getUsername().equals(lccHelper.getUser().getUsername())) {
			lccHelper.addOwnChallenge(challenge);
			if (lccHelper.getOwnSeeksCount() > LccHelper.OWN_SEEKS_LIMIT) {
				lccHelper.getClient().cancelChallenge(challenge);
				Log.d(TAG, "Challenge received: cancel own challenge because of challenges count limit");
				//todo: lccHelper.showOwnSeeksLimitMessage();
				return;
			}
		}
		if (challenge.isSeek()) {
			if (challenge.getFrom().getUsername().equals(lccHelper.getUser().getUsername())) {
				Log.d(TAG, "My seek added: user: " + lccHelper.getUser().getUsername() + ", seek: " + challenge);
				lccHelper.putSeek(challenge);
			}
			return;
		}
		if (lccHelper.isUserBlocked(challenge.getBy())) {
			Log.d(TAG, "CHALLENGE LISTENER. Challenge received: blocked user");
			return;
		}
		// todo: fix!
		if (lccHelper.isUserPlaying()) {
			lccHelper.getClient().rejectChallenge(challenge, this);
			Log.d(TAG, "CHALLENGE LISTENER. Challenge received (automatically rejected because of active game): " + challenge);
			return;
		}

		if (challenge.getTo().equals(lccHelper.getUser().getUsername())) {
			SoundPlayer.getInstance(lccHelper.getContext()).playNotify();
			// show popup dialog with challenge invitation
			outerChallengeListener.showDialog(challenge);
		}
		lccHelper.putChallenge(challenge.getId(), challenge);

	}

	@Override
	public void onChallengeAccepted(Long challengeId, String by, String warning) {
		Log.d(TAG, "CHALLENGE LISTENER. Seek/Challenge accepted: user: " + lccHelper.getUser().getUsername() + AppConstants.CHALLENGE +
						challengeId + ", by: " + by + AppConstants.WARNING + warning);
		lccHelper.removeChallenge(challengeId);

		lccHelper.addPendingWarning(warning, by);
	}

	@Override
	public void onChallengeRejected(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		Log.d(TAG, "CHALLENGE LISTENER. Seek/Challenge rejected: user: " + lccHelper.getUser().getUsername() + AppConstants.CHALLENGE +
						challengeId + ", by: " + by + AppConstants.WARNING + warning);
		lccHelper.removeChallenge(challengeId);
		lccHelper.addPendingWarning(warning, by);
	}

	@Override
	public void onChallengeCancelled(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		Log.d(TAG, "CHALLENGE LISTENER. Seek/Challenge cancelled: user: " + lccHelper.getUser().getUsername() + AppConstants.CHALLENGE +
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
}
