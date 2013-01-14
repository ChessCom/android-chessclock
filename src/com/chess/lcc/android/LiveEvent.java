/*
 * GameEvent.java
 */

package com.chess.lcc.android;

import com.chess.live.client.Challenge;

public class LiveEvent {

	private boolean challengeDelayed;

	public enum Event {CONNECTION_FAILURE, CHALLENGE}

	private String message;
	private Challenge challenge;

	private Event event;

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setChallenge(Challenge challenge) {
		this.challenge = challenge;
	}

	public Challenge getChallenge() {
		return challenge;
	}

	public void setChallengeDelayed(boolean challengeDelayed) {
		this.challengeDelayed = challengeDelayed;
	}

	public boolean isChallengeDelayed() {
		return challengeDelayed;
	}
}