package com.chess.lcc.android;

import com.chess.live.client.Challenge;
import com.chess.live.client.ChallengeListener;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.IntentConstants;

import java.util.Collection;

public class LccChallengeListener implements ChallengeListener {
	public LccChallengeListener(LccHolder lccHolder) {
		if (lccHolder == null) {
			throw new NullPointerException(AppConstants.LCC_HOLDER_IS_NULL);
		}
		this.lccHolder = lccHolder;
	}

	@Override
	public void onChallengeListReceived(Collection<Challenge> challenges) {
		String text =
				"CHALLENGE LISTENER. Private Seek/Challenge List received: user: " + lccHolder.getUser().getUsername() + ", size: " +
						challenges.size();
		lccHolder.clearChallenges();
		for (Challenge ch : challenges) {
			text += "\n  Private Seek/Challenge: user: " + lccHolder.getUser().getUsername() + ", challengeId=" + ch.getId() +
					", from=" + ch.getFrom().getUsername() + ", to=" + ch.getTo();
		}
		LccHolder.LOG.info(text);
		lccHolder.clearOwnChallenges();
		for (Challenge challenge : challenges) {
			if (challenge.getFrom().getUsername().equals(lccHolder.getUser().getUsername())) {
				lccHolder.addOwnChallenge(challenge);
			}
		}
	}

	@Override
	public void onChallengeReceived(Challenge challenge) {
		LccHolder.LOG.info("CHALLENGE LISTENER. Challenge received: " + challenge);
		if (challenge.getFrom().isComputer()) {
			LccHolder.LOG.info("Challenge received: ignore computer player");
			return;
		}
		if (lccHolder.isUserBlocked(challenge.getFrom().getUsername())) {
			LccHolder.LOG.info("Challenge received: blocked user");
			return;
		}
		if (challenge.getFrom().getUsername().equals(lccHolder.getUser().getUsername())) {
			lccHolder.addOwnChallenge(challenge);
			if (lccHolder.getOwnSeeksCount() > LccHolder.OWN_SEEKS_LIMIT) {
				lccHolder.getClient().cancelChallenge(challenge);
				LccHolder.LOG.info("Challenge received: cancel own challenge because of challenges count limit");
				//todo: lccHolder.showOwnSeeksLimitMessage();
				return;
			}
		}
		if (challenge.isSeek()) {
			if (challenge.getFrom().getUsername().equals(lccHolder.getUser().getUsername())) {
				LccHolder.LOG.info("My seek added: user: " + lccHolder.getUser().getUsername() + ", seek: " + challenge);
				lccHolder.putSeek(challenge);
			}
			return;
		}
		if (lccHolder.isUserBlocked(challenge.getBy())) {
			LccHolder.LOG.info("CHALLENGE LISTENER. Challenge received: blocked user");
			return;
		}
		// todo: fix!
		if (lccHolder.isUserPlaying()) {
			lccHolder.getClient().rejectChallenge(challenge, this);
			LccHolder.LOG
					.info("CHALLENGE LISTENER. Challenge received (automatically rejected because of active game): " + challenge);
			return;
		}

		//lccHolder.getAndroid().sendBroadcastIntent(true, 1);

		/*User challenger = challenge.getFrom();
			User receiver = lccHolder.getUser();
			String challengerChessTitle = challenger.getChessTitle() != null ? "(" + challenger.getChessTitle() + ")" : "";
			String receiverChessTitle = receiver.getChessTitle() != null ? "(" + challenger.getChessTitle() + ")" : "";
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
			String ratingCategoryString = "";
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
			lccHolder.getAndroid().getContext().getSoundPlayer().playNotify();
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
		LccHolder.LOG.info(
				"CHALLENGE LISTENER. Seek/Challenge accepted: user: " + lccHolder.getUser().getUsername() + AppConstants.CHALLENGE +
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
		showWarning(warning);
	}

	@Override
	public void onChallengeRejected(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		LccHolder.LOG.info(
				"CHALLENGE LISTENER. Seek/Challenge rejected: user: " + lccHolder.getUser().getUsername() + AppConstants.CHALLENGE +
						challengeId + ", by: " + by + AppConstants.WARNING + warning);
		lccHolder.removeChallenge(challengeId);
		showWarning(warning);
	}

	@Override
	public void onChallengeCancelled(Long challengeId, String by, String warning) {
		// TODO: Show the warning to user if it is not null
		LccHolder.LOG.info(
				"CHALLENGE LISTENER. Seek/Challenge cancelled: user: " + lccHolder.getUser().getUsername() + AppConstants.CHALLENGE +
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
		showWarning(warning);
	}

	private void showWarning(String warning) {
		if (warning != null && !warning.equals("")) {
			lccHolder.getAndroid().sendBroadcastMessageIntent(0, IntentConstants.FILTER_INFO, "WARNING", warning);
		}
	}

	private final LccHolder lccHolder;
}
