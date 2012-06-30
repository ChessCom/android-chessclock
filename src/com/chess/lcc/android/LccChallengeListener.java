package com.chess.lcc.android;

import android.util.Log;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.statics.AppConstants;
import com.chess.live.client.Challenge;
import com.chess.live.client.ChallengeListener;

import java.util.Collection;

public class LccChallengeListener implements ChallengeListener {
	private OuterChallengeListener outerChallengeListener;
	private static final String TAG = "LccChallengeListener";
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

		//lccHolder.getAndroidStuff().sendBroadcastIntent(true, 1);

		/*User challenger = challenge.getFrom();
			User receiver = lccHolder.getUser();
			String challengerChessTitle = challenger.getChessTitle() != null ? StaticData.SYMBOL_LEFT_PAR + challenger.getChessTitle() + StaticData.SYMBOL_RIGHT_PAR : StaticData.SYMBOL_EMPTY;
			String receiverChessTitle = receiver.getChessTitle() != null ? StaticData.SYMBOL_LEFT_PAR + challenger.getChessTitle() + StaticData.SYMBOL_RIGHT_PAR : StaticData.SYMBOL_EMPTY;
			boolean isChallengerProvisional = false; // todo
			boolean isReceiverProvisional = false; // todo
			GameTimeConfig challengerTimeConfig = challenge.getGameTimeConfig();
			TimeControl challengerTimeControl =
			  new FischerTimeControl(challengerTimeConfig.getBaseTime() * 100, challengerTimeConfig.getTimeIncrement() * 100);
			TimeControl receiverTimeControl =
			  new FischerTimeControl(challengerTimeConfig.getBaseTime() * 100, challengerTimeConfig.getTimeIncrement() * 100);
			boolean isRated = challenge.isRated();
			Integer challengerRating = 0;
			Integer receiverRating = 0;
			switch(challengerTimeConfig.getGameTimeClass())
			{
			  case BLITZ:
			  {
				challengerRating = challenger.getBlitzRating();
				receiverRating = receiver.getBlitzRating();
				break;
			  }
			  case LIGHTNING:
			  {
				challengerRating = challenger.getQuickRating();
				receiverRating = receiver.getQuickRating();
				break;
			  }
			  case STANDARD:
			  {
				challengerRating = challenger.getStandardRating();
				receiverRating = receiver.getStandardRating();
				break;
			  }
			}
			if(challengerRating == null)
			{
			  challengerRating = 0;
			}
			if(receiverRating == null)
			{
			  receiverRating = 0;
			}
			WildVariant variant = Chess.getInstance();
			String ratingCategoryString = StaticData.SYMBOL_EMPTY;
			Player color = null;
			switch(challenge.getColor())
			{
			  case WHITE:
				color = Player.WHITE_PLAYER;
			  case BLACK:
				color = Player.BLACK_PLAYER;
			}
			MatchOffer matchOffer = new MatchOffer(new ChesscomUser(challenger.getUsername()), challengerChessTitle,
												   challengerRating, isChallengerProvisional,
												   new ChesscomUser(receiver.getUsername()), receiverChessTitle,
												   receiverRating, isReceiverProvisional, challengerTimeControl,
												   receiverTimeControl, isRated, variant, ratingCategoryString, color);*/
		if (challenge.getTo().equals(lccHolder.getUser().getUsername())) {
			SoundPlayer.getInstance(lccHolder.getContext()).playNotify();
			// show popup dialog with challenge invitation
			outerChallengeListener.showDialog(challenge);
		}
		lccHolder.putChallenge(challenge.getId(), challenge);

		/*if(!challenge.getTo().equals(lccHolder.getUser().getUsername()))
			{
			  //Seek seek = user.getConnection().mapLccChallengeToJinSeek(challenge, challenge.getTo());
			  lccHolder.putSeek(challenge);
			  //user.getListenerManager().fireSeekEvent(new SeekEvent(user.getConnection(), null, SeekEvent.SEEK_ADDED, seek));
			}*/
		/*else
			{
			  user.getListenerManager().fireMatchOfferEvent(
				new MatchOfferEvent(user.getConnection(), null, MatchOfferEvent.MATCH_OFFER_MADE, matchOffer));
			}*/
	}

	@Override
	public void onChallengeAccepted(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		Log.d(TAG, "CHALLENGE LISTENER. Seek/Challenge accepted: user: " + lccHolder.getUser().getUsername() + AppConstants.CHALLENGE +
						challengeId + ", by: " + by + AppConstants.WARNING + warning);
		lccHolder.removeChallenge(challengeId);
		/*MatchOffer matchOffer = user.getConnection().getJinChallenge(challengeId);
				if(matchOffer != null)
				{
				  user.getConnection().removeJinAndLccChallenge(matchOffer, challengeId);
				  // close Incoming pop-up
				  user.getListenerManager().fireMatchOfferEvent(
					new MatchOfferEvent(user.getConnection(), null, MatchOfferEvent.MATCH_OFFER_WITHDRAWN, matchOffer));
				}*/
//		showWarning(warning);
//		showWarning(warning);
		Log.d("TEST", "onChallengeAccepted , warning" + warning);
		lccHolder.addPendingWarning(warning);
	}

	@Override
	public void onChallengeRejected(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		Log.d(TAG, "CHALLENGE LISTENER. Seek/Challenge rejected: user: " + lccHolder.getUser().getUsername() + AppConstants.CHALLENGE +
						challengeId + ", by: " + by + AppConstants.WARNING + warning);
		lccHolder.removeChallenge(challengeId);
//		showWarning(warning);
		lccHolder.addPendingWarning(warning);
	}

	@Override
	public void onChallengeCancelled(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		Log.d(TAG, "CHALLENGE LISTENER. Seek/Challenge cancelled: user: " + lccHolder.getUser().getUsername() + AppConstants.CHALLENGE +
						challengeId + ", by: " + by + AppConstants.WARNING + warning);

		/*if(by == null) // in second onChallengeCancelled invocation the "by" field is null
			{
			  return;
			}*/
		/*Challenge challenge = lccHolder.getChallenge(challengeId.toString());
			if(challenge != null && challenge.getFrom().getUsername().equals(lccHolder.getUser().getUsername()))
			{
			  lccHolder.removeOwnChallenge(challenge.getId());
			}*/
		lccHolder.removeChallenge(challengeId);
//		showWarning(warning);
		lccHolder.addPendingWarning(warning);
	}

//	private void showWarning(String warning) {
//		if (warning != null && !warning.equals(StaticData.SYMBOL_EMPTY)) {
//			lccHolder.getAndroidStuff().sendBroadcastMessageIntent(0, IntentConstants.FILTER_INFO, "WARNING", warning);
//		}
//	}


	public void setOuterChallengeListener(OuterChallengeListener outerChallengeListener) {
		this.outerChallengeListener = outerChallengeListener;
	}

	public OuterChallengeListener getOuterChallengeListener() {
		return outerChallengeListener;
	}
}
