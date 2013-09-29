package com.chess.statics;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.09.13
 * Time: 19:06
 */
public class WelcomeHolder {

	private boolean fullscreen;
	private static WelcomeHolder ourInstance = new WelcomeHolder();

	public static WelcomeHolder getInstance() {
		return ourInstance;
	}

	private WelcomeHolder() {
	}

	public boolean isFullscreen() {
		return fullscreen;
	}

	public void setFullscreen(boolean fullscreen) {
		this.fullscreen = fullscreen;
	}
}
