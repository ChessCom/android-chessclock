package com.chess.statics;

import android.content.Context;
import android.content.SharedPreferences;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.google.gson.Gson;

import static com.chess.statics.AppConstants.*;

/**
 * AppData class
 *
 * @author alien_roger
 * @created at: 15.04.12 21:36
 */
public class AppData {

	public static final int UNDEFINED = -1;
	public static final int TRUE = 1;
	public static final int FALSE = 0;

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
		return preferences.getString(USER_TOKEN, Symbol.EMPTY);
	}

	public long getUserId() {
		return getLongValue(PREF_USER_ID, UNDEFINED);
	}

	public void setAfterMoveAction(int action) {
		setIntValue(PREF_ACTION_AFTER_MY_MOVE, action);
	}

	public int getAfterMoveAction() {
		return getIntValue(PREF_ACTION_AFTER_MY_MOVE, StaticData.AFTER_MOVE_GO_TO_NEXT_GAME);
	}

	public void setUserFirstName(String value) {
		setStringValue(FIRST_NAME, value);
	}

	public String getUserFirstName() {
		return getStringValue(FIRST_NAME, Symbol.EMPTY);
	}

	public void setUserLastName(String value) {
		setStringValue(LAST_NAME, value);
	}

	public String getUserLastName() {
		return getStringValue(LAST_NAME, Symbol.EMPTY);
	}

	public void setUserLocation(String value) {
		setStringValue(LOCATION, value);
	}

	public String getUserLocation() {
		return getStringValue(LOCATION, Symbol.EMPTY);
	}

	public String getUsername() {
		return preferences.getString(USERNAME, Symbol.EMPTY);
	}

	public void setUsername(String username) {
		editor.putString(USERNAME, username).commit();
	}

	public void setPassword(String value) {
		editor.putString(PASSWORD, value).commit();
	}

	public String getPassword() {
		return preferences.getString(PASSWORD, Symbol.EMPTY);
	}

	public void setFacebookToken(String value) {
		setStringValue(FACEBOOK_TOKEN, value);
	}

	public String getFacebookToken() {
		return getStringValue(FACEBOOK_TOKEN, Symbol.EMPTY);
	}

	public void setBackgroundSetId(int value) {
		setIntValue(PREF_BACKGROUND_SET, value);
	}

	public int getLanguageCode() {
		return getIntValue(PREF_LANGUAGE, 0);
	}

	public String getLiveSessionId() {
		return preferences.getString(LIVE_SESSION_ID, Symbol.EMPTY);
	}



	public void setShowSubmitButtonsDaily(boolean show) {
		setBooleanValue(PREF_SHOW_SUBMIT_MOVE_DAILY, show);
	}

	public boolean getShowSubmitButtonsDaily() {
		return getBooleanValue(PREF_SHOW_SUBMIT_MOVE_DAILY, true);
	}

	public void setShowSubmitButtonsLive(boolean show) {
		setBooleanValue(PREF_SHOW_SUBMIT_MOVE_LIVE, show);
	}

	public boolean getShowSubmitButtonsLive() {
		return getBooleanValue(PREF_SHOW_SUBMIT_MOVE_LIVE, false);
	}

	public void setOnVacation(boolean show) {
		setBooleanValue(PREF_ON_VACATION, show);
	}

	public boolean isOnVacation() {
		return getBooleanValue(PREF_ON_VACATION, false);
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
		return preferences.getString(USER_PREMIUM_SKU, Symbol.EMPTY);
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

//	public Intent getMembershipAndroidIntent() {
//		return getMembershipIntent("?c=androidads");
//	}
//
//	public Intent getMembershipVideoIntent() {
//		return getMembershipIntent("?c=androidvideos");
//	}
//
//	public Intent getMembershipIntent(String param) {
//		String memberShipUrl = RestHelper.getMembershipLink(getUserToken(), param);
//		return new Intent(Intent.ACTION_VIEW, Uri.parse(memberShipUrl));
//	}

	public String getCompSavedGame() {
		return getStringValue(SAVED_COMPUTER_GAME, Symbol.EMPTY);
	}

	public boolean haveSavedCompGame() {
		return !getCompSavedGame().equals(Symbol.EMPTY);
	}

	public void clearSavedCompGame() {
		setStringValue(AppConstants.SAVED_COMPUTER_GAME, Symbol.EMPTY);
	}

	public void setSavedCompGame(String compGame) {
		setStringValue(AppConstants.SAVED_COMPUTER_GAME, compGame);
	}

	public int getCompThinkTime() {
		return getIntValue(PREF_COMPUTER_DELAY, COMPUTER_THINK_TIME);
	}

	public void setNotificationsEnabled(boolean value) {
		setBooleanValue(PREF_DAILY_NOTIFICATIONS, value);
	}

	public boolean isNotificationsEnabled() {
		return getBooleanValue(PREF_DAILY_NOTIFICATIONS);
	}

	public boolean isRegisterOnChessGCM() {
		String savedToken = preferences.getString(GCM_SAVED_TOKEN, Symbol.EMPTY);
		String userToken = preferences.getString(USER_TOKEN, Symbol.EMPTY);

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
		editor.putString(GCM_SAVED_TOKEN, Symbol.EMPTY);
		editor.commit();
	}

	public boolean isLiveChess() {
		return getBooleanValue(IS_LIVE_CHESS_ON, false);
	}

	public void setLiveChessMode(boolean enabled) {
		setBooleanValue(IS_LIVE_CHESS_ON, enabled);
	}

	public void incrementLiveConnectAttempts() {
		int attempts = getLiveConnectAttempts();
		setIntValue(AppConstants.LIVE_CONNECT_ATTEMPTS, ++attempts);
	}

	public int getLiveConnectAttempts() {
		return getIntValue(LIVE_CONNECT_ATTEMPTS, 0);
	}

	public void resetLiveConnectAttempts() {
		setIntValue(LIVE_CONNECT_ATTEMPTS, 0);
	}

	public boolean isLastUsedDailyMode() {
		return getBooleanValue(PREF_LAST_USED_DAILY_MODE, true);
	}

	public void setLastUsedDailyMode(boolean value) {
		setBooleanValue(PREF_LAST_USED_DAILY_MODE, value);
	}

	public int getDefaultDailyMode() {
		return getIntValue(PREF_DEFAULT_DAILY_MODE, 5); // 10 days by default
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
		String username = preferences.getString(USERNAME, Symbol.EMPTY);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(username + PREF_USER_SKILL_LEVEL, skillCode);
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
		return getStringValue(PREF_USER_AVATAR_URL, Symbol.EMPTY);
	}

	public void setUserTacticsRating(int value) {
		setIntValue(PREF_USER_TACTICS_RATING, value);
	}

	public int getUserTacticsRating() {
		return getIntValue(PREF_USER_TACTICS_RATING, 0);
	}

	public void setUserLessonsRating(int value) {
		setIntValue(PREF_USER_LESSONS_RATING, value);
	}

	public int getUserLessonsRating() {
		return getIntValue(PREF_USER_LESSONS_RATING, 0);
	}

	public void setUserLessonsCompleteCnt(int value) {
		setIntValue(PREF_USER_LESSONS_COMPLETE, value);
	}

	public int getUserLessonsCompleteCnt() {
		return getIntValue(PREF_USER_LESSONS_COMPLETE, 0);
	}

	public void setUserCourseCompleteCnt(int value) {
		setIntValue(PREF_USER_COURSE_COMPLETE, value);
	}

	public int getUserCourseCompleteCnt() {
		return getIntValue(PREF_USER_COURSE_COMPLETE, 0);
	}

	public int getUserDailyRating() {
		return getIntValue(PREF_USER_TACTICS_RATING, 0);
	}

	/**
	 *
	 * @param play int value used to differentiate if it was set manually by user or depends on general sound system profile
	 */
	public void setPlaySounds(Context context, int play) {
		setIntValue(PREF_SOUNDS, play);
		SoundPlayer.getInstance(context);// update internal flag
	}

	public int isPlaySounds() {
		return getIntValue(PREF_SOUNDS, UNDEFINED);
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

	public void resetThemeToDefault() {
		setThemeBackPathPort(Symbol.EMPTY);
		setThemeBackPathLand(Symbol.EMPTY);
		setThemeBackgroundName(Symbol.EMPTY);

		setThemeBoardPath(Symbol.EMPTY);
		setThemeBoardId(UNDEFINED);
		setThemeBoardCoordinateDark(UNDEFINED);
		setThemeBoardCoordinateLight(UNDEFINED);
		setThemeBoardHighlight(UNDEFINED);
		setUseThemeBoard(false);

		setUseThemePieces(false);
		setThemePiecesPath(Symbol.EMPTY);
		setThemePiecesId(UNDEFINED);
		setThemePieces3d(false);

		setThemeSoundsPath(Symbol.EMPTY);
		setThemeSoundsId(UNDEFINED);

	}

	public void setThemeBackPathPort(String path) {
		setStringValue(PREF_THEME_BACKGROUND_PATH_PORT, path);
	}

	public String getThemeBackPathPort() {
		return getStringValue(PREF_THEME_BACKGROUND_PATH_PORT, Symbol.EMPTY);
	}

	public void setThemeBackPathLand(String path) {
		setStringValue(PREF_THEME_BACKGROUND_PATH_LAND, path);
	}

	public String getThemeBackPathLand() {
		return getStringValue(PREF_THEME_BACKGROUND_PATH_LAND, Symbol.EMPTY);
	}

	public void setThemeFontColor(String path) {
		setStringValue(PREF_THEME_FONT_COLOR, path);
	}

	public String getThemeFontColor() {
		return getStringValue(PREF_THEME_FONT_COLOR, "FFFFFFBF");
	}

	public void setThemeBoardPath(String path) {
		setStringValue(PREF_THEME_BOARD_PATH, path);
	}

	public String getThemeBoardPath() {
		return getStringValue(PREF_THEME_BOARD_PATH, "");
	}

	public void setThemeBoardCoordinateLight(int value) {
		setIntValue(PREF_THEME_BOARD_COORDINATE_LIGHT, value);
	}

	public int getThemeBoardCoordinateLight() {
		return getIntValue(PREF_THEME_BOARD_COORDINATE_LIGHT, UNDEFINED);
	}

	public void setThemeBoardCoordinateDark(int value) {
		setIntValue(PREF_THEME_BOARD_COORDINATE_DARK, value);
	}

	public int getThemeBoardCoordinateDark() {
		return getIntValue(PREF_THEME_BOARD_COORDINATE_DARK, UNDEFINED);
	}

	public void setThemeBoardHighlight(int value) {
		setIntValue(PREF_THEME_BOARD_HIGHLIGHT, value);
	}

	public int getThemeBoardHighlight() {
		return getIntValue(PREF_THEME_BOARD_HIGHLIGHT, UNDEFINED);
	}

	public int getThemeBackId() {
		return R.drawable.img_theme_green_felt; // won't be changed
	}

	public void setThemeName(String themeName) {
		setStringValue(PREF_THEME_NAME, themeName);
	}

	public String getThemeName() {
		return getStringValue(PREF_THEME_NAME, DEFAULT_THEME_NAME);
	}

	public void setThemeBackgroundName(String themeName) {
		setStringValue(PREF_THEME_BACKGROUND_NAME, themeName);
	}

	public String getThemeBackgroundName() {
		return getStringValue(PREF_THEME_BACKGROUND_NAME, DEFAULT_THEME_NAME);
	}

	public void setThemeBackgroundPreviewUrl(String value) {
		setStringValue(PREF_THEME_BACKGROUND_PREVIEW, value);
	}

	public String getThemeBackgroundPreviewUrl() {
		return getStringValue(PREF_THEME_BACKGROUND_PREVIEW, Symbol.EMPTY);
	}

	public void setSoundSetPosition(int value) {
		setIntValue(PREF_SOUNDS_SET, value);
	}

	public int getSoundSetPosition() {
		return getIntValue(PREF_SOUNDS_SET, 0);
	}

	public void setThemeSoundsPath(String themeName) {
		setStringValue(PREF_THEME_SOUNDS_PATH, themeName);
	}

	public String getThemeSoundsPath() {
		return getStringValue(PREF_THEME_SOUNDS_PATH, Symbol.EMPTY);
	}

	public void setThemeSoundsId(int value) {
		setIntValue(PREF_THEME_SOUNDS_ID, value);
	}

	public int getThemeSoundsId() {
		return getIntValue(PREF_THEME_SOUNDS_ID, UNDEFINED);
	}

	public void setThemePiecesPath(String themeName) {
		setStringValue(PREF_THEME_PIECES_PATH, themeName);
	}

	public String getThemePiecesPath() {
		return getStringValue(PREF_THEME_PIECES_PATH, Symbol.EMPTY);
	}

	public void setThemePiecesPreviewUrl(String value) {
		setStringValue(PREF_THEME_PIECES_PREVIEW, value);
	}

	public String getThemePiecesPreviewUrl() {
		return getStringValue(PREF_THEME_PIECES_PREVIEW, Symbol.EMPTY);
	}

	public void setThemeBoardPreviewUrl(String value) {
		setStringValue(PREF_THEME_BOARD_PREVIEW, value);
	}

	public String getThemeBoardPreviewUrl() {
		return getStringValue(PREF_THEME_BOARD_PREVIEW, Symbol.EMPTY);
	}

	public void setThemePiecesName(String piecesName) {
		setStringValue(PREF_THEME_PIECES_NAME, piecesName);
	}

	public String getThemePiecesName() {
		return getStringValue(PREF_THEME_PIECES_NAME, Symbol.EMPTY);
	}

	public void setThemeBoardName(String piecesName) {
		setStringValue(PREF_THEME_BOARD_NAME, piecesName);
	}

	public String getThemeBoardName() {
		return getStringValue(PREF_THEME_BOARD_NAME, Symbol.EMPTY);
	}

	public void setThemeBoardId(int value) {
		setIntValue(PREF_THEME_BOARD_ID, value);
	}

	public int getThemeBoardId() {
		return getIntValue(PREF_THEME_BOARD_ID, UNDEFINED);
	}

	public void setThemePiecesId(int value) {
		setIntValue(PREF_THEME_PIECES_ID, value);
	}

	public int getThemePiecesId() {
		return getIntValue(PREF_THEME_PIECES_ID, UNDEFINED);
	}

	public void setUseThemeBoard(boolean value) {
		setBooleanValue(PREF_THEME_BOARD_USE_THEME, value);
	}

	public boolean isUseThemeBoard() {
		return getBooleanValue(PREF_THEME_BOARD_USE_THEME, false);
	}

	public void setUseThemePieces(boolean value) {
		setBooleanValue(PREF_THEME_PIECES_USE_THEME, value);
	}

	public boolean isUseThemePieces() {
		return getBooleanValue(PREF_THEME_PIECES_USE_THEME, false);
	}

	public void setThemePieces3d(boolean value) {
		setBooleanValue(PREF_THEME_IS_PIECES_3D_PATH, value);
	}

	public boolean isThemePieces3d() {
		return getBooleanValue(PREF_THEME_IS_PIECES_3D_PATH, false);
	}

	public void setThemeBackgroundsLoaded(boolean value) {
		setBooleanValue(PREF_THEME_BACKGROUNDS_LOADED, value);
	}

	public boolean isThemeBackgroundsLoaded() {
		return getBooleanValue(PREF_THEME_BACKGROUNDS_LOADED, false);
	}

	public void setThemeBoardsLoaded(boolean value) {
		setBooleanValue(PREF_THEME_BOARDS_LOADED, value);
	}

	public boolean isThemeBoardsLoaded() {
		return getBooleanValue(PREF_THEME_BOARDS_LOADED, false);
	}

	public void setThemePiecesLoaded(boolean value) {
		setBooleanValue(PREF_THEME_PIECES_LOADED, value);
	}

	public boolean isThemePiecesLoaded() {
		return getBooleanValue(PREF_THEME_PIECES_LOADED, false);
	}

	public void setThemeSoundsLoaded(boolean value) {
		setBooleanValue(PREF_THEME_SOUNDS_LOADED, value);
	}

	public boolean isThemeSoundsLoaded() {
		return getBooleanValue(PREF_THEME_SOUNDS_LOADED, false);
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
		return getBooleanValue(PREF_USER_CHOOSE_LESSONS_LIBRARY, false);
	}

	public void setCompLevel(int value) {
		setIntValue(PREF_COMPUTER_LEVEL, value);
	}

	public int getCompLevel() {
		return getIntValue(PREF_COMPUTER_LEVEL, DEFAULT_COMP_LEVEL);
	}

	/**
	 * Set Vs Computer default mode for auth user.
	 * @param value mode ( PLAYER_WHITE & COMB_BLACK, PLAYER_BLACK & COMP_WHITE, COMP & COMP, PLAY & PLAYER
	 */
	public void setCompGameMode(int value) {
		setIntValue(PREF_COMPUTER_MODE, value);
	}

	/**
	 * Get default Vs Computer mode for CompOptionsFragment
	 * @return mode
	 */
	public int getCompGameMode() {
		return getIntValue(PREF_COMPUTER_MODE, AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE);
	}

	/**
	 * When it is WHITE human to play, he should be on bottom. once he moves, flip so BLACK is on the bottom.
	 * @param autoFlip flag
	 */
	public void setAutoFlipFor2Players(boolean autoFlip) {
		setBooleanValue(PREF_AUTO_FLIP, autoFlip);
	}

	/**
	 * When it is WHITE human to play, he should be on bottom. once he moves, flip so BLACK is on the bottom.
	 */
	public boolean isAutoFlipFor2Players() {
		return getBooleanValue(PREF_AUTO_FLIP, true);
	}

	public boolean isLessonLimitWasReached() {
		return getBooleanValue(LESSONS_LIMIT_HAS_REACHED, false);
	}

	public void setLessonLimitWasReached(boolean value) {
		setBooleanValue(LESSONS_LIMIT_HAS_REACHED, value);
	}

	public void setLiveGameConfig(LiveGameConfig liveGameConfig) {
		Gson gson = new Gson();
		setStringValue(LIVE_GAME_CONFIG, gson.toJson(liveGameConfig));
		editor.commit();
	}

	public LiveGameConfig getLiveGameConfig() {
		Gson gson = new Gson();

		LiveGameConfig liveGameConfig = gson.fromJson(getStringValue(LIVE_GAME_CONFIG, ""), LiveGameConfig .class);

		if (liveGameConfig == null) {
			liveGameConfig = new LiveGameConfig.Builder().build();
		}

		return liveGameConfig;
	}

	public void setTestUsername(String value) {
		preferences.edit().putString("TEST_USERNAME", value).commit();
	}

	public String getTestUsername() {
		return preferences.getString("TEST_USERNAME", "");
	}

	public void setTestPassword(String value) {
		preferences.edit().putString("TEST_PASS", value).commit();
	}

	public String getTestPassword() {
		return preferences.getString("TEST_PASS", "");
	}

	public void setProdUsername(String value) {
		preferences.edit().putString("PROD_USERNAME", value).commit();
	}

	public String getProdUsername() {
		return preferences.getString("PROD_USERNAME", "");
	}

	public void setProdPassword(String value) {
		preferences.edit().putString("PROD_PASS", value).commit();
	}

	public String getProdPassword() {
		return preferences.getString("PROD_PASS", "");
	}

	public void setApiRoute(String value) {
		preferences.edit().putString("API", value).commit();
	}

	public String getApiRoute() {
		return preferences.getString("API", RestHelper.HOST_PRODUCTION);
	}

	public void setDemoTacticsLoaded(boolean value) {
		setBooleanValue(PREF_DEMO_TACTICS_LOADED, value);
	}

	public boolean isDemoTacticsLoaded() {
		return getBooleanValue(PREF_DEMO_TACTICS_LOADED, false);
	}

	public void setPullHeaderTopInset(int value) {
		editor.putInt(PULL_TO_REFRESH_HEADER_TOP_INSET, value);
	}

	public int getPullHeaderTopInset() {
		return preferences.getInt(PULL_TO_REFRESH_HEADER_TOP_INSET, UNDEFINED);
	}

	public void setUserCreateDate(long value) {
		setLongValue(USER_CREATE_DATE, value);
	}

	public long getUserCreateDate() {
		return getLongValue(USER_CREATE_DATE, UNDEFINED);
	}

	public void setUserInfoSaved(boolean value) {
		setBooleanValue(USER_INFO_SAVED, value);
	}

	public boolean isUserInfoSaved() {
		return getBooleanValue(USER_INFO_SAVED, false);
	}


	/*--------------------------- Common Shared logic ------------------------*/

	private void setBooleanValue(String field, boolean checked) {
		String username = getUsername();
		preferences.edit().putBoolean(username + field, checked).commit();
	}

	private boolean getBooleanValue(String field) {
		return getBooleanValue(field, true);
	}

	private boolean getBooleanValue(String field, boolean defValue) {
		String username = getUsername();
		return preferences.getBoolean(username + field, defValue);
	}

	private void setStringValue(String field, String value) {
		String username = getUsername();
		editor.putString(username + field, value).commit();
	}

	private void setLongValue(String field, long value) {
		String username = getUsername();
		editor.putLong(username + field, value).commit();
	}

	private String getStringValue(String field, String defValue) {
		String username = getUsername();
		return preferences.getString(username + field, defValue);
	}

	private int getIntValue(String field, int defValue) {
		String username = getUsername();
		return preferences.getInt(username + field, defValue);
	}

	private long getLongValue(String field, int defValue) {
		String username = getUsername();
		return preferences.getLong(username + field, defValue);
	}

	private void setIntValue(String field, int value) {
		String username = getUsername();
		editor.putInt(username + field, value).commit();
	}

}
