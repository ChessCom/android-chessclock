package com.chess.lcc.android;

import com.chess.live.client.SubscriptionId;
import com.chess.live.client.SubscriptionListener;
import com.chess.utilities.LogMe;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by vm on 08.01.14.
 */
public class LccSubscriptionListener implements SubscriptionListener {

	private static final String TAG = "LccLog-SubscriptionListener";

	@Override
	public void onSuccessfulSubscription(SubscriptionId subscriptionId) {
		LogMe.dl(TAG, "Successful Subscription: subscriptionId=" + subscriptionId);
	}

	@Override
	public void onSubscriptionFailure(final SubscriptionId subscriptionId, final String failureDetails, final AtomicBoolean mustBeRetried)
	{
		LogMe.dl(TAG, "Subscription Failure: subscriptionId=" + subscriptionId + ", failureDetails=" + failureDetails +
				", mustBeRetried=" + mustBeRetried);
	}
}
