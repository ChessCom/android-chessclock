package com.chess.lcc.android;

import android.util.Log;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.statics.AppConstants;
import com.chess.live.client.Challenge;
import com.chess.live.client.ChallengeListener;

import java.util.Collection;

public class LccChallengeListener implements ChallengeListener {
	private OuterChallengeListener outerChallengeListener;
	private static final String TAG = "LCCLOG-CHALLENGE";
	private final LccHolder lccHolder;

	public LccChallengeListener(LccHolder lccHolder) {
		this.lccHolder = lccHolder;
	}

	@Override
	public void onChallengeListReceived(Collection<Challenge> challenges) {
		String text = "CHALLENGE LISTENER. Private Seek/Challenge List received: user: "
				+ lccHolder.getUser().getUsername() + ", size: " + challenges.size();
		lccHolder.clearChallenges();
		for (Challenge ch : challenges) {
			text += "\n  Private Seek/Challenge: user: " + lccHolder.getUser().getUsername()
					+ ", challengeId=" + ch.getId() + ", from=" + ch.getFrom().getUsername() + ", to=" + ch.getTo();
		}
		Log.d(TAG, text);
		lccHolder.clearOwnChallenges();
		for (Challenge challenge : challenges) {
			if (challenge.getFrom().getUsername().equals(lccHolder.getUser().getUsername())) {
				lccHolder.addOwnChallenge(challenge);
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
		if (lccHolder.isUserBlocked(challenge.getFrom().getUsername())) {
			Log.d(TAG, "Challenge received: blocked user");
			return;
		}
		if (challenge.getFrom().getUsername().equals(lccHolder.getUser().getUsername())) {
			lccHolder.addOwnChallenge(challenge);
			if (lccHolder.getOwnSeeksCount() > LccHolder.OWN_SEEKS_LIMIT) {
				lccHolder.getClient().cancelChallenge(challenge);
				Log.d(TAG, "Challenge received: cancel own challenge because of challenges count limit");
				//todo: lccHolder.showOwnSeeksLimitMessage();
				return;
			}
		}
		if (challenge.isSeek()) {
			if (challenge.getFrom().getUsername().equals(lccHolder.getUser().getUsername())) {
				Log.d(TAG, "My seek added: user: " + lccHolder.getUser().getUsername() + ", seek: " + challenge);
				lccHolder.putSeek(challenge);
			}
			return;
		}
		if (lccHolder.isUserBlocked(challenge.getBy())) {
			Log.d(TAG, "CHALLENGE LISTENER. Challenge received: blocked user");
			return;
		}
		// todo: fix!
		if (lccHolder.isUserPlaying()) {
			lccHolder.getClient().rejectChallenge(challenge, this);
			Log.d(TAG, "CHALLENGE LISTENER. Challenge received (automatically rejected because of active game): " + challenge);
			return;
		}

		if (challenge.getTo().equals(lccHolder.getUser().getUsername())) {
			SoundPlayer.getInstance(lccHolder.getContext()).playNotify();
			// show popup dialog with challenge invitation
			outerChallengeListener.showDialog(challenge);
		}
		lccHolder.putChallenge(challenge.getId(), challenge);

	}

	@Override
	public void onChallengeAccepted(Long challengeId, String by, String warning) {
		Log.d(TAG, "CHALLENGE LISTENER. Seek/Challenge accepted: user: " + lccHolder.getUser().getUsername() + AppConstants.CHALLENGE +
						challengeId + ", by: " + by + AppConstants.WARNING + warning);
		lccHolder.removeChallenge(challengeId);

		Log.d("TEST", "onChallengeAccepted , warning" + warning);
		lccHolder.addPendingWarning(warning, by);
	}

	@Override
	public void onChallengeRejected(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		Log.d(TAG, "CHALLENGE LISTENER. Seek/Challenge rejected: user: " + lccHolder.getUser().getUsername() + AppConstants.CHALLENGE +
						challengeId + ", by: " + by + AppConstants.WARNING + warning);
		lccHolder.removeChallenge(challengeId);
		lccHolder.addPendingWarning(warning, by);
	}

	@Override
	public void onChallengeCancelled(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		Log.d(TAG, "CHALLENGE LISTENER. Seek/Challenge cancelled: user: " + lccHolder.getUser().getUsername() + AppConstants.CHALLENGE +
						challengeId + ", by: " + by + AppConstants.WARNING + warning);


		lccHolder.removeChallenge(challengeId);
		lccHolder.addPendingWarning(warning, by);
	}

	public void setOuterChallengeListener(OuterChallengeListener outerChallengeListener) {
		this.outerChallengeListener = outerChallengeListener;
	}

	public OuterChallengeListener getOuterChallengeListener() {
		return outerChallengeListener;
	}
}
