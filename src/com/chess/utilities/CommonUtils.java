package com.chess.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.View;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.GameListItem;
import com.chess.ui.views.BackgroundChessDrawable;

/**
 * CommonUtils class
 *
 * @author alien_roger
 * @created at: 01.02.12 7:50
 */
public class CommonUtils {

	private static final int MDPI_DENSITY = 1;

	public static void setBackground(View mainView, Context context) {
		mainView.setBackgroundDrawable(new BackgroundChessDrawable(context));

//		int padding = getResources().getDrawable(R.drawable.chess_cell).getIntrinsicWidth() / 2;
		int paddingTop = (int) context.getResources().getDimension(R.dimen.dashboard_padding_top);
		int paddingLeft = (int) context.getResources().getDimension(R.dimen.dashboard_padding_side);
		int paddingRight = (int) context.getResources().getDimension(R.dimen.dashboard_padding_side);
		int paddingBot = (int) context.getResources().getDimension(R.dimen.dashboard_padding_bot);
		mainView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBot);
	}

	public static boolean needFullScreen(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return displayMetrics.density < MDPI_DENSITY || displayMetrics.densityDpi == DisplayMetrics.DENSITY_LOW;
	}

    /**
     * Fire notification with deafined arguments
     *
     * @param context - Application Context for resources
     * @param taskTitle - title that will be visible at status bar
     * @param id - request code id
     * @param sound - sound to play
     * @param body - short description for notification message content
     * @param clazz - which class to open when User press notification
     */
	public static void showNotification(Context context, String taskTitle, long id, String sound,String body,Class<?> clazz) {
		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// Set the icon, for boarding flight status
		Notification notification = new Notification(R.drawable.ic_stat_chess, context.getString(R.string.you_got_new_msg), System.currentTimeMillis());
//		notification.sound = Uri.parse(sound); // SettingsActivity.getAlarmRingtone(context);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
//		if (SettingsActivity.vibrate4Alarm(context)) { // TODO
//			notification.defaults = Notification.DEFAULT_VIBRATE;
//		}
		Intent openList = new Intent(context, clazz);
		openList.putExtra(StaticData.CLEAR_CHAT_NOTIFICATION, true);
//		openList.putExtra(StaticData.REQUEST_CODE, id);
		openList.putExtra(GameListItem.GAME_ID, id);
		openList.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				|Intent.FLAG_ACTIVITY_NEW_TASK
//				|Intent.FLAG_ACTIVITY_SINGLE_TOP
				/*|Intent.FLAG_ACTIVITY_CLEAR_TOP*/);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, openList, PendingIntent.FLAG_ONE_SHOT); // TODO use flags

		notification.setLatestEventInfo(context, context.getText(R.string.you_got_new_msg), context.getText(R.string.open_app_t_see_msg), contentIntent);

		notifyManager.notify(R.string.you_got_new_msg, notification);

	}
}
