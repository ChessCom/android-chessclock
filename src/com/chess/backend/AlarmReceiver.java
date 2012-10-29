package com.chess.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import com.chess.R;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameListCurrentItem;
import com.chess.ui.activities.OnlineScreenActivity;
import com.chess.utilities.AppUtils;
import com.chess.utilities.ChessComApiParser;

import java.util.List;


public class AlarmReceiver extends BroadcastReceiver {

	private static final int NOTIFICATIONS_ICON_CNT = 1;

	@Override
	public void onReceive(Context context, Intent intent) {

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(context));
		loadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ONLY_USER_TURN);
		new GetStringObjTask(new UpdateListener(context)).executeTask(loadItem);

	}

	private class UpdateListener extends AbstractUpdateListener<String> {
		public UpdateListener(Context context) {
			super(context);
		}

		@Override
		public void updateData(String returnedObj) {
//			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				int haveMoves = 0;
				List<GameListCurrentItem> itemList = ChessComApiParser.getCurrentOnlineGames(returnedObj);
				if(itemList.size() == NOTIFICATIONS_ICON_CNT) {
					AppUtils.cancelNotification(getMeContext(), R.id.notification_message);

					GameListCurrentItem gameListItem = itemList.get(0);

					AppUtils.showNewMoveStatusNotification(getMeContext(),
							getMeContext().getString(R.string.your_move),
							getMeContext().getString(R.string.your_turn_in_game_with,
									gameListItem.getOpponentUsername(),
									gameListItem.getLastMoveFromSquare() + gameListItem.getLastMoveToSquare()),
							StaticData.MOVE_REQUEST_CODE,
							gameListItem);
					haveMoves++;
				} else if(itemList.size() > 0) {
					for (GameListCurrentItem currentItem : itemList) {
						AppUtils.cancelNotification(getMeContext(), (int) currentItem.getGameId());
					}

					AppUtils.showMoveStatusNotification(getMeContext(),
							getMeContext().getString(R.string.your_turn),
							getMeContext().getString(R.string.your_move) + StaticData.SYMBOL_SPACE
									+ StaticData.SYMBOL_LEFT_PAR + itemList.size() + StaticData.SYMBOL_RIGHT_PAR,
							0, OnlineScreenActivity.class);
					getMeContext().sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
				}

				if(haveMoves == 1){ // play for one
					SharedPreferences preferences = AppData.getPreferences(getMeContext());
					boolean playSounds = preferences.getBoolean(AppData.getUserName(getMeContext()) + AppConstants.PREF_SOUNDS, false);
					if(playSounds){
						final MediaPlayer player = MediaPlayer.create(getMeContext(), R.raw.move_opponent);
						if(player != null){
							player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
								@Override
								public void onCompletion(MediaPlayer mediaPlayer) {
									player.release();
								}
							});
							player.start();
						}
					}


					getMeContext().sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
				}
//			}
		}
	}

}
