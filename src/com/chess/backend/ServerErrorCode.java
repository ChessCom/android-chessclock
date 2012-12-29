package com.chess.backend;

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

	public static String getUserFriendlyMessage(int code){

		return null;
	}
}
