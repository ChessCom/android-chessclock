package com.chess.backend.share.twitter;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 24.02.12
 * @modified 24.02.12
 */
public class ShareConstants {

	/*Twitter constants*/

    public static final String CONSUMER_KEY = "LyHy94VBiaHEmmYbpbGNmg";
    public static final String CONSUMER_SECRET= "j3xPpXPup77pjlVi2ZvNcXm5Zo5XWaooEnbOov81o";

    public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
    public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
    public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";

    public static final String	OAUTH_CALLBACK_SCHEME	= "x-oauthflow-twitter";
    private static final String	OAUTH_CALLBACK_HOST		= "callback";
    public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;

    public static final String TWEET_MSG = "tweet_msg";
}
