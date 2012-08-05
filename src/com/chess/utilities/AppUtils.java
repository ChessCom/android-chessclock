package com.chess.utilities;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.AlarmReceiver;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.User;
import com.chess.model.BaseGameItem;
import com.chess.model.GameListCurrentItem;
import com.chess.ui.activities.GameOnlineScreenActivity;
import com.chess.ui.views.BackgroundChessDrawable;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AppUtils class
 *
 * @author alien_roger
 * @created at: 01.02.12 7:50
 */
public class AppUtils {

	private static final int MDPI_DENSITY = 1;
	private static boolean ENABLE_LOG = true;

	public static class ListSelector implements Runnable{
		private int pos;
		private ListView listView;

		public ListSelector(int pos, ListView listView){
			this.pos = pos;
			this.listView = listView;
		}
		@Override
		public void run() {
			listView.setSelection(pos);
		}
	}

	public static void setBackground(View mainView, Context context) {
		mainView.setBackgroundDrawable(new BackgroundChessDrawable(context));

		int paddingTop = (int) context.getResources().getDimension(R.dimen.dashboard_padding_top);
		int paddingLeft = (int) context.getResources().getDimension(R.dimen.dashboard_padding_side);
		int paddingRight = (int) context.getResources().getDimension(R.dimen.dashboard_padding_side);
		int paddingBot = (int) context.getResources().getDimension(R.dimen.dashboard_padding_bot);
		mainView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBot);
	}

	/**
	 * For QVGA screens we don't need a title bar and Action bar
	 * @param context
	 * @return
	 */
	public static boolean needFullScreen(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return displayMetrics.density < MDPI_DENSITY
				|| displayMetrics.densityDpi == DisplayMetrics.DENSITY_LOW;
	}

	/**
	 * For mdpi normal screens we don't need a action bar only
	 * @param context
	 * @return
	 */
	public static boolean noNeedTitleBar(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return (displayMetrics.density == MDPI_DENSITY
				|| displayMetrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM)
				&& displayMetrics.heightPixels <= 480;
	}

	public static void showNewMoveStatusNotification(Context context, String title,  String body, int id,
													 GameListCurrentItem currentGameItem) {
		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(R.drawable.ic_stat_chess, title, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Intent intent = new Intent(context, GameOnlineScreenActivity.class);
		intent.putExtra(BaseGameItem.GAME_INFO_ITEM, currentGameItem);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent contentIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_ONE_SHOT);

		notification.setLatestEventInfo(context, title, body, contentIntent);

		notifyManager.notify((int) currentGameItem.getGameId(), notification);
	}

	/**
	 * Use default android.util.Log with Flag trigger
	 * Use this method to track changes, but avoid to use in uncertain cases,
	 * where release version can tell where some bugs were born
	 * @param tag
	 * @param message
	 */
	public static void logD(String tag, String message){
		if(ENABLE_LOG) // can be set false for release version.
			Log.d(tag, message);
	}

	public static void startNotificationsUpdate(Context context){
		Intent statusUpdate = new Intent(context, AlarmReceiver.class);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, StaticData.YOUR_MOVE_UPDATE_ID,
				statusUpdate, PendingIntent.FLAG_UPDATE_CURRENT);

		// schedule the service for updating
		AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarms.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), StaticData.REMIND_ALARM_INTERVAL, pendingIntent);
	}

	public static void stopNotificationsUpdate(Context context){
		Intent statusUpdate = new Intent(context, AlarmReceiver.class);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, StaticData.YOUR_MOVE_UPDATE_ID, statusUpdate,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarms.cancel(pendingIntent);

		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		notifyManager.cancel(R.id.notification_message);
	}

	public static boolean isNeedToUpgrade(Context context){
		boolean liveMembershipLevel = false;
		User user = LccHolder.getInstance(context).getUser();
		if (user != null) {
			liveMembershipLevel = DataHolder.getInstance().isLiveChess()
					&& (user.getMembershipLevel() < StaticData.GOLD_LEVEL);
		}
		return liveMembershipLevel
				|| (!DataHolder.getInstance().isLiveChess() && AppData.getUserPremiumStatus(context) < StaticData.GOLD_USER);
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm
				= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	// Get i18n strings for server messages (temporary)
	public static String getI18nString(Context context, String actual, int expected, int translated) {
		if (isLocaleEn(context.getResources())) {
			return null;
		}
		return actual.equals(context.getString(expected)) ? context.getString(translated) : null;
	}

	public static String getI18nString(Context context, int regexp, String actual, int expected, int translated) {
		if (isLocaleEn(context.getResources())) {
			return null;
		}

		String substring = null;
		Matcher m = Pattern.compile(context.getString(regexp)).matcher(actual);
		if (m.find()) {
			substring = m.group(1);
		}

		return actual.equals(context.getString(expected, substring)) ? context.getString(translated, substring) : null;
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
}
