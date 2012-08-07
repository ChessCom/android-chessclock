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

	public static SharedPreferences getPreferences(Context context){
		return context.getSharedPreferences(StaticData.SHARED_DATA_NAME, Context.MODE_PRIVATE);
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

	public static int getCompStrength(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getInt(userName + AppConstants.PREF_COMPUTER_STRENGTH, 0);
	}

	public static int getChessBoardId(Context context) {
		SharedPreferences preferences = getPreferences(context);
		String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
		return preferences.getInt(userName + AppConstants.PREF_BOARD_TYPE, StaticData.B_WOOD_DARK_ID);
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

	public static int getUserPremiumStatus(Context context){
		return Integer.parseInt(getPreferences(context).getString(AppConstants.USER_PREMIUM_STATUS, "0"));
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

	public static boolean haveSavedCompGame(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return !preferences.getString(AppConstants.SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY)
				.equals(StaticData.SYMBOL_EMPTY);
	}

	public static boolean haveSavedTacticGame(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return !preferences.getString(AppConstants.SAVED_TACTICS_ITEM, StaticData.SYMBOL_EMPTY)
				.equals(StaticData.SYMBOL_EMPTY);
	}
}
