package com.chess.backend;

import android.content.Context;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 17:47
 */
public class ServerErrorCode {

	public static final int MULTIPLE_LOGIN = 1;							// Multiple login methods found. Do not submit username or password with other credentials.
	public static final int INVALID_FACEBOOK_TOKEN = 2;					// Invalid Facebook access token.
	public static final int FACEBOOK_USER_NO_ACCOUNT = 3;				// Facebook user has no Chess.com account.
	public static final int ACCOUNT_LOCKED = 4;							// Account locked. Login at Chess.com.
	public static final int INVALID_USERNAME_PASSWORD = 5;				// Invalid password or username.
	public static final int ACCOUNT_NOT_YET_ACTIVE = 6;					// Account not yet active!
	public static final int ACCOUNT_DISABLED = 7;						// Account has been disabled.
	public static final int MUST_SETUP_PASS = 8;						// You must setup a password at Chess.com.
	public static final int RESOURCE_NOT_FOUND = 9;						// Resource not found.
	public static final int TACTICS_DAILY_LIMIT_REACHED = 10;			// Tactics daily limit reached.
	public static final int CANT_ADD_SELF_AS_FRIEND = 11;					// You cannot add yourself as a friend.
	public static final int ALREADY_REQUESTED_TO_FRIEND_THIS_MEMBER = 12;	// You have already requested to add this member as your friend.
	public static final int ALREADY_YOUR_FRIEND = 13;					// This member is already your friend.
	public static final int USER_NOT_ACCEPTING_FRIEND_REQUESTS = 14;	// This user is not accepting friend requests.
	public static final int USER_BLOCKED_YOU = 15;						// This user has blocked you!
	public static final int INVALID_FEN_PASSED = 16;					// Invalid fen passed.
	public static final int CANT_FIND_USER_BY_TOKEN_OR_USERNAME = 17;	// Cannot find user by loginToken or username.
	public static final int PLY_GREATER_THAN_MAX_ALLOWED = 18;			// Passed ply is greater than max allowed.
	public static final int PLEASE_REFRESH_GAME = 19;					// Please refresh the game.
	public static final int GAME_ALREADY_OVER = 20;						// Game already over.
	public static final int NOT_YOUR_MOVE = 21;							// It is not your move.
	public static final int ILLEGAL_MOVE = 22;							// Illegal move.
	public static final int NO_OPEN_DRAW_OFFER = 23;					// No open draw offer.
	public static final int NOT_YOUR_GAME_DUDE = 24;					// Oops! Not your game.
	public static final int CHAT_DISABLED = 25;							// Chat is disabled.
	public static final int ALREADY_ON_VACATAION = 26;					// Already on vacation.
	public static final int OUT_OF_VACATION_TIME = 27;					// Out of vacation time.
	public static final int COMPLETE_ALL_GAME_MOVES = 28;				// Complete all game moves.
	public static final int USER_NOT_ON_VACATION = 29;					// User not on vacation.
	public static final int YOUR_ARE_ON_VACATAION = 30;					// You are on vacation.
	public static final int INVALID_DAYS_PER_MOVE = 31;					// Invalid days per move.
	public static final int MIN_RATING_TOO_LOW = 32;					// Min rating too low.
	public static final int MIN_RATING_TOO_HIGH = 33;					// Min rating too high.
	public static final int MAX_RATING_TOO_LOW = 34;					// Max rating too low.
	public static final int MAX_RATING_TOO_HIGH = 35;					// Max rating too high.
	public static final int RATING_RANGE_MUST_INCLIDE_OWN_RATING = 36;	// Rating range must include your own rating.
	public static final int RATING_RANGE_TOO_SMALL = 37;				// Rating range too small.
	public static final int OPPONENT_ON_VACATION = 38;					// Opponent on vacation.
	public static final int OPPONENT_UNAVAILABLE = 39;					// Opponent unavailable.
	public static final int RATING_TOO_LOW = 40;						// Rating too low.
	public static final int RATING_TOO_HIGH = 41;						// Rating too high.
	public static final int OPPONENT_MISMATCHED_TIME_CONTROL = 42;		// Opponent mismatched time control.
	public static final int YOU_BLOCKED = 43;							// You are blocked.
	public static final int MAX_SEEKS_EXCEEDED = 44;					// Max seeks exceeded.
	public static final int MAKE_MOVES_BEFORE_CREATING_MORE_GAMES = 45;	// Make moves before creating more games.
	public static final int INVALID_USER_ID = 46;						// Invalid user id.
	public static final int INVALID_PARAMTER_TOTAL_GAME = 47;			// Invalid parameter - totalGames
	public static final int PLEASE_REQUEST_50_LESS_GAMES = 48;			// Please request 50 or less games
	public static final int GAME_ALREADY_STARTED = 49;					// Game already started!
	public static final int TIMEOUT_PERCENTAGE_TOO_HIGH = 50;			// Timeout percentage too high.
	public static final int TOO_FEW_GAMES = 51;							// Too few games.
	public static final int MUST_BE_PREMIUM = 52;						// Must be premium.
	public static final int MOVE_SPEED_TOO_SLOW = 53;					// Move speed too slow.
	public static final int CANT_ACCEPT_OWN_SEEK = 54;					// Can't accept own seek.
	public static final int NOT_YOUR_CHALLENGE = 55;					// Not your challenge.
	public static final int INVALID_COUNTRY_PASSED = 56;				// Invalid country id or code passed.
	public static final int USERNAME_ALREADY_TAKEN = 57;				// Username already taken.
	public static final int EMAIL_ALREADY_TAKEN = 58;					// Email already taken.
	public static final int INVALID_CHESSCOM_EMAIL = 59;				// Invalid Chess.com email.
	public static final int INVALID_USERNAME = 60;						// Invalid username.
	public static final int USERNAME_NOT_ALLOWED = 61;					// Username not allowed.
	public static final int INVALID_EMAIL_DOMAIN = 62;					// Invalid email domain.
	public static final int SYSTEM_ERROR = 63;							// System error.
	public static final int WEAK_PASSWORD = 64;							// Weak password.
	public static final int INVALID_LOGIN_TOKEN_SUPPLIED = 65;			// Invalid login token supplied.
	public static final int PLEASE_LOGIN_TO_CONTINUE = 66;				// Please login to continue.

