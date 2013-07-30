package com.chess.backend.statics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.ui.interfaces.boards.BoardFace;

import static com.chess.backend.statics.AppConstants.*;

/**
 * AppData class
 *
 * @author alien_roger
 * @created at: 15.04.12 21:36
 */
public class AppData {

	private final SharedPreferences.Editor editor;
	private SharedPreferences preferences;

	public AppData(Context context) {
		this.preferences = context.getSharedPreferences(StaticData.SHARED_DATA_NAME, Context.MODE_PRIVATE);
		editor = preferences.edit();
	}

	public SharedPreferences getPreferences() {
		return preferences;
	}

	public SharedPreferences.Editor getEditor() {
		return editor;
	}

	public void clearPreferences() {
		preferences.edit()
				.clear()
				.commit();
	}

	public void setUserToken(String token) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(USER_TOKEN, token);
		editor.commit();
	}

	public String getUserToken() {
		return preferences.getString(USER_TOKEN, StaticData.SYMBOL_EMPTY);
	}

	public int getAfterMoveAction() {
		return getIntValue(PREF_ACTION_AFTER_MY_MOVE, StaticData.AFTER_MOVE_GO_TO_NEXT_GAME);
	}

	public void setUserFirstName(String value) {
		setStringValue(FIRST_NAME, value);
	}

	public String getUserFirstName() {
		return getStringValue(FIRST_NAME, StaticData.SYMBOL_EMPTY);
	}

	public void setUserLastName(String value) {
		setStringValue(LAST_NAME, value);
	}

	public String getUserLastName() {
		return getStringValue(LAST_NAME, StaticData.SYMBOL_EMPTY);
	}

	public void setUserLocation(String value) {
		setStringValue(LOCATION, value);
	}

	public String getUserLocation() {
		return getStringValue(LOCATION, StaticData.SYMBOL_EMPTY);
	}

	public String getUsername() {
		return preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
	}

	public void setUserName(String userName) {
		editor.putString(USERNAME, userName).commit();
	}

	public String getPassword() {
		return preferences.getString(PASSWORD, StaticData.SYMBOL_EMPTY);
	}

	public int getChessBoardId() {
		return getIntValue(PREF_BOARD_STYLE, StaticData.B_WOOD_DARK_ID);
	}

	public void setChessBoardId(int value) {
		setIntValue(PREF_BOARD_STYLE, value);
	}

	public int getPiecesId() {
		return getIntValue(PREF_PIECES_SET, 0);
	}

	public void setPiecesId(int value) {
		setIntValue(PREF_PIECES_SET, value);
	}

	public void setBackgroundSetId(int value) {
		setIntValue(PREF_BACKGROUND_SET, value);
	}

	public int getLanguageCode() {
		return getIntValue(PREF_LANGUAGE, 0);
	}

	public String getLiveSessionId() {
		return preferences.getString(LIVE_SESSION_ID, StaticData.SYMBOL_EMPTY);
	}

	/* Game modes */
	public boolean isFinishedEchessGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_VIEW_FINISHED_ECHESS;
	}

	public boolean isComputerVsComputerGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_COMPUTER_VS_COMPUTER;
	}

	public boolean isComputerVsHumanGameMode(BoardFace boardFace) {
		final int mode = boardFace.getMode();
		return mode == GAME_MODE_COMPUTER_VS_HUMAN_WHITE
				|| mode == GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}

	public boolean isHumanVsHumanGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_HUMAN_VS_HUMAN;
	}

	public boolean isComputerVsHumanWhiteGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
	}

	public boolean isComputerVsHumanBlackGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}

	public void setUserPremiumStatus(int level) {
		setIntValue(USER_PREMIUM_STATUS, level);
	}

	public int getUserPremiumStatus() {
		return getIntValue(USER_PREMIUM_STATUS, StaticData.BASIC_USER);
	}

	public void setUserPremiumSku(String value) {
		setStringValue(USER_PREMIUM_SKU, value);
	}

	public String getUserPremiumSku() {
		return preferences.getString(USER_PREMIUM_SKU, StaticData.SYMBOL_EMPTY);
	}

	public String getUserPremiumStatusStr(Context context) {
		int status = getIntValue(USER_PREMIUM_STATUS, StaticData.BASIC_USER);
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

	public Intent getMembershipAndroidIntent() {
		return getMembershipIntent("?c=androidads");
	}

	public Intent getMembershipVideoIntent() {
		return getMembershipIntent("?c=androidvideos");
	}

	public Intent getMembershipIntent(String param) {
		String memberShipUrl = RestHelper.getMembershipLink(getUserToken(), param);
		return new Intent(Intent.ACTION_VIEW, Uri.parse(memberShipUrl));
	}

	public void setCompGameMode(int mode) {
		setIntValue(PREF_COMPUTER_MODE, mode);
	}

	public int getCompGameMode() {
		return getIntValue(PREF_COMPUTER_MODE, AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);
	}

	public String getCompSavedGame() {
		return getStringValue(SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY);
	}

	public boolean haveSavedCompGame() {
		return !getCompSavedGame().equals(StaticData.SYMBOL_EMPTY);
	}

	public void clearSavedCompGame() {
		String userName = preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
		preferences.edit()
				.putString(userName + SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY)
				.commit();
	}

	public int getCompThinkTime() {
		return getIntValue(PREF_COMPUTER_DELAY, COMPUTER_THINK_TIME);
	}

	public boolean isNotificationsEnabled() {
		return getBooleanValue(PREF_DAILY_NOTIFICATIONS);
	}

	public boolean isRegisterOnChessGCM() {
		String savedToken = preferences.getString(GCM_SAVED_TOKEN, StaticData.SYMBOL_EMPTY);
		String userToken = preferences.getString(USER_TOKEN, StaticData.SYMBOL_EMPTY);

		return savedToken.equals(userToken) && preferences.getBoolean(GCM_REGISTERED_ON_SERVER, false);
	}

	public void registerOnChessGCM(String token) {
		editor.putBoolean(GCM_REGISTERED_ON_SERVER, true);
		editor.putString(GCM_SAVED_TOKEN, token);
		editor.commit();
	}

	public void unRegisterOnChessGCM() {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(GCM_REGISTERED_ON_SERVER, false);
		editor.putString(GCM_SAVED_TOKEN, StaticData.SYMBOL_EMPTY);
		editor.commit();
	}

	public boolean isLiveChess() {
		return getBooleanValue(IS_LIVE_CHESS_ON, false);
	}

	public void setLiveChessMode(boolean enabled) {
		setBooleanValue(IS_LIVE_CHESS_ON, enabled);
	}

	public int getDefaultDailyMode() {
		return getIntValue(PREF_DEFAULT_DAILY_MODE, 0);
	}

	public void setDefaultDailyMode(int mode) {
		setIntValue(PREF_DEFAULT_DAILY_MODE, mode);
	}

	public int getDefaultLiveMode() {
		return getIntValue(PREF_DEFAULT_LIVE_MODE, 0);
	}

	public void setDefaultLiveMode(int mode) {
		setIntValue(PREF_DEFAULT_LIVE_MODE, mode);
	}

	public void setUserSkill(int skillCode) {
		String userName = preferences.getString(USERNAME, StaticData.SYMBOL_EMPTY);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(userName + PREF_USER_SKILL_LEVEL, skillCode);
		editor.putBoolean(PREF_USER_SKILL_LEVEL_SET, true);
		editor.commit();
	}

	public boolean isUserSkillSet() {
		return preferences.getBoolean(PREF_USER_SKILL_LEVEL_SET, false);
	}

	public int getUserSkill() {
		return getIntValue(PREF_USER_SKILL_LEVEL, 0);
	}

	public void setUserCountry(String countryName) {
		setStringValue(PREF_USER_COUNTRY, countryName);
	}

	public String getUserCountry() {
		return getStringValue(PREF_USER_COUNTRY, null);
	}

	public void setUserCountryId(int id) {
		setIntValue(PREF_USER_COUNTRY_ID, id);
	}

	public int getUserCountryId() {
		return getIntValue(PREF_USER_COUNTRY_ID, 0);
	}

	public void setUserAvatar(String url) {
		setStringValue(PREF_USER_AVATAR_URL, url);
	}

	public String getUserAvatar() {
		return getStringValue(PREF_USER_AVATAR_URL, StaticData.SYMBOL_EMPTY);
	}

	public void setUserTacticsRating(int value) {
		setIntValue(PREF_USER_TACTICS_RATING, value);
	}

	public int getUserTacticsRating() {
		return getIntValue(PREF_USER_TACTICS_RATING, 0);
	}

	public int getUserDailyRating() {
		return getIntValue(PREF_USER_TACTICS_RATING, 0);
	}

	public void setPlaySounds(Context context, boolean checked) {
		setBooleanValue(PREF_SOUNDS, checked);
		SoundPlayer.getInstance(context);// update internal flag
	}

	public boolean isPlaySounds() {
		return getBooleanValue(PREF_SOUNDS);
	}

	public void setShowLegalMoves(boolean checked) {
		setBooleanValue(PREF_SHOW_LEGAL_MOVES, checked);
	}

	public boolean isShowLegalMoves() {
		return getBooleanValue(PREF_SHOW_LEGAL_MOVES);
	}

	public void setShowCoordinates(boolean checked) {
		setBooleanValue(PREF_BOARD_COORDINATES, checked);
	}

	public boolean isShowCoordinates() {
		return getBooleanValue(PREF_BOARD_COORDINATES);
	}

	public boolean isHighlightLastMove() {
		return getBooleanValue(PREF_BOARD_HIGHLIGHT_LAST_MOVE);
	}

	public void setHighlightLastMove(boolean checked) {
		setBooleanValue(PREF_BOARD_HIGHLIGHT_LAST_MOVE, checked);
	}

	public void setAnswerShowBottom(boolean checked) {
		setBooleanValue(PREF_BOARD_SHOW_ANSWER_BOTTOM, checked);
	}

	public void setThemeBackPath(String path) {
		setStringValue(PREF_THEME_BACK_PATH, path);
	}

	public String getThemeBackPath() {
		return getStringValue(PREF_THEME_BACK_PATH, "");
	}

	public void setThemeBoardPath(String path) {
		setStringValue(PREF_THEME_BOARD_PATH, path);
	}

	public String getThemeBoardPath() {
		return getStringValue(PREF_THEME_BOARD_PATH, "");
	}

	public void setThemeBackId(int themeId) {
		setIntValue(PREF_THEME_BACK_ID, themeId);
	}

	public int getThemeBackId() {
		return getIntValue(PREF_THEME_BACK_ID, R.drawable.img_theme_green_felt);
	}

	public void setThemeName(String themeName) {
		setStringValue(PREF_THEME_NAME, themeName);
	}

	public String getThemeName() {
		return getStringValue(PREF_THEME_NAME, DEFAULT_THEME_NAME);
	}

	public void setUserChooseVideoLibrary(boolean value) {
		setBooleanValue(PREF_USER_CHOOSE_VIDEO_LIBRARY, value);
	}

	public boolean isUserChooseVideoLibrary() {
		return getBooleanValue(PREF_USER_CHOOSE_VIDEO_LIBRARY, false);
	}

	public void setUserChooseLessonsLibrary(boolean value) {
		setBooleanValue(PREF_USER_CHOOSE_LESSONS_LIBRARY, value);
	}

	public boolean isUserChooseLessonsLibrary() {
		return getBooleanValue(PREF_USER_CHOOSE_LESSONS_LIBRARY, true);
	}

	/*--------------------------- Common Shared logic ------------------------*/

	private void setBooleanValue(String field, boolean checked) {
		String userName = getUsername();
		preferences.edit().putBoolean(userName + field, checked).commit();
	}

	private boolean getBooleanValue(String field) {
		return getBooleanValue(field, true);
	}

	private boolean getBooleanValue(String field, boolean defValue) {
		String userName = getUsername();
		return preferences.getBoolean(userName + field, defValue);
	}

	private void setStringValue(String field, String value) {
		String userName = getUsername();
		editor.putString(userName + field, value).commit();
	}

	private String getStringValue(String field, String defValue) {
		String userName = getUsername();
		return preferences.getString(userName + field, defValue);
	}

	private int getIntValue(String field, int defValue) {
		String userName = getUsername();
		return preferences.getInt(userName + field, defValue);
	}

	private void setIntValue(String field, int value) {
		String userName = getUsername();
		editor.putInt(userName + field, value).commit();
	}

	public void setCompStrength(Context context, int value) {
		setIntValue(PREF_COMPUTER_STRENGTH, 2);
	}

	public int getCompStrength(Context context) {
		return getIntValue(PREF_COMPUTER_STRENGTH, 2);
	}
}
