package com.chess.ui.interfaces;

/**
 * Created by vm on 04.02.14.
 */
public interface LoginErrorUpdateListener { // todo: to vm: this should be removed from this package and you should use LiveChesConnectionListener instead!

	void onInvalidLoginCredentials();

	void onFacebookUserNoAccount();
}
