package com.chess.utilities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.entity.api.YourTurnItem;
import com.chess.backend.image_load.bitmapfun.ImageCache;
import com.chess.model.BaseGameItem;
import com.chess.model.DataHolder;
import com.chess.statics.AppData;
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.activities.MainFragmentFaceActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AppUtils class
 *
 * @author alien_roger
 * @created at: 01.02.12 7:50
 */
public class AppUtils {

	public static final String DEFAULT_COUNTRY = "United States";
	public static final String UNZIPPED = "/unzipped/";

	public static final float MDPI = 1.0f;
	public static final float HDPI = 1.5f;
	public static final float XHDPI = 2.0f;
	private static final String VERSION_CODE = "3_2";
	private static boolean ENABLE_LOG = true;

	public static final long SECONDS_IN_DAY = 86400;

	public static final boolean HONEYCOMB_PLUS_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	public static final boolean ICS_PLUS_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	public static final boolean JELLYBEAN_PLUS_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	public static final boolean JELLYBEAN_MR1_PLUS_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public static int sizeOfBitmap(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return data.getRowBytes() * data.getHeight();
		} else {
			return data.getByteCount();
		}
	}

	public static String getApplicationCacheDirPath(String packageName) {
		// path should match the specified string
		// /Android/data/<package_name>/files/
		return "Android/data/" + packageName + "/cache/";
	}

	/**
	 * @param context te get packageName & {@code cacheDir} from internal storage
	 * @return file for {@code cacheDir} either SD card or internal storage. Or {@code null} if cacheDir doesn't exist
	 */
	public static File getCacheDir(Context context) throws IOException {
		File cacheDir;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String cacheDirPath = getApplicationCacheDirPath(context.getPackageName());
			cacheDir = new File(Environment.getExternalStorageDirectory(), cacheDirPath);
		} else {
			cacheDir = context.getCacheDir();
		}

		if (cacheDir != null && !cacheDir.exists()) {
			if (!cacheDir.mkdirs()) {
				throw new IOException("Can't use cacheDir");
//				throw new IllegalStateException("can't use cacheDir");
			}
		}
		return cacheDir;
	}

	public static File getSoundsThemeDir(Context context) {
		AppData appData = new AppData(context);
		String soundThemePath = appData.getThemeSoundsPath();
		return getLocalDirForPath(context, "sounds" + UNZIPPED + soundThemePath);
	}

	public static File getLocalDirForSounds(Context context) {
		return getLocalDirForPath(context, "sounds");
	}

	public static File getLocalDirForPieces(Context context, String themePath) {
		return getLocalDirForPath(context, "pieces/" + themePath);
	}

	public static File getLocalDirForBoards(Context context, String themePath) {
		return getLocalDirForPath(context, "boards/" + themePath);
	}

	public static File getLocalDirForPath(Context context, String path) {
		File cacheDir;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String cacheDirPath = "Android/data/" + context.getPackageName() + "/" + path + "/";
			cacheDir = new File(Environment.getExternalStorageDirectory(), cacheDirPath);
		} else {
			cacheDir = context.getCacheDir();
		}

		if (cacheDir != null && !cacheDir.exists()) {
			if (!cacheDir.mkdirs()) {
				throw new IllegalStateException("can't use cacheDir");
			}
		}
		return cacheDir;
	}


	public static File openFileByName(Context context, String filename) throws IOException {
		File cacheDir = getCacheDir(context);
		return new File(cacheDir, filename);
	}

	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (; ; ) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ignored) {
		}
	}

	public static long getLast30DaysTimeStamp() {
		long currentTimeMillis = System.currentTimeMillis();
		currentTimeMillis -= SECONDS_IN_DAY * 30 * 1000L;
		return currentTimeMillis / 1000L;
	}

	public static long getLast90DaysTimeStamp() {
		long currentTimeMillis = System.currentTimeMillis();
		currentTimeMillis -= SECONDS_IN_DAY * 90 * 1000L;
		return currentTimeMillis / 1000L;
	}

	public static long getLastYearTimeStamp() {
		long currentTimeMillis = System.currentTimeMillis();
		currentTimeMillis -= SECONDS_IN_DAY * 365 * 1000L;
		return currentTimeMillis / 1000L;
	}

	public static String getGooglePlayLinkForApp(Context context) {
		return "market://details?id=" + context.getPackageName();
	}

	public static class ListSelector implements Runnable {
		private int pos;
		private ListView listView;

		public ListSelector(int pos, ListView listView) {
			this.pos = pos;
			this.listView = listView;
		}

		@Override
		public void run() {
			listView.setSelection(pos);
		}
	}

	/**
	 * For QVGA screens we don't need a title bar and Action bar
	 *
	 * @param context
	 * @return
	 */
	public static boolean isNeedFullScreen(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		Configuration config = context.getResources().getConfiguration();
		return (displayMetrics.density < MDPI || displayMetrics.densityDpi == DisplayMetrics.DENSITY_LOW)
				&& config.orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	/**
	 * For mdpi normal screens we don't need a action bar only
	 *
	 * @param context
	 * @return
	 */
	public static boolean isSmallScreen(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		Configuration config = context.getResources().getConfiguration();
		return (displayMetrics.density <= MDPI || displayMetrics.densityDpi <= DisplayMetrics.DENSITY_MEDIUM)
				&& (displayMetrics.heightPixels <= 480 && config.orientation == Configuration.ORIENTATION_PORTRAIT
				|| displayMetrics.heightPixels <= 300 && config.orientation == Configuration.ORIENTATION_LANDSCAPE);
	}

	/**
	 * For mdpi normal screens we don't need a action bar only
	 *
	 * @param context
	 * @return
	 */
	public static boolean isHdpi800(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		Configuration config = context.getResources().getConfiguration();
		return (displayMetrics.density == HDPI || displayMetrics.densityDpi == DisplayMetrics.DENSITY_HIGH)
				&& (displayMetrics.heightPixels <= 800 && config.orientation == Configuration.ORIENTATION_PORTRAIT);
	}

	public static boolean isNexus4Kind(Context context) {
		return AppUtils.hasSoftKeys(context) && !is7InchTablet(context) && !is10InchTablet(context) && !isTallScreen(context);
	}

	/**
	 * Check if device has software keys and height of screen need to be adjusted
	 *
	 * @return {@code true} if device has software keys like Nexus 4 or Galaxy Nexus
	 */
	public static boolean hasSoftKeys(Context context) {
		if (JELLYBEAN_MR1_PLUS_API) {
			Display display = ((Activity) context).getWindowManager().getDefaultDisplay();

			DisplayMetrics realDisplayMetrics = new DisplayMetrics();
			display.getRealMetrics(realDisplayMetrics);

			int realHeight = realDisplayMetrics.heightPixels;
			int realWidth = realDisplayMetrics.widthPixels;

			DisplayMetrics displayMetrics = new DisplayMetrics();
			display.getMetrics(displayMetrics);

			int displayHeight = displayMetrics.heightPixels;
			int displayWidth = displayMetrics.widthPixels;

			return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
		} else {
			return false;
		}
	}

	public static boolean useLtr(Context context) {
		boolean useLtr = true;
		if (AppUtils.JELLYBEAN_MR1_PLUS_API) {
			int layoutDirection = context.getResources().getConfiguration().getLayoutDirection();
			useLtr = layoutDirection == View.LAYOUT_DIRECTION_LTR;
		}

		return useLtr;
	}

	public static boolean isTablet(Context context) {
		return is7InchTablet(context) || is10InchTablet(context);
	}

	public static boolean is7InchTablet(Context context) {
		return context.getResources().getBoolean(R.bool.is_large_tablet);
	}

	public static boolean is10InchTablet(Context context) {
		return context.getResources().getBoolean(R.bool.is_x_large_tablet);
	}

	public static boolean isTallScreen(Context context) {
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);

		int displayHeight = displayMetrics.heightPixels;

		int orientation = context.getResources().getConfiguration().orientation;
		return orientation != Configuration.ORIENTATION_PORTRAIT || displayHeight >= 1280;
	}

	public static boolean inLandscape(Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	public static boolean inPortrait(Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	public static boolean getSoundsPlayFlag(Context context) {
		int appSoundMode = new AppData(context).isPlaySounds();
		boolean playSoundsFlag = false;
		AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		switch (audio.getRingerMode()) {
			case AudioManager.RINGER_MODE_NORMAL:
				playSoundsFlag = appSoundMode == AppData.UNDEFINED || appSoundMode == AppData.TRUE;
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
			case AudioManager.RINGER_MODE_SILENT:
				playSoundsFlag = appSoundMode != AppData.UNDEFINED && appSoundMode == AppData.TRUE;
				break;
		}
		return playSoundsFlag;
	}

	public static void showNewMoveStatusNotification(Context context, List<YourTurnItem> yourTurnItems, int requestCode) {
		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

		if (yourTurnItems.size() == 0) {
			notifyManager.cancel(R.id.notification_id);
			return;
		}

		int number = yourTurnItems.size();
		YourTurnItem yourTurnItem = yourTurnItems.get(0);

		Intent notifyIntent = new Intent(context, MainFragmentFaceActivity.class);

		notifyIntent.putExtra(IntentConstants.USER_MOVE_UPDATE, true);
		// Creates an Intent for the Activity
		if (DataHolder.getInstance().isMainActivityVisible()) {
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		} else {
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}

		Bitmap bigImage;
		if (number == 1) {
			notifyIntent.putExtra(BaseGameItem.GAME_ID, yourTurnItem.getGameId());

			bigImage = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_stat_chess)).getBitmap();
			String title = context.getString(R.string.daily_chess);
			String body = context.getString(R.string.your_move_vs,
					yourTurnItem.getOpponent(),
					yourTurnItem.getLastMove());

			notificationBuilder.setContentTitle(title)
					.setTicker(body)
					.setContentText(body)
					.setSmallIcon(R.drawable.ic_stat_chess)
					.setLargeIcon(bigImage)
					.setNumber(number)
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.setAutoCancel(true);
		} else {
			bigImage = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_stat_notification_big_many)).getBitmap();
			String title = context.getString(R.string.my_move);
			String content = context.getString(R.string.total_games);

			NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			// Sets a title for the Inbox style big view
			inboxStyle.setBigContentTitle(title);
			// Moves events into the big view
			for (YourTurnItem turnItem : yourTurnItems) {
				String body = context.getString(R.string.your_move_vs,
						turnItem.getOpponent(),
						turnItem.getLastMove());
				inboxStyle.addLine(body);
			}
			inboxStyle.setSummaryText(content);

			// Moves the big view style object into the notification object.
			notificationBuilder.setStyle(inboxStyle);

			notificationBuilder.setContentTitle(title)
					.setTicker(title + Symbol.NEW_STR + content)
					.setSmallIcon(R.drawable.ic_stat_chess)
					.setContentText(content)
					.setLargeIcon(bigImage)
					.setNumber(number)
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.setAutoCancel(true);
		}

		// Creates the PendingIntent
		PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, notifyIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Puts the PendingIntent into the notification builder
		notificationBuilder.setContentIntent(pendingIntent);

		notifyManager.notify(R.id.notification_id, notificationBuilder.build());
	}

	public static void showStatusBarNotification(Context context, String title, String body) { // TODO add logic to open corresponding screen
		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

		Intent notifyIntent = new Intent(context, MainFragmentFaceActivity.class);
		// Creates an Intent for the Activity
		if (DataHolder.getInstance().isMainActivityVisible()) {
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		} else {
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}

		// Creates the PendingIntent
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 22, notifyIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Bitmap bigImage = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_stat_chess)).getBitmap();
		notificationBuilder.setContentTitle(title)
				.setTicker(title + Symbol.NEW_STR + body)
				.setContentText(body)
				.setSmallIcon(R.drawable.ic_stat_chess)
				.setLargeIcon(bigImage)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setAutoCancel(true);

		// Puts the PendingIntent into the notification builder
		notificationBuilder.setContentIntent(pendingIntent);

		notifyManager.notify(R.id.notification_message, notificationBuilder.build());
	}

	public static void cancelNotifications(Context context) {
		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifyManager.cancelAll();
	}

	public static void cancelNotification(Context context, int id) {
		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifyManager.cancel(id);
	}

	public static boolean isNeedToUpgrade(Context context) {
		return new AppData(context).getUserPremiumStatus() < StaticData.GOLD_USER;
	}

	public static boolean isNeedToUpgradePremium(Context context) {
		return new AppData(context).getUserPremiumStatus() < StaticData.DIAMOND_USER;
	}

	public static int getPremiumIcon(int status) {
		switch (status) {
			case StaticData.GOLD_USER:
				return R.drawable.ic_upgrade_gold;
			case StaticData.PLATINUM_USER:
				return R.drawable.ic_upgrade_platinum;
			case StaticData.DIAMOND_USER:
				return R.drawable.ic_upgrade_diamond;
			case StaticData.BASIC_USER:
			default:
				return R.drawable.empty;
		}
	}

	public static String getTimeLeftFromSeconds(long duration, Context context) {
		long minutes = duration / 60 % 60;
		long hours = duration / 3600 % 24;
		long days = duration / 86400;
		StringBuilder sb = new StringBuilder();

		if (days > 0) {
			if (days > 1) {
				sb.append(context.getString(R.string.arg_days, days));
			} else {
				sb.append(context.getString(R.string.arg_day, days));
			}
		} else if (hours > 0) {
			if (!sb.toString().trim().equals(Symbol.EMPTY)) {
				sb.append(Symbol.SPACE);
			}
			if (hours > 1) {
				sb.append(context.getString(R.string.arg_hours, hours));
			} else {
				sb.append(context.getString(R.string.arg_hour, hours));
			}
		} else if (minutes > 0) {
			if (!sb.toString().trim().equals(Symbol.EMPTY)) {
				sb.append(Symbol.SPACE);
			}
			sb.append(context.getString(R.string.arg_min, minutes));
		}

		return sb.toString();
	}

	public static String getMomentsAgoFromSeconds(long lastStamp, Context context) {
		long current = System.currentTimeMillis() / 1000L;
		long difference = current - lastStamp;
		long minutes = difference / 60 % 60;
		long hours = difference / 3600 % 24;
		long days = difference / 86400;
		long months = difference / 2592000;
		long years = difference / 31536000;
		StringBuilder sb = new StringBuilder();

		if (years > 0) {
			if (years > 1) {
				sb.append(context.getString(R.string.arg_years_ago, years));
			} else {
				sb.append(context.getString(R.string.arg_year_ago, years));
			}
		} else if (months > 0) {
			if (months > 1) {
				sb.append(context.getString(R.string.arg_months_ago, months));
			} else {
				sb.append(context.getString(R.string.arg_month_ago, months));
			}
		} else if (days > 0) {
			if (days > 1) {
				sb.append(context.getString(R.string.arg_days_ago, days));
			} else {
				sb.append(context.getString(R.string.arg_day_ago, days));
			}
		} else if (hours > 0) {
			if (!sb.toString().trim().equals(Symbol.EMPTY)) {
				sb.append(Symbol.SPACE);
			}
			if (hours > 1) {
				sb.append(context.getString(R.string.arg_hours_ago, hours));
			} else {
				sb.append(context.getString(R.string.arg_hour_ago, hours));
			}
		} else if (minutes > 0) {
			if (!sb.toString().trim().equals(Symbol.EMPTY)) {
				sb.append(Symbol.SPACE);
			}
			sb.append(context.getString(R.string.arg_min_ago, minutes));
		} else {
			sb.append(context.getString(R.string.just_now));
			return sb.toString();
		}

		return sb.toString();
	}

	public static String getSecondsTimeFromSecondsStr(long duration) {
		long seconds = duration % 60;
		long minutes = duration / 60 % 60;
		long hours = duration / 3600 % 24;
		long days = duration / 86400;
		StringBuilder sb = new StringBuilder();

		if (days > 0) {
			sb.append(days).append(Symbol.COLON);
		}

		if (hours > 0) {
			sb.append(hours).append(Symbol.COLON);
		}

		if (minutes < 10 && sb.length() > 0) {
			sb.append(0);
		}
		sb.append(minutes).append(Symbol.COLON);

		if (seconds < 10) {
			sb.append(0);
		}
		sb.append(seconds);

		return sb.toString();
	}

	public static String getDaysString(int cnt, Context context) {
		if (cnt > 1) {
			return context.getString(R.string.arg_days, cnt);
		} else {
			return context.getString(R.string.arg_day, cnt);
		}
	}

	public static String getLiveModeButtonLabel(String label, Context context) {
		int initialTime;
		int bonusTime = 0;
		if (label.contains(Symbol.SLASH)) {
			// "5 | 2",
			String[] params = label.split(Symbol.DIVIDER);
			initialTime = Integer.valueOf(params[0].trim());
			bonusTime = Integer.valueOf(params[2].trim());
		} else {
			// "10 min",
			initialTime = Integer.valueOf(label);
		}

		if (bonusTime == 0) { // "10 min"
			return context.getString(R.string.arg_min, initialTime);
		} else { // "5 | 2"
			return context.getString(R.string.live_chess_time_mode_arg, initialTime, bonusTime);
		}
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm
				= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	public static String getI18nString(Context context, String codeMessage, String... parameters) {
		int stringIdentifier = context.getResources().getIdentifier(codeMessage, "string", context.getPackageName());
		return stringIdentifier != 0 ? context.getString(stringIdentifier, parameters) : null;
	}

	public static String getI18nStringForAPIError(Context context, String message) {
		final Resources resources = context.getResources();

		if (isLocaleEn(resources)) {
			return message;
		}

		final int positionOfMessage = Arrays.asList(resources.getStringArray(R.array.site_api_error_messages)).indexOf(message);
		if (positionOfMessage != -1) {
			final String messageKey = resources.getStringArray(R.array.site_api_error_keys)[positionOfMessage];
			return context.getString(resources.getIdentifier(messageKey, "string", context.getPackageName()));
		}

		return message;
	}

	private static boolean isLocaleEn(Resources resources) {
		return resources.getConfiguration().locale.getLanguage().equals(StaticData.LOCALE_EN);
	}

	public static String getAppId(Context context) {
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "Android0";
		}
		return "Android" + VERSION_CODE;
	}

	public static class DeviceInfo {
		public String MODEL;
		public int SDK_API;
		public String APP_VERSION_NAME = Symbol.EMPTY;
		public int APP_VERSION_CODE = 0;
		public String android_id;

		/*
		 * Get information about device model, App version and API version
		 */
		public DeviceInfo getDeviceInfo(Context context) {
			DeviceInfo deviceInfo = new DeviceInfo();

			deviceInfo.android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);


			deviceInfo.MODEL = Build.MODEL;
			Log.i("requested MODEL = ", deviceInfo.MODEL);

			deviceInfo.SDK_API = Build.VERSION.SDK_INT;
			Log.i("requested SDK_INT = ", deviceInfo.SDK_API + Symbol.EMPTY);
			// get version number and name
			try {
				PackageManager manager = context.getPackageManager();
				PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
				deviceInfo.APP_VERSION_CODE = info.versionCode;
				Log.i("requested versionCode = ", deviceInfo.APP_VERSION_CODE + Symbol.EMPTY);

				deviceInfo.APP_VERSION_NAME = info.versionName;
				Log.i("requested versionName = ", deviceInfo.APP_VERSION_NAME);

			} catch (PackageManager.NameNotFoundException nnf) {
				nnf.printStackTrace();
			}
			return deviceInfo;
		}
	}

	public static void showKeyBoard(Context context, EditText view) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, 0);
	}

	//	public static void showKeyBoard(Context context, EditText editText){
