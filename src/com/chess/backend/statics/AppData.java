package com.chess.backend.statics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.ui.interfaces.BoardFace;
import com.chess.utilities.AppUtils;

import static com.chess.backend.statics.AppConstants.*;

/**
 * AppData class
 *
 * @author alien_roger
 * @created at: 15.04.12 21:36
 */
public class AppData {

	public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(StaticData.SHARED_DATA_NAME, Context.MODE_PRIVATE);
	}

	public static void clearPreferences(Context context) {
		getPreferences(context).edit()
				.clear()
				.commit();
	}

	public static void setUserToken(Context context, String token) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putString(USER_TOKEN, token);
		editor.commit();
	}

	public static String getUserToken(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(USER_TOKEN, StaticData.SYMBOL_EMPTY);
	}

	public static int getAfterMoveAction(Context context) {
		return getIntValue(context, PREF_ACTION_AFTER_MY_MOVE, StaticData.AFTER_MOVE_GO_TO_NEXT_GAME);
	}

	public static void setUserFirstName(Context context, String value) {
		setStringValue(context, FIRST_NAME, value);
	}

	public static String getUserFirstName(Context context) {
		return getStringValue(context, FIRST_NAME, StaticData.SYMBOL_EMPTY);
	}

	public static void setUserLastName(Context context, String value) {
		setStringValue(context, LAST_NAME, value);
	}

	public static String getUserLastName(Context context) {
		return getStringValue(context, LAST_NAME, StaticData.SYMBOL_EMPTY);
	}

	public static void setUserLocation(Context context, String value) {
		setStringValue(context, LOCATION, value);
	}

	public static String getUserLocation(Context context) {
		return getStringValue(context, LOCATION, StaticData.SYMBOL_EMPTY);
	}

	public static String getUserName(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
	}

	public static String getPassword(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(PASSWORD, StaticData.SYMBOL_EMPTY);
	}

	/**
	 * @param context
	 * @return
	 */
	@Deprecated
	public static String getOpponentName(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(OPPONENT, StaticData.SYMBOL_EMPTY);
	}

	public static int getChessBoardId(Context context) {
		return getIntValue(context, PREF_BOARD_STYLE, StaticData.B_GREEN_ID);
	}

	public static void setChessBoardId(Context context, int value) {
		setIntValue(context, PREF_BOARD_STYLE, value);
	}

	public static int getPiecesId(Context context) {
		return getIntValue(context, PREF_PIECES_SET, 0);
	}

	public static void setPiecesId(Context context, int value) {
		setIntValue(context, PREF_PIECES_SET, value);
	}

	public static int getLanguageCode(Context context) {
		return getIntValue(context, PREF_LANGUAGE, 0);
	}

	public static String getUserSessionId(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(USER_SESSION_ID, StaticData.SYMBOL_EMPTY);
	}

	/* Game modes */
	public static boolean isFinishedEchessGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_VIEW_FINISHED_ECHESS;
	}

	public static boolean isComputerVsComputerGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_COMPUTER_VS_COMPUTER;
	}

	public static boolean isComputerVsHumanGameMode(BoardFace boardFace) {
		final int mode = boardFace.getMode();
		return mode == GAME_MODE_COMPUTER_VS_HUMAN_WHITE
				|| mode == GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}

	public static boolean isHumanVsHumanGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_HUMAN_VS_HUMAN;
	}

	public static boolean isComputerVsHumanWhiteGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
	}

	public static boolean isComputerVsHumanBlackGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}

	public static void setUserPremiumStatus(Context context, int level) {
		setIntValue(context, USER_PREMIUM_STATUS, level);
	}

	public static int getUserPremiumStatus(Context context) {
		return getPreferences(context).getInt(USER_PREMIUM_STATUS, StaticData.BASIC_USER);
	}

	public static String getUserPremiumStatusStr(Context context) {
		int status = getPreferences(context).getInt(USER_PREMIUM_STATUS, StaticData.BASIC_USER);
		switch (status) {
			case StaticData.GOLD_USER:
				return context.getString(R.string.gold);
			case StaticData.PLATINUM_USER:
				return context.getString(R.string.platinum);
			case StaticData.DIAMOND_USER:
				return context.getString(R.string.gold);
			case StaticData.BASIC_USER:
			default:
				return context.getString(R.string.basic);
		}
	}

	public static Intent getMembershipAndroidIntent(Context context) {
		return getMembershipIntent("?c=androidads", context);
	}

	public static Intent getMembershipVideoIntent(Context context) {
		return getMembershipIntent("?c=androidvideos", context);
	}

	public static Intent getMembershipIntent(String param, Context context) {
		String memberShipUrl = RestHelper.getMembershipLink(AppData.getUserToken(context), param);
		return new Intent(Intent.ACTION_VIEW, Uri.parse(memberShipUrl));
	}

	public static void setCompGameMode(Context context, int mode) {
		setIntValue(context, PREF_COMPUTER_MODE, mode);
	}

	public static int getCompGameMode(Context context) {
		return getIntValue(context, PREF_COMPUTER_MODE, AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);
	}

	public static String getCompSavedGame(Context context) {
		return getStringValue(context, SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY);
	}

	public static boolean haveSavedCompGame(Context context) {
		return !getCompSavedGame(context).equals(StaticData.SYMBOL_EMPTY);
	}

	public static void clearSavedCompGame(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
		preferences.edit()
				.putString(userName + SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY)
				.commit();
	}

	public static int getCompThinkTime(Context context) {
		return getIntValue(context, PREF_COMPUTER_DELAY, COMPUTER_THINK_TIME);
	}

	public static boolean isNotificationsEnabled(Context context) {
		return getBooleanValue(context, PREF_DAILY_NOTIFICATIONS);
	}

	public static boolean isRegisterOnChessGCM(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String savedToken = preferences.getString(GCM_SAVED_TOKEN, StaticData.SYMBOL_EMPTY);
		String userToken = preferences.getString(USER_TOKEN, StaticData.SYMBOL_EMPTY);

		return savedToken.equals(userToken) && preferences.getBoolean(GCM_REGISTERED_ON_SERVER, false);
	}

	public static void registerOnChessGCM(Context context, String token) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putBoolean(GCM_REGISTERED_ON_SERVER, true);
		editor.putString(GCM_SAVED_TOKEN, token);
		editor.commit();
	}

	public static void unRegisterOnChessGCM(Context context) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putBoolean(GCM_REGISTERED_ON_SERVER, false);
		editor.putString(GCM_SAVED_TOKEN, StaticData.SYMBOL_EMPTY);
		editor.commit();
	}

	public static boolean isLiveChess(Context context) {
		return getBooleanValue(context, IS_LIVE_CHESS_ON, false);
	}

	public static void setLiveChessMode(Context context, boolean enabled) {
		setBooleanValue(context, IS_LIVE_CHESS_ON, enabled);
	}

	public static int getDefaultDailyMode(Context context) {
		return getIntValue(context, PREF_DEFAULT_DAILY_MODE, 0);
	}

	public static void setDefaultDailyMode(Context context, int mode) {
		setIntValue(context, PREF_DEFAULT_DAILY_MODE, mode);
	}

	public static int getDefaultLiveMode(Context context) {
		return getIntValue(context, PREF_DEFAULT_LIVE_MODE, 0);
	}

	public static void setDefaultLiveMode(Context context, int mode) {
		setIntValue(context, PREF_DEFAULT_LIVE_MODE, mode);
	}

	public static void setUserSkill(Context context, int skillCode) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(userName + PREF_USER_SKILL_LEVEL, skillCode);
		editor.putBoolean(PREF_USER_SKILL_LEVEL_SET, true);
		editor.commit();
	}

	public static boolean isUserSkillSet(Context context) {
		return getPreferences(context).getBoolean(PREF_USER_SKILL_LEVEL_SET, false);
	}

	public static int getUserSkill(Context context) {
		return getIntValue(context, PREF_USER_SKILL_LEVEL, 0);
	}

	public static void setUserCountry(Context context, String countryName) {
		setStringValue(context, PREF_USER_COUNTRY, countryName);
	}

	public static String getUserCountry(Context context) {
		return getStringValue(context, PREF_USER_COUNTRY, null);
	}

	public static void setUserCountryId(Context context, int id) {
		setIntValue(context, PREF_USER_COUNTRY_ID, id);
	}

	public static int getUserCountryId(Context context) {
		return getIntValue(context, PREF_USER_COUNTRY_ID, 0);
	}

	public static void setUserId(Context context, long userId) {
		getPreferences(context).edit().putLong(USER_ID, userId).commit();
	}

	public static long getUserId(Context context) {
		return getPreferences(context).getLong(USER_ID, 0);
	}

	public static void setUserAvatar(Context context, String url) {
		setStringValue(context, PREF_USER_AVATAR_URL, url);
	}

	public static String getUserAvatar(Context context) {
		return getStringValue(context, PREF_USER_AVATAR_URL, StaticData.SYMBOL_EMPTY);
//		return getPreferences(context).getString(PREF_USER_AVATAR_URL, "http://d1lalstwiwz2br.cloudfront.net/images_users/avatars/alien_roger.gif"); // TODO restore
//		return getPreferences(context).getString(PREF_USER_AVATAR_URL, "http://d1lalstwiwz2br.cloudfront.net/images_users/articles/chesscom-player-profiles-roman-dzindzichasvili_small.1.png"); // TODO restore
	}

	public static void setUserTacticsRating(Context context, int value) {
		setIntValue(context, PREF_USER_TACTICS_RATING, value);
	}

	public static int getUserTacticsRating(Context context) {
		return getIntValue(context, PREF_USER_TACTICS_RATING, 0);
	}

	public static void setPlaySounds(Context context, boolean checked) {
		setBooleanValue(context, PREF_SOUNDS, checked);
		SoundPlayer.getInstance(context);// update internal flag
	}

	public static boolean isPlaySounds(Context context) {
		return getBooleanValue(context, PREF_SOUNDS);
	}

	public static void setShowLegalMoves(Context context, boolean checked) {
		setBooleanValue(context, PREF_SHOW_LEGAL_MOVES, checked);
	}

	public static boolean isShowLegalMoves(Context context) {
		return getBooleanValue(context, PREF_SHOW_LEGAL_MOVES);
	}

	public static void setShowCoordinates(Context context, boolean checked) {
		setBooleanValue(context, PREF_BOARD_COORDINATES, checked);
	}

	public static boolean isShowCoordinates(Context context) {
		return getBooleanValue(context, PREF_BOARD_COORDINATES);
	}

	public static boolean isHighlightLastMove(Context context) {
		return getBooleanValue(context, PREF_BOARD_HIGHLIGHT_LAST_MOVE);
	}

	public static void setHighlightLastMove(Context context, boolean checked) {
		setBooleanValue(context, PREF_BOARD_HIGHLIGHT_LAST_MOVE, checked);
	}

	public static void setAnswerShowBottom(Context context, boolean checked) {
		setBooleanValue(context, PREF_BOARD_SHOW_ANSWER_BOTTOM, checked);
	}

	public static void setThemeBackId(Context context, int themeId) {
		setIntValue(context, PREF_THEME_BACK_ID, themeId);
	}

	/**
	 * Get saved theme background Id
	 * @param context
	 * @return
	 */
	public static int getThemeBackId(Context context) {
		int backId = getIntValue(context, PREF_THEME_BACK_ID, R.drawable.img_theme_green_felt);
		return AppUtils.isThemeBackIdValid(backId) ? backId : R.drawable.img_theme_green_felt;
//		return R.color.white;
	}


	/*--------------------------- Common Shared logic ------------------------*/

	private static void setBooleanValue(Context context, String field, boolean checked) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
		preferences.edit().putBoolean(userName + field, checked).commit();
	}

	private static boolean getBooleanValue(Context context, String field) {
		return getBooleanValue(context, field, true);
	}

	private static boolean getBooleanValue(Context context, String field, boolean defValue) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getBoolean(userName + field, defValue);
	}

	private static void setStringValue(Context context, String field, String value) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
		preferences.edit().putString(userName + field, value).commit();
	}

	private static String getStringValue(Context context, String field, String defValue) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getString(userName + field, defValue);
	}

	private static int getIntValue(Context context, String field, int defValue) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getInt(userName + field, defValue);
	}

	private static void setIntValue(Context context, String field, int value) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
		preferences.edit().putInt(userName + field, value).commit();
	}


}