	public static final int UNABLE_TO_RESET_CHESS_GAME = 68; // "Unable to reset chess game.";				//
	public static final int YOU_MUST_SPECIFY_METHOD = 69;// "You must specify \"method\\", \"url\" and \"requestId\".";				//
	public static final int INVALID_PAGE_NUMBER = 70;// "Invalid page number";				//
	public static final int YOU_HAVE_ALREADY_CHANGED_YOUR_USERNAME = 71;// "You have already changed your username. Only one change is allowed.";				//
	public static final int YOU_CAN_T_CHANGE_YOUR_USERNAME_WHILE_YOU_ARE_LOGGED_IN_TO_LIVE_CHESS = 72;// "You can't change your username while you are logged in to Live Chess.";				//
	public static final int PASSED_PLY_IS_GREATER_THAN_MAX_ALLOWED = 73;// "Passed ply is greater than max allowed.";				//
	public static final int INVALID_PARAMETER_PASSED = 75;// "Invalid parameter passed.";				//
	public static final int GAMES_MUST_BE_A_MINIMUM_OF_16_MOVES_TO_BE_SUBMITTED_FOR_COMPUTER_ANALYSIS = 76;// "Games must be a minimum of 16 moves to be submitted for computer analysis.";				//
	public static final int THIS_GAME_HAS_ALREADY_BEEN_SUBMITTED_FOR_COMPUTER_ANALYSIS = 77;// "This game has already been submitted for computer analysis.";				//
	public static final int GAME_LIMIT_REACHED = 78;// "Game limit reached.";				//
	public static final int UNABLE_TO_BE_ANALYZED_BY_OUR_COMPUTER = 79;// "Unable to be analyzed by our computer.";				//
	public static final int ANALYSIS_IS_STILL_IN_PROGRESS = 80;// "Analysis is still in progress.";				//
	public static final int SELF_CHALLENGE = 81;// "Self challenge.";				//
	public static final int AVATAR_IMAGE_IS_INVALID_ALLOWED_FILE_SIZE_ = 82;// "Avatar image is invalid. Allowed file size is 2 MB and allowed mime types are: "image/jpeg, image/gif, image/png".                                    ";				//
	public static final int USERNAME_SHOULD_HAVE_3_CHARACTERS_OR_MORE = 83;// "Username should have 3 characters or more.                                                                                                            ";				//
	public static final int USERNAME_SHOULD_HAVE_20_CHARACTERS_OR_LESS = 84;// "Username should have 20 characters or less.                                                                                                           ";				//
	public static final int USERNAME_IS_NOT_ALLOWED_PLEASE_TRY_AGAIN = 85;// "Username is not allowed. Please try again.                                                                                                            ";				//
	public static final int TO_PREVENT_SPAM_AND_ABUSE_CHESS_COM_DOESN_T_ALLOW_THROW = 86;// "To prevent spam and abuse, Chess.com doesn't allow throw-away email addresses. Please use a legitimate email address or connect with Facebook. Thanks!";				//
	public static final int PASSWORD_SHOULD_HAVE_AT_LEAST_6_CHARACTERS = 87;// "Password should have at least 6 characters.                                                                                                           ";				//
	public static final int FIRST_NAME_SHOULD_HAVE_20_CHARACTERS_OR_LESS = 88;// "First name should have 20 characters or less.                                                                                                         ";				//
	public static final int LAST_NAME_SHOULD_HAVE_20_CHARACTERS_OR_LESS = 89;// "Last name should have 20 characters or less.                                                                                                          ";				//
	public static final int THE_IP_HAS_BEEN_ALREADY_BANNED = 90;// "The IP has been already banned                                                                                                                        ";				//
	public static final int USERNAME_SHOULD_NOT_BE_BLANK = 91;// "Username should not be blank.                                                                                                                         ";				//
	public static final int INVALID_PRODUCT_SKU = 92;// "Invalid product sku                                                                                                                                   ";				//
	public static final int INVALID_PURCHASE_DATA = 93;// "Invalid purchase data                                                                                                                                 ";				//
	public static final int INVALID_PACKAGE_NAME = 94;// "Invalid package name                                                                                                                                  ";				//
	public static final int INVALID_DEVELOPER_PAYLOAD = 95;// "Invalid developer payload                                                                                                                             ";				//
	public static final int INVALID_DATA_SIGNATURE = 96;// "Invalid data signature                                                                                                                                ";				//
	public static final int INVALID_ORDER = 97;// "Invalid order	                                                                                                                                        ";				//
	public static final int USER_DONT_HAVE_VALID_PAYLOAD = 98;// "The user does not have a valid developer payload	                                                                                                                                        ";				//


/*
68	Unable to reset chess game.
69	You must specify "method", "url" and "requestId".
70	Invalid page number
71	You have already changed your username. Only one change is allowed.
72	You can't change your username while you are logged in to Live Chess.
73	Passed ply is greater than max allowed.
75	Invalid parameter passed.
76	Games must be a minimum of 16 moves to be submitted for computer analysis.
77	This game has already been submitted for computer analysis.
78	Game limit reached.
79	Unable to be analyzed by our computer.
80	Analysis is still in progress.
81	Self challenge.
82	Avatar image is invalid. Allowed file size is 2 MB and allowed mime types are: "image/jpeg, image/gif, image/png".
83	Username should have 3 characters or more.
84	Username should have 20 characters or less.
85	Username is not allowed. Please try again.
86	To prevent spam and abuse, Chess.com doesn't allow throw-away email addresses. Please use a legitimate email address or connect with Facebook. Thanks!
87	Password should have at least 6 characters.
88	First name should have 20 characters or less.
89	Last name should have 20 characters or less.
90	The IP has been already banned
91	Username should not be blank.
92	Invalid product sku
93	Invalid purchase data
94	Invalid package name
95	Invalid developer payload
96	Invalid data signature
97	Invalid order
	 */


	public static String getUserFriendlyMessage(Context context, int code){    // TODO convert to strings for i18n
		String[] codesArray = context.getResources().getStringArray(R.array.new_site_api_error_messages);
		if (code < codesArray.length) {
			return codesArray[code-1];
		} else {
			return "Something wrong has happened, developers working on it";
		}
	}
}