//		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
//	}
//
	public static void hideKeyBoard(Context context, View editText) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	public static List<String> convertArrayToList(String[] array) {
		List<String> items = new ArrayList<String>();
		items.addAll(Arrays.asList(array));
		return items;
	}

	/**
	 * Given either a Spannable String or a regular String and a token, apply
	 * the given CharacterStyle to the span between the tokens, and also
	 * remove tokens.
	 * <p/>
	 * For example, {@code setSpanBetweenTokens("Hello ##world##!", "##",
	 *new ForegroundColorSpan(0xFFFF0000));} will return a CharSequence
	 * {@code "Hello world!"} with {@code world} in red.
	 *
	 * @param text  The text, with the tokens, to adjust.
	 * @param token The token string; there should be at least two instances
	 *              of token in text.
	 * @param cs    The style to apply to the CharSequence. WARNING: You cannot
	 *              send the same two instances of this parameter, otherwise
	 *              the second call will remove the original span.
	 * @return A Spannable CharSequence with the new style applied.
	 * @see {@link <a>http://developer.android.com/reference/android/text/style/CharacterStyle.html</a> }
	 */
	public static CharSequence setSpanBetweenTokens(CharSequence text, String token, CharacterStyle... cs) {
		// Start and end refer to the points where the span will apply
		int tokenLen = token.length();
		int start = text.toString().indexOf(token) + tokenLen;
		int end = text.toString().indexOf(token, start);

		if (start > -1 && end > -1) {
			// Copy the spannable string to a mutable spannable string
			SpannableStringBuilder ssb = new SpannableStringBuilder(text);
			for (CharacterStyle c : cs)
				ssb.setSpan(c, start, end, 0);

			// Delete the tokens before and after the span
			ssb.delete(end, end + tokenLen);
			ssb.delete(start - tokenLen, start);

			text = ssb;
		}

		return text;
	}

	public static Drawable getCountryFlag(Context context, String userCountry) {
		if (userCountry == null) {
			try {
				Log.e("getCountryFlag", " userCountry == NULL");
				return Drawable.createFromStream(context.getResources().getAssets().open("flags/" + "United States" + ".png"), null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			return Drawable.createFromStream(context.getResources().getAssets().open("flags/" + userCountry + ".png"), null);
		} catch (IOException e) { // there might be countries that have different names, like Trinidad/Tobago
			e.printStackTrace();
			try {
				Log.e("getCountryFlag", " userCountry == NULL");
				return Drawable.createFromStream(context.getResources().getAssets().open("flags/" + "International" + ".png"), null);
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
		}
	}

	public static Drawable getCountryFlagScaled(Context context, String userCountry) {
		int flagSize = (int) context.getResources().getDimension(R.dimen.country_flag_size);
		Drawable drawable = getCountryFlag(context, userCountry);
		// get bitmap to scale properly
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

		return new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, flagSize, flagSize, true));
	}

	public static Drawable getUserFlag(Context context) {
		String userCountry = new AppData(context).getUserCountry();
		return getCountryFlag(context, userCountry);
	}

	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void printCursorContent(Cursor cursor, String tableName) {
		if (cursor.moveToFirst()) {
			Log.d("TABLE", "____________" + tableName + "_______________");
			Log.d("TABLE", "[");

			do {
				int columnCount = cursor.getColumnCount();
				StringBuilder builder = new StringBuilder();

				builder.append("{");
				for (int i = 0; i <= columnCount; i++) {
					switch (cursor.getType(i)) {
						case Cursor.FIELD_TYPE_INTEGER: {
							builder.append("\"").append(cursor.getColumnName(i)).append("\": ").append(cursor.getInt(i)).append(", ");
						}
						break;
						case Cursor.FIELD_TYPE_STRING: {
							builder.append("\"").append(cursor.getColumnName(i)).append("\": \"").append(cursor.getString(i)).append("\",");
						}
						break;
					}
				}
				builder.append(" \"end\": \"end\" }, \n");
				Log.d("TABLE", builder.toString());

			} while (cursor.moveToNext());
			Log.d("TABLE", "]");
		}
	}


	public static String getCountryIdByName(String[] countryNames, int[] countryCodes, int userCountryId) {
		for (int i = 0; i < countryCodes.length; i++) {
			int countryCode = countryCodes[i];
			if (userCountryId == countryCode) {
				return countryNames[i];
			}
		}
		return DEFAULT_COUNTRY;
	}

	public static Bitmap getBitmapFromView(View view, int width, int height) {
		view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
				View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

		// Build the Drawing Cache
		view.buildDrawingCache();

		// Create Bitmap
		Bitmap drawingCache = view.getDrawingCache();
		if (drawingCache == null) {
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(drawingCache);
		drawingCache.recycle();
		view.setDrawingCacheEnabled(false);
		return bitmap;
	}

/*	public static Bitmap getBitmapFromView(View view, int width, int height) {
		// Create bitmap
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		//Pre-measure the view so that height and width don't remain null.
		view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
		//Assign a size and position to the view and all of its descendants
		view.layout(0, 0, width, height);

		//Create a canvas with the specified bitmap to draw into
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);

		//Render this view (and all of its children) to the given Canvas
		view.draw(canvas);
		return bitmap;
	}*/

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public static String upCaseFirst(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
	}

	public static String getDeviceId(Context context) {
		if (context != null) {
			AppData appData = new AppData(context);
			String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
			if (TextUtils.isEmpty(deviceId)) {
				deviceId = appData.getDeviceId();
				if (TextUtils.isEmpty(deviceId)) { // generate a new one
					deviceId = "Hello" + (Math.random() * 100) + "There" + System.currentTimeMillis();
					appData.setDeviceId(deviceId);
				}
			}

			deviceId = ImageCache.hashKeyForDisk(deviceId);
			return deviceId.substring(0, 32);
		} else {
			return ImageCache.hashKeyForDisk("Hello" + (Math.random() * 100) + "There" + System.currentTimeMillis());
		}
	}
}
