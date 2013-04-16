package com.chess.backend.statics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import com.chess.backend.RestHelper;
import com.chess.ui.interfaces.BoardFace;

/**
 * AppData class
 *
 * @author alien_roger
 * @created at: 15.04.12 21:36
 */
public class AppData {

    public static final String GUEST_USER_NAME = "guest"; // it is invalid username to login, so use it for clear logic

	public static SharedPreferences getPreferences(Context context){
		return context.getSharedPreferences(StaticData.SHARED_DATA_NAME, Context.MODE_PRIVATE);
	}

	public static void setUserToken(Context context, String token) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putString(AppConstants.USER_TOKEN, token);
		editor.commit();
	}

	public static String getUserToken(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY);
	}

    public static int getAfterMoveAction(Context context) {
        SharedPreferences preferences = getPreferences(context);
        String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
        return preferences.getInt(userName + AppConstants.PREF_ACTION_AFTER_MY_MOVE, StaticData.AFTER_MOVE_GO_TO_NEXT_GAME);
    }

    public static String getUserName(Context context){
        SharedPreferences preferences = getPreferences(context);
        return preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
    }

	public static String getPassword(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY);
	}

	public static String getOpponentName(Context context){
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(AppConstants.OPPONENT, StaticData.SYMBOL_EMPTY);
	}

	public static boolean isHighlightEnabled(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getBoolean(userName + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, true);
	}

	public static boolean showCoordinates(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getBoolean(userName + AppConstants.PREF_BOARD_COORDINATES, true);
	}

	public static int getChessBoardId(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
//		return preferences.getInt(userName + AppConstants.PREF_BOARD_TYPE, StaticData.B_WOOD_DARK_ID);
		return preferences.getInt(userName + AppConstants.PREF_BOARD_TYPE, StaticData.B_GREEN_ID);
	}

	public static int getPiecesId(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getInt(userName + AppConstants.PREF_PIECES_SET, 0);
	}

	public static int getLanguageCode(Context context){
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getInt(userName + StaticData.SHP_LANGUAGE, 0);
	}

	public static String getUserSessionId(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(AppConstants.USER_SESSION_ID, StaticData.SYMBOL_EMPTY);
	}

	public static boolean playSounds(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getBoolean(userName + AppConstants.PREF_SOUNDS, true);
	}

	/* Game modes */
	public static boolean isFinishedEchessGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS;
	}

	public static boolean isComputerVsComputerGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
	}

	public static boolean isComputerVsHumanGameMode(BoardFace boardFace) {
		final int mode = boardFace.getMode();
		return mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE
				|| mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}

	public static boolean isHumanVsHumanGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_HUMAN_VS_HUMAN;
	}

	public static boolean isComputerVsHumanWhiteGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
	}

	public static boolean isComputerVsHumanBlackGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}

	public static int getUserPremiumStatus(Context context) {
		return getPreferences(context).getInt(AppConstants.USER_PREMIUM_STATUS, StaticData.NOT_INITIALIZED_USER);
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

	public static String getCompSavedGame(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getString(userName + AppConstants.SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY);
	}


	public static boolean haveSavedCompGame(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return !preferences.getString(userName + AppConstants.SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY)
				.equals(StaticData.SYMBOL_EMPTY);
	}

	public static void clearSavedCompGame(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		preferences.edit()
				.putString(userName + AppConstants.SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY)
				.commit();
	}

	public static int getCompThinkTime(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getInt(userName + AppConstants.PREF_COMPUTER_DELAY, AppConstants.COMPUTER_THINK_TIME); //milliseconds
	}

	public static boolean isNotificationsEnabled(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getBoolean(userName + AppConstants.PREF_NOTIFICATION, true);
	}

	public static boolean isRegisterOnChessGCM(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String savedToken = preferences.getString(AppConstants.GCM_SAVED_TOKEN, StaticData.SYMBOL_EMPTY);
		String userToken = preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY);

		return savedToken.equals(userToken) && preferences.getBoolean(AppConstants.GCM_REGISTERED_ON_SERVER, false);
	}

	public static void registerOnChessGCM(Context context, String token) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putBoolean(AppConstants.GCM_REGISTERED_ON_SERVER, true);
		editor.putString(AppConstants.GCM_SAVED_TOKEN, token);
		editor.commit();
	}

	public static void unRegisterOnChessGCM(Context context) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putBoolean(AppConstants.GCM_REGISTERED_ON_SERVER, false);
		editor.putString(AppConstants.GCM_SAVED_TOKEN, StaticData.SYMBOL_EMPTY);
		editor.commit();
	}

	public static boolean isLiveChess(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getBoolean(AppConstants.IS_LIVE_CHESS_ON, true);
	}

	public static void setLiveChessMode(Context context, boolean enabled) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putBoolean(AppConstants.IS_LIVE_CHESS_ON, enabled);
		editor.commit();
	}

	public static void clearPreferences(Context context) {
		getPreferences(context).edit()
				.clear()
				.commit();
	}

	public static int getDefaultDailyMode(Context context) {
		return getPreferences(context).getInt(AppConstants.PREF_DEFAULT_DAILY_MODE, 0);
	}

	public static void setDefaultDailyMode(Context context, int mode) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putInt(AppConstants.PREF_DEFAULT_DAILY_MODE, mode);
		editor.commit();
	}

	public static int getDefaultLiveMode(Context context) {
		return getPreferences(context).getInt(AppConstants.PREF_DEFAULT_LIVE_MODE, 0);
	}

	public static void setDefaultLiveMode(Context context, int mode) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putInt(AppConstants.PREF_DEFAULT_LIVE_MODE, mode);
		editor.commit();
	}

	public static void setUserSkill(Context context, int skillCode) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putInt(AppConstants.PREF_USER_SKILL_LEVEL, skillCode);
		editor.putBoolean(AppConstants.PREF_USER_SKILL_LEVEL_SET, true);
		editor.commit();
	}

	public static boolean isUserSkillSet(Context context) {
		return getPreferences(context).getBoolean(AppConstants.PREF_USER_SKILL_LEVEL_SET, false);
	}

	public static int getUserSkill(Context context) {
		return getPreferences(context).getInt(AppConstants.PREF_USER_SKILL_LEVEL, 0);
	}

	public static void setUserCountry(Context context, String countryName) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putString(AppConstants.PREF_USER_COUNTRY, countryName);
		editor.commit();
	}

	public static String getUserCountry(Context context) {
		return getPreferences(context).getString(AppConstants.PREF_USER_COUNTRY, null);
	}

	public static String getUserAvatar(Context context) {
		return getPreferences(context).getString(AppConstants.PREF_USER_AVATAR_URL, StaticData.SYMBOL_EMPTY);
	}

	public static int getUserTacticsRating(Context context) {
		return getPreferences(context).getInt(AppConstants.PREF_USER_TACTICS_RATING, 0);
	}

	public static void setPossibleMoveHighlight(Context context, boolean checked) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		preferences.edit().putBoolean(userName + AppConstants.PREF_POSSIBLE_MOVE_HIGHLIGHTS, checked).commit();
	}

	public static boolean isPossibleMoveHighlightOn(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getBoolean(userName + AppConstants.PREF_POSSIBLE_MOVE_HIGHLIGHTS, true);
	}
}
