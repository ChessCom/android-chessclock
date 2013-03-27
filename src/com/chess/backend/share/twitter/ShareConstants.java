package com.chess.backend.share.twitter;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 24.02.12
 * @modified 24.02.12
 */
public class ShareConstants {

	/*Twitter constants*/

    public static final String CONSUMER_KEY = "j6ExpY8RXthNBTvZxrEGdQ";
    public static final String CONSUMER_SECRET= "F6B7Fml7KpJpBNuHNxCj4EiDN3cEBuHT5Yr7RExCc";

    public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
    public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
    public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";

    public static final String	OAUTH_CALLBACK_SCHEME	= "x-oauthflow-twitter";
    private static final String	OAUTH_CALLBACK_HOST		= "callback";
    public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;

    public static final String TWEET_MSG = "tweet_msg";
}
