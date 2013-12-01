package com.chess.lcc.android;

import com.chess.live.client.Challenge;

/**
 * OuterChallengeListener class
 *
 * @author alien_roger
 * @created at: 09.04.12 4:03
 */
public interface OuterChallengeListener {

	void showDialog(Challenge challenge);

	void showDialogImmediately(Challenge challenge);

	void hidePopups();

	void showDelayedDialog(Challenge challenge);

	void showDelayedDialogImmediately(Challenge challenge);
}
